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

package eu.europa.ec.markt.dss.signature.pdf;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.util.Map;

import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.parameter.SignatureParameters;
import eu.europa.ec.markt.dss.validation102853.CertificatePool;

/**
 * The usage of this interface permit the user to choose the underlying PDF library use to created PDF signatures.
 *
 * @version $Revision: 4264 $ - $Date: 2014-07-14 17:21:17 +0200 (Mon, 14 Jul 2014) $
 */
public interface PDFSignatureService {

	/**
	 * Return the digest value of a PDF document
	 *
	 * @param toSignDocument
	 * @param parameters
	 * @param digestAlgorithm
	 * @param extraDictionariesToAddBeforeSign @return
	 * @throws DSSException
	 */
	byte[] digest(final InputStream toSignDocument, final SignatureParameters parameters, final DigestAlgorithm digestAlgorithm,
	              final Map.Entry<String, PdfDict>... extraDictionariesToAddBeforeSign) throws DSSException;

	/**
	 * Sign a PDF document
	 *
	 * @param pdfData
	 * @param signatureValue
	 * @param signedStream
	 * @param parameters
	 * @param digestAlgorithm
	 * @param extraDictionariesToAddBeforeSign
	 * @throws DSSException
	 */
	void sign(final InputStream pdfData, final byte[] signatureValue, final OutputStream signedStream, final SignatureParameters parameters, final DigestAlgorithm digestAlgorithm,
	          final Map.Entry<String, PdfDict>... extraDictionariesToAddBeforeSign) throws DSSException;

	/**
	 * Retrieve and trigger validation of the signatures from a PDF document
	 *
	 * @param validationCertPool
	 * @param input
	 * @param callback
	 * @throws DSSException
	 * @throws SignatureException
	 */
	void validateSignatures(final CertificatePool validationCertPool, final InputStream input, final SignatureValidationCallback callback) throws DSSException;

}