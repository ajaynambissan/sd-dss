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

import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.dsig.DigestMethod;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import eu.europa.ec.markt.dss.exception.DSSException;

/**
 * Supported Algorithms
 *
 * @version $Revision: 4211 $ - $Date: 2014-07-08 09:53:52 +0200 (Tue, 08 Jul 2014) $
 */
public enum DigestAlgorithm {

	// see DEPRECATED http://www.w3.org/TR/2012/WD-xmlsec-algorithms-20120105/
	// see http://www.w3.org/TR/2013/NOTE-xmlsec-algorithms-20130411/
	//@formatter:off
	SHA1("SHA1", "1.3.14.3.2.26", DigestMethod.SHA1),
	SHA224("SHA224", "2.16.840.1.101.3.4.2.4", "http://www.w3.org/2001/04/xmldsig-more#sha224"),
	SHA256("SHA256", "2.16.840.1.101.3.4.2.1", DigestMethod.SHA256),
	SHA384("SHA384", "2.16.840.1.101.3.4.2.2", "http://www.w3.org/2001/04/xmldsig-more#sha384"),
	SHA512("SHA512", "2.16.840.1.101.3.4.2.3", DigestMethod.SHA512),
	RIPEMD160("RIPEMD160", "1.3.36.3.2.1", DigestMethod.RIPEMD160),
	MD2("MD2", "1.2.840.113549.1.1.2", "http://www.w3.org/2001/04/xmldsig-more#md2"),
	MD5("MD5", "1.2.840.113549.2.5", "http://www.w3.org/2001/04/xmldsig-more#md5");
	/**
	 * RFC 2313
	 * "MD2", "1.2.840.113549.2.2"
	 * "MD4", "1.2.840.113549.2.4"
	 * "MD5", "1.2.840.113549.2.5"
	 */
	//@formatter:on

	private String name;
	private ASN1ObjectIdentifier oid;
	private String xmlId;

	private static class Registry {

		private final static Map<ASN1ObjectIdentifier, DigestAlgorithm> OID_ALGORITHMS = registerOIDAlgorithms();
		private final static Map<String, DigestAlgorithm> XML_ALGORITHMS = registerXMLAlgorithms();
		private final static Map<String, DigestAlgorithm> ALGORITHMS = registerAlgorithms();

		private static Map<ASN1ObjectIdentifier, DigestAlgorithm> registerOIDAlgorithms() {

			final Map<ASN1ObjectIdentifier, DigestAlgorithm> map = new HashMap<ASN1ObjectIdentifier, DigestAlgorithm>();
			for (final DigestAlgorithm digestAlgorithm : values()) {
				map.put(digestAlgorithm.oid, digestAlgorithm);
			}
			return map;
		}

		private static Map<String, DigestAlgorithm> registerXMLAlgorithms() {

			final Map<String, DigestAlgorithm> map = new HashMap<String, DigestAlgorithm>();
			for (final DigestAlgorithm digestAlgorithm : values()) {
				map.put(digestAlgorithm.xmlId, digestAlgorithm);
			}
			return map;
		}

		private static Map<String, DigestAlgorithm> registerAlgorithms() {

			final Map<String, DigestAlgorithm> map = new HashMap<String, DigestAlgorithm>();
			for (final DigestAlgorithm digestAlgorithm : values()) {
				map.put(digestAlgorithm.name, digestAlgorithm);
			}
			return map;
		}
	}

	/**
	 * Returns the digest algorithm associated to the given JCE name.
	 *
	 * @param name
	 * @return
	 */
	public static DigestAlgorithm forName(final String name) {

		final String c14nName = DSSUtils.replaceStrStr(name, "-", "");
		final DigestAlgorithm algorithm = Registry.ALGORITHMS.get(c14nName);
		if (algorithm == null) {
			throw new DSSException("Unsupported algorithm: " + name + "/" + c14nName);
		}
		return algorithm;
	}

	/**
	 * Returns the digest algorithm associated to the given JCE name.
	 *
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static DigestAlgorithm forName(final String name, final DigestAlgorithm defaultValue) {

		final String c14nName = DSSUtils.replaceStrStr(name, "-", "");
		final DigestAlgorithm algorithm = Registry.ALGORITHMS.get(c14nName);
		if (algorithm == null) {

			return defaultValue;
		}
		return algorithm;
	}

	/**
	 * Returns the digest algorithm associated to the given OID.
	 *
	 * @param oid
	 * @return
	 */
	public static DigestAlgorithm forOID(final String oid) {

		ASN1ObjectIdentifier asn1ObjectIdentifier = new ASN1ObjectIdentifier(oid);
		final DigestAlgorithm algorithm = forOID(asn1ObjectIdentifier);
		return algorithm;
	}

	/**
	 * Returns the digest algorithm associated to the given OID.
	 *
	 * @param oid
	 * @return
	 */
	public static DigestAlgorithm forOID(final ASN1ObjectIdentifier oid) throws DSSException {

		final DigestAlgorithm algorithm = Registry.OID_ALGORITHMS.get(oid);
		if (algorithm == null) {
			throw new DSSException("Unsupported algorithm: " + oid);
		}
		return algorithm;
	}

	/**
	 * Returns the digest algorithm associated to the given XML url.
	 *
	 * @param xmlName
	 * @return
	 */
	public static DigestAlgorithm forXML(final String xmlName) {

		final DigestAlgorithm algorithm = Registry.XML_ALGORITHMS.get(xmlName);
		if (algorithm == null) {
			throw new DSSException("Unsupported algorithm: " + xmlName);
		}
		return algorithm;
	}

	/**
	 * Returns the digest algorithm associated to the given XML url or the default one if the algorithm does not exist.
	 *
	 * @param xmlName
	 * @param defaultValue
	 * @return
	 */
	public static DigestAlgorithm forXML(final String xmlName, final DigestAlgorithm defaultValue) {

		final DigestAlgorithm algorithm = Registry.XML_ALGORITHMS.get(xmlName);
		if (algorithm == null) {
			return defaultValue;
		}
		return algorithm;
	}

	private DigestAlgorithm(final String name, final String oid, final String xmlId) {

		this.name = name;
		this.oid = new ASN1ObjectIdentifier(oid);
		this.xmlId = xmlId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the OID
	 */
	public ASN1ObjectIdentifier getOid() {
		return oid;
	}

	/**
	 * @return the xmlId
	 */
	public String getXmlId() {
		return xmlId;
	}

	/**
	 * Gets the ASN.1 algorithm identifier structure corresponding to this digest algorithm
	 *
	 * @return the AlgorithmIdentifier
	 */
	public AlgorithmIdentifier getAlgorithmIdentifier() {

	    /*
	     * The recommendation (cf. RFC 3380 section 2.1) is to omit the parameter for SHA-1, but some implementations still expect a
		 * NULL there. Therefore we always include a NULL parameter even with SHA-1, despite the recommendation, because the RFC
		 * states that implementations SHOULD support it as well anyway
		 */
		final ASN1ObjectIdentifier asn1ObjectIdentifier = this.getOid();
		final AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(asn1ObjectIdentifier, DERNull.INSTANCE);
		return algorithmIdentifier;
	}
}
