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

package eu.europa.ec.markt.dss.validation102853;

import java.io.IOException;
import java.io.StringWriter;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.DSSRevocationUtils;
import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.exception.DSSNullException;
import eu.europa.ec.markt.dss.validation.certificate.CertificateSourceType;

/**
 * OCSP Signed Token which encapsulate BasicOCSPResp (BC).
 *
 * @version $Revision: 1824 $ - $Date: 2013-03-28 15:57:23 +0100 (Thu, 28 Mar 2013) $
 */

public class OCSPToken extends RevocationToken {

    private static final Logger LOG = LoggerFactory.getLogger(OCSPToken.class);

    /**
     * The encapsulated basic OCSP response.
     */
    private final BasicOCSPResp basicOCSPResp;

    /**
     * In case of online source this is the source URI.
     */
    private String sourceURI;

    /**
     * The default constructor for OCSPToken.
     *
     * @param basicOCSPResp      The basic OCSP response.
     * @param certificatePool The certificate pool used to validate/hold the certificate used to sign this OCSP response.
     */
    public OCSPToken(final BasicOCSPResp basicOCSPResp, final CertificatePool certificatePool) {

        if (basicOCSPResp == null) {

            throw new DSSNullException(BasicOCSPResp.class);
        }
        if (certificatePool == null) {

            throw new DSSNullException(CertificatePool.class);
        }
        this.basicOCSPResp = basicOCSPResp;
        this.extraInfo = new TokenValidationExtraInfo();
        try {

            for (final X509CertificateHolder x509CertificateHolder : basicOCSPResp.getCerts()) {

                final byte[] encoded = x509CertificateHolder.getEncoded();
                final X509Certificate x509Certificate = DSSUtils.loadCertificate(encoded);
                final CertificateToken certToken = certificatePool.getInstance(x509Certificate, CertificateSourceType.OCSP_RESPONSE);
                if (isSignedBy(certToken)) {

                    break;
                }
            }
        } catch (IOException e) {
            throw new DSSException(e);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("OCSP token, produced at '" + DSSUtils.formatInternal(basicOCSPResp.getProducedAt()) + "' added.");
        }
    }

    /**
     * @return the ocspResp
     */
    public BasicOCSPResp getBasicOCSPResp() {

        return basicOCSPResp;
    }

    @Override
    public boolean isSignedBy(final CertificateToken issuerToken) {

        if (this.issuerToken != null) {

            return this.issuerToken.equals(issuerToken);
        }
        try {

            signatureInvalidityReason = "";
            JcaContentVerifierProviderBuilder jcaContentVerifierProviderBuilder = new JcaContentVerifierProviderBuilder();
            jcaContentVerifierProviderBuilder.setProvider("BC");
            final PublicKey publicKey = issuerToken.getCertificate().getPublicKey();
            ContentVerifierProvider contentVerifierProvider = jcaContentVerifierProviderBuilder.build(publicKey);
            signatureValid = basicOCSPResp.isSignatureValid(contentVerifierProvider);
            if (signatureValid) {

                this.issuerToken = issuerToken;
            }
            algoUsedToSignToken = issuerToken.getSignatureAlgo();
            issuerX500Principal = issuerToken.getCertificate().getSubjectX500Principal();
        } catch (OCSPException e) {

            signatureInvalidityReason = e.getClass().getSimpleName() + " - " + e.getMessage();
            signatureValid = false;
        } catch (OperatorCreationException e) {
            signatureInvalidityReason = e.getClass().getSimpleName() + " - " + e.getMessage();
            signatureValid = false;
        }
        return signatureValid;
    }

    public String getSourceURL() {

        return sourceURI;
    }

    public void setSourceURI(final String sourceURI) {

        this.sourceURI = sourceURI;
    }

    @Override
    public int hashCode() {
        return basicOCSPResp != null ? basicOCSPResp.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OCSPToken ocspToken = (OCSPToken) o;

        if (basicOCSPResp != null ? !basicOCSPResp.equals(ocspToken.basicOCSPResp) : ocspToken.basicOCSPResp != null) {
            return false;
        }
        return true;
    }

    /**
     * This method returns the DSS abbreviation of the certificate. It is used for debugging purpose.
     *
     * @return
     */
    public String getAbbreviation() {

        return "OCSPToken[" + DSSUtils.formatInternal(basicOCSPResp.getProducedAt()) + ", signedBy=" + (issuerToken == null ? "?" : issuerToken.getDSSIdAsString()) +
              "]";
    }

    @Override
    public String toString(String indentStr) {

        final StringWriter out = new StringWriter();
        out.append(indentStr).append("OCSPToken[");
        out.append(DSSUtils.formatInternal(basicOCSPResp.getProducedAt()));
        out.append(", signedBy=").append(issuerToken != null ? issuerToken.getDSSIdAsString() : null).append('\n');
        indentStr += "\t";
        out.append(indentStr).append("Signature algorithm: ").append(algoUsedToSignToken == null ? "?" : algoUsedToSignToken.getJCEId()).append('\n');
        out.append(issuerToken != null ? issuerToken.toString(indentStr) : null).append('\n');
        final List<String> validationExtraInfo = extraInfo.getValidationInfo();
        if (validationExtraInfo.size() > 0) {

            for (final String info : validationExtraInfo) {

                out.append('\n').append(indentStr).append("\t- ").append(info);
            }
            out.append('\n');
        }
        indentStr = indentStr.substring(1);
        out.append(indentStr).append("]");
        return out.toString();
    }

    @Override
    public byte[] getEncoded() {

        final OCSPResp ocspResp = DSSRevocationUtils.fromBasicToResp(basicOCSPResp);
        try {

            final byte[] bytes = ocspResp.getEncoded();
            return bytes;
        } catch (IOException e) {
            throw new DSSException("CRL encoding error: " + e.getMessage(), e);
        }
    }
}
