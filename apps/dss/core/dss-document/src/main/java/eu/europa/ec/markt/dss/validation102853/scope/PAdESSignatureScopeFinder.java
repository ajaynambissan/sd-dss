/*
 * DSS - Digital Signature Services
 *
 * Copyright (C) 2014 European Commission, Directorate-General Internal Market and Services (DG MARKT), B-1049 Bruxelles/Brussel
 *
 * Developed by: 2014 ARHS Developments S.A. (rue Nicolas Bové 2B, L-1253 Luxembourg) http://www.arhs-developments.com
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

package eu.europa.ec.markt.dss.validation102853.scope;

import java.util.ArrayList;
import java.util.List;

import eu.europa.ec.markt.dss.signature.pdf.PdfSignatureInfo;
import eu.europa.ec.markt.dss.validation102853.pades.PAdESSignature;

/**
 *
 */
public class PAdESSignatureScopeFinder implements SignatureScopeFinder<PAdESSignature> {

    @Override
    public List<SignatureScope> findSignatureScope(final PAdESSignature pAdESSignature) {

        List<SignatureScope> result = new ArrayList<SignatureScope>();
        final PdfSignatureInfo pdfSignature = pAdESSignature.getPdfSignatureInfo();
        final int outerSignatureSize = pdfSignature.getOuterSignatures().size();
        if (pAdESSignature.hasOuterSignatures()) {
            result.add(new PdfByteRangeSignatureScope("PDF previous version #" + outerSignatureSize, pdfSignature.getSignatureByteRange()));
        } else {
            result.add(new FullSignatureScope("Full PDF"));
        }
        return result;
    }
}
