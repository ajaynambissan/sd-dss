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

package eu.europa.ec.markt.dss.signature.pdf.pdfbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.DSSPDFUtils;
import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.parameter.SignatureParameters;
import eu.europa.ec.markt.dss.signature.pdf.PDFSignatureService;
import eu.europa.ec.markt.dss.signature.pdf.PdfDict;
import eu.europa.ec.markt.dss.signature.pdf.PdfDocTimestampInfo;
import eu.europa.ec.markt.dss.signature.pdf.PdfSignatureOrDocTimestampInfo;
import eu.europa.ec.markt.dss.signature.pdf.SignatureValidationCallback;
import eu.europa.ec.markt.dss.validation102853.CertificatePool;
import eu.europa.ec.markt.dss.validation102853.TimestampType;

class PdfBoxSignatureService implements PDFSignatureService {

	private static final Logger LOG = LoggerFactory.getLogger(PdfBoxSignatureService.class);

	@Override
	public byte[] digest(final InputStream toSignDocument, final SignatureParameters parameters, final DigestAlgorithm digestAlgorithm,
	                     final Map.Entry<String, PdfDict>... extraDictionariesToAddBeforeSign) throws DSSException {

		final byte[] signatureValue = DSSUtils.EMPTY_BYTE_ARRAY;
		File toSignFile = null;
		File signedFile = null;
		PDDocument pdDocument = null;
		try {

			toSignFile = DSSPDFUtils.getFileFromPdfData(toSignDocument);

			pdDocument = PDDocument.load(toSignFile);
			addExtraDictionaries(pdDocument, extraDictionariesToAddBeforeSign);
			PDSignature pdSignature = createSignatureDictionary(parameters);

			signedFile = File.createTempFile("sd-dss-", "-signed.pdf");
			final FileOutputStream fileOutputStream = DSSPDFUtils.getFileOutputStream(toSignFile, signedFile);

			final byte[] digestValue = signDocumentAndReturnDigest(parameters, signatureValue, signedFile, fileOutputStream, pdDocument, pdSignature, digestAlgorithm);
			return digestValue;
		} catch (IOException e) {
			throw new DSSException(e);
		} finally {
			DSSUtils.delete(toSignFile);
			DSSUtils.delete(signedFile);
			DSSPDFUtils.close(pdDocument);
		}
	}

	@Override
	public void sign(final InputStream pdfData, final byte[] signatureValue, final OutputStream signedStream, final SignatureParameters parameters,
	                 final DigestAlgorithm digestAlgorithm, final Map.Entry<String, PdfDict>... extraDictionariesToAddBeforeSign) throws DSSException {

		File toSignFile = null;
		File signedFile = null;
		FileInputStream fileInputStream = null;
		FileInputStream finalFileInputStream = null;
		PDDocument pdDocument = null;
		try {

			toSignFile = DSSPDFUtils.getFileFromPdfData(pdfData);

			pdDocument = PDDocument.load(toSignFile);
			addExtraDictionaries(pdDocument, extraDictionariesToAddBeforeSign);
			final PDSignature pdSignature = createSignatureDictionary(parameters);

			signedFile = File.createTempFile("sd-dss-", "-signed.pdf");
			final FileOutputStream fileOutputStream = DSSPDFUtils.getFileOutputStream(toSignFile, signedFile);

			signDocumentAndReturnDigest(parameters, signatureValue, signedFile, fileOutputStream, pdDocument, pdSignature, digestAlgorithm);

			finalFileInputStream = new FileInputStream(signedFile);
			DSSUtils.copy(finalFileInputStream, signedStream);
		} catch (IOException e) {
			throw new DSSException(e);
		} finally {
			DSSUtils.closeQuietly(fileInputStream);
			DSSUtils.closeQuietly(finalFileInputStream);
			DSSUtils.delete(toSignFile);
			DSSUtils.delete(signedFile);
			DSSPDFUtils.close(pdDocument);
		}
	}

