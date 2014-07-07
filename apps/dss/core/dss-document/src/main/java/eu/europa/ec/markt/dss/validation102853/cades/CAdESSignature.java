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

package eu.europa.ec.markt.dss.validation102853.cades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.esf.CommitmentTypeIndication;
import org.bouncycastle.asn1.esf.CrlListID;
import org.bouncycastle.asn1.esf.CrlOcspRef;
import org.bouncycastle.asn1.esf.CrlValidatedID;
import org.bouncycastle.asn1.esf.OcspListID;
import org.bouncycastle.asn1.esf.OcspResponsesID;
import org.bouncycastle.asn1.esf.OtherHashAlgAndValue;
import org.bouncycastle.asn1.esf.SigPolicyQualifierInfo;
import org.bouncycastle.asn1.esf.SigPolicyQualifiers;
import org.bouncycastle.asn1.esf.SignaturePolicyId;
import org.bouncycastle.asn1.esf.SignerAttribute;
import org.bouncycastle.asn1.esf.SignerLocation;
import org.bouncycastle.asn1.ess.ContentHints;
import org.bouncycastle.asn1.ess.ContentIdentifier;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.OtherCertID;
import org.bouncycastle.asn1.ess.SigningCertificate;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttCertValidityPeriod;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.asn1.x509.RoleSyntax;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.RuntimeOperatorException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.DSSASN1Utils;
import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.EncryptionAlgorithm;
import eu.europa.ec.markt.dss.OID;
import eu.europa.ec.markt.dss.SignatureAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.SignatureLevel;
import eu.europa.ec.markt.dss.signature.cades.CadesLevelBaselineLTATimestampExtractor;
import eu.europa.ec.markt.dss.validation102853.AdvancedSignature;
import eu.europa.ec.markt.dss.validation102853.ArchiveTimestampType;
import eu.europa.ec.markt.dss.validation102853.CAdESCertificateSource;
import eu.europa.ec.markt.dss.validation102853.CertificatePool;
import eu.europa.ec.markt.dss.validation102853.CertificateToken;
import eu.europa.ec.markt.dss.validation102853.DefaultAdvancedSignature;
import eu.europa.ec.markt.dss.validation102853.SignatureForm;
import eu.europa.ec.markt.dss.validation102853.SignaturePolicy;
import eu.europa.ec.markt.dss.validation102853.TimestampReference;
import eu.europa.ec.markt.dss.validation102853.TimestampReferenceCategory;
import eu.europa.ec.markt.dss.validation102853.TimestampToken;
import eu.europa.ec.markt.dss.validation102853.TimestampType;
import eu.europa.ec.markt.dss.validation102853.bean.CandidatesForSigningCertificate;
import eu.europa.ec.markt.dss.validation102853.bean.CertifiedRole;
import eu.europa.ec.markt.dss.validation102853.bean.CommitmentType;
import eu.europa.ec.markt.dss.validation102853.bean.SignatureCryptographicVerification;
import eu.europa.ec.markt.dss.validation102853.bean.SignatureProductionPlace;
import eu.europa.ec.markt.dss.validation102853.bean.SigningCertificateValidity;
import eu.europa.ec.markt.dss.validation102853.certificate.CertificateRef;
import eu.europa.ec.markt.dss.validation102853.crl.CRLRef;
import eu.europa.ec.markt.dss.validation102853.crl.OfflineCRLSource;
import eu.europa.ec.markt.dss.validation102853.ocsp.OCSPRef;
import eu.europa.ec.markt.dss.validation102853.ocsp.OfflineOCSPSource;

/**
 * CAdES Signature class helper
 *
 * @version $Revision: 1821 $ - $Date: 2013-03-28 15:56:00 +0100 (Thu, 28 Mar 2013) $
 */

public class CAdESSignature extends DefaultAdvancedSignature {

	private static final Logger LOG = LoggerFactory.getLogger(CAdESSignature.class);

	private static final Date JANUARY_1950 = DSSUtils.getUtcDate(1950, 1, 1);

	private static final Date JANUARY_2050 = DSSUtils.getUtcDate(2050, 1, 1);

	private final CMSSignedData cmsSignedData;

	private final SignerInformation signerInformation;

	/**
	 * The detached signed content if signature is a detached signature
	 */
	private final DSSDocument externalContent;

	private CAdESCertificateSource certSource;

	/**
	 * The reference to the signing certificate. If the signing certificate is an input provided by the DA then getSigningCer MUST be called.
	 */
	private SigningCertificateValidity signingCertificateValidity;

	/**
	 * This is the reference to the global (external) pool of certificates. All encapsulated certificates in the signature are added to this pool. See {@link
	 * eu.europa.ec.markt.dss.validation102853.CertificatePool}
	 */
	private final CertificatePool certPool;

	/**
	 * This list represents all digest algorithms used to calculate the digest values of certificates.
	 */
	private Set<DigestAlgorithm> usedCertificatesDigestAlgorithms = new HashSet<DigestAlgorithm>();
	/**
	 * This id identifies the signature, it is calculated on the signing time if present and on the certificate.
	 */
	private String signatureId;

	/**
	 * @param data byte array representing CMSSignedData
	 * @throws org.bouncycastle.cms.CMSException
	 */
	public CAdESSignature(final byte[] data) throws CMSException {

		this(new CMSSignedData(data), new CertificatePool());
	}

	/**
	 * @param data     byte array representing CMSSignedData
	 * @param certPool can be null
	 * @throws org.bouncycastle.cms.CMSException
	 */
	public CAdESSignature(final byte[] data, final CertificatePool certPool) throws CMSException {

		this(new CMSSignedData(data), certPool);
	}

	/**
	 * The default constructor for CAdESSignature.
	 *
	 * @param cms      CMSSignedData
	 * @param certPool can be null
	 */
	public CAdESSignature(final CMSSignedData cms, final CertificatePool certPool) {

		this(cms, getFirstSignerInformation(cms), certPool);
	}

	/**
	 * The default constructor for CAdESSignature.
	 *
	 * @param cms               CMSSignedData
	 * @param signerInformation an expanded SignerInfo block from a CMS Signed message
	 * @param certPool          can be null
	 */
	public CAdESSignature(final CMSSignedData cms, final SignerInformation signerInformation, final CertificatePool certPool) {
		this(cms, signerInformation, certPool, null);
	}

	/**
	 * @param cmsSignedData     CMSSignedData
	 * @param signerInformation an expanded SignerInfo block from a CMS Signed message
	 * @param externalContent   the external signed content if detached signature
	 */
	public CAdESSignature(final CMSSignedData cmsSignedData, final SignerInformation signerInformation, final DSSDocument externalContent) {
		this(cmsSignedData, signerInformation, new CertificatePool(), externalContent);
	}

	/**
	 * The default constructor for CAdESSignature.
	 *
	 * @param cmsSignedData     CMSSignedData
	 * @param signerInformation an expanded SignerInfo block from a CMS Signed message
	 * @param certPool          can be null
	 * @param externalContent   the external signed content if detached signature
	 */
	public CAdESSignature(final CMSSignedData cmsSignedData, final SignerInformation signerInformation, final CertificatePool certPool, final DSSDocument externalContent) {

		this.cmsSignedData = cmsSignedData;
		this.signerInformation = signerInformation;
		this.certPool = certPool;
		this.externalContent = externalContent;
	}

	/**
	 * Returns the first {@code SignerInformation} extracted from {@code CMSSignedData}.
	 *
	 * @param cms CMSSignedData
	 * @return returns {@code SignerInformation}
	 */
	private static SignerInformation getFirstSignerInformation(final CMSSignedData cms) {

		final SignerInformation signerInformation = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
		return signerInformation;
	}

	/**
	 * This method returns the certificate pool used by this instance to handle encapsulated certificates.
	 *
	 * @return the certificate pool associated with the signature
	 */
	public CertificatePool getCertPool() {
		return certPool;
	}

	@Override
	public SignatureForm getSignatureForm() {

		return SignatureForm.CAdES;
	}

	@Override
	public CAdESCertificateSource getCertificateSource() {

		if (certSource == null) {

			certSource = new CAdESCertificateSource(cmsSignedData, signerInformation, certPool);
		}
		return certSource;
	}

	@SuppressWarnings("unchecked")
	@Override
	public OfflineCRLSource getCRLSource() {

		CAdESCRLSource crlSource = null;
		try {
			crlSource = new CAdESCRLSource(cmsSignedData, signerInformation);
		} catch (Exception e) {
			// When error in computing or in format the algorithm: just continues (will try to get online information)
			LOG.warn("When error in computing or in format the algorithm just continue...", e);
		}
		return crlSource;
	}

	@Override
	public OfflineOCSPSource getOCSPSource() {

		final CAdESOCSPSource cadesOCSPSource = new CAdESOCSPSource(cmsSignedData, signerInformation);
		return cadesOCSPSource;
	}

