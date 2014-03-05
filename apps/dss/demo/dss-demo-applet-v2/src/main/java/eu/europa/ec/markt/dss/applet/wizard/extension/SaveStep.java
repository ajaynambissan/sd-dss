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

package eu.europa.ec.markt.dss.applet.wizard.extension;

import java.io.File;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.applet.model.ExtendSignatureModel;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.ControllerException;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardView;
import eu.europa.ec.markt.dss.signature.SignaturePackaging;

/**
 * TODO
 *
 * <p>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public class SaveStep extends WizardStep<ExtendSignatureModel, ExtensionWizardController> {

    /**
     * The default constructor for SaveStep.
     *
     * @param model
     * @param view
     * @param controller
     */
    public SaveStep(ExtendSignatureModel model, WizardView<ExtendSignatureModel, ExtensionWizardController> view, ExtensionWizardController controller) {
        super(model, view, controller);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep#finish()
     */
    @Override
    protected void finish() throws ControllerException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep#getBackStep()
     */
    @Override
    protected Class<? extends WizardStep<ExtendSignatureModel, ExtensionWizardController>> getBackStep() {
        return SignatureStep.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep#getNextStep()
     */
    @Override
    protected Class<? extends WizardStep<ExtendSignatureModel, ExtensionWizardController>> getNextStep() {
        return FinishStep.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep#getStepProgression()
     */
    @Override
    protected int getStepProgression() {
        return 3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep#execute()
     */
    @Override
    protected void init() {

        final File selectedFile = getModel().getSelectedFile();
        // Initialize the target file base on the current selected file

        final SignaturePackaging signaturePackaging = getModel().getPackaging();
        final String signatureLevel = getModel().getLevel();
        final File targetFile = prepareTargetFileName(selectedFile, signaturePackaging, signatureLevel);

        getModel().setTargetFile(targetFile);

    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep#isValid()
     */
    @Override
    protected boolean isValid() {
        final File targetFile = getModel().getTargetFile();
        return targetFile != null;
    }

    private File prepareTargetFileName(final File file, final SignaturePackaging signaturePackaging, final String signatureLevel) {
        // FIXME move to util class
        final File parentDir = file.getParentFile();
        final String originalName = DSSUtils.substringBeforeLast(file.getName(), ".");
        final String originalExtension = "." + DSSUtils.substringAfterLast(file.getName(), ".");
        final String level = signatureLevel.toUpperCase();

        if ((SignaturePackaging.ENVELOPING == signaturePackaging || SignaturePackaging.DETACHED == signaturePackaging) && level.startsWith("XADES")) {

            // final String form = "xades";
            final String levelOnly = DSSUtils.replaceStrStr(level, "XADES-", "").toLowerCase();
            // final String packaging = signaturePackaging.name().toLowerCase();
            return new File(parentDir, originalName + "-extended-to-" + levelOnly + ".xml");
        }

        if (level.startsWith("CADES") && !originalExtension.toLowerCase().equals(".p7m")) {
            return new File(parentDir, originalName + originalExtension + ".p7m");
        }

        if (level.startsWith("ASIC")) {
            return new File(parentDir, originalName + originalExtension + ".asics");
        }

        return new File(parentDir, originalName + "-signed" + originalExtension);

    }
}
