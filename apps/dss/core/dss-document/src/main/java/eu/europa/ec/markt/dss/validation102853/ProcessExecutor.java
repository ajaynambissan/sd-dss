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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.validation102853.engine.rules.ProcessParameters;
import eu.europa.ec.markt.dss.validation102853.engine.rules.processes.LongTermValidation;
import eu.europa.ec.markt.dss.validation102853.engine.rules.wrapper.ValidationPolicy;
import eu.europa.ec.markt.dss.validation102853.report.DetailedReport;
import eu.europa.ec.markt.dss.validation102853.report.DiagnosticData;
import eu.europa.ec.markt.dss.validation102853.report.SimpleReport;
import eu.europa.ec.markt.dss.validation102853.report.SimpleReportBuilder;
import eu.europa.ec.markt.dss.validation102853.rules.NodeName;
import eu.europa.ec.markt.dss.validation102853.xml.XmlDom;
import eu.europa.ec.markt.dss.validation102853.xml.XmlNode;

/**
 * @author bielecro
 */
public class ProcessExecutor {

    protected static final Logger LOG = LoggerFactory.getLogger(SignedDocumentValidator.class);

    /**
     * DOM representation of the diagnostic data.
     */
    protected Document diagnosticDataDom;

    protected DiagnosticData diagnosticData;

    /**
     * Validation policy constraint data DOM representation
     */
    protected Document validationPolicyDom;

    /**
     * Wrapper for the validation policy constraints
     */
    protected ValidationPolicy validationPolicy;

    protected ProcessParameters processParams;

    /**
     * The simple validation report, contains only the most important information like validation date, signer from DN,
     * indication, sub-indication...
     */
    protected SimpleReport simpleReport;

    /**
     * The detailed report contains all information collected during the validation process.
     */
    protected DetailedReport detailedReport;

    /**
     * See {@link eu.europa.ec.markt.dss.validation102853.engine.rules.ProcessParameters#getCurrentTime()} TODO The management of the currentTime must be updated between
     * different processes!
     */
    protected Date currentTime = new Date();

    /**
     * This is the default constructor. The process parameters must be initialised wih setters: {@code setDiagnosticDataDom} and {@code setValidationPolicyDom}
     */
    public ProcessExecutor() {

    }

    public void setDiagnosticDataDom(Document diagnosticDataDom) {
        this.diagnosticDataDom = diagnosticDataDom;
    }

    public void setValidationPolicyDom(Document validationPolicyDom) {
        this.validationPolicyDom = validationPolicyDom;
    }

    /**
     * This method executes the long term validation processes. The underlying processes are automatically executed.
     */
    public DetailedReport execute() {

        processParams = new ProcessParameters();
        diagnosticData = new DiagnosticData(diagnosticDataDom);
        processParams.setDiagnosticData(diagnosticData);
        validationPolicy = new ValidationPolicy(validationPolicyDom);
        processParams.setValidationPolicy(validationPolicy);
        processParams.setCurrentTime(currentTime);
        final XmlDom usedCertificates = diagnosticData.getElement("/DiagnosticData/UsedCertificates");
        processParams.setCertPool(usedCertificates);

        final XmlNode mainNode = new XmlNode(NodeName.VALIDATION_DATA);
        mainNode.setNameSpace(XmlDom.NAMESPACE);

        final LongTermValidation ltv = new LongTermValidation();
        ltv.run(mainNode, processParams);

        final Document validationReportDocument = mainNode.toDocument();
        detailedReport = new DetailedReport(validationReportDocument);

        final SimpleReportBuilder simpleReportBuilder = new SimpleReportBuilder(validationPolicy, diagnosticData);
        simpleReport = simpleReportBuilder.build(processParams);

        return detailedReport;
    }

    /**
     * Returns the time of the validation.
     *
     * @return
     */
    public Date getCurrentTime() {
        return currentTime;
    }

    /**
     * Returns the diagnostic report. This is another representation od {code DiagnosticData}.
     *
     * @return
     */
    public DiagnosticData getDiagnosticData() {

        return diagnosticData;
    }

    /**
     * Returns the simple report. This is the simplest representation of the validation result.
     *
     * @return
     */
    public SimpleReport getSimpleReport() {

        return simpleReport;
    }

    /**
     * Returns the detailed validation report.
     *
     * @return
     */
    public DetailedReport getDetailedReport() {

        return detailedReport;
    }

    /**
     * Returns an object containing all reports.
     *
     * @return
     */
    public Reports getReports() {

        final Reports reports = new Reports();
        reports.diagnosticData = diagnosticData;
        reports.detailedReport = detailedReport;
        reports.simpleReport = simpleReport;
        return reports;
    }

    public static class Reports {

        public DiagnosticData diagnosticData;
        public DetailedReport detailedReport;
        public SimpleReport simpleReport;

        public void print() {

            System.out.println("----------------Diagnostic data-----------------");
            System.out.println(diagnosticData);

            System.out.println("----------------Validation report---------------");
            System.out.println(detailedReport);

            System.out.println("----------------Simple report-------------------");
            System.out.println(simpleReport);

            System.out.println("------------------------------------------------");
        }
    }

}
