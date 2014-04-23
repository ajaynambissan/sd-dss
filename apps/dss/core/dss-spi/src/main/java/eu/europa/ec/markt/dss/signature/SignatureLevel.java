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

/**
 * Signature profiles (form+level) handled by the SD-DSS framework.
 *
 * @version $Revision: 3765 $ - $Date: 2014-04-21 08:12:49 +0200 (Mon, 21 Apr 2014) $
 */

public enum SignatureLevel {

	XAdES_C(null), XAdES_X(null), XAdES_XL(null), XAdES_A(null), XAdES_BASELINE_LTA(null), XAdES_BASELINE_LT(XAdES_BASELINE_LTA), XAdES_BASELINE_T( XAdES_BASELINE_LT), XAdES_BASELINE_B(XAdES_BASELINE_T),

	CAdES_BASELINE_LTA(null), CAdES_BASELINE_LT(CAdES_BASELINE_LTA), CAdES_BASELINE_T(CAdES_BASELINE_LT), CAdES_BASELINE_B(CAdES_BASELINE_T), CADES_101733_C(null), CADES_101733_X(null), CADES_101733_A(null),

	PAdES_BASELINE_LTA(null), PAdES_BASELINE_LT(PAdES_BASELINE_LTA), PAdES_BASELINE_T(PAdES_BASELINE_LT), PAdES_BASELINE_B(PAdES_BASELINE_T), PAdES_102778_LTV(PAdES_BASELINE_B),

	ASiC_S_BASELINE_LTA(null), ASiC_S_BASELINE_LT(ASiC_S_BASELINE_LTA), ASiC_S_BASELINE_T(ASiC_S_BASELINE_LT), ASiC_S_BASELINE_B(ASiC_S_BASELINE_T),

	ASiC_E_BASELINE_LTA(null), ASiC_E_BASELINE_LT(ASiC_E_BASELINE_LTA), ASiC_E_BASELINE_T(ASiC_E_BASELINE_LT), ASiC_E_BASELINE_B(ASiC_E_BASELINE_T);

	public final SignatureLevel upperLevel;

	private SignatureLevel(final SignatureLevel upperLevel) {
		this.upperLevel = upperLevel;
	}

	/**
	 * Returns the SignatureLevel based on the name (String)
	 *
	 * @param name
	 * @return
	 */
	public static SignatureLevel valueByName(String name) {
		return valueOf(name.replace("-", "_"));
	}

	@Override
	public String toString() {
		return super.toString().replace("_", "-");
	}
}
