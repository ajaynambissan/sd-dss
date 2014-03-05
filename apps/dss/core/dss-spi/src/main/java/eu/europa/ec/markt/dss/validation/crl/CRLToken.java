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
package eu.europa.ec.markt.dss.validation.crl;

import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.TBSCertList;
import org.bouncycastle.cert.X509CRLHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.DSSRevocationUtils;
import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.SignatureAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.exception.DSSNotApplicableMethodException;
import eu.europa.ec.markt.dss.exception.DSSNullException;
import eu.europa.ec.markt.dss.validation102853.CertificateToken;
import eu.europa.ec.markt.dss.validation102853.RevocationToken;
import eu.europa.ec.markt.dss.validation102853.TokenValidationExtraInfo;

public class CRLToken extends RevocationToken {

    private static final Logger LOG = LoggerFactory.getLogger(CRLToken.class);

    private final CRLValidity crlValidity;

    private String sourceURL;

    /**
     * The constructor to be used when the issuing certificate is unknown where creating the revocation token. It can happened when extracting data from signature.
     *
     * @param crlValidity
     */
    public CRLToken(final CertificateToken certificateToken, final CRLValidity crlValidity) {

        ensureNotNull(crlValidity);
        this.crlValidity = crlValidity;
        setDefaultValues();
        setRevocationStatus(certificateToken);
    }

    private void ensureNotNull(final CRLValidity crlValidity) {

        if (crlValidity == null) {

            throw new DSSNullException(CRLValidity.class);
        }
        if (crlValidity.x509CRL == null) {

            throw new DSSNullException(X509CRL.class);
        }
    }

    private void setDefaultValues() {

        final X509CRL x509crl = crlValidity.x509CRL;
        final String sigAlgOID = x509crl.getSigAlgOID();
        final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forOID(sigAlgOID);
        this.algoUsedToSignToken = signatureAlgorithm;
        this.issuingTime = x509crl.getThisUpdate();
        this.nextUpdate = x509crl.getNextUpdate();
        issuerX500Principal = x509crl.getIssuerX500Principal();
        this.extraInfo = new TokenValidationExtraInfo();

        issuerToken = crlValidity.issuerToken;
        signatureValid = crlValidity.signatureIntact;
        signatureInvalidityReason = crlValidity.signatureInvalidityReason;
    }

    private void setRevocationStatus(final CertificateToken certificateToken) {

        final CertificateToken issuerToken = certificateToken.getIssuerToken();
        if (!issuerToken.equals(crlValidity.issuerToken)) {

            throw new DSSException("The CRLToken is not signed by the same issuer as the CertificateToken to be verified!");
        }

        final BigInteger serialNumber = certificateToken.getSerialNumber();
        X509CRL x509crl = crlValidity.x509CRL;
        final X509CRLEntry crlEntry = x509crl.getRevokedCertificate(serialNumber);
        status = null == crlEntry;
        if (!status) {

            revocationDate = crlEntry.getRevocationDate();

            final String revocationReason = DSSRevocationUtils.getRevocationReason(crlEntry);
            reason = revocationReason;
        }
    }

    /**
     * @return the x509crl
     */
    public X509CRL getX509crl() {

        return crlValidity.x509CRL;
    }

    /**
     * @return the a copy of x509crl as a X509CRLHolder
     */
    public X509CRLHolder getX509CrlHolder() {
        try {
            final X509CRL x509crl = getX509crl();
            final TBSCertList tbsCertList = TBSCertList.getInstance(x509crl.getTBSCertList());
            final AlgorithmIdentifier sigAlgOID = new AlgorithmIdentifier(new ASN1ObjectIdentifier(x509crl.getSigAlgOID()));
            final byte[] signature = x509crl.getSignature();
            final X509CRLHolder x509crlHolder = new X509CRLHolder(new CertificateList(new DERSequence(new ASN1Encodable[]{tbsCertList, sigAlgOID, new DERBitString(signature)})));
            return x509crlHolder;
        } catch (CRLException e) {
            throw new DSSException(e);
        }
    }

    public String getSourceURL() {

        return sourceURL;
    }

    /**
     * This sets the revocation data source URL. It is only used in case of {@code OnlineCRLSource}.
     *
     * @param sourceURL
     */
    public void setSourceURL(final String sourceURL) {

        this.sourceURL = sourceURL;
    }

    /**
     *
     */
    private void addInfoNotValidSignature() {

        extraInfo.add("The CRL signature is not valid!");
    }

    /**
     *
     */
    private void addInfoNoKeyUsageExtension() {

        extraInfo.add("No KeyUsage extension for CRL issuing certificate!");
    }

    @Override
    protected boolean isSignedBy(final CertificateToken issuerToken) {

        throw new DSSNotApplicableMethodException(this.getClass());
    }

    /**
     * This method returns the DSS abbreviation of the CRLToken. It is used for debugging purpose.
     *
     * @return
     */
    public String getAbbreviation() {

        return "CRLToken[" + (issuingTime == null ? "?" : DSSUtils.formatInternal(issuingTime)) + ", signedBy=" + (issuerToken == null ? "?" : issuerToken
              .getDSSIdAsString()) + "]";
    }

    @Override
    public byte[] getEncoded() {

        try {

            return crlValidity.x509CRL.getEncoded();
        } catch (CRLException e) {
            throw new DSSException("CRL encoding error: " + e.getMessage(), e);
        }
    }

    /**
     * Indicates if the token signature is intact and the signing certificate has cRLSign key usage bit set.
     *
     * @return
     */
    public boolean isValid() {

        return crlValidity.isValid();
    }

    /**
     * Gets the thisUpdate date from the CRL.
     *
     * @return the thisUpdate date from the CRL.
     */
    public Date getThisUpdate() {

        return crlValidity.x509CRL.getThisUpdate();
    }

    @Override
    public String toString(String indentStr) {

        try {

            StringBuffer out = new StringBuffer();
            out.append(indentStr).append("CRLToken[\n");
            indentStr += "\t";
            out.append(indentStr).append("Version: ").append(crlValidity.x509CRL.getVersion()).append('\n');
            out.append(indentStr).append("Issuing time: ").append(issuingTime == null ? "?" : DSSUtils.formatInternal(issuingTime)).append('\n');
            out.append(indentStr).append("Signature algorithm: ").append(algoUsedToSignToken == null ? "?" : algoUsedToSignToken).append('\n');
            out.append(indentStr).append("Status: ").append(getStatus()).append('\n');
            if (issuerToken != null) {
                out.append(indentStr).append("Issuer's certificate: ").append(issuerToken.getDSSIdAsString()).append('\n');
            }
            List<String> validationExtraInfo = extraInfo.getValidationInfo();
            if (validationExtraInfo.size() > 0) {

                for (String info : validationExtraInfo) {

                    out.append('\n').append(indentStr).append("\t- ").append(info);
                }
                out.append('\n');
            }
            indentStr = indentStr.substring(1);
            out.append(indentStr).append("]");
            return out.toString();
        } catch (Exception e) {

            return ((Object) this).toString();
        }
    }
}
