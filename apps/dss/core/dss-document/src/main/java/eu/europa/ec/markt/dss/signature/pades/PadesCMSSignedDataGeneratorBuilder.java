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

package eu.europa.ec.markt.dss.signature.pades;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cms.CMSAttributeTableGenerationException;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;

import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.parameter.SignatureParameters;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.cades.CAdESLevelBaselineB;
import eu.europa.ec.markt.dss.signature.cades.CMSSignedDataGeneratorBuilder;
import eu.europa.ec.markt.dss.validation102853.CertificateVerifier;

/**
 * TODO
 * <p/>
 * <p/>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
class PadesCMSSignedDataGeneratorBuilder extends CMSSignedDataGeneratorBuilder {

    @Override
    protected CMSSignedDataGenerator createCMSSignedDataGenerator(CertificateVerifier certificateVerifier,
                                                                  X509Certificate signerCertificate, List<X509Certificate> certificateChain,
                                                                  ContentSigner contentSigner,
                                                                  SignerInfoGeneratorBuilder signerInfoGeneratorBuilder,
                                                                  CMSSignedData originalSignedData) throws DSSException {
        return super.createCMSSignedDataGenerator(certificateVerifier, signerCertificate, certificateChain, contentSigner,
              signerInfoGeneratorBuilder, originalSignedData);
    }

    /**
     * @param document                 the document for which the attribute needs to be generated
     * @param parameters               the parameters of the signature containing values for the attributes
     * @param digestCalculatorProvider
     * @param messageDigest
     * @return a SignerInfoGeneratorBuilder that generate the signed and unsigned attributes according to the CAdESLevelBaselineB and
     *         PAdESLevelBaselineB
     */
    protected SignerInfoGeneratorBuilder getSignerInfoGeneratorBuilder(final DSSDocument document, final SignatureParameters parameters,
                                                                       final DigestCalculatorProvider digestCalculatorProvider,
                                                                       final byte[] messageDigest) {

        final CAdESLevelBaselineB cAdESLevelBaselineB = new CAdESLevelBaselineB();
        final PAdESLevelBaselineB pAdESProfileEPES = new PAdESLevelBaselineB();

        SignerInfoGeneratorBuilder signerInfoGeneratorBuilder = new SignerInfoGeneratorBuilder(digestCalculatorProvider);
        signerInfoGeneratorBuilder = signerInfoGeneratorBuilder.setSignedAttributeGenerator(new CMSAttributeTableGenerator() {

            @SuppressWarnings("unchecked")
            @Override
            public AttributeTable getAttributes(@SuppressWarnings("rawtypes") Map params) throws CMSAttributeTableGenerationException {

                return pAdESProfileEPES.getSignedAttributes(params, cAdESLevelBaselineB, document, parameters, messageDigest);
            }
        });

        signerInfoGeneratorBuilder.setUnsignedAttributeGenerator(new CMSAttributeTableGenerator() {
            @Override
            public AttributeTable getAttributes(Map params) throws CMSAttributeTableGenerationException {
                return pAdESProfileEPES.getUnsignedAttributes(params, cAdESLevelBaselineB, document, parameters, messageDigest);
            }
        });

        return signerInfoGeneratorBuilder;
    }

}