	/**
	 * ETSI TS 101 733 V2.2.1 (2013-04)<p/>
	 * 5.6.3 Signature Verification Process<p/>
	 * TODO (Bob 28.05.2014) The position of the signing certificate must be clarified
	 * ...the public key from the first certificate identified in the sequence of certificate identifiers from SigningCertificate shall be the key used to verify the digital
	 * signature.
	 *
	 * @return
	 */
	@Override
	public CandidatesForSigningCertificate getCandidatesForSigningCertificate() {

		if (candidatesForSigningCertificate != null) {

			return candidatesForSigningCertificate;
		}
		LOG.debug("--> Searching the signing certificate...");
		candidatesForSigningCertificate = new CandidatesForSigningCertificate();

		final Collection<CertificateToken> keyInfoCertificates = getCertificateSource().getKeyInfoCertificates();
		final SignerId sid = signerInformation.getSID();
		for (final CertificateToken certificateToken : keyInfoCertificates) {

			final SigningCertificateValidity signingCertificateValidity = new SigningCertificateValidity();
			signingCertificateValidity.setCertificateToken(certificateToken);
			candidatesForSigningCertificate.addSigningCertificateValidityList(signingCertificateValidity);

			final X509CertificateHolder x509CertificateHolder = DSSUtils.getX509CertificateHolder(certificateToken);
			final boolean match = sid.match(x509CertificateHolder);
			if (match) {

				this.signingCertificateValidity = signingCertificateValidity;
				break;
			}
		}
		if (signingCertificateValidity == null) {

			LOG.debug("--> Signing certificate not found: " + sid);
			return candidatesForSigningCertificate;
		}

		final IssuerSerial signingTokenIssuerSerial = DSSUtils.getIssuerSerial(signingCertificateValidity.getCertificateToken());
		final BigInteger signingTokenSerialNumber = signingTokenIssuerSerial.getSerial().getValue();
		final GeneralNames signingTokenIssuerName = signingTokenIssuerSerial.getIssuer();

		final AttributeTable signedAttributes = getSignedAttributes(signerInformation);
		final Attribute signingCertificateAttributeV1 = signedAttributes.get(PKCSObjectIdentifiers.id_aa_signingCertificate);
		if (signingCertificateAttributeV1 != null) {

			signingCertificateValidity.setAttributePresent(true);
			verifySigningCertificateV1(signingTokenSerialNumber, signingTokenIssuerName, signingCertificateAttributeV1);
			return candidatesForSigningCertificate;
		}
		final Attribute signingCertificateAttributeV2 = signedAttributes.get(PKCSObjectIdentifiers.id_aa_signingCertificateV2);
		if (signingCertificateAttributeV2 != null) {

			signingCertificateValidity.setAttributePresent(true);
			verifySigningCertificateV2(signingTokenSerialNumber, signingTokenIssuerName, signingCertificateAttributeV2);
			return candidatesForSigningCertificate;
		}
		LOG.debug("--> There is no signed reference to the signing certificate: " + signingCertificateValidity.getCertificateToken().getAbbreviation());
		return candidatesForSigningCertificate;
	}

	private void verifySigningCertificateV1(final BigInteger signingTokenSerialNumber, final GeneralNames signingTokenIssuerName, final Attribute signingCertificateAttributeV1) {

		final DigestAlgorithm digestAlgorithm = DigestAlgorithm.SHA1;
		final byte[] signingTokenCertHash = DSSUtils.digest(digestAlgorithm, signingCertificateValidity.getCertificateToken().getEncoded());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Candidate Certificate Hash {} with algorithm {}", DSSUtils.encodeHexString(signingTokenCertHash), digestAlgorithm.getName());
		}

