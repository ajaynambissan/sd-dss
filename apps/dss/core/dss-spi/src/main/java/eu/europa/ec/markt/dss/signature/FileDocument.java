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

package eu.europa.ec.markt.dss.signature;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.exception.DSSNullException;

/**
 * Document implementation stored on file-system.
 *
 * @version $Revision$ - $Date$
 */

public class FileDocument implements DSSDocument {

	private final File file;

	private MimeType mimeType;

	/**
	 * Create a FileDocument
	 *
	 * @param path the path to the file
	 */
	public FileDocument(final String path) {
		this(new File(path));
	}

	/**
	 * Create a FileDocument
	 *
	 * @param file {@code File}
	 */
	public FileDocument(final File file) {

		if (file == null) {

			throw new DSSNullException(File.class);
		}
		if (!file.exists()) {

			throw new DSSException("File Not Found: " + file.getAbsolutePath());
		}
		this.file = file;
		this.mimeType = MimeType.fromFileName(file.getName());
	}

	@Override
	public InputStream openStream() throws DSSException {

		final InputStream inputStream = DSSUtils.toInputStream(file);
		return inputStream;
	}

	public boolean exists() {
		return file.exists();
	}

	public File getParentFile() {
		return file.getParentFile();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
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

		final InputStream inputStream = openStream();
		final byte[] bytes = DSSUtils.toByteArray(inputStream);
		DSSUtils.closeQuietly(inputStream);
		return bytes;
	}

	@Override
	public void save(final String path) throws DSSException {

		final InputStream inputStream = openStream();
		DSSUtils.saveToFile(inputStream, path);
		DSSUtils.closeQuietly(inputStream);
	}

	@Override
	public String getDigest(final DigestAlgorithm digestAlgorithm) {

		final InputStream inputStream = openStream();
		final byte[] digestBytes = DSSUtils.digest(digestAlgorithm, inputStream);
		DSSUtils.closeQuietly(inputStream);
		final String base64Encode = DSSUtils.base64Encode(digestBytes);
		return base64Encode;
	}

	@Override
	public String toString() {

		final StringWriter stringWriter = new StringWriter();
		final MimeType mimeType = getMimeType();
		stringWriter.append("Name: " + getName()).append(" / ").append(mimeType == null ? "" : getMimeType().name()).append(" / ").append(getAbsolutePath());
		final String string = stringWriter.toString();
		return string;
	}
}
