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

package eu.europa.ec.markt.dss.ws.impl;

import java.io.InputStream;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.exception.DSSNullException;
import eu.europa.ec.markt.dss.validation102853.CertificateVerifier;
import eu.europa.ec.markt.dss.validation102853.SignedDocumentValidator;
import eu.europa.ec.markt.dss.validation102853.report.DetailedReport;
import eu.europa.ec.markt.dss.validation102853.report.DiagnosticData;
import eu.europa.ec.markt.dss.validation102853.report.SimpleReport;
import eu.europa.ec.markt.dss.ws.ValidationService;
import eu.europa.ec.markt.dss.ws.WSDocument;
import eu.europa.ec.markt.dss.ws.report.WSValidationReport;

/**
 * Implementation of the Interface for the Contract of the Validation Web Service.
 *
 * @version $Revision: 3376 $ - $Date: 2014-01-22 22:20:23 +0100 (Wed, 22 Jan 2014) $
 */

@WebService(endpointInterface = "eu.europa.ec.markt.dss.ws.ValidationService", serviceName = "ValidationService")
public class ValidationServiceImpl implements ValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureServiceImpl.class);

    private CertificateVerifier certificateVerifier;

    /**
     * @param certificateVerifier the certificateVerifier to set
     */
    public void setCertificateVerifier(CertificateVerifier certificateVerifier) {
        this.certificateVerifier = certificateVerifier;
    }

    @Override
    public WSValidationReport validateDocument(WSDocument wsDocument, WSDocument externalContent, WSDocument policy, boolean diagnosticDataToBeReturned) throws DSSException {

        String exceptionMessage;
        try {
            if (LOG.isInfoEnabled()) {

                LOG.info("WsValidateDocument: begin");
            }
            if (wsDocument == null) {

                throw new DSSNullException(WSDocument.class);
            }
            final SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(wsDocument);
            validator.setCertificateVerifier(certificateVerifier);
            if (externalContent != null) {

                validator.setExternalContent(externalContent);
            }

            final InputStream inputStream = policy == null ? null : policy.openStream();
            validator.validateDocument(inputStream);

            final SimpleReport simpleReport = validator.getSimpleReport();
            final String simpleReportXml = simpleReport.toString();

            final DetailedReport detailedReport = validator.getDetailedReport();
            final String detailedReportXml = detailedReport.toString();

            final WSValidationReport wsValidationReport = new WSValidationReport();
            wsValidationReport.setXmlSimpleReport(simpleReportXml);
            wsValidationReport.setXmlDetailedReport(detailedReportXml);
            if (diagnosticDataToBeReturned) {

                final DiagnosticData diagnosticData = validator.getDiagnosticData();
                final String diagnosticDataXml = diagnosticData.toString();
                wsValidationReport.setXmlDiagnosticData(diagnosticDataXml);
            }
            if (LOG.isInfoEnabled()) {

                LOG.info("WsValidateDocument: end");
            }
            return wsValidationReport;
        } catch (Throwable e) {
            e.printStackTrace();
            exceptionMessage = e.getMessage();
        }
        LOG.info("WsValidateDocument: end with exception");
        throw new DSSException(exceptionMessage);
    }
}