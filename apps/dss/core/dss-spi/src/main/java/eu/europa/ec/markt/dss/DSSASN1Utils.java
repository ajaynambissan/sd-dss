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

package eu.europa.ec.markt.dss;

import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Date;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;

import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.signature.DSSDocument;

/**
 * Utility class that contains some XML related method.
 *
 * @version $Revision: 2221 $ - $Date: 2013-06-11 11:53:27 +0200 (Tue, 11 Jun 2013) $
 */

public final class DSSASN1Utils {

	/**
	 * This class is an utility class and cannot be instantiated.
	 */
	private DSSASN1Utils() {

	}

	/**
	 * This method returns {@code T extends ASN1Primitive} created from array of bytes. The {@code IOException} is transformed in {@code DSSException}.
	 *
	 * @param bytes array of bytes to be transformed to {@code ASN1Primitive}
	 * @return new {@code T extends ASN1Primitive}
	 */
	public static <T extends ASN1Primitive> T toASN1Primitive(final byte[] bytes) throws DSSException {

		try {
			@SuppressWarnings("unchecked")
			final T asn1Primitive = (T) ASN1Primitive.fromByteArray(bytes);
			return asn1Primitive;
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method checks if a given {@code DEROctetString} is null.
	 *
	 * @param derOctetString
	 * @return
	 */
	public static boolean isDEROctetStringNull(final DEROctetString derOctetString) {

		final byte[] derOctetStringBytes = derOctetString.getOctets();
		final ASN1Primitive asn1Null = DSSASN1Utils.toASN1Primitive(derOctetStringBytes);
		return DERNull.INSTANCE.equals(asn1Null);
	}

	/**
	 * This method return DER encoded ASN1 attribute. The {@code IOException} is transformed in {@code DSSException}.
	 *
	 * @param asn1Encodable asn1Encodable to be DER encoded
	 * @return array of bytes representing the DER encoded asn1Encodable
	 */
	public static byte[] getDEREncoded(ASN1Encodable asn1Encodable) {
		try {
			return asn1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method return {@code X509Certificate} representing {@code X509CertificateHolder}. The {@code CertificateParsingException} is transformed in {@code
	 * DSSException}.
	 *
	 * @param certificateHolder {@code X509CertificateHolder}
	 * @return {@code X509Certificate}.
	 * @throws DSSException
	 */
	public static X509Certificate getCertificate(final X509CertificateHolder certificateHolder) throws DSSException {

		try {

			final X509Certificate certificate = new X509CertificateObject(certificateHolder.toASN1Structure());
			return certificate;
		} catch (CertificateParsingException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method returns DER encoded array of bytes representing {@code X509Certificate} for given {@code X509CertificateHolder}. The {@code
	 * IOException} is transformed in {@code DSSException}.
	 *
	 * @param certificateHolder {@code X509CertificateHolder}
	 * @return DER encoded array of bytes representing {@code X509Certificate}.
	 * @throws DSSException
	 */
	public static byte[] getCertificateDEREncoded(final X509CertificateHolder certificateHolder) throws DSSException {

		try {

			final byte[] bytes = certificateHolder.getEncoded();
			return bytes;
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	public static byte[] getEncoded(final AlgorithmIdentifier algorithmIdentifier) throws DSSException {

		try {
			return algorithmIdentifier.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	public static byte[] getEncoded(final ASN1Sequence signPolicyInfo) throws DSSException {

		try {
			return signPolicyInfo.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	public static Date toDate(final ASN1UTCTime attrValue) throws DSSException {

		try {
			return attrValue.getDate();
		} catch (ParseException e) {
			throw new DSSException(e);
		}
	}

	public static Date toDate(final ASN1GeneralizedTime notBeforeTime) throws DSSException {

		try {
			return notBeforeTime.getDate();
		} catch (ParseException e) {
			throw new DSSException(e);
		}
	}

	public static String toString(final ASN1OctetString value) {

		return new String(value.getOctets());
	}

	/**
	 * Returns the ASN.1 encoded representation of {@code CMSSignedData}.
	 *
	 * @param data
	 * @return
	 * @throws DSSException
	 */
	public static byte[] getEncoded(final CMSSignedData data) throws DSSException {

		try {
			return data.getEncoded();
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method generate {@code CMSSignedData} using the provided #{@code CMSSignedDataGenerator}, the content and the indication if the content should be encapsulated.
	 *
	 * @param generator
	 * @param content
	 * @param encapsulate
	 * @return
	 * @throws DSSException
	 */
	public static CMSSignedData generateCMSSignedData(final CMSSignedDataGenerator generator, final CMSProcessableByteArray content,
	                                                  final boolean encapsulate) throws DSSException {

		try {
			final CMSSignedData cmsSignedData = generator.generate(content, encapsulate);
			return cmsSignedData;
		} catch (CMSException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * Returns an ASN.1 encoded bytes representing the {@code TimeStampToken}
	 *
	 * @param timeStampToken {@code TimeStampToken}
	 * @return Returns an ASN.1 encoded bytes representing the {@code TimeStampToken}
	 */
	public static byte[] getEncoded(final TimeStampToken timeStampToken) throws DSSException {

		try {
			final byte[] encoded = timeStampToken.getEncoded();
			return encoded;
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * If the {@code DSSDocument} is a CMS message and the signed content's content is not null then the {@code CMSSignedData} is returned.
	 * All exceptions are hidden
	 *
	 * @param dssDocument
	 * @return {@code CMSSignedData} or {@code null}
	 */
	public static CMSSignedData getOriginalSignedData(final DSSDocument dssDocument) {

		CMSSignedData originalSignedData = null;

		try {
			// check if input toSignDocument is already signed
			originalSignedData = new CMSSignedData(dssDocument.getBytes());
			if (originalSignedData.getSignedContent().getContent() == null) {
				originalSignedData = null;
			}
		} catch (Exception e) {
			// not a parallel signature
		}
		return originalSignedData;
	}

	/**
	 * This method generates a bouncycastle {@code TimeStampToken} based on base 64 encoded {@code String}.
	 *
	 * @param base64EncodedTimestamp
	 * @return bouncycastle {@code TimeStampToken}
	 * @throws DSSException
	 */
	public static TimeStampToken createTimeStampToken(final String base64EncodedTimestamp) throws DSSException {

		try {

			final byte[] tokenBytes = DSSUtils.base64Decode(base64EncodedTimestamp);
			final CMSSignedData signedData = new CMSSignedData(tokenBytes);
			return new TimeStampToken(signedData);
		} catch (DSSException e) {
			throw new DSSException(e);
		} catch (CMSException e) {
			throw new DSSException(e);
		} catch (TSPException e) {
			throw new DSSException(e);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}
}
