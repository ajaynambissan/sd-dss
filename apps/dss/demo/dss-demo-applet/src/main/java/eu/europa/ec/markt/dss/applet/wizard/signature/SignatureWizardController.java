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

package eu.europa.ec.markt.dss.applet.wizard.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.applet.controller.ActivityController;
import eu.europa.ec.markt.dss.applet.controller.DSSWizardController;
import eu.europa.ec.markt.dss.applet.main.DSSAppletCore;
import eu.europa.ec.markt.dss.applet.model.SignatureModel;
import eu.europa.ec.markt.dss.applet.util.SigningUtils;
import eu.europa.ec.markt.dss.applet.view.signature.CertificateView;
import eu.europa.ec.markt.dss.applet.view.signature.FileView;
import eu.europa.ec.markt.dss.applet.view.signature.FinishView;
import eu.europa.ec.markt.dss.applet.view.signature.PKCS11View;
import eu.europa.ec.markt.dss.applet.view.signature.PKCS12View;
import eu.europa.ec.markt.dss.applet.view.signature.PersonalDataView;
import eu.europa.ec.markt.dss.applet.view.signature.SaveView;
import eu.europa.ec.markt.dss.applet.view.signature.SignatureDigestAlgorithmView;
import eu.europa.ec.markt.dss.applet.view.signature.SignatureView;
import eu.europa.ec.markt.dss.applet.view.signature.TokenView;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardController;
import eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardStep;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.parameter.BLevelParameters.Policy;
import eu.europa.ec.markt.dss.parameter.SignatureParameters;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.SignatureLevel;
import eu.europa.ec.markt.dss.signature.token.DSSPrivateKeyEntry;
import eu.europa.ec.markt.dss.signature.token.SignatureTokenConnection;

/**
 * TODO
 *
 * <p>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public class SignatureWizardController extends DSSWizardController<SignatureModel> {

    private FileView fileView;
    private SignatureView signatureView;
    private TokenView tokenView;
    private PKCS11View pkcs11View;
    private PKCS12View pkcs12View;
    private SignatureDigestAlgorithmView signatureDigestAlgorithmView;
    private CertificateView certificateView;
    private PersonalDataView personalDataView;
    private SaveView saveView;
    private FinishView signView;

    /**
     * The default constructor for SignatureWizardController.
     *
     * @param core
     * @param model
     */
    public SignatureWizardController(final DSSAppletCore core, final SignatureModel model) {
        super(core, model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardController#doCancel()
     */
    @Override
    protected void doCancel() {

        getCore().getController(ActivityController.class).display();
    }

    /**
     *
     */
    public void doRefreshPrivateKeys() {

        try {
            final SignatureTokenConnection tokenConnection = getModel().getTokenConnection();
            getModel().setPrivateKeys(tokenConnection.getKeys());
        } catch (final DSSException e) {
            // FIXME
            LOG.error(e.getMessage(), e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardController#doStart()
     */
    @Override
    protected Class<? extends WizardStep<SignatureModel, ? extends WizardController<SignatureModel>>> doStart() {

        return FileStep.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardController#registerViews()
     */
    @Override
    protected void registerViews() {

        fileView = new FileView(getCore(), this, getModel());
        signatureView = new SignatureView(getCore(), this, getModel());
        tokenView = new TokenView(getCore(), this, getModel());
        pkcs11View = new PKCS11View(getCore(), this, getModel());
        pkcs12View = new PKCS12View(getCore(), this, getModel());
        signatureDigestAlgorithmView = new SignatureDigestAlgorithmView(getCore(), this, getModel());
        certificateView = new CertificateView(getCore(), this, getModel());
        personalDataView = new PersonalDataView(getCore(), this, getModel());
        saveView = new SaveView(getCore(), this, getModel());
        signView = new FinishView(getCore(), this, getModel());
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europa.ec.markt.dss.commons.swing.mvc.applet.wizard.WizardController#registerWizardStep()
     */
    @Override
    protected Map<Class<? extends WizardStep<SignatureModel, ? extends WizardController<SignatureModel>>>, ? extends WizardStep<SignatureModel, ? extends WizardController<SignatureModel>>> registerWizardStep() {

        final SignatureModel model = getModel();

        final Map steps = new HashMap();
        steps.put(FileStep.class, new FileStep(model, fileView, this));
        steps.put(SignatureStep.class, new SignatureStep(model, signatureView, this));
        steps.put(TokenStep.class, new TokenStep(model, tokenView, this));
        steps.put(PKCS11Step.class, new PKCS11Step(model, pkcs11View, this));
        steps.put(PKCS12Step.class, new PKCS12Step(model, pkcs12View, this));
        steps.put(SignatureDigestAlgorithmStep.class, new SignatureDigestAlgorithmStep(model, signatureDigestAlgorithmView, this));
        steps.put(CertificateStep.class, new CertificateStep(model, certificateView, this));
        steps.put(PersonalDataStep.class, new PersonalDataStep(model, personalDataView, this));
        steps.put(SaveStep.class, new SaveStep(model, saveView, this));
        steps.put(FinishStep.class, new FinishStep(model, signView, this));

        return steps;
    }

    /**
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws DSSException
     */
    public void signDocument() throws IOException, NoSuchAlgorithmException, DSSException {

        final SignatureModel model = getModel();

        final File fileToSign = model.getSelectedFile();
        final SignatureTokenConnection tokenConnection = model.getTokenConnection();
        final DSSPrivateKeyEntry privateKey = model.getSelectedPrivateKey();

        final SignatureParameters parameters = new SignatureParameters();
        parameters.setPrivateKeyEntry(privateKey);
        final String signatureLevelString = model.getLevel();
        final SignatureLevel signatureLevel = SignatureLevel.valueByName(signatureLevelString);
        parameters.setSignatureLevel(signatureLevel);
        parameters.setSignaturePackaging(model.getPackaging());
        parameters.setSigningToken(tokenConnection);

        if (model.isClaimedCheck()) {
            parameters.bLevel().addClaimedSignerRole(model.getClaimedRole());
        }

        DigestAlgorithm signatureAlgorithm = model.getSignatureDigestAlgorithm();
        if (signatureAlgorithm == null) {
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        } else {
            parameters.setDigestAlgorithm(signatureAlgorithm);
        }

        if (model.isSignaturePolicyCheck()) {
            final byte[] hashValue = DSSUtils.base64Decode(model.getSignaturePolicyValue());
            final Policy policy = parameters.bLevel().getSignaturePolicy();
            policy.setDigestValue(hashValue);
            policy.setId(model.getSignaturePolicyId());
            DigestAlgorithm digestAlgo = DigestAlgorithm.forName(model.getSignaturePolicyAlgo());
            policy.setDigestAlgorithm(digestAlgo);
        }

        final DSSDocument signedDocument = SigningUtils.signDocument(serviceURL, fileToSign, parameters);
        FileOutputStream fos = new FileOutputStream(model.getTargetFile());
        DSSUtils.copy(signedDocument.openStream(), fos);
        fos.close();

    }
}
