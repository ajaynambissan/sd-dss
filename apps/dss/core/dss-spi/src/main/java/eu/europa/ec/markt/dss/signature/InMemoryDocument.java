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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.exception.DSSException;

/**
 * In memory representation of a document
 *
 * @version $Revision: 3478 $ - $Date: 2014-02-19 10:45:39 +0100 (Wed, 19 Feb 2014) $
 */

public class InMemoryDocument implements DSSDocument {

    private byte[] bytes;

    private String name;

    private String absolutePath;

    private MimeType mimeType;

    /**
     * Creates dss document that retains the data in memory
     *
     * @param bytes array of bytes representing the document
     */
    public InMemoryDocument(final byte[] bytes) {
        this(bytes, null, null);
    }

    /**
     * Creates dss document that retains the data in memory
     *
     * @param bytes array of bytes representing the document
     * @param name  the file name if the data originates from a file
     */
    public InMemoryDocument(final byte[] bytes, final String name) {

        this.bytes = bytes;
        this.name = name;
        this.mimeType = MimeType.fromFileName(name);
    }

    /**
     * Creates dss document that retains the data in memory
     *
     * @param bytes    array of bytes representing the document
     * @param name     the file name if the data originates from a file
     * @param mimeType the mime type of the file if the data originates from a file
     */
    public InMemoryDocument(final byte[] bytes, final String name, final MimeType mimeType) {
        this.bytes = bytes;
        this.name = name;
        this.mimeType = mimeType;
    }

    /**
     * Creates dss document that retains the data in memory
     *
     * @param inputStream input stream representing the document
     * @throws DSSException
     */
    public InMemoryDocument(final InputStream inputStream) throws DSSException {
        this(DSSUtils.toByteArray(inputStream), null, null);
    }

    /**
     * Creates dss document that retains the data in memory
     *
     * @param inputStream input stream representing the document
     * @param name        the file name if the data originates from a file
     * @throws IOException
     */
    public InMemoryDocument(final InputStream inputStream, final String name) throws DSSException {
        this(DSSUtils.toByteArray(inputStream), name);
    }

    /**
     * Creates dss document that retains the data in memory
     *
     * @param inputStream input stream representing the document
     * @param name        the file name if the data originates from a file
     * @param mimeType    the mime type of the file if the data originates from a file
     * @throws IOException
     */
    public InMemoryDocument(final InputStream inputStream, final String name, final MimeType mimeType) throws DSSException {
        this(DSSUtils.toByteArray(inputStream), name, mimeType);
    }

    @Override
    public InputStream openStream() throws DSSException {

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        return byteArrayInputStream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public byte[] getBytes() throws DSSException {

        return bytes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public void setAbsolutePath(final String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Override
    public void save(final String filePath) {

        try {

            final FileOutputStream fos = new FileOutputStream(filePath);
            DSSUtils.write(getBytes(), fos);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new DSSException(e);
        } catch (DSSException e) {
            throw new DSSException(e);
        } catch (IOException e) {
            throw new DSSException(e);
        }
    }

    @Override
    public String getAbsolutePath() {

        return absolutePath;
    }

    @Override
    public String toString() {

        final StringWriter stringWriter = new StringWriter();
        final MimeType mimeType = getMimeType();
        final String name = getName();
        if (name != null) {

            stringWriter.append("Name: ").append(name).append(" / ");
        }
        if (mimeType != null) {

            stringWriter.append(mimeType.name()).append(" / ");
        }
        stringWriter.append(getAbsolutePath());
        final String string = stringWriter.toString();
        return string;
    }
}