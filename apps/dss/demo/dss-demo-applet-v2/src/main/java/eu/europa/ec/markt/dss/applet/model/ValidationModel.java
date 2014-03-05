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

package eu.europa.ec.markt.dss.applet.model;

import java.io.File;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.jgoodies.binding.beans.Model;
import eu.europa.ec.markt.dss.validation102853.xml.XmlDom;

/**
 * TODO
 * <p/>
 * <p/>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
@SuppressWarnings("serial")
public class ValidationModel extends Model {
    /**
     *
     */
    public static final String CHANGE_PROPERTY_SIGNED_FILE = "signedFile";
    private File signedFile;
    /**
     *
     */
    public static final String CHANGE_PROPERTY_ORIGINAL_FILE = "originalFile";
    private File originalFile;

    public static final String CHANGE_PROPERTY_VALIDATION_LEGACY_CHOSEN = "validationLegacyChosen";

    private boolean validationLegacyChosen = false;

    public static final String CHANGE_PROPERTY_DEFAULT_POLICY = "defaultPolicy";
    private boolean defaultPolicy = true;

    public static final String CHANGE_PROPERTY_SELECTED_POLICY_FILE = "selectedPolicyFile";
    private File selectedPolicyFile;

    public static final String CHANGE_PROPERTY_DIAGNOSTIC_DATA_ = "diagnosticData";
    private XmlDom diagnosticData;

    public static final String CHANGE_PROPERTY_DETAILED_REPORT = "detailedReport";
    private XmlDom detailedReport;

    public static final String CHANGE_PROPERTY_SIMPLE_REPORT_ = "simpleReport";
    private XmlDom simpleReport;

    /**
     * @return the originalFile
     */
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * @return the signedFile
     */
    public File getSignedFile() {
        return signedFile;
    }

    /**
     * @param originalFile the originalFile to set
     */
    public void setOriginalFile(final File originalFile) {
        final File oldValue = this.originalFile;
        final File newValue = originalFile;
        this.originalFile = newValue;
        firePropertyChange(CHANGE_PROPERTY_ORIGINAL_FILE, oldValue, newValue);
    }

    /**
     * @param signedFile the signedFile to set
     */
    public void setSignedFile(final File signedFile) {
        final File oldValue = this.signedFile;
        final File newValue = signedFile;
        this.signedFile = newValue;
        firePropertyChange(CHANGE_PROPERTY_SIGNED_FILE, oldValue, newValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }

    public boolean isValidationLegacyChosen() {
        return validationLegacyChosen;
    }

    /**
     * @param validationLegacyChosen the validationLegacyChosen to set
     */
    public void setValidationLegacyChosen(final boolean validationLegacyChosen) {
        final boolean oldValue = this.validationLegacyChosen;
        final boolean newValue = validationLegacyChosen;
        this.validationLegacyChosen = newValue;
        firePropertyChange(CHANGE_PROPERTY_VALIDATION_LEGACY_CHOSEN, oldValue, newValue);
    }

    public boolean isDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDefaultPolicy(boolean defaultPolicy) {
        final boolean oldValue = this.defaultPolicy;
        final boolean newValue = defaultPolicy;
        this.defaultPolicy = newValue;
        firePropertyChange(CHANGE_PROPERTY_DEFAULT_POLICY, oldValue, newValue);
    }

    public File getSelectedPolicyFile() {
        return selectedPolicyFile;
    }

    public void setSelectedPolicyFile(File selectedPolicyFile) {
        final File oldValue = this.selectedPolicyFile;
        final File newValue = selectedPolicyFile;
        this.selectedPolicyFile = newValue;
        firePropertyChange(CHANGE_PROPERTY_SELECTED_POLICY_FILE, oldValue, newValue);
    }

    public XmlDom getDetailedReport() {
        return detailedReport;
    }

    public void setDetailedReport(XmlDom detailedReport) {
        final XmlDom oldValue = this.detailedReport;
        final XmlDom newValue = detailedReport;
        this.detailedReport = detailedReport;
        firePropertyChange(CHANGE_PROPERTY_DETAILED_REPORT, oldValue, newValue);
    }

    public void setDiagnosticData(XmlDom diagnosticData) {
        final XmlDom oldValue = this.diagnosticData;
        final XmlDom newValue = diagnosticData;
        this.diagnosticData = diagnosticData;
        firePropertyChange(CHANGE_PROPERTY_DIAGNOSTIC_DATA_, oldValue, newValue);
    }

    public XmlDom getDiagnosticData() {
        return diagnosticData;
    }

    public XmlDom getSimpleReport() {
        return simpleReport;
    }

    public void setSimpleReport(XmlDom simpleReport) {
        final XmlDom oldValue = this.simpleReport;
        final XmlDom newValue = simpleReport;
        this.simpleReport = simpleReport;
        firePropertyChange(CHANGE_PROPERTY_SIMPLE_REPORT_, oldValue, newValue);
    }
}


