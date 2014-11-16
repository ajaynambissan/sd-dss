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

package eu.europa.ec.markt.dss.validation102853;

import java.util.Date;
import java.util.Set;

import eu.europa.ec.markt.dss.exception.DSSException;

/**
 * This interface allows the implementation of the validators for: certificates,  timestamps and revocation data.
 */
public interface ValidationContext {

	/**
	 * This function sets the validation time.
	 *
	 * @param currentTime {@code Date}
	 */
	public void setCurrentTime(final Date currentTime);

	void initialize(final CertificateVerifier certificateVerifier);

	public Date getCurrentTime();

	/**
	 * Adds a new revocation token to the list of tokes to verify. If the revocation token has already been added then it is ignored.
	 *
	 * @param revocationToken {@code RevocationToken} revocation token to verify
	 */
	void addRevocationTokenForVerification(final RevocationToken revocationToken);

	/**
	 * Adds a new certificate token to the list of tokes to verify. If the certificate token has already been added then it is ignored.
	 *
	 * @param certificateToken {@code CertificateToken} certificate token to verify
	 */
	void addCertificateTokenForVerification(final CertificateToken certificateToken);

	/**
	 * Adds a new timestamp token to the list of tokes to verify. If the timestamp token has already been added then it is ignored.
	 *
	 * @param timestampToken {@code TimestampToken} timestamp token to verify
	 */
	void addTimestampTokenForVerification(final TimestampToken timestampToken);

	/**
	 * Carries out the validation process in recursive manner for not yet checked tokens.
	 *
	 * @throws DSSException
	 */
	public abstract void validate() throws DSSException;

	/**
	 * Returns a read only list of all certificates used in the process of the validation of all signatures from the given document. This list
	 * includes the certificate to check, certification chain certificates, OCSP response certificate...
	 *
	 * @return The list of CertificateToken(s)
	 */
	public abstract Set<CertificateToken> getProcessedCertificates();

	/**
	 * Returns a read only list of all revocations used in the process of the validation of all signatures from the given document.
	 *
	 * @return The list of CertificateToken(s)
	 */
	public abstract Set<RevocationToken> getProcessedRevocations();

	/**
	 * Returns a read only list of all timestamps processed during the validation of all signatures from the given document.
	 *
	 * @return The list of CertificateToken(s)
	 */
	public abstract Set<TimestampToken> getProcessedTimestamps();
}