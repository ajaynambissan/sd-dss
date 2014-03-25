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

package eu.europa.ec.markt.dss.applet.view.signature;

import java.awt.*;

import javax.swing.*;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.applet.model.SignatureModel;
import eu.europa.ec.markt.dss.applet.util.ComponentFactory;
import eu.europa.ec.markt.dss.applet.util.ResourceUtils;
import eu.europa.ec.markt.dss.applet.wizard.signature.SignatureWizardController;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.AppletCore;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardView;

public class SignatureDigestAlgorithmView extends WizardView<SignatureModel, SignatureWizardController> {

    private static final DigestAlgorithm[] signatureDigestAlgorithms = {DigestAlgorithm.SHA1, DigestAlgorithm.SHA256, DigestAlgorithm.SHA512};

    private final PresentationModel<SignatureModel> presentationModel;

    private final ValueModel signatureDigestAlgorithmValue;

    private final JComboBox signatureAlgorithmComboBox;

    /**
     * 
     * The default constructor for SignatureDigestAlgorithmView.
     * 
     * @param core
     * @param controller
     * @param model
     */
    public SignatureDigestAlgorithmView(AppletCore core, SignatureWizardController controller, SignatureModel model) {
        super(core, controller, model);

        this.presentationModel = new PresentationModel<SignatureModel>(getModel());
        signatureDigestAlgorithmValue = presentationModel.getModel(SignatureModel.PROPERTY_SIGNATURE_DIGEST_ALGORITHM);
        final SelectionInList<DigestAlgorithm> algorithms = new SelectionInList<DigestAlgorithm>(signatureDigestAlgorithms, signatureDigestAlgorithmValue);
        signatureAlgorithmComboBox = ComponentFactory.createComboBox(algorithms);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.applet.view.DSSAppletView#doLayout()
     */
    @Override
    protected Container doLayout() {

        final String[] colSpecs = new String[] { "5dlu", "pref", "5dlu" };
        final String[] rowSpecs = new String[] { "5dlu", "pref", "5dlu", "pref", "5dlu" };

        final PanelBuilder builder = ComponentFactory.createBuilder(colSpecs, rowSpecs);
        final CellConstraints cc = new CellConstraints();

        builder.addSeparator(ResourceUtils.getI18n("SIGNATURE_DIGEST_ALGORITHM"), cc.xyw(2, 2, 1));
        builder.add(signatureAlgorithmComboBox, cc.xyw(2, 4, 1));
        final DigestAlgorithm signatureAlgorithm = getModel().getSignatureDigestAlgorithm();
        if (signatureAlgorithm != null) {
            signatureAlgorithmComboBox.setSelectedItem(signatureAlgorithm);
        } else {
            signatureAlgorithmComboBox.setSelectedIndex(1); // SHA256
        }

        return ComponentFactory.createPanel(builder);
    }

}