		final ASN1Set attrValues = signingCertificateAttributeV1.getAttrValues();
		for (int ii = 0; ii < attrValues.size(); ii++) {

			final ASN1Encodable asn1Encodable = attrValues.getObjectAt(ii);
			final SigningCertificate signingCertificate = SigningCertificate.getInstance(asn1Encodable);
			final ESSCertID[] essCertIDs = signingCertificate.getCerts();
			for (final ESSCertID essCertID : essCertIDs) {

				final byte[] certHash = essCertID.getCertHash();
				signingCertificateValidity.setDigestPresent(true);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found Certificate Hash in signingCertificateAttributeV1 {} with algorithm {}", DSSUtils.encodeHexString(signingTokenCertHash),
						  digestAlgorithm.getName());
				}
				final IssuerSerial issuerSerial = essCertID.getIssuerSerial();
				final boolean match = verifySigningCertificateReferences(signingTokenSerialNumber, signingTokenIssuerName, signingTokenCertHash, certHash, issuerSerial);
				if (match) {
					break;
				}
			}
		}
	}

	private void verifySigningCertificateV2(final BigInteger signingTokenSerialNumber, final GeneralNames signingTokenIssuerName, final Attribute signingCertificateAttributeV2) {

		final ASN1Set attrValues = signingCertificateAttributeV2.getAttrValues();

		DigestAlgorithm lastDigestAlgorithm = null;
		byte[] signingTokenCertHash = null;

		for (int ii = 0; ii < attrValues.size(); ii++) {

			final ASN1Encodable asn1Encodable = attrValues.getObjectAt(ii);
			final SigningCertificateV2 signingCertificateAttribute = SigningCertificateV2.getInstance(asn1Encodable);
			final ESSCertIDv2[] essCertIDv2s = signingCertificateAttribute.getCerts();
			for (final ESSCertIDv2 essCertIDv2 : essCertIDv2s) {

				final String algorithmId = essCertIDv2.getHashAlgorithm().getAlgorithm().getId();
				final DigestAlgorithm digestAlgorithm = DigestAlgorithm.forOID(algorithmId);
				if (digestAlgorithm != lastDigestAlgorithm) {

					signingTokenCertHash = DSSUtils.digest(digestAlgorithm, signingCertificateValidity.getCertificateToken().getEncoded());
					if (LOG.isDebugEnabled()) {
						LOG.debug("Candidate Certificate Hash {} with algorithm {}", DSSUtils.encodeHexString(signingTokenCertHash), digestAlgorithm.getName());
					}
					lastDigestAlgorithm = digestAlgorithm;
				}
				final byte[] certHash = essCertIDv2.getCertHash();
				signingCertificateValidity.setDigestPresent(true);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found Certificate Hash in signingCertificateAttributeV2 {} with algorithm {}", DSSUtils.encodeHexString(signingTokenCertHash),
						  digestAlgorithm.getName());
				}
				final IssuerSerial issuerSerial = essCertIDv2.getIssuerSerial();
				final boolean match = verifySigningCertificateReferences(signingTokenSerialNumber, signingTokenIssuerName, signingTokenCertHash, certHash, issuerSerial);
				if (match) {
					break;
				}
			}
		}
	}

	private boolean verifySigningCertificateReferences(final BigInteger signingTokenSerialNumber, final GeneralNames signingTokenIssuerName, final byte[] signingTokenCertHash,
	                                                   final byte[] certHash, final IssuerSerial issuerSerial) {

		final boolean hashEqual = Arrays.equals(certHash, signingTokenCertHash);
		signingCertificateValidity.setDigestEqual(hashEqual);

		boolean serialNumberEqual = false;
		if (issuerSerial != null) {

			final BigInteger serialNumber = issuerSerial.getSerial().getValue();
			serialNumberEqual = serialNumber.equals(signingTokenSerialNumber);

			signingCertificateValidity.setSerialNumberEqual(serialNumberEqual);
		}
		boolean issuerNameEqual = false;
		if (issuerSerial != null) {

			final GeneralNames issuerName = issuerSerial.getIssuer();

			final String canonicalizedIssuerName = getCanonicalizedName(issuerName);
			final String canonicalizedSigningTokenIssuerName = getCanonicalizedName(signingTokenIssuerName);

			issuerNameEqual = canonicalizedIssuerName.equals(canonicalizedSigningTokenIssuerName);

			// DOES NOT WORK:
			// issuerNameEqual = issuerName.equals(signingTokenIssuerName);
			signingCertificateValidity.setDistinguishedNameEqual(issuerNameEqual);
		}
		// candidatesForSigningCertificate.setSerialNumberEqual(true);
		// candidatesForSigningCertificate.setDistinguishedNameEqual(true);
		// return true;
		return hashEqual && serialNumberEqual && issuerNameEqual;
	}

	static String getCanonicalizedName(final GeneralNames generalNames) {

		final GeneralName[] names = generalNames.getNames();
		final TreeMap<String, String> treeMap = new TreeMap<String, String>();
		for (final GeneralName name : names) {

			final String ldapString = String.valueOf(name.getName());
			LOG.debug("ldapString to canonicalize: {} ", ldapString);
			try {

				final LdapName ldapName = new LdapName(ldapString);
				final List<Rdn> rdns = ldapName.getRdns();
				for (final Rdn rdn : rdns) {

					treeMap.put(rdn.getType().toLowerCase(), String.valueOf(rdn.getValue()).toLowerCase());
				}
			} catch (InvalidNameException e) {
				throw new DSSException(e);
			}
		}
		final StringBuilder stringBuilder = new StringBuilder();
		for (final Map.Entry<String, String> entry : treeMap.entrySet()) {

			stringBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append('|');
		}
		final String canonicalizedName = stringBuilder.toString();
		LOG.debug("canonicalizedName: {} ", canonicalizedName);
		return canonicalizedName;

/*
        final X500Principal x500Name = new X500Principal(name);
        final String canonicalizedNameCANONICAL = x500Name.getName(X500Principal.CANONICAL);
        System.out.println("===== CANONICAL ==== " + canonicalizedNameCANONICAL);
        final String canonicalizedNameRFC1779 = x500Name.getName(X500Principal.RFC1779);
        System.out.println("===== RFC1779   ==== " + canonicalizedNameRFC1779);
        final String canonicalizedNameRFC2253 = x500Name.getName(X500Principal.RFC2253);
        System.out.println("===== RFC2253   ==== " + canonicalizedNameRFC2253);
        return canonicalizedNameRFC2253;
*/
	}

	@Override
	public List<CertificateToken> getCertificates() {
		return getCertificateSource().getCertificates();
	}

	/**
	 * 31 ETSI TS 101 733 V2.2.1 (2013-04)
	 * <p/>
	 * 5.8.1 signature-policy-identifier
	 * The present document mandates that for CAdES-EPES, a reference to the signature policy is included in the
	 * signedData. This reference is explicitly identified. A signature policy defines the rules for creation and validation of
	 * an electronic signature, and is included as a signed attribute with every Explicit Policy-based Electronic Signature. The
	 * signature-policy-identifier shall be a signed attribute.
	 * <p/>
	 * The following object identifier identifies the signature-policy-identifier attribute:
	 * ... id-aa-ets-sigPolicyId OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs9(9) smime(16) id-aa(2) 15 }
	 * signature-policy-identifier attribute values have ASN.1 type SignaturePolicyIdentifier:
	 * ... SignaturePolicyIdentifier ::=CHOICE{
	 * ...... signaturePolicyId ......... SignaturePolicyId,
	 * ...... signaturePolicyImplied .... SignaturePolicyImplied -- not used in this version}
	 * <p/>
	 * ... SignaturePolicyId ::= SEQUENCE {
	 * ...... sigPolicyId ......... SigPolicyId,
	 * ...... sigPolicyHash ....... SigPolicyHash,
	 * ...... sigPolicyQualifiers . SEQUENCE SIZE (1..MAX) OF SigPolicyQualifierInfo OPTIONAL}
	 * <p/>
	 * ... SignaturePolicyImplied ::= NULL
	 * <p/>
	 * NOTE: {@code SignaturePolicyImplied} -- not used in this version
	 *
	 * @return
	 */
	@Override
	public SignaturePolicy getPolicyId() {

		final AttributeTable attributes = signerInformation.getSignedAttributes();
		if (attributes == null) {
			return null;
		}

		final Attribute attribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_sigPolicyId);
		if (attribute == null) {
			return null;
		}

		final ASN1Encodable attrValue = attribute.getAttrValues().getObjectAt(0);
		if (attrValue instanceof DERNull) {
			return null;
		}

		final SignaturePolicyId sigPolicy = SignaturePolicyId.getInstance(attrValue);
		if (sigPolicy == null) {
			return null;
		}

		final String policyId = sigPolicy.getSigPolicyId().getId();
		final SignaturePolicy signaturePolicy = new SignaturePolicy(policyId);

		final OtherHashAlgAndValue hashAlgAndValue = sigPolicy.getSigPolicyHash();

		final AlgorithmIdentifier digestAlgorithmIdentifier = hashAlgAndValue.getHashAlgorithm();
		final String digestAlgorithmOID = digestAlgorithmIdentifier.getAlgorithm().getId();
		final DigestAlgorithm digestAlgorithm = DigestAlgorithm.forOID(digestAlgorithmOID);
		signaturePolicy.setDigestAlgorithm(digestAlgorithm);

		final ASN1OctetString digestValue = hashAlgAndValue.getHashValue();
		final byte[] digestValueBytes = digestValue.getOctets();
		final String policyDigestHexValue = DSSUtils.toHex(digestValueBytes);
		signaturePolicy.setDigestValue(policyDigestHexValue);

		final SigPolicyQualifiers sigPolicyQualifiers = sigPolicy.getSigPolicyQualifiers();
		if (sigPolicyQualifiers == null) {

			return signaturePolicy;
		}
		for (int ii = 0; ii < sigPolicyQualifiers.size(); ii++) {

			final SigPolicyQualifierInfo policyQualifierInfo = sigPolicyQualifiers.getInfoAt(ii);
			final ASN1ObjectIdentifier policyQualifierInfoId = policyQualifierInfo.getSigPolicyQualifierId();
			final String policyQualifierInfoValue = policyQualifierInfo.getSigQualifier().toString();

			if (PKCSObjectIdentifiers.id_spq_ets_unotice.equals(policyQualifierInfoId)) {

				signaturePolicy.setNotice(policyQualifierInfoValue);
			} else if (PKCSObjectIdentifiers.id_spq_ets_uri.equals(policyQualifierInfoId)) {

				signaturePolicy.setUrl(policyQualifierInfoValue);
			} else {
				LOG.error("Unknown signature policy qualifier id: " + policyQualifierInfoId + " with value: " + policyQualifierInfoValue);
			}
		}
		return signaturePolicy;
	}

	@Override
	public Date getSigningTime() {

		final AttributeTable attributes = signerInformation.getSignedAttributes();
		if (attributes == null) {
			return null;
		}

		final Attribute attr = attributes.get(PKCSObjectIdentifiers.pkcs_9_at_signingTime);
		if (attr == null) {

			return null;
		}
		final ASN1Set attrValues = attr.getAttrValues();
		final ASN1Encodable attrValue = attrValues.getObjectAt(0);
		final Date signingDate;
		if (attrValue instanceof ASN1UTCTime) {
			signingDate = DSSASN1Utils.toDate((ASN1UTCTime) attrValue);
		} else if (attrValue instanceof Time) {
			signingDate = ((Time) attrValue).getDate();
		} else if (attrValue instanceof ASN1GeneralizedTime) {
			signingDate = DSSASN1Utils.toDate((ASN1GeneralizedTime) attrValue);
		} else {
			signingDate = null;
		}
		if (signingDate != null) {
		        /*
		        RFC 3852 [4] states that "dates between January 1, 1950 and December 31, 2049 (inclusive) must be
                encoded as UTCTime. Any dates with year values before 1950 or after 2049 must be encoded as
                GeneralizedTime".
                */
			if (!(signingDate.before(JANUARY_1950) && signingDate.after(JANUARY_2050))) {
				// must be ASN1UTCTime
				if (!(attrValue instanceof ASN1UTCTime)) {
					LOG.error(
						  "RFC 3852 states that dates between January 1, 1950 and December 31, 2049 (inclusive) must be encoded as UTCTime. Any dates with year values before 1950 or after 2049 must be encoded as GeneralizedTime. Date found is %s encoded as %s",
						  signingDate.toString(), attrValue.getClass());
					return null;
				}
			}
			return signingDate;
		}
		if (LOG.isErrorEnabled()) {
			LOG.error("Error when reading signing time. Unrecognized " + attrValue.getClass());
		}
		return null;
	}

	/**
	 * @return the cmsSignedData
	 */
	public CMSSignedData getCmsSignedData() {

		return cmsSignedData;
	}

	@Override
	public SignatureProductionPlace getSignatureProductionPlace() {

		final AttributeTable attributes = signerInformation.getSignedAttributes();
		if (attributes == null) {

			return null;
		}
		Attribute signatureProductionPlaceAttr = attributes.get(PKCSObjectIdentifiers.id_aa_ets_signerLocation);
		if (signatureProductionPlaceAttr == null) {

			return null;
		}

		final ASN1Encodable asn1Encodable = signatureProductionPlaceAttr.getAttrValues().getObjectAt(0);
		SignerLocation signerLocation = null;
		try {
			signerLocation = SignerLocation.getInstance(asn1Encodable);
		} catch (Exception e) {
/*
            // TODO: (Bob: 2013 Dec 11) ---> Validation: /C:/ws_trunk/apps/dss/core/dss-document/target/test-classes/cades2013/CAdES-EPES.SCOK/BULL/Signature-C-EPES-2.p7s

            WARN  SignedDocumentValidator.java:557 - org.bouncycastle.asn1.DERUTF8String cannot be cast to org.bouncycastle.asn1.DERTaggedObject
            java.lang.ClassCastException: org.bouncycastle.asn1.DERUTF8String cannot be cast to org.bouncycastle.asn1.DERTaggedObject
            at org.bouncycastle.asn1.esf.SignerLocation.<init>(Unknown Source) ~[bcprov-ext-jdk15on-1.49.jar:1.49.0]
            at org.bouncycastle.asn1.esf.SignerLocation.getInstance(Unknown Source) ~[bcprov-ext-jdk15on-1.49.jar:1.49.0]
            at eu.europa.ec.markt.dss.validation102853.cades.CAdESSignature.getSignatureProductionPlace(CAdESSignature.java:663) ~[classes/:na]
            at eu.europa.ec.markt.dss.validation102853.SignedDocumentValidator.dealSignature(SignedDocumentValidator.java:1209) [classes/:na]
            at eu.europa.ec.markt.dss.validation102853.SignedDocumentValidator.validateSignature(SignedDocumentValidator.java:535) [classes/:na]
            at eu.europa.ec.markt.dss.validation102853.SignedDocumentValidator.generateDiagnosticData(SignedDocumentValidator.java:510) [classes/:na]
            at eu.europa.ec.markt.dss.validation102853.SignedDocumentValidator.validateDocument(SignedDocumentValidator.java:469) [classes/:na]
            at eu.europa.ec.markt.dss.validation102853.SignedDocumentValidator.validateDocument(SignedDocumentValidator.java:450) [classes/:na]
*/
			LOG.error(e.getMessage(), e);
		}
		if (signerLocation == null) {
			return null;
		}
		final SignatureProductionPlace signatureProductionPlace = new SignatureProductionPlace();
		final DERUTF8String countryName = signerLocation.getCountryName();
		if (countryName != null) {

			signatureProductionPlace.setCountryName(countryName.getString());
		}
		final DERUTF8String localityName = signerLocation.getLocalityName();
		if (localityName != null) {

			signatureProductionPlace.setCity(localityName.getString());
		}
		final StringBuilder address = new StringBuilder();
		final ASN1Sequence seq = signerLocation.getPostalAddress();
		if (seq != null) {

			for (int ii = 0; ii < seq.size(); ii++) {

				if (seq.getObjectAt(ii) instanceof DEROctetString) {
					if (address.length() > 0) {
						address.append(" / ");
					}
					// TODO: getOctets returns an array
					address.append(new String(((DEROctetString) seq.getObjectAt(ii)).getOctets()));
				} else if (seq.getObjectAt(ii) instanceof DERUTF8String) {

					if (address.length() > 0) {
						address.append(" / ");
					}
					final DERUTF8String derutf8String = (DERUTF8String) seq.getObjectAt(ii);
					address.append(derutf8String.getString());
				}
			}
		}
		signatureProductionPlace.setAddress(address.toString());
		// This property is not used in CAdES version of signature
		// signatureProductionPlace.setStateOrProvince(stateOrProvince);
		return signatureProductionPlace;
	}

	@Override
	public CommitmentType getCommitmentTypeIndication() {

		final AttributeTable attributes = signerInformation.getSignedAttributes();
		if (attributes == null) {

			return null;
		}

		Attribute commitmentTypeIndicationAttribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_commitmentType);
		if (commitmentTypeIndicationAttribute != null) {

			try {
				final DERSequence derSequence = (DERSequence) commitmentTypeIndicationAttribute.getAttrValues().getObjectAt(0);
				final int size = derSequence.size();
				if (size > 0) {

					CommitmentType commitmentType = new CommitmentType();
					for (int ii = 0; ii < size; ii++) {

						final ASN1Encodable objectAt = derSequence.getObjectAt(ii);
						final CommitmentTypeIndication commitmentTypeIndication = CommitmentTypeIndication.getInstance(objectAt);
						final ASN1ObjectIdentifier commitmentTypeId = commitmentTypeIndication.getCommitmentTypeId();
						commitmentType.addIdentifier(commitmentTypeId.getId());
					}
					return commitmentType;
				}
			} catch (Exception e) {
				throw new DSSException("Error when dealing with CommitmentTypeIndication!", e);
			}
		}
		return null;
	}

	@Override
	public String[] getClaimedSignerRoles() {

		final AttributeTable attributes = signerInformation.getSignedAttributes();
		if (attributes == null) {
			return null;
		}

		final Attribute id_aa_ets_signerAttr = attributes.get(PKCSObjectIdentifiers.id_aa_ets_signerAttr);
		if (id_aa_ets_signerAttr == null) {
			return null;
		}
		final ASN1Set attrValues = id_aa_ets_signerAttr.getAttrValues();
		final ASN1Encodable attrValue = attrValues.getObjectAt(0);
		try {

			final SignerAttribute signerAttr = SignerAttribute.getInstance(attrValue);
			if (signerAttr == null) {
				return null;
			}
			final List<String> claimedRoles = new ArrayList<String>();
			final Object[] signerAttrValues = signerAttr.getValues();
			for (final Object signerAttrValue : signerAttrValues) {

				if (!(signerAttrValue instanceof org.bouncycastle.asn1.x509.Attribute[])) {

					continue;
				}
				final org.bouncycastle.asn1.x509.Attribute[] signerAttrValueArray = (org.bouncycastle.asn1.x509.Attribute[]) signerAttrValue;
				for (final org.bouncycastle.asn1.x509.Attribute claimedRole : signerAttrValueArray) {

					final ASN1Encodable[] attrValues1 = claimedRole.getAttrValues().toArray();
					for (final ASN1Encodable asn1Encodable : attrValues1) {
						if (asn1Encodable instanceof ASN1String) {
							ASN1String asn1String = (ASN1String) asn1Encodable;
							final String s = asn1String.getString();
							claimedRoles.add(s);
						}
					}
				}
			}
			final String[] strings = claimedRoles.toArray(new String[claimedRoles.size()]);
			return strings;
		} catch (Exception e) {

			throw new DSSException("Error when dealing with claimed signer roles: [" + attrValue.toString() + "]", e);
		}
	}

	@Override
	public List<CertifiedRole> getCertifiedSignerRoles() {

		final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
		if (signedAttributes == null) {
			return null;
		}

		final Attribute id_aa_ets_signerAttr = signedAttributes.get(PKCSObjectIdentifiers.id_aa_ets_signerAttr);
		if (id_aa_ets_signerAttr == null) {
			return null;
		}
		final ASN1Set attrValues = id_aa_ets_signerAttr.getAttrValues();
		final ASN1Encodable asn1EncodableAttrValue = attrValues.getObjectAt(0);
		try {

			final SignerAttribute signerAttr = SignerAttribute.getInstance(asn1EncodableAttrValue);
			if (signerAttr == null) {
				return null;
			}
			List<CertifiedRole> roles = null;
			final Object[] signerAttrValues = signerAttr.getValues();
			for (final Object signerAttrValue : signerAttrValues) {

				if (signerAttrValue instanceof AttributeCertificate) {

					if (roles == null) {

						roles = new ArrayList<CertifiedRole>();
					}
					final AttributeCertificate attributeCertificate = (AttributeCertificate) signerAttrValue;
					final AttributeCertificateInfo acInfo = attributeCertificate.getAcinfo();
					final AttCertValidityPeriod attrCertValidityPeriod = acInfo.getAttrCertValidityPeriod();
					final ASN1Sequence attributes = acInfo.getAttributes();
					for (int ii = 0; ii < attributes.size(); ii++) {

						final ASN1Encodable objectAt = attributes.getObjectAt(ii);
						final org.bouncycastle.asn1.x509.Attribute attribute = org.bouncycastle.asn1.x509.Attribute.getInstance(objectAt);
						// System.out.println(attribute.getAttrType().getId());
						final ASN1Set attrValues1 = attribute.getAttrValues();
						DERSequence derSequence = (DERSequence) attrValues1.getObjectAt(0);
						RoleSyntax roleSyntax = RoleSyntax.getInstance(derSequence);
						CertifiedRole certifiedRole = new CertifiedRole();
						certifiedRole.setRole(roleSyntax.getRoleNameAsString());
						certifiedRole.setNotBefore(DSSASN1Utils.toDate(attrCertValidityPeriod.getNotBeforeTime()));
						certifiedRole.setNotAfter(DSSASN1Utils.toDate(attrCertValidityPeriod.getNotAfterTime()));
						roles.add(certifiedRole);
					}
				}
			}
			return roles;
		} catch (Exception e) {

			throw new DSSException("Error when dealing with certified signer roles: [" + asn1EncodableAttrValue.toString() + "]", e);
		}
	}

	private List<TimestampToken> getTimestampList(final ASN1ObjectIdentifier attrType, final TimestampType timestampType, final ArchiveTimestampType archiveTimestampType) {

		final List<TimestampToken> list = new ArrayList<TimestampToken>();

		final AttributeTable attributes;
		if (attrType.equals(PKCSObjectIdentifiers.id_aa_ets_contentTimestamp)) {

			attributes = signerInformation.getSignedAttributes();
		} else {

			attributes = signerInformation.getUnsignedAttributes();
		}
		if (attributes == null) {
			return list;
		}
		final ASN1EncodableVector archiveList = attributes.getAll(attrType);
		for (int i = 0; i < archiveList.size(); i++) {
			final Attribute attribute = (Attribute) archiveList.get(i);

			final ASN1Set attrValues = attribute.getAttrValues();
			for (final ASN1Encodable value : attrValues.toArray()) {
				try {
					TimeStampToken token = new TimeStampToken(new CMSSignedData(value.toASN1Primitive().getEncoded(ASN1Encoding.DER)));
					final TimestampToken timestampToken = new TimestampToken(token, timestampType, certPool);
					timestampToken.setArchiveTimestampType(archiveTimestampType);
					list.add(timestampToken);
				} catch (Exception e) {
					throw new RuntimeException("Parsing error", e);
				}
			}
		}
		return list;
	}

	public List<TimestampToken> getContentTimestamps() {

		if (contentTimestamps == null) {
			contentTimestamps = getTimestampList(PKCSObjectIdentifiers.id_aa_ets_contentTimestamp, TimestampType.CONTENT_TIMESTAMP, null);
		}
		return contentTimestamps;
	}

	@Override
	public byte[] getContentTimestampData(final TimestampToken timestampToken) {

		final ContentInfo contentInfo = cmsSignedData.toASN1Structure();
		final SignedData signedData = SignedData.getInstance(contentInfo.getContent());

		ContentInfo content = signedData.getEncapContentInfo();
		//		if (content == null || content.getContent() == null) {
		//			    /* Detached signatures have either no encapContentInfo in signedData, or it exists but has no eContent */
		//			if (getOriginalDocumentBytes() != null) {
		//				data.write(content.toASN1Primitive().getEncoded());
		//				data.write(getOriginalDocumentBytes());
		//			} else {
		//				throw new DSSException("Signature is detached and no original data provided.");
		//			}
		//		} else {

		ASN1OctetString octet = (ASN1OctetString) content.getContent();
		return octet.getOctets();
		//		ContentInfo info2 = new ContentInfo(PKCSObjectIdentifiers.data, octet);
		//		byte[] contentInfoBytes = null;
		//		try {
		//			contentInfoBytes = info2.getEncoded();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		if (LOG.isTraceEnabled()) {
		//			LOG.trace("Content Info: {}", DSSUtils.toHex(contentInfoBytes));
		//		}
		//		return contentInfoBytes;

		//		return DSSUtils.EMPTY_BYTE_ARRAY;
	}

	@Override
	public List<TimestampToken> getSignatureTimestamps() throws RuntimeException {

		if (signatureTimestamps == null) {
			signatureTimestamps = getTimestampList(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken, TimestampType.SIGNATURE_TIMESTAMP, null);
		}
		return signatureTimestamps;
	}

	@Override
	public List<TimestampToken> getTimestampsX1() {

		if (sigAndRefsTimestamps == null) {
			sigAndRefsTimestamps = getTimestampList(PKCSObjectIdentifiers.id_aa_ets_escTimeStamp, TimestampType.VALIDATION_DATA_TIMESTAMP, null);
		}
		return sigAndRefsTimestamps;
	}

	@Override
	public List<TimestampToken> getTimestampsX2() {

		if (refsOnlyTimestamps == null) {
			refsOnlyTimestamps = getTimestampList(PKCSObjectIdentifiers.id_aa_ets_certCRLTimestamp, TimestampType.VALIDATION_DATA_REFSONLY_TIMESTAMP, null);
		}
		return refsOnlyTimestamps;
	}

	@Override
	public List<TimestampToken> getArchiveTimestamps() {

		if (archiveTimestamps == null) {

			archiveTimestamps = getTimestampList(OID.id_aa_ets_archiveTimestampV2, TimestampType.ARCHIVE_TIMESTAMP, ArchiveTimestampType.CAdES_V2);
			final List<TimestampToken> timestampList2 = getTimestampList(OID.id_aa_ets_archiveTimestampV3, TimestampType.ARCHIVE_TIMESTAMP, ArchiveTimestampType.CAdES_v3);
			archiveTimestamps.addAll(timestampList2);
		}
		return archiveTimestamps;
	}

	@Override
	public EncryptionAlgorithm getEncryptionAlgo() {

		String oid = signerInformation.getEncryptionAlgOID();

		try {
			return EncryptionAlgorithm.forOID(oid);
		} catch (RuntimeException e) {
			// purposely empty
		}

		// fallback to identify via signature algorithm
		SignatureAlgorithm signatureAlgo = SignatureAlgorithm.forOID(oid);
		return signatureAlgo.getEncryptionAlgo();
	}

	@Override
	public DigestAlgorithm getDigestAlgo() {

		final String digestAlgOID = signerInformation.getDigestAlgOID();
		return DigestAlgorithm.forOID(digestAlgOID);
	}

	@Override
	public SignatureCryptographicVerification checkIntegrity(final DSSDocument detachedDocument) {

		return checkIntegrity(detachedDocument, null);
	}

	@Override
	public SignatureCryptographicVerification checkIntegrity(final DSSDocument detachedDocument, final CertificateToken providedSigningCertificate) {

		final SignatureCryptographicVerification scv = new SignatureCryptographicVerification();
		try {

			final SignerInformation signerInformationToCheck;
			if (detachedDocument == null) {
				signerInformationToCheck = signerInformation;
			} else {
				// Recreate a SignerInformation with the content using a CMSSignedDataParser
				final CMSTypedStream signedContent = new CMSTypedStream(detachedDocument.openStream());
				final CMSSignedDataParser sp = new CMSSignedDataParser(new BcDigestCalculatorProvider(), signedContent, cmsSignedData.getEncoded());
				sp.getSignedContent().drain();
				final SignerId sid = signerInformation.getSID();
				signerInformationToCheck = sp.getSignerInfos().get(sid);
			}
			final List<SigningCertificateValidity> signingCertificateValidityList;
			if (providedSigningCertificate == null) {

				// To determine the signing certificate it is necessary to browse through all candidates found before.
				final CandidatesForSigningCertificate candidatesForSigningCertificate = getCandidatesForSigningCertificate();
				signingCertificateValidityList = candidatesForSigningCertificate.getSigningCertificateValidityList();
				if (signingCertificateValidityList.size() == 0) {

					scv.setErrorMessage("There is no signing certificate within the signature.");
					return scv;
				}
			} else {

				candidatesForSigningCertificate = new CandidatesForSigningCertificate();
				final SigningCertificateValidity signingCertificateValidity = new SigningCertificateValidity();
				signingCertificateValidity.setCertificateToken(providedSigningCertificate);
				candidatesForSigningCertificate.addSigningCertificateValidityList(signingCertificateValidity);
				signingCertificateValidityList = candidatesForSigningCertificate.getSigningCertificateValidityList();

			}
			LOG.debug("CHECK SIGNATURE VALIDITY: ");
			for (final SigningCertificateValidity signingCertificateValidity : signingCertificateValidityList) {

				try {

					// In the case where one of the mandatory attributes is missing we set already the candidate for the signing certificate.
					// see: validation.at.nqs.bdc.TestNotQualifiedBDC.test1()
					candidatesForSigningCertificate.setTheSigningCertificateValidity(signingCertificateValidity);

					final JcaSimpleSignerInfoVerifierBuilder verifier = new JcaSimpleSignerInfoVerifierBuilder();
					final CertificateToken certificateToken = signingCertificateValidity.getCertificateToken();
					final X509Certificate certificate = certificateToken.getCertificate();
					final SignerInformationVerifier signerInformationVerifier = verifier.build(certificate);
					// TODO: (Bob: 2013 Dec 06) The BC does not implement if way indicated in ETSI 102853 the validation of the signature. Each time a problem is encountered an exception
					// TODO: (Bob: 2013 Dec 06) is raised. Solution extract the BC method and adapt.
					LOG.debug(" - WITH SIGNING CERTIFICATE: " + certificateToken.getAbbreviation());

					boolean signatureIntact = signerInformationToCheck.verify(signerInformationVerifier);
					scv.setReferenceDataFound(signatureIntact);
					scv.setReferenceDataIntact(signatureIntact);
					scv.setSignatureIntact(signatureIntact);
					if (signatureIntact) {
						break;
					}
				} catch (RuntimeOperatorException e) {

					// C’est un problème de compatibilité avec Java 7. L’implémentation de la classe sun.security.rsa.RSASignature a changé entre la version 6 et 7. Bouncy castle ne
					// prend pas correctement en compte ce changement. En effet, une exception est levée par la version 7 que BC ne catch pas correctement ce qui se traduit par
					// l’envoi d’une exception : org.bouncycastle.operator.RuntimeOperatorException (Bob)
					LOG.warn(e.getMessage(), e);
				} catch (CMSSignerDigestMismatchException e) {
					LOG.error(e.getMessage(), e);
					scv.setReferenceDataFound(true);
					scv.setReferenceDataIntact(false);
					scv.setSignatureIntact(false);
					scv.setErrorMessage(e.getMessage());
				} catch (OperatorCreationException e) {
					LOG.error(e.getMessage(), e);
					scv.setErrorMessage(e.getMessage());
				} catch (CMSException e) {
					LOG.error(e.getMessage(), e);
					scv.setErrorMessage(e.getMessage());
				} catch (IllegalArgumentException e) {
					// Can arrive when for example:
					// java.lang.IllegalArgumentException: Unknown signature type requested: RIPEMD160WITH0.4.0.127.0.7.1.1.4.1.6
					// at org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder.generate(Unknown Source) ~[bcpkix-jdk15on-1.49.jar:1.49.0]
					LOG.error(e.getMessage(), e);
					scv.setErrorMessage(e.getMessage());
				}
			}
		} catch (CMSException e) {
			LOG.error(e.getMessage(), e);
			scv.setErrorMessage(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			scv.setErrorMessage(e.getMessage());
		}
		LOG.debug(" - RESULT: " + scv.isReferenceDataFound() + "/" + scv.isReferenceDataIntact() + "/" + scv.isSignatureIntact());
		return scv;
	}

	@Override
	public String getContentType() {

		final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
		if (signedAttributes == null) {
			return null;
		}
		final Attribute contentTypeAttribute = signedAttributes.get(PKCSObjectIdentifiers.pkcs_9_at_contentType);
		if (contentTypeAttribute == null) {
			return null;
		}
		final ASN1ObjectIdentifier asn1Encodable = (ASN1ObjectIdentifier) contentTypeAttribute.getAttrValues().getObjectAt(0);
		final String contentType = asn1Encodable.getId();
		return contentType;
	}

	@Override
	public String getContentIdentifier() {

		final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
		if (signedAttributes == null) {
			return null;
		}
		final Attribute contentIdentifierAttribute = signedAttributes.get(PKCSObjectIdentifiers.id_aa_contentIdentifier);
		if (contentIdentifierAttribute == null) {
			return null;
		}
		final ASN1Encodable asn1Encodable = contentIdentifierAttribute.getAttrValues().getObjectAt(0);
		final ContentIdentifier contentIdentifier = ContentIdentifier.getInstance(asn1Encodable);
		final String contentIdentifierString = DSSASN1Utils.toString(contentIdentifier.getValue());
		return contentIdentifierString;
	}

	@Override
	public String getContentHints() {

		final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
		if (signedAttributes == null) {
			return null;
		}
		final Attribute contentHintAttribute = signedAttributes.get(PKCSObjectIdentifiers.id_aa_contentHint);
		if (contentHintAttribute == null) {
			return null;
		}
		final ASN1Encodable asn1Encodable = contentHintAttribute.getAttrValues().getObjectAt(0);
		final ContentHints contentHints = ContentHints.getInstance(asn1Encodable);
		final String contentHintsContentType = contentHints.getContentType().toString();
		final String contentHintsContentDescription = contentHints.getContentDescription().getString();
		final String contentHint = contentHintsContentType + " [" + contentHintsContentDescription + "]";
		return contentHint;
	}

	/**
	 * @return the signerInformation
	 */
	public SignerInformation getSignerInformation() {
		return signerInformation;
	}

	@Override
	public List<AdvancedSignature> getCounterSignatures() {

		final List<AdvancedSignature> list = new ArrayList<AdvancedSignature>();

		for (Object o : this.signerInformation.getCounterSignatures().getSigners()) {
			SignerInformation signerInformation = (SignerInformation) o;
			CAdESSignature info = new CAdESSignature(this.cmsSignedData, signerInformation, certPool);
			list.add(info);
		}

		return list;
	}

	@Override
	public List<CertificateRef> getCertificateRefs() {

		final List<CertificateRef> list = new ArrayList<CertificateRef>();

		final AttributeTable attributes = signerInformation.getUnsignedAttributes();
		if (attributes == null) {

			return list;
		}

		final Attribute attribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_certificateRefs);

		if (attribute == null) {
			return list;
		}

		final ASN1Set attrValues = attribute.getAttrValues();
		if (attrValues.size() <= 0) {
			return list;
		}

		final ASN1Encodable attrValue = attrValues.getObjectAt(0);
		final ASN1Sequence completeCertificateRefs = (ASN1Sequence) attrValue;

		for (int i = 0; i < completeCertificateRefs.size(); i++) {

			final OtherCertID otherCertId = OtherCertID.getInstance(completeCertificateRefs.getObjectAt(i));
			final CertificateRef certId = new CertificateRef();
			certId.setDigestAlgorithm(otherCertId.getAlgorithmHash().getAlgorithm().getId());
			certId.setDigestValue(otherCertId.getCertHash());

			final IssuerSerial issuer = otherCertId.getIssuerSerial();
			if (issuer != null) {
				final GeneralNames issuerName = issuer.getIssuer();
				if (issuerName != null) {
					certId.setIssuerName(issuerName.toString());
				}
				final DERInteger issuerSerial = issuer.getSerial();
				if (issuerSerial != null) {
					certId.setIssuerSerial(issuerSerial.toString());
				}
			}
			list.add(certId);
		}
		return list;
	}

	@Override
	public List<CRLRef> getCRLRefs() {

		final List<CRLRef> list = new ArrayList<CRLRef>();

		try {
			final AttributeTable attributes = signerInformation.getUnsignedAttributes();
			if (attributes == null) {
				return list;
			}

			final Attribute attribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_revocationRefs);

			if (attribute == null) {
				return list;
			}

			final ASN1Set attrValues = attribute.getAttrValues();
			if (attrValues.size() <= 0) {
				return list;
			}

			final ASN1Encodable attrValue = attrValues.getObjectAt(0);
			final ASN1Sequence completeCertificateRefs = (ASN1Sequence) attrValue;
			for (int ii = 0; ii < completeCertificateRefs.size(); ii++) {

				final ASN1Encodable completeCertificateRef = completeCertificateRefs.getObjectAt(ii);
				final CrlOcspRef otherCertId = CrlOcspRef.getInstance(completeCertificateRef);
				final CrlListID otherCertIds = otherCertId.getCrlids();
				if (otherCertIds != null) {

					for (final CrlValidatedID id : otherCertIds.getCrls()) {

						final CRLRef crlRef = new CRLRef(id);
						list.add(crlRef);
					}
				}
			}
		} catch (Exception e) {
			// When error in computing or in format, the algorithm just continues.
			LOG.warn("When error in computing or in format the algorithm just continue...", e);
		}

		return list;
	}

	@Override
	public List<OCSPRef> getOCSPRefs() {

		final List<OCSPRef> list = new ArrayList<OCSPRef>();

		final AttributeTable attributes = signerInformation.getUnsignedAttributes();
		if (attributes == null) {
			return list;
		}

		final Attribute attribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_revocationRefs);
		if (attribute == null) {
			return list;
		}
		final ASN1Set attrValues = attribute.getAttrValues();
		if (attrValues.size() <= 0) {
			return list;
		}

		final ASN1Encodable attrValue = attrValues.getObjectAt(0);
		final ASN1Sequence completeRevocationRefs = (ASN1Sequence) attrValue;
		for (int i = 0; i < completeRevocationRefs.size(); i++) {
			final CrlOcspRef otherCertId = CrlOcspRef.getInstance(completeRevocationRefs.getObjectAt(i));
			final OcspListID ocspids = otherCertId.getOcspids();
			if (ocspids != null) {
				for (final OcspResponsesID id : ocspids.getOcspResponses()) {
					list.add(new OCSPRef(id, true));
				}
			}
		}
		return list;
	}

	@Override
	public byte[] getSignatureTimestampData(final TimestampToken timestampToken) {
		return signerInformation.getSignature();
	}

	@Override
	public byte[] getTimestampX1Data(final TimestampToken timestampToken) {

		try {
			@SuppressWarnings("resource")
			final ByteArrayOutputStream data = new ByteArrayOutputStream();

			data.write(signerInformation.getSignature());

         /*
          * We don't include the outer SEQUENCE, only the attrType and attrValues as stated by the TS Â§6.3.5, NOTE 2
          */
			final AttributeTable attributes = signerInformation.getUnsignedAttributes();
			if (attributes != null) {

				final Attribute attribute = attributes.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
				if (attribute != null) {

					data.write(DSSASN1Utils.getDEREncoded(attribute.getAttrType()));
					data.write(DSSASN1Utils.getDEREncoded(attribute.getAttrValues()));
				}
			}

         /* Those are common to Type 1 and Type 2 */
			data.write(getTimestampX2Data(timestampToken));

			return data.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public byte[] getTimestampX2Data(final TimestampToken timestampToken) {

		try {
			@SuppressWarnings("resource")
			final ByteArrayOutputStream data = new ByteArrayOutputStream();

         /* Those are common to Type 1 and Type 2 */
			final AttributeTable attributes = signerInformation.getUnsignedAttributes();

			if (attributes != null) {

				final Attribute certAttribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_certificateRefs);
				if (certAttribute != null) {

					data.write(DSSASN1Utils.getDEREncoded(certAttribute.getAttrType()));
					data.write(DSSASN1Utils.getDEREncoded(certAttribute.getAttrValues()));
				}

				final Attribute revAttribute = attributes.get(PKCSObjectIdentifiers.id_aa_ets_revocationRefs);
				if (revAttribute != null) {

					data.write(DSSASN1Utils.getDEREncoded(revAttribute.getAttrType()));
					data.write(DSSASN1Utils.getDEREncoded(revAttribute.getAttrValues()));
				}
			}
			return data.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}

	@Override
	public byte[] getArchiveTimestampData(final TimestampToken timestampToken) throws DSSException {

		final ArchiveTimestampType archiveTimestampType = timestampToken.getArchiveTimestampType();
		final byte[] archiveTimestampData;
		switch (archiveTimestampType) {
			case CAdES_V2:
				archiveTimestampData = getArchiveTimestampDataV2(timestampToken);
				break;
			case CAdES_v3:
				archiveTimestampData = getArchiveTimestampDataV3(timestampToken);
				break;
			default:
				throw new DSSException("Unsupported ArchiveTimestampType " + archiveTimestampType);
		}
		return archiveTimestampData;
	}

	private byte[] getArchiveTimestampDataV3(TimestampToken timestampToken) throws DSSException {

		byte[] archiveTimestampData;
		final CadesLevelBaselineLTATimestampExtractor cadesLevelBaselineLTATimestampExtractor = new CadesLevelBaselineLTATimestampExtractor();
		final Attribute atsHashIndexAttribute = cadesLevelBaselineLTATimestampExtractor.getVerifiedAtsHashIndex(signerInformation, this, timestampToken);

		byte[] originalDocumentBytes = getOriginalDocumentBytes();
		archiveTimestampData = cadesLevelBaselineLTATimestampExtractor
			  .getArchiveTimestampDataV3(this, getSignerInformation(), atsHashIndexAttribute, originalDocumentBytes, timestampToken.getSignedDataDigestAlgo());
		return archiveTimestampData;
	}

	private byte[] getOriginalDocumentBytes() throws DSSException {

		try {
			final java.io.ByteArrayOutputStream originalSignedFileByteArrayOutputStream = new java.io.ByteArrayOutputStream();
			if (cmsSignedData.getSignedContent() != null) {
				cmsSignedData.getSignedContent().write(originalSignedFileByteArrayOutputStream);
			} else {
				originalSignedFileByteArrayOutputStream.write(externalContent.getBytes());
			}

			return originalSignedFileByteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new DSSException(e);
		} catch (CMSException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method handles the archive-timestamp-v2
	 * <p/>
	 * The value of the messageImprint field within TimeStampToken shall be a hash of the concatenation of:
	 * • the encapContentInfo element of the SignedData sequence;
	 * • any external content being protected by the signature, if the eContent element of the encapContentInfo is omitted;
	 * • the Certificates and crls elements of the SignedData sequence, when present; and
	 * • all data elements in the SignerInfo sequence including all signed and unsigned attributes.
	 * <p/>
	 * NOTE 1: An alternative archiveTimestamp attribute, identified by an object identifier { iso(1) member-body(2)
	 * us(840) rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) id-aa(2) 27, is defined in prior versions of
	 * TS 101 733. The archiveTimestamp attribute, defined in versions of TS 101 733 prior to 1.5.1, is not
	 * compatible with the attribute defined in the present document. The archiveTimestamp attribute, defined in
	 * versions 1.5.1 to 1.6.3 of TS 101 733, is compatible with the present document if the content is internal to
	 * encapContentInfo. Unless the version of TS 101 733 employed by the signing party is known by all
	 * recipients, use of the archiveTimestamp attribute defined in prior versions of TS 101 733 is deprecated.
	 * NOTE 2: Counter signatures held as countersignature attributes do not require independent archive time-stamps as
	 * they are protected by the archive time-stamp against the containing SignedData structure.
	 * NOTE 3: Unless DER is used throughout, it is recommended that the binary encoding of the ASN.1 structures
	 * being time-stamped be preserved when being archived to ensure that the recalculation of the data hash is
	 * consistent.
	 * NOTE 4: The hash is calculated over the concatenated data elements as received /stored including the Type and
	 * Length encoding.
	 * NOTE 5: Whilst it is recommended that unsigned attributes be DER encoded, it cannot generally be so guaranteed
	 * except by prior arrangement.
	 *
	 * @param timestampToken
	 * @return
	 * @throws DSSException
	 */
	private byte[] getArchiveTimestampDataV2(TimestampToken timestampToken) throws DSSException {

		try {

			final ByteArrayOutputStream data = new ByteArrayOutputStream();

			final ContentInfo contentInfo = cmsSignedData.toASN1Structure();
			final SignedData signedData = SignedData.getInstance(contentInfo.getContent());

			ContentInfo content = signedData.getEncapContentInfo();
			if (content == null || content.getContent() == null) {
			    /* Detached signatures have either no encapContentInfo in signedData, or it exists but has no eContent */
				if (getOriginalDocumentBytes() != null) {
					data.write(content.toASN1Primitive().getEncoded());
					data.write(getOriginalDocumentBytes());
				} else {
					throw new DSSException("Signature is detached and no original data provided.");
				}
			} else {

				ASN1OctetString octet = (ASN1OctetString) content.getContent();

				ContentInfo info2 = new ContentInfo(PKCSObjectIdentifiers.data, octet);
				final byte[] contentInfoBytes = info2.getEncoded();
				if (LOG.isTraceEnabled()) {
					LOG.trace("Content Info: {}", DSSUtils.toHex(contentInfoBytes));
				}
				data.write(contentInfoBytes);
			}
			final ASN1Set certificates = signedData.getCertificates();
			if (certificates != null) {

				final byte[] certificatesBytes = new DERTaggedObject(false, 0, new DERSequence(certificates.toArray())).getEncoded();
				if (LOG.isTraceEnabled()) {
					LOG.trace("Certificates: {}", DSSUtils.toHex(certificatesBytes));
				}
				data.write(certificatesBytes);
			}
			if (signedData.getCRLs() != null) {

				final byte[] crlBytes = signedData.getCRLs().getEncoded();
				if (LOG.isTraceEnabled()) {
					LOG.trace("CRLs: {}", DSSUtils.toHex(crlBytes));
				}
				data.write(crlBytes);
			}
			final SignerInfo signerInfo = signerInformation.toASN1Structure();
			final ByteArrayOutputStream signerByteArrayOutputStream = new ByteArrayOutputStream();
			final ASN1Set unauthenticatedAttributes = signerInfo.getUnauthenticatedAttributes();
			final ASN1Sequence filteredUnauthenticatedAttributes = filterUnauthenticatedAttributes(unauthenticatedAttributes, timestampToken);
			final ASN1Sequence asn1Object = getSignerInfoEncoded(signerInfo, filteredUnauthenticatedAttributes);
			for (int ii = 0; ii < asn1Object.size(); ii++) {

				final byte[] signerInfoBytes = DSSASN1Utils.getDEREncoded(asn1Object.getObjectAt(ii).toASN1Primitive());
				signerByteArrayOutputStream.write(signerInfoBytes);
			}
			final byte[] signerInfoBytes = signerByteArrayOutputStream.toByteArray();
			if (LOG.isTraceEnabled()) {
				LOG.trace("SignerInfoBytes: {}", DSSUtils.toHex(signerInfoBytes));
			}
			data.write(signerInfoBytes);

			final byte[] result = data.toByteArray();
			return result;

		} catch (IOException e) {
			throw new DSSException(e);
		} catch (Exception e) {
			// When error in computing or in format the algorithm just continues.
			LOG.warn("When error in computing or in format the algorithm just continue...", e);
			return DSSUtils.EMPTY_BYTE_ARRAY;
		}
	}

	/**
	 * Copied from org.bouncycastle.asn1.cms.SignerInfo#toASN1Object() and adapted to be able to use the custom unauthenticatedAttributes
	 *
	 * @param signerInfo
	 * @param unauthenticatedAttributes
	 * @return
	 */
	private ASN1Sequence getSignerInfoEncoded(SignerInfo signerInfo, ASN1Encodable unauthenticatedAttributes) {

		ASN1EncodableVector v = new ASN1EncodableVector();

		v.add(signerInfo.getVersion());
		v.add(signerInfo.getSID());
		v.add(signerInfo.getDigestAlgorithm());

		if (signerInfo.getAuthenticatedAttributes() != null) {
			v.add(new DERTaggedObject(false, 0, signerInfo.getAuthenticatedAttributes()));
		}

		v.add(signerInfo.getDigestEncryptionAlgorithm());
		v.add(signerInfo.getEncryptedDigest());

		if (unauthenticatedAttributes != null) {
			v.add(new DERTaggedObject(false, 1, unauthenticatedAttributes));
		}

		return new DERSequence(v);
	}

	/**
	 * Remove any archive-timestamp-v2/3 attribute added after the timestampToken
	 */
	private ASN1Sequence filterUnauthenticatedAttributes(ASN1Set unauthenticatedAttributes, TimestampToken timestampToken) {

		ASN1EncodableVector result = new ASN1EncodableVector();
		for (int ii = 0; ii < unauthenticatedAttributes.size(); ii++) {

			final Attribute attribute = Attribute.getInstance(unauthenticatedAttributes.getObjectAt(ii));
			final ASN1ObjectIdentifier attrType = attribute.getAttrType();
			if (OID.id_aa_ets_archiveTimestampV2.equals(attrType) || OID.id_aa_ets_archiveTimestampV3.equals(attrType)) {
				try {

					TimeStampToken token = new TimeStampToken(new CMSSignedData(DSSASN1Utils.getDEREncoded(attribute.getAttrValues().getObjectAt(0).toASN1Primitive())));
					if (!token.getTimeStampInfo().getGenTime().before(timestampToken.getGenerationTime())) {
						continue;
					}
				} catch (Exception e) {
					throw new DSSException(e);
				}
			}
			result.add(unauthenticatedAttributes.getObjectAt(ii));
		}
		return new DERSequence(result);
	}

	@Override
	public String getId() {

		if (signatureId == null) {

			final CertificateToken certificateToken = getSigningCertificateToken();
			final int dssId = certificateToken == null ? 0 : certificateToken.getDSSId();
			signatureId = DSSUtils.getDeterministicId(getSigningTime(), dssId);
		}
		return signatureId;
	}

	@Override
	public List<TimestampReference> getTimestampedReferences() {

		final List<TimestampReference> references = new ArrayList<TimestampReference>();
		final List<CertificateRef> certRefs = getCertificateRefs();
		for (final CertificateRef certificateRef : certRefs) {

			final String digestValue = DSSUtils.base64Encode(certificateRef.getDigestValue());
			TimestampReference reference = new TimestampReference();
			reference.setCategory(TimestampReferenceCategory.CERTIFICATE);
			DigestAlgorithm digestAlgorithmObj = DigestAlgorithm.forOID(certificateRef.getDigestAlgorithm());
			reference.setDigestAlgorithm(certificateRef.getDigestAlgorithm());
			if (!usedCertificatesDigestAlgorithms.contains(digestAlgorithmObj)) {

				usedCertificatesDigestAlgorithms.add(digestAlgorithmObj);
			}
			reference.setDigestValue(digestValue);
			references.add(reference);
		}

		final List<OCSPRef> ocspRefs = getOCSPRefs();
		for (final OCSPRef ocspRef : ocspRefs) {

			final String digestValue = DSSUtils.base64Encode(ocspRef.getDigestValue());
			TimestampReference reference = new TimestampReference();
			reference.setCategory(TimestampReferenceCategory.REVOCATION);
			reference.setDigestAlgorithm(ocspRef.getDigestAlgorithm().getName());
			reference.setDigestValue(digestValue);
			references.add(reference);
		}

		final List<CRLRef> crlRefs = getCRLRefs();
		for (final CRLRef crlRef : crlRefs) {

			final String digestValue = DSSUtils.base64Encode(crlRef.getDigestValue());
			TimestampReference reference = new TimestampReference();
			reference.setCategory(TimestampReferenceCategory.REVOCATION);
			reference.setDigestAlgorithm(crlRef.getDigestAlgorithm().getName());
			reference.setDigestValue(digestValue);
			references.add(reference);
		}
		return references;
	}

	@Override
	public Set<DigestAlgorithm> getUsedCertificatesDigestAlgorithms() {

		return usedCertificatesDigestAlgorithms;
	}

	/**
	 * @param signerInformation
	 * @return the existing unsigned attributes or an empty attributes hashtable
	 */
	public static AttributeTable getUnsignedAttributes(final SignerInformation signerInformation) {
		final AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
		if (unsignedAttributes == null) {
			return new AttributeTable(new Hashtable<ASN1ObjectIdentifier, Attribute>());
		} else {
			return unsignedAttributes;
		}
	}

	/**
	 * @param signerInformation
	 * @return the existing signed attributes or an empty attributes hashtable
	 */
	public static AttributeTable getSignedAttributes(final SignerInformation signerInformation) {
		final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
		if (signedAttributes == null) {
			return new AttributeTable(new Hashtable<ASN1ObjectIdentifier, Attribute>());
		} else {
			return signedAttributes;
		}
	}

	public boolean isDataForSignatureLevelPresent(SignatureLevel signatureLevel) {

		/**
		 * This list contains the detail information collected during the check. It is reset for each call.
		 */
		info = new ArrayList<String>();

		final AttributeTable unsignedAttributes = getUnsignedAttributes(signerInformation);
		final AttributeTable signedAttributes = getSignedAttributes(signerInformation);
		boolean dataForProfilePresent = true;
		switch (signatureLevel) {
			case CAdES_BASELINE_LTA:
				dataForProfilePresent = unsignedAttributes.get(OID.id_aa_ets_archiveTimestampV3) != null;
				// break omitted purposely
			case CADES_101733_A:
				if (signatureLevel != SignatureLevel.CAdES_BASELINE_LTA) {
					dataForProfilePresent &= unsignedAttributes.get(OID.id_aa_ets_archiveTimestampV2) != null;
				}
				// break omitted purposely
			case CAdES_BASELINE_LT:
				final Store certificateStore = cmsSignedData.getCertificates();
				final Store crlStore = cmsSignedData.getCRLs();
				final Store ocspStore = cmsSignedData.getOtherRevocationInfo(CMSObjectIdentifiers.id_ri_ocsp_response);
				final Store ocspBasicStore = cmsSignedData.getOtherRevocationInfo(OCSPObjectIdentifiers.id_pkix_ocsp_basic);
				final int certificateStoreSize = certificateStore.getMatches(null).size();
				final int crlStoreSize = crlStore.getMatches(null).size();
				info.add("CRL founds: " + crlStoreSize);
				final int ocspStoreSize = ocspStore.getMatches(null).size();
				info.add("OCSP founds: " + ocspStoreSize);
				final int basicOcspStoreSize = ocspBasicStore.getMatches(null).size();
				info.add("BasicOCSP founds: " + basicOcspStoreSize);
				final int ltInfoSize = crlStoreSize + ocspStoreSize + basicOcspStoreSize;
				dataForProfilePresent &= (ltInfoSize > 0);
				// break omitted purposely
			case CADES_101733_X:
				if (!signatureLevel.toString().contains("BASELINE")) {
					dataForProfilePresent &= (unsignedAttributes.get(PKCSObjectIdentifiers.id_aa_ets_certCRLTimestamp) != null || unsignedAttributes
						  .get(PKCSObjectIdentifiers.id_aa_ets_escTimeStamp) != null);
				}
				// break omitted purposely
			case CADES_101733_C:
				if (!signatureLevel.toString().contains("BASELINE")) {
					dataForProfilePresent &= unsignedAttributes.get(PKCSObjectIdentifiers.id_aa_ets_certificateRefs) != null;
					dataForProfilePresent &= isDataForSignatureLevelPresent(SignatureLevel.CAdES_BASELINE_T);
				}
				// break omitted purposely
			case CAdES_BASELINE_T:
				dataForProfilePresent &= unsignedAttributes.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken) != null;
				// break omitted purposely
			case CAdES_BASELINE_B:
				dataForProfilePresent &= ((signedAttributes.get(PKCSObjectIdentifiers.id_aa_signingCertificate) != null) || (signedAttributes
					  .get(PKCSObjectIdentifiers.id_aa_signingCertificateV2) != null));
				break; // break placed purposely
			default:
				throw new IllegalArgumentException("Unknown level " + signatureLevel);
		}
		return dataForProfilePresent;
	}

	public SignatureLevel[] getSignatureLevels() {
		return new SignatureLevel[]{SignatureLevel.CAdES_BASELINE_B, SignatureLevel.CAdES_BASELINE_T, SignatureLevel.CADES_101733_C, SignatureLevel.CADES_101733_X, SignatureLevel.CAdES_BASELINE_LT, SignatureLevel.CADES_101733_A, SignatureLevel.CAdES_BASELINE_LTA};
	}
}