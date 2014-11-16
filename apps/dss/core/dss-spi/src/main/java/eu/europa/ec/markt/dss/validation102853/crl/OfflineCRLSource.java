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

package eu.europa.ec.markt.dss.validation102853.crl;

import java.security.cert.X509CRL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.exception.DSSNullException;
import eu.europa.ec.markt.dss.validation102853.CertificateToken;

/**
 * This class if a basic skeleton that is able to retrieve needed CRL data from the contained list. The child need to retrieve
 * the list of wrapped CRLs.
 *
 * @version $Revision$ - $Date$
 */

public abstract class OfflineCRLSource extends CommonCRLSource {

	private static final Logger LOG = LoggerFactory.getLogger(OfflineCRLSource.class);

	/**
	 * List of contained {@code X509CRL}s. One CRL list contains many CRLToken(s).
	 */
	protected List<X509CRL> x509CRLList;

	protected HashMap<CertificateToken, CRLToken> validCRLTokenList = new HashMap<CertificateToken, CRLToken>();

	/**
	 * This {@code HashMap} contains the {@code CRLValidity} object for each {@code X509CRL}. It is used for performance reasons.
	 */
	protected Map<X509CRL, CRLValidity> crlValidityMap = new HashMap<X509CRL, CRLValidity>();

	@Override
	final public CRLToken findCrl(final CertificateToken certificateToken) {

		if (certificateToken == null) {

			throw new DSSNullException(CertificateToken.class, "certificateToken");
		}
		final CRLToken validCRLToken = validCRLTokenList.get(certificateToken);
		if (validCRLToken != null) {

			return validCRLToken;
		}
		final CertificateToken issuerToken = certificateToken.getIssuerToken();
		if (issuerToken == null) {

			throw new DSSNullException(CertificateToken.class, "issuerToken");
		}
		final CRLValidity bestCRLValidity = getBestCrlValidity(issuerToken);
		if (bestCRLValidity == null) {
			return null;
		}
		final CRLToken crlToken = new CRLToken(certificateToken, bestCRLValidity);
		validCRLTokenList.put(certificateToken, crlToken);
		return crlToken;
	}

	/**
	 * This method returns the best {@code CRLValidity} containing the most recent {@code X509CRL}.
	 *
	 * @param issuerToken {@code CertificateToken} representing the signing certificate of the CRL
	 * @return {@code CRLValidity}
	 */
	private CRLValidity getBestCrlValidity(final CertificateToken issuerToken) {

		CRLValidity bestCRLValidity = null;
		Date bestX509UpdateDate = null;

		for (final X509CRL x509CRL : x509CRLList) {

			final CRLValidity crlValidity = getCrlValidity(issuerToken, x509CRL);
			if (crlValidity == null) {
				continue;
			}
			if (issuerToken.equals(crlValidity.issuerToken) && crlValidity.isValid()) {

				final Date thisUpdate = x509CRL.getThisUpdate();
				if (bestX509UpdateDate == null || thisUpdate.after(bestX509UpdateDate)) {

					bestCRLValidity = crlValidity;
					bestX509UpdateDate = thisUpdate;
				}
			}
		}
		return bestCRLValidity;
	}

	/**
	 * This method returns {@code CRLValidity} object based on the given {@code X509CRL}. The check of the validity of the CRL is performed.
	 *
	 * @param issuerToken {@code CertificateToken} issuer of the CRL
	 * @param x509CRL     {@code X509CRL} the validity to be checked
	 * @return returns updated {@code CRLValidity} object
	 */
	private synchronized CRLValidity getCrlValidity(final CertificateToken issuerToken, final X509CRL x509CRL) {

		CRLValidity crlValidity = crlValidityMap.get(x509CRL);
		if (crlValidity == null) {

			crlValidity = isValidCRL(x509CRL, issuerToken);
			if (crlValidity.isValid()) {

				crlValidityMap.put(x509CRL, crlValidity);
			}
		}
		return crlValidity;
	}

	/**
	 * @return unmodifiable {@code List} of {@code X509CRL}s
	 */
	public List<X509CRL> getContainedX509CRLs() {

		final List<X509CRL> x509CRLs = Collections.unmodifiableList(x509CRLList);
		return x509CRLs;
	}
}
