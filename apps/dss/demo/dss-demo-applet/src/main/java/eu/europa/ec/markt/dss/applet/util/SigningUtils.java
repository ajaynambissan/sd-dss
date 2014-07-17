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

package eu.europa.ec.markt.dss.applet.util;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DSSXMLUtils;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.parameter.BLevelParameters;
import eu.europa.ec.markt.dss.parameter.DSSReference;
import eu.europa.ec.markt.dss.parameter.DSSTransform;
import eu.europa.ec.markt.dss.parameter.SignatureParameters;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.FileDocument;
import eu.europa.ec.markt.dss.signature.InMemoryDocument;
import eu.europa.ec.markt.dss.signature.MimeType;
import eu.europa.ec.markt.dss.signature.token.DSSPrivateKeyEntry;
import eu.europa.ec.markt.dss.signature.token.SignatureTokenConnection;
import eu.europa.ec.markt.dss.ws.signature.DSSException_Exception;
import eu.europa.ec.markt.dss.ws.signature.DigestAlgorithm;
import eu.europa.ec.markt.dss.ws.signature.DssReference;
import eu.europa.ec.markt.dss.ws.signature.DssTransform;
import eu.europa.ec.markt.dss.ws.signature.EncryptionAlgorithm;
import eu.europa.ec.markt.dss.ws.signature.ObjectFactory;
import eu.europa.ec.markt.dss.ws.signature.Policy;
import eu.europa.ec.markt.dss.ws.signature.SignatureLevel;
import eu.europa.ec.markt.dss.ws.signature.SignaturePackaging;
import eu.europa.ec.markt.dss.ws.signature.SignatureService;
import eu.europa.ec.markt.dss.ws.signature.SignatureService_Service;
import eu.europa.ec.markt.dss.ws.signature.WsDocument;
import eu.europa.ec.markt.dss.ws.signature.WsParameters;

