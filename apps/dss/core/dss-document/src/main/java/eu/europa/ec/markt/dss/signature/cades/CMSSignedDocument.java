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

package eu.europa.ec.markt.dss.signature.cades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.cms.CMSSignedData;

import eu.europa.ec.markt.dss.DSSASN1Utils;
import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.exception.DSSNullException;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.MimeType;

/**
 * A document composed by a CMSSignedData
 *
 * @version $Revision: 4182 $ - $Date: 2014-07-02 14:40:17 +0200 (Wed, 02 Jul 2014) $
 */

public class CMSSignedDocument implements DSSDocument {

	protected CMSSignedData signedData;
	protected MimeType mimeType = MimeType.PKCS7;

	/**
	 * The default constructor for CMSSignedDocument.
	 *
	 * @param data
	 * @throws IOException
	 */
	public CMSSignedDocument(final CMSSignedData data) throws DSSException {

		this.signedData = data;
		if (data == null) {

			throw new DSSNullException(CMSSignedData.class);
		}
	}

	@Override
	public InputStream openStream() throws DSSException {

		final byte[] bytes = getBytes();
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		return byteArrayInputStream;
	}

	/**
	 * @return the signedData
	 */
	public CMSSignedData getCMSSignedData() {

		return signedData;
	}

	@Override
	public String getName() {

		return "CMSSignedDocument";
	}

	@Override
	public MimeType getMimeType() {
		return mimeType;
	}

	@Override
	public void setMimeType(final MimeType mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public byte[] getBytes() throws DSSException {

		try {

			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final DEROutputStream derOutputStream = new DEROutputStream(output);
			final byte[] encoded = signedData.getEncoded();
			final ASN1Primitive asn1Primitive = DSSASN1Utils.toASN1Primitive(encoded);
			derOutputStream.writeObject(asn1Primitive);
			derOutputStream.close();
			return output.toByteArray();
		} catch (IOException e) {

			throw new DSSException(e);
		}
	}

	@Override
	public void save(final String filePath) {

		try {

			final FileOutputStream fos = new FileOutputStream(filePath);
			DSSUtils.write(getBytes(), fos);
			fos.close();
		} catch (FileNotFoundException e) {
			throw new DSSException(e);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	@Override
	public String getDigest(final DigestAlgorithm digestAlgorithm) {

		final byte[] digestBytes = DSSUtils.digest(digestAlgorithm, getBytes());
		final String base64Encode = DSSUtils.base64Encode(digestBytes);
		return base64Encode;
	}

	@Override
	public String getAbsolutePath() {
		return getName();
	}

	@Override
	public String toString() {

		final StringWriter stringWriter = new StringWriter();
		final MimeType mimeType = getMimeType();
		stringWriter.append("Name: " + getName()).append(" / ").append(mimeType == null ? "" : getMimeType().name());
		final String string = stringWriter.toString();
		return string;
	}
}
