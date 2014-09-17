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
package eu.europa.ec.markt.dss.parameter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import eu.europa.ec.markt.dss.DSSXMLUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;

/**
 * This class represents the parameters provided when generating specific timestamps in a signature, such as an AllDataObjectsTimestamp or an
 * IndividualDataObjectsTimestamp.
 */
public class TimestampParameters {

	/**
	 * The digest algorithm to provide to the timestamping authority
	 */
	private DigestAlgorithm digestAlgorithm;
	private String canonicalizationMethod;


	public DigestAlgorithm getDigestAlgorithm() {

		//TODO-Vincent (7/8/2014): This is a temporary measure, returning the previous default value in case the digest algorithm was not specified by the user.
		if (digestAlgorithm == null) {
			return DigestAlgorithm.SHA256;
		}
		return digestAlgorithm;
	}

	public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	public String getCanonicalizationMethod() {

		//TODO-Vincent (7/8/2014): This is a temporary measure, returning the previous default value in case the digest algorithm was not specified by the user.
		if (canonicalizationMethod == null) {
			return CanonicalizationMethod.EXCLUSIVE;
		}
		return canonicalizationMethod;
	}

	public void setCanonicalizationMethod(String canonicalizationMethod) {
		this.canonicalizationMethod = canonicalizationMethod;
	}

	public String toString() {
		return "TimestampParameters{" +
				", digestAlgorithm=" + digestAlgorithm.getName() +
				", canonicalizationMethod=" + canonicalizationMethod +
				"}";
	}
}