/**
 * TODO
 * <p/>
 * <p/>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public final class SigningUtils {

	private static ObjectFactory FACTORY;

	static {

		System.setProperty("javax.xml.bind.JAXBContext", "com.sun.xml.internal.bind.v2.ContextFactory");
		FACTORY = new ObjectFactory();

	}

	private SigningUtils() {

	}

	/**
	 * @param serviceURL
	 * @param signedFile
	 * @param detachedFile
	 * @param parameters
	 * @return
	 * @throws DSSException
	 */
	public static DSSDocument extendDocument(final String serviceURL, final File signedFile, final File detachedFile, final SignatureParameters parameters) throws DSSException {

		try {

			final WsDocument wsSignedDocument = toWsDocument(signedFile);
			if (detachedFile != null) {

				final DSSDocument detachedContent = new FileDocument(detachedFile);
				parameters.setDetachedContent(detachedContent);
			}

			final WsParameters wsParameters = new WsParameters();

			final String signatureLevelString = parameters.getSignatureLevel().name();
			final SignatureLevel signatureLevel = SignatureLevel.fromValue(signatureLevelString);
			wsParameters.setSignatureLevel(signatureLevel);

			final String signaturePackagingString = parameters.getSignaturePackaging().name();
			SignaturePackaging signaturePackaging = SignaturePackaging.valueOf(signaturePackagingString);
			wsParameters.setSignaturePackaging(signaturePackaging);

			SignatureService_Service.setROOT_SERVICE_URL(serviceURL);
			final SignatureService_Service signatureService_service = new SignatureService_Service();
			final SignatureService signatureServiceImplPort = signatureService_service.getSignatureServiceImplPort();

			final WsDocument wsExtendedDocument = signatureServiceImplPort.extendSignature(wsSignedDocument, wsParameters);

			final InMemoryDocument inMemoryDocument = toInMemoryDocument(wsExtendedDocument);
			return inMemoryDocument;
		} catch (DSSException_Exception e) {
			throw new DSSException(e);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new DSSException(e);
		}
	}

	/**
	 * @param file
	 * @param parameters
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws DSSException
	 */
	public static DSSDocument signDocument(final String serviceURL, final File file, final SignatureParameters parameters) throws DSSException {

		try {

			final WsDocument wsDocument = toWsDocument(file);

			final WsParameters wsParameters = new WsParameters();

			final String signatureLevelString = parameters.getSignatureLevel().name();
			final SignatureLevel signatureLevel = SignatureLevel.fromValue(signatureLevelString);
			wsParameters.setSignatureLevel(signatureLevel);

			final String signaturePackagingString = parameters.getSignaturePackaging().name();
			final SignaturePackaging signaturePackaging = SignaturePackaging.valueOf(signaturePackagingString);
			wsParameters.setSignaturePackaging(signaturePackaging);

			final String encryptionAlgorithmString = parameters.getEncryptionAlgorithm().name();
			final EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.fromValue(encryptionAlgorithmString);
			wsParameters.setEncryptionAlgorithm(encryptionAlgorithm);

			final String digestAlgorithmString = parameters.getDigestAlgorithm().name();
			final DigestAlgorithm digestAlgorithm = DigestAlgorithm.fromValue(digestAlgorithmString);
			wsParameters.setDigestAlgorithm(digestAlgorithm);

			wsParameters.setSigningDate(DSSXMLUtils.createXMLGregorianCalendar(new Date()));
			wsParameters.setSigningCertificateBytes(DSSUtils.getEncoded(parameters.getSigningCertificate()));

			final List<X509Certificate> certificateChain = parameters.getCertificateChain();
			if (certificateChain.size() > 0) {

				final List<byte[]> certificateChainByteArrayList = wsParameters.getCertificateChainByteArrayList();
				for (final X509Certificate x509Certificate : certificateChain) {

					certificateChainByteArrayList.add(DSSUtils.getEncoded(x509Certificate));
				}
			}
			final BLevelParameters bLevelParameters = parameters.bLevel();
			final BLevelParameters.Policy signaturePolicy = bLevelParameters.getSignaturePolicy();
			if (signaturePolicy != null) {

				final Policy policy = new Policy();
				policy.setId(signaturePolicy.getId());
				final String policyDigestAlgorithmString = signaturePolicy.getDigestAlgorithm().name();
				final DigestAlgorithm wsDigestAlgorithm = DigestAlgorithm.valueOf(policyDigestAlgorithmString);
				policy.setDigestAlgorithm(wsDigestAlgorithm);
				final byte[] digestValue = signaturePolicy.getDigestValue();
				policy.setDigestValue(digestValue);
				wsParameters.setSignaturePolicy(policy);
			}

			final List<String> claimedSignerRoles = bLevelParameters.getClaimedSignerRoles();
			if (claimedSignerRoles != null) {
				for (final String claimedSignerRole : claimedSignerRoles) {

					final List<String> wsClaimedSignerRoles = wsParameters.getClaimedSignerRole();
					wsClaimedSignerRoles.add(claimedSignerRole);
				}
			}

			final List<DssReference> wsDssReferences = wsParameters.getReferences();
			for (DSSReference dssReference : parameters.getReferences()) {

				final DssReference wsDssReference = FACTORY.createDssReference();
				wsDssReference.setId(dssReference.getId());
				wsDssReference.setType(dssReference.getType());
				wsDssReference.setUri(dssReference.getUri());
				wsDssReference.setDigestMethod(dssReference.getDigestMethod());

				final List<DSSTransform> dssTransforms = dssReference.getTransforms();
				if (dssTransforms != null) {

					for (DSSTransform dssTransform : dssTransforms) {

						final DssTransform wsDssTransform = FACTORY.createDssTransform();
						wsDssTransform.setElementName(dssTransform.getElementName());
						wsDssTransform.setTextContent(dssTransform.getTextContent());
						wsDssTransform.setNamespace(dssTransform.getNamespace());
						wsDssTransform.setAlgorithm(dssTransform.getAlgorithm());
						final List<DssTransform> wsDssTransforms = wsDssReference.getTransforms();
						wsDssTransforms.add(wsDssTransform);
					}
				}
				wsDssReferences.add(wsDssReference);
			}
			wsParameters.setDeterministicId(parameters.getDeterministicId());

			// System.out.println("#@@@@@@@@: " + serviceURL);
			SignatureService_Service.setROOT_SERVICE_URL(serviceURL);
			final SignatureService_Service signatureService_service = new SignatureService_Service();
			final SignatureService signatureServiceImplPort = signatureService_service.getSignatureServiceImplPort();

			final byte[] toBeSignedBytes = signatureServiceImplPort.getDataToSign(wsDocument, wsParameters);

			final String wsDigestAlgorithmValue = digestAlgorithm.value();
			eu.europa.ec.markt.dss.DigestAlgorithm dssDigestAlgorithm = eu.europa.ec.markt.dss.DigestAlgorithm.forName(wsDigestAlgorithmValue);

			final DSSPrivateKeyEntry privateKey = parameters.getPrivateKeyEntry();
			final SignatureTokenConnection tokenConnection = parameters.getSigningToken();
			final byte[] encrypted = tokenConnection.sign(toBeSignedBytes, dssDigestAlgorithm, privateKey);

			final WsDocument wsSignedDocument = signatureServiceImplPort.signDocument(wsDocument, wsParameters, encrypted);

			final InMemoryDocument inMemoryDocument = toInMemoryDocument(wsSignedDocument);
			return inMemoryDocument;
		} catch (DSSException_Exception e) {
			throw new DSSException(e);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new DSSException(e);
		}
	}

	public static WsDocument toWsDocument(final File file) {

		final DSSDocument dssDocument = new FileDocument(file);
		final WsDocument wsDocument = new WsDocument();
		wsDocument.setBytes(dssDocument.getBytes());
		wsDocument.setName(dssDocument.getName());
		wsDocument.setAbsolutePath(dssDocument.getAbsolutePath());
		final MimeType mimeType = dssDocument.getMimeType();
		if (mimeType != null) {

			wsDocument.setMimeTypeString(mimeType.getCode());
			wsDocument.setMimeType(eu.europa.ec.markt.dss.ws.signature.MimeType.fromValue(mimeType.name()));
		}
		return wsDocument;
	}

	public static InMemoryDocument toInMemoryDocument(final WsDocument wsSignedDocument) {

		final InMemoryDocument inMemoryDocument = new InMemoryDocument(wsSignedDocument.getBytes());
		inMemoryDocument.setName(wsSignedDocument.getName());
		inMemoryDocument.setAbsolutePath(wsSignedDocument.getAbsolutePath());
		final String mimeTypeString = wsSignedDocument.getMimeTypeString();
		final MimeType mimeType = MimeType.fromCode(mimeTypeString);
		inMemoryDocument.setMimeType(mimeType);
		return inMemoryDocument;
	}
}