	private byte[] signDocumentAndReturnDigest(final SignatureParameters parameters, final byte[] signatureBytes, final File signedFile, final FileOutputStream fileOutputStream,
	                                           final PDDocument pdDocument, final PDSignature pdSignature, final DigestAlgorithm digestAlgorithm) throws DSSException {

		try {

			final MessageDigest digest = DSSUtils.getMessageDigest(digestAlgorithm);
			// register signature dictionary and sign interface
			SignatureInterface signatureInterface = new SignatureInterface() {

				@Override
				public byte[] sign(InputStream content) throws SignatureException, IOException {

					byte[] b = new byte[4096];
					int count;
					while ((count = content.read(b)) > 0) {
						digest.update(b, 0, count);
					}
					return signatureBytes;
				}
			};
			pdDocument.addSignature(pdSignature, signatureInterface);

			saveDocumentIncrementally(parameters, signedFile, fileOutputStream, pdDocument);
			final byte[] digestValue = digest.digest();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Digest to be signed: " + DSSUtils.encodeHexString(digestValue));
			}
			fileOutputStream.close();
			return digestValue;
		} catch (NoSuchAlgorithmException e) {
			throw new DSSException(e);
		} catch (IOException e) {
			throw new DSSException(e);
		} catch (SignatureException e) {
			throw new DSSException(e);
		}
	}

	private void addExtraDictionaries(final PDDocument doc, final Map.Entry<String, PdfDict>[] extraDictionariesToAddBeforeSign) {

		final COSDictionary cosDictionary = doc.getDocumentCatalog().getCOSDictionary();
		for (final Map.Entry<String, PdfDict> pdfDictEntry : extraDictionariesToAddBeforeSign) {

			final String key = pdfDictEntry.getKey();
			final PdfBoxDict value = (PdfBoxDict) pdfDictEntry.getValue();
			final COSDictionary wrapped = value.getWrapped();
			cosDictionary.setItem(key, wrapped);
		}
	}

	private PDSignature createSignatureDictionary(final SignatureParameters parameters) {

		final PDSignature signature = new PDSignature();
		signature.setName(String.format("SD-DSS Signature %s", parameters.getDeterministicId()));
		signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
		// sub-filter for basic and PAdES Part 2 signatures
		signature.setSubFilter(getSubFilter());

		// the signing date, needed for valid signature
		final Calendar cal = Calendar.getInstance();
		final Date signingDate = parameters.bLevel().getSigningDate();
		cal.setTime(signingDate);
		signature.setSignDate(cal);
		return signature;
	}

	public static void saveDocumentIncrementally(SignatureParameters parameters, File signedFile, FileOutputStream fileOutputStream, PDDocument pdDocument) throws DSSException {

		FileInputStream signedFileInputStream = null;
		try {

			signedFileInputStream = new FileInputStream(signedFile);
			// the document needs to have an ID, if not a ID based on the current system time is used, and then the digest of the signed data is different
			if (pdDocument.getDocumentId() == null) {

				final byte[] documentIdBytes = DSSUtils.digest(DigestAlgorithm.MD5, parameters.bLevel().getSigningDate().toString().getBytes());
				pdDocument.setDocumentId(DSSUtils.toLong(documentIdBytes));
				pdDocument.setDocumentId(0L);
			}
			pdDocument.saveIncremental(signedFileInputStream, fileOutputStream);
		} catch (IOException e) {
			throw new DSSException(e);
		} catch (COSVisitorException e) {
			throw new DSSException(e);
		} finally {
			DSSUtils.closeQuietly(signedFileInputStream);
		}
	}

	protected COSName getSubFilter() {
		return PDSignature.SUBFILTER_ETSI_CADES_DETACHED;
	}

	@Override
	public void validateSignatures(CertificatePool validationCertPool, InputStream input, SignatureValidationCallback callback) throws DSSException {
		// recursive search of signature
		Map<String, Set<PdfSignatureOrDocTimestampInfo>> byteRangeMap = new HashMap<String, Set<PdfSignatureOrDocTimestampInfo>>();
		final Set<PdfSignatureOrDocTimestampInfo> signaturesFound = validateSignatures(validationCertPool, byteRangeMap, null, input);
		for (final PdfSignatureOrDocTimestampInfo pdfSignatureOrDocTimestampInfo : signaturesFound) {
			callback.validate(pdfSignatureOrDocTimestampInfo);
		}
	}

    /* This is O(scary), but seems quick enough in practice. */

	/**
	 * @param validationCertPool
	 * @param byteRangeMap
	 * @param outerCatalog       the PdfDictionary of the document that enclose the document stored in the input InputStream
	 * @param input              the Pdf bytes to open as a PDF
	 * @return
	 * @throws DSSException
	 */
	private Set<PdfSignatureOrDocTimestampInfo> validateSignatures(CertificatePool validationCertPool, Map<String, Set<PdfSignatureOrDocTimestampInfo>> byteRangeMap,
	                                                               PdfDict outerCatalog, InputStream input) throws DSSException {
		Set<PdfSignatureOrDocTimestampInfo> signaturesFound = new LinkedHashSet<PdfSignatureOrDocTimestampInfo>();
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PDDocument doc = null;
		try {
			DSSUtils.copy(input, buffer);

			doc = PDDocument.load(new ByteArrayInputStream(buffer.toByteArray()));
			final PdfDict catalog = new PdfBoxDict(doc.getDocumentCatalog().getCOSDictionary(), doc);

			final List<PDSignature> signatureDictionaries = doc.getSignatureDictionaries();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Found {} signatures in PDF dictionary of PDF sized {} bytes", signatureDictionaries.size(), buffer.size());
			}
			for (int i = 0; i < signatureDictionaries.size(); i++) {
				final PDSignature signature = signatureDictionaries.get(i);
				/**
				 * SubFilter Name (Required) The value of SubFilter identifies the format of the data contained in the stream.
				 * A conforming reader may use any conforming signature handler that supports the specified format.
				 * When the value of Type is DocTimestamp, the value of SubFilter shall be ETSI.RFC3161.
				 */
				final String subFilter = signature.getSubFilter();

				byte[] cms = new PdfBoxDict(signature.getDictionary(), doc).get("Contents");

				PdfSignatureOrDocTimestampInfo signatureInfo;
				try {
					if (PdfBoxDocTimeStampService.SUB_FILTER_ETSI_RFC3161.getName().equals(subFilter)) {
						signatureInfo = PdfSignatureFactory.createPdfTimestampInfo(validationCertPool, outerCatalog, doc, signature, cms, buffer);
					} else {
						signatureInfo = PdfSignatureFactory.createPdfSignatureInfo(validationCertPool, outerCatalog, doc, signature, cms, buffer);
					}
				} catch (PdfSignatureOrDocTimestampInfo.DSSPadesNoSignatureFound e) {
					LOG.debug("No signature found in signature Dictionary:Content", e);
					continue;
				}

				signatureInfo = signatureAlreadyInListOrSelf(signaturesFound, signatureInfo);

				// should store in memory this byte range with a list of signature found there
				final String byteRange = Arrays.toString(signature.getByteRange());
				Set<PdfSignatureOrDocTimestampInfo> innerSignaturesFound = byteRangeMap.get(byteRange);
				if (innerSignaturesFound == null) {
					// Recursive call to find inner signatures in the byte range covered by this signature. Deep first search.
					final byte[] originalBytes = signatureInfo.getOriginalBytes();
					if (LOG.isDebugEnabled()) {
						LOG.debug("Searching signature in the previous revision of the document, size of revision is {} bytes", originalBytes.length);
					}
					innerSignaturesFound = validateSignatures(validationCertPool, byteRangeMap, catalog, new ByteArrayInputStream(originalBytes));
					byteRangeMap.put(byteRange, innerSignaturesFound);
				}

				// need to mark a signature as included inside another one. It's needed to link timestamp signature with the signatures covered by the timestamp.
				for (PdfSignatureOrDocTimestampInfo innerSignature : innerSignaturesFound) {
					innerSignature = signatureAlreadyInListOrSelf(signaturesFound, innerSignature);
					signaturesFound.add(innerSignature);
					innerSignature.addOuterSignature(signatureInfo);
				}

				signaturesFound.add(signatureInfo);
			}
			return signaturesFound;
		} catch (IOException up) {
			LOG.error("Error loading buffer of size {}", buffer.size(), up);
			// ignore error when loading signatures
			return signaturesFound;
		} finally {
			DSSPDFUtils.close(doc);
		}
	}


    /*
        This method is needed because we will encounter many times the same signature during our document analysis.
        We make sure that we always add it only once.
     */

	public static PdfSignatureOrDocTimestampInfo signatureAlreadyInListOrSelf(Set<PdfSignatureOrDocTimestampInfo> signaturesFound,
	                                                                          PdfSignatureOrDocTimestampInfo pdfSignatureOrDocTimestampInfo) {
		final int infoId = pdfSignatureOrDocTimestampInfo.uniqueId();
		for (Iterator<PdfSignatureOrDocTimestampInfo> iterator = signaturesFound.iterator(); iterator.hasNext(); ) {
			PdfSignatureOrDocTimestampInfo existingSignature = iterator.next();
			if (existingSignature.uniqueId() == infoId) {
				if (existingSignature instanceof PdfDocTimestampInfo) {
					PdfDocTimestampInfo existingSignatureDocTimestamp = (PdfDocTimestampInfo) existingSignature;
					if (existingSignatureDocTimestamp.getTimestampToken().getTimeStampType() == TimestampType.SIGNATURE_TIMESTAMP) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Signature was already found in the external doc. Returning newly (inner) found signature {} {}",
								  existingSignature.getClass().getSimpleName(), existingSignature.uniqueId());
						}
						return existingSignatureDocTimestamp;
					}
				}
				for (final PdfSignatureOrDocTimestampInfo outerSignature : existingSignature.getOuterSignatures()) {
					pdfSignatureOrDocTimestampInfo.addOuterSignature(outerSignature);
				}
				iterator.remove();
				signaturesFound.add(pdfSignatureOrDocTimestampInfo);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Signature was already found in the external doc. Returning newly (inner) found signature {} {}",
						  pdfSignatureOrDocTimestampInfo.getClass().getSimpleName(), pdfSignatureOrDocTimestampInfo.uniqueId());
				}

				return pdfSignatureOrDocTimestampInfo;
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Signature newly found {} {}", pdfSignatureOrDocTimestampInfo.getClass().getSimpleName(), pdfSignatureOrDocTimestampInfo.uniqueId());
		}
		return pdfSignatureOrDocTimestampInfo;
	}
}
