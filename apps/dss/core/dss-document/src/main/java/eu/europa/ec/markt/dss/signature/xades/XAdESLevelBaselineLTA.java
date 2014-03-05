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

package eu.europa.ec.markt.dss.signature.xades;

import java.util.List;

import org.w3c.dom.Element;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DSSXMLUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.validation102853.CertificateVerifier;
import eu.europa.ec.markt.dss.validation102853.SignatureValidationContext;
import eu.europa.ec.markt.dss.validation102853.TimestampToken;
import eu.europa.ec.markt.dss.validation102853.TimestampType;
import eu.europa.ec.markt.dss.validation102853.xades.XAdESSignature;

/**
 * Holds level A aspects of XAdES
 *
 * @version $Revision: 3406 $ - $Date: 2014-02-04 09:23:21 +0100 (Tue, 04 Feb 2014) $
 */

public class XAdESLevelBaselineLTA extends XAdESLevelBaselineLT {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XAdESLevelBaselineLTA.class);

    /**
     * The default constructor for XAdESLevelBaselineLTA.
     */
    public XAdESLevelBaselineLTA(final CertificateVerifier certVerifier) {

        super(certVerifier);
    }

    /**
     * Adds the ArchiveTimeStamp element which is an unsigned property qualifying the signature. The hash sent to the TSA
     * (messageImprint) is computed on the XAdES-LT form of the electronic signature and the signed data objects.<br>
     * <p/>
     * A XAdES-LTA form MAY contain several ArchiveTimeStamp elements.
     */
    @Override
    protected void extendSignatureTag() throws DSSException {

        // check if -LT is present
        super.extendSignatureTag();
        if (xadesSignature.hasLTAProfile()) {

            checkSignatureIntegrity();

            final SignatureValidationContext valContext = xadesSignature.getSignatureValidationContext(certificateVerifier);

            removeLastTimestampValidationData();
            incorporateTimestampValidationData(valContext);
        }

        incorporateArchiveTimestamp();
    }

    /**
     * This method removes the timestamp validation data of the las archive timestamp.
     */
    private void removeLastTimestampValidationData() {

        final Element toRemove = xadesSignature.getLastTimestampValidationData();
        if (toRemove != null) {

            unsignedSignaturePropertiesDom.removeChild(toRemove);
        }
    }

    /**
     * This method incorporates the timestamp validation data in the signature
     *
     * @param validationContext
     */
    private void incorporateTimestampValidationData(final SignatureValidationContext validationContext) {

        final Element timeStampValidationDataDom = DSSXMLUtils.addElement(documentDom, unsignedSignaturePropertiesDom, xPathQueryHolder.XADES141_NAMESPACE, "xades141:TimeStampValidationData");

        incorporateCertificateValues(timeStampValidationDataDom, validationContext);

        incorporateRevocationValues(timeStampValidationDataDom, validationContext);
        String id = "1";
        final List<TimestampToken> archiveTimestamps = xadesSignature.getArchiveTimestamps();
        if (archiveTimestamps.size() > 0) {

            final TimestampToken timestampToken = archiveTimestamps.get(archiveTimestamps.size() - 1);
            id = "" + timestampToken.getDSSId();
        }

        timeStampValidationDataDom.setAttribute("Id", "id-" + id);
    }

    /**
     * This method incorporate timestamp type object.
     */
    private void incorporateArchiveTimestamp() {

        final byte[] archiveTimestampData = xadesSignature.getArchiveTimestampData(null);
        final DigestAlgorithm timestampDigestAlgorithm = params.getTimestampDigestAlgorithm();
        final byte[] digestBytes = DSSUtils.digest(timestampDigestAlgorithm, archiveTimestampData);
        createXAdESTimeStampType(TimestampType.ARCHIVE_TIMESTAMP, XAdESSignature.DEFAULT_TIMESTAMP_CREATION_CANONICALIZATION_METHOD, digestBytes);
    }
}
