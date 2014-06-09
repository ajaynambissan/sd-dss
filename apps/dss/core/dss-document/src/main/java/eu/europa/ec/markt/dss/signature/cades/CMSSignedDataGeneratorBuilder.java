/*
 * DSS - Digital Signature Services
 *
 * Copyright (C) 2013 European Commission, Directorate-General Internal Market and Services (DG MARKT), B-1049 Bruxelles/Brussel
 *
 * Developed by: 2013 ARHS Developments S.A. (rue Nicolas Bové 2B, L-1253 Luxembourg) http://www.arhs-developments.com
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * "DSS - Digital Signature Services" is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * DSS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * "DSS - Digital Signature Services".  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.markt.dss.signature.cades;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.cms.SimpleAttributeTableGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.Store;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.parameter.SignatureParameters;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.validation102853.CertificateVerifier;
import eu.europa.ec.markt.dss.validation102853.TrustedCertificateSource;

/**
 * <p/>
 * <p/>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public class CMSSignedDataGeneratorBuilder {

	/**
	 * Note:
	 * Section 5.1 of RFC 3852 [4] requires that, the CMS SignedData version be set to 3 if certificates from
	 * SignedData is present AND (any version 1 attribute certificates are present OR any SignerInfo structures
	 * are version 3 OR eContentType from encapContentInfo is other than id-data). Otherwise, the CMS
	 * SignedData version is required to be set to 1.
	 * ---> CMS SignedData Version is handled automatically by BouncyCastle.
	 *
	 * @param certificateVerifier the certificate verifier used to find the trusted certificates
	 * @param signerCertificate   the certificate used to sign
	 * @param contentSigner       the contentSigned to get the hash of the data to be signed
	 * @param originalSignedData  the original signed data if extending an existing signature. null otherwise.
	 * @return the bouncycastle signed data generator which will sign the document and add the required signed and unsigned CMS attributes
	 * @throws eu.europa.ec.markt.dss.exception.DSSException
	 */
	protected CMSSignedDataGenerator createCMSSignedDataGenerator(CertificateVerifier certificateVerifier, X509Certificate signerCertificate,
	                                                              List<X509Certificate> certificateChain, ContentSigner contentSigner,
	                                                              SignerInfoGeneratorBuilder signerInfoGeneratorBuilder, CMSSignedData originalSignedData) throws DSSException {
		try {

			CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

			X509CertificateHolder certHolder = new X509CertificateHolder(signerCertificate.getEncoded());
			SignerInfoGenerator signerInfoGenerator = signerInfoGeneratorBuilder.build(contentSigner, certHolder);

			generator.addSignerInfoGenerator(signerInfoGenerator);
			if (originalSignedData != null) {
				generator.addSigners(originalSignedData.getSignerInfos());
				generator.addAttributeCertificates(originalSignedData.getAttributeCertificates());
				generator.addCRLs(originalSignedData.getCRLs());
				generator.addOtherRevocationInfo(OCSPObjectIdentifiers.id_pkix_ocsp_basic, originalSignedData.getOtherRevocationInfo(OCSPObjectIdentifiers.id_pkix_ocsp_basic));
				generator.addOtherRevocationInfo(CMSObjectIdentifiers.id_ri_ocsp_response, originalSignedData.getOtherRevocationInfo(CMSObjectIdentifiers.id_ri_ocsp_response));
			}

			final Set<X509Certificate> newCertificateChain = new HashSet<X509Certificate>();
			if (originalSignedData != null) {
				final Store certificates = originalSignedData.getCertificates();
				final Collection<X509CertificateHolder> certificatesMatches = certificates.getMatches(null);
				for (final X509CertificateHolder certificatesMatch : certificatesMatches) {
					newCertificateChain.add(DSSUtils.getCertificate(certificatesMatch));
				}
			}
			newCertificateChain.addAll(certificateChain);
			final Store jcaCertStore = getJcaCertStore(certificateVerifier, signerCertificate, newCertificateChain);
			generator.addCertificates(jcaCertStore);
			return generator;

		} catch (CMSException e) {
			throw new DSSException(e);
		} catch (CertificateEncodingException e) {
			throw new DSSException(e);
		} catch (OperatorCreationException e) {
			throw new DSSException(e);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * @param document                  the document for which the attribute needs to be generated
	 * @param parameters                the parameters of the signature containing values for the attributes
	 * @param includeUnsignedAttributes true if the unsigned attributes shall be included
	 * @return a SignerInfoGeneratorBuilder that generate the signed and unsigned attributes according to the CAdESLevelBaselineB
	 */
	protected SignerInfoGeneratorBuilder getSignerInfoGeneratorBuilder(final DSSDocument document, final SignatureParameters parameters, final boolean includeUnsignedAttributes) {
		final CAdESLevelBaselineB cadesProfile = new CAdESLevelBaselineB();
		final AttributeTable signedAttributes = cadesProfile.getSignedAttributes(document, parameters);
		AttributeTable unsignedAttributes = null;
		if (includeUnsignedAttributes) {
			unsignedAttributes = cadesProfile.getUnsignedAttributes(document, parameters);
		}

		return getSignerInfoGeneratorBuilder(signedAttributes, unsignedAttributes);
	}

	/**
	 * @param signedAttributes   the signedAttributes
	 * @param unsignedAttributes the unsignedAttributes
	 * @return a SignerInfoGeneratorBuilder that generate the signed and unsigned attributes according to the parameters
	 */
	protected SignerInfoGeneratorBuilder getSignerInfoGeneratorBuilder(AttributeTable signedAttributes, AttributeTable unsignedAttributes) {
		if (signedAttributes != null && signedAttributes.size() == 0) {
			signedAttributes = null;
		}
		final DefaultSignedAttributeTableGenerator signedAttributeGenerator = new DefaultSignedAttributeTableGenerator(signedAttributes);
		if (unsignedAttributes != null && unsignedAttributes.size() == 0) {
			unsignedAttributes = null;
		}
		final SimpleAttributeTableGenerator unsignedAttributeGenerator = new SimpleAttributeTableGenerator(unsignedAttributes);

		return getSignerInfoGeneratorBuilder(signedAttributeGenerator, unsignedAttributeGenerator);
	}

	/**
	 * @param signedAttributeGenerator   the signedAttribute generator
	 * @param unsignedAttributeGenerator the unsignedAttribute generator
	 * @return a SignerInfoGeneratorBuilder that generate the signed and unsigned attributes according to the parameters
	 */
	protected SignerInfoGeneratorBuilder getSignerInfoGeneratorBuilder(DefaultSignedAttributeTableGenerator signedAttributeGenerator,
	                                                                   SimpleAttributeTableGenerator unsignedAttributeGenerator) {
		final DigestCalculatorProvider digestCalculatorProvider = new BcDigestCalculatorProvider();
		SignerInfoGeneratorBuilder sigInfoGeneratorBuilder = new SignerInfoGeneratorBuilder(digestCalculatorProvider);
		sigInfoGeneratorBuilder.setSignedAttributeGenerator(signedAttributeGenerator);
		sigInfoGeneratorBuilder.setUnsignedAttributeGenerator(unsignedAttributeGenerator);
		return sigInfoGeneratorBuilder;
	}

	/**
	 * TODO (Bob 28.05.2014) The position of the signing certificate must be clarified
	 * The order of the certificates is important, the fist one must be the signing certificate.
	 *
	 * @return a store with the certificate chain of the signing certificate
	 * @throws CertificateEncodingException
	 */
	private JcaCertStore getJcaCertStore(final CertificateVerifier certificateVerifier, final X509Certificate signingCertificate,
	                                     final Collection<X509Certificate> certificateChain) {

		try {
			final Collection<X509Certificate> certs = new ArrayList<X509Certificate>();
			certs.add(signingCertificate);

			if (certificateChain != null) {

				final X500Principal signingCertificateSubjectX500Principal = signingCertificate.getSubjectX500Principal();
				for (X509Certificate certificateInChain : certificateChain) {

					final X500Principal subjectX500Principal = certificateInChain.getSubjectX500Principal();
					if (subjectX500Principal.equals(signingCertificateSubjectX500Principal)) {
						continue;
					}
					// CAdES-Baseline-B: do not include certificates found in the trusted list
					final TrustedCertificateSource trustedCertSource = certificateVerifier.getTrustedCertSource();
					if (trustedCertSource != null) {

						if (!trustedCertSource.get(subjectX500Principal).isEmpty()) {
							continue;
						}
					}
					if (!certs.contains(certificateInChain)) {
						certs.add(certificateInChain);
					}
				}
			}
			return new JcaCertStore(certs);
		} catch (CertificateEncodingException e) {
			throw new DSSException(e);
		}
	}


	protected CMSSignedData regenerateCMSSignedData(CMSSignedData cmsSignedData, SignatureParameters parameters, Store certificatesStore, Store attributeCertificatesStore,
	                                                Store crlsStore, Store otherRevocationInfoFormatStoreBasic, Store otherRevocationInfoFormatStoreOcsp) {
		try {
			final CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();
			cmsSignedDataGenerator.addSigners(cmsSignedData.getSignerInfos());
			cmsSignedDataGenerator.addAttributeCertificates(attributeCertificatesStore);
			cmsSignedDataGenerator.addCertificates(certificatesStore);
			cmsSignedDataGenerator.addCRLs(crlsStore);
			cmsSignedDataGenerator.addOtherRevocationInfo(OCSPObjectIdentifiers.id_pkix_ocsp_basic, otherRevocationInfoFormatStoreBasic);
			cmsSignedDataGenerator.addOtherRevocationInfo(CMSObjectIdentifiers.id_ri_ocsp_response, otherRevocationInfoFormatStoreOcsp);
			final boolean encapsulate = cmsSignedData.getSignedContent() != null;
			if (!encapsulate) {
				final CMSProcessableByteArray content = new CMSProcessableByteArray(DSSUtils.toByteArray(parameters.getOriginalDocument().openStream()));
				cmsSignedData = cmsSignedDataGenerator.generate(content, encapsulate);
			} else {
				cmsSignedData = cmsSignedDataGenerator.generate(cmsSignedData.getSignedContent(), encapsulate);
			}
			return cmsSignedData;
		} catch (CMSException e) {
			throw new DSSException(e);
		}
	}
}
