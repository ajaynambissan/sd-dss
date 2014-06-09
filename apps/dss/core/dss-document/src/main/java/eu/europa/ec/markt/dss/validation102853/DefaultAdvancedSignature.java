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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.cert.ocsp.BasicOCSPResp;

import eu.europa.ec.markt.dss.DSSRevocationUtils;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.signature.SignatureLevel;
import eu.europa.ec.markt.dss.validation102853.bean.CandidatesForSigningCertificate;
import eu.europa.ec.markt.dss.validation102853.bean.SigningCertificateValidity;
import eu.europa.ec.markt.dss.validation102853.crl.CRLToken;
import eu.europa.ec.markt.dss.validation102853.crl.ListCRLSource;
import eu.europa.ec.markt.dss.validation102853.crl.OfflineCRLSource;
import eu.europa.ec.markt.dss.validation102853.ocsp.ListOCSPSource;
import eu.europa.ec.markt.dss.validation102853.ocsp.OfflineOCSPSource;
import eu.europa.ec.markt.dss.validation102853.scope.SignatureScope;

/**
 * TODO <p/> <p/> DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public abstract class DefaultAdvancedSignature implements AdvancedSignature {

	/**
	 * This variable is used to ensure the uniqueness of the signature in the same document.
	 */
	protected static int signatureCounter = 0;

	/**
	 * The reference to the object containing all candidates to the signing certificate.
	 */
	protected CandidatesForSigningCertificate candidatesForSigningCertificate;
	/**
	 * This list contains the detail information collected during the check. It is reset for each call of {@code isDataForSignatureLevelPresent}
	 */
	protected List<String> info;

	// Enclosed content timestamps.
	protected List<TimestampToken> contentTimestamps;

	// Enclosed signature timestamps.
	protected List<TimestampToken> signatureTimestamps;

	// Enclosed SignAndRefs timestamps.
	protected List<TimestampToken> sigAndRefsTimestamps;

	// Enclosed RefsOnly timestamps.
	protected List<TimestampToken> refsOnlyTimestamps;

	// This variable contains the list of enclosed archive signature timestamps.
	protected List<TimestampToken> archiveTimestamps;

    /**
     * The scope of the signature (full document, parts of documents, etc).
     */
    private SignatureScope signatureScope;

	/**
	 * @return the upper level for which data have been found. Doesn't mean any validity of the data found. Null if unknown.
	 */
	@Override
	public SignatureLevel getDataFoundUpToLevel() {
		final SignatureLevel[] signatureLevels = getSignatureLevels();
		final SignatureLevel dataFoundUpToProfile = getDataFoundUpToProfile(signatureLevels);
		return dataFoundUpToProfile;
	}

	private SignatureLevel getDataFoundUpToProfile(SignatureLevel... signatureLevels) {

		for (int ii = signatureLevels.length - 1; ii >= 0; ii--) {

			final SignatureLevel signatureLevel = signatureLevels[ii];
			if (isDataForSignatureLevelPresent(signatureLevel)) {
				return signatureLevel;
			}
		}
		return null;
	}

	/**
	 * This method validates the signing certificate and all timestamps.
	 *
	 * @return signature validation context containing all certificates and revocation data used during the validation process.
	 */
	public ValidationContext getSignatureValidationContext(final CertificateVerifier certificateVerifier) {

		final ValidationContext validationContext = new SignatureValidationContext();
		final CertificateToken signingCertificateToken = getSigningCertificateToken();
		validationContext.addCertificateTokenForVerification(signingCertificateToken);
		prepareTimestamps(validationContext);
		certificateVerifier.setSignatureCRLSource(new ListCRLSource(getCRLSource()));
		certificateVerifier.setSignatureOCSPSource(new ListOCSPSource(getOCSPSource()));
		validationContext.initialize(certificateVerifier);
		validationContext.validate();
		return validationContext;
	}

	/**
	 * This method returns all certificates used during the validation process. If a certificate is already present within the signature then it is ignored.
	 *
	 * @param validationContext validation context containing all information about the validation process of the signing certificate and time-stamps
	 * @return set of certificates not yet present within the signature
	 */
	public Set<CertificateToken> getCertificatesForInclusion(final ValidationContext validationContext) {

		final Set<CertificateToken> certificates = new HashSet<CertificateToken>();
		final List<CertificateToken> certWithinSignatures = getCertificatesWithinSignatureAndTimestamps();
		for (final CertificateToken certificateToken : validationContext.getProcessedCertificates()) {
			if (certWithinSignatures.contains(certificateToken)) {
				continue;
			}
			certificates.add(certificateToken);
		}
		return certificates;
	}

	public List<CertificateToken> getCertificatesWithinSignatureAndTimestamps() {
		final List<CertificateToken> certWithinSignatures = new ArrayList<CertificateToken>();
		certWithinSignatures.addAll(getCertificates());
		//TODO (2013-12-11 Nicolas -> Bob): Create a convenient method to get all the timestamptokens // to get all the certificates
		for (final TimestampToken timestampToken : getSignatureTimestamps()) {
			certWithinSignatures.addAll(timestampToken.getCertificates());
		}
		for (final TimestampToken timestampToken : getArchiveTimestamps()) {
			certWithinSignatures.addAll(timestampToken.getCertificates());
		}
		for (final TimestampToken timestampToken : getContentTimestamps()) {
			certWithinSignatures.addAll(timestampToken.getCertificates());
		}
		for (final TimestampToken timestampToken : getTimestampsX1()) {
			certWithinSignatures.addAll(timestampToken.getCertificates());
		}
		for (final TimestampToken timestampToken : getTimestampsX2()) {
			certWithinSignatures.addAll(timestampToken.getCertificates());
		}
		return certWithinSignatures;
	}

	/**
	 * This method returns revocation values (ocsp and crl) that will be included in the LT profile
	 *
	 * @param validationContext
	 * @return
	 */
	public RevocationDataForInclusion getRevocationDataForInclusion(final ValidationContext validationContext) {

		//TODO: there can be also CRL and OCSP in TimestampToken CMS data
		final Set<RevocationToken> revocationTokens = validationContext.getProcessedRevocations();
		final OfflineCRLSource crlSource = getCRLSource();
		final List<CRLToken> containedCRLs = crlSource.getContainedCRLTokens();
		final OfflineOCSPSource ocspSource = getOCSPSource();
		final List<BasicOCSPResp> containedBasicOCSPResponses = ocspSource.getContainedOCSPResponses();
		final List<CRLToken> crlTokens = new ArrayList<CRLToken>();
		final List<OCSPToken> ocspTokens = new ArrayList<OCSPToken>();
		for (final RevocationToken revocationToken : revocationTokens) {

			if (revocationToken instanceof CRLToken) {

				final boolean tokenIn = containedCRLs.contains(revocationToken);
				if (!tokenIn) {

					final CRLToken crlToken = (CRLToken) revocationToken;
					crlTokens.add(crlToken);
				}
			} else if (revocationToken instanceof OCSPToken) {

				final boolean tokenIn = DSSRevocationUtils.isTokenIn(revocationToken, containedBasicOCSPResponses);
				if (!tokenIn) {

					final OCSPToken ocspToken = (OCSPToken) revocationToken;
					ocspTokens.add(ocspToken);
				}
			} else {
				throw new DSSException("Unknown type for revocationToken: " + revocationToken.getClass().getName());
			}
		}
		return new RevocationDataForInclusion(crlTokens, ocspTokens);
	}

	/**
	 * This list contains the detail information collected during the check. It is reset for each call.
	 *
	 * @return
	 */
	@Override
	public List<String> getInfo() {

		return Collections.unmodifiableList(info);
	}

	public static class RevocationDataForInclusion {

		public final List<CRLToken> crlTokens;
		public final List<OCSPToken> ocspTokens;

		public RevocationDataForInclusion(final List<CRLToken> crlTokens, final List<OCSPToken> ocspTokens) {

			this.crlTokens = crlTokens;
			this.ocspTokens = ocspTokens;
		}

		public boolean isEmpty() {

			return crlTokens.isEmpty() && ocspTokens.isEmpty();
		}
	}

	@Override
	public CertificateToken getSigningCertificateToken() {

		candidatesForSigningCertificate = getCandidatesForSigningCertificate();
		final SigningCertificateValidity theSigningCertificateValidity = candidatesForSigningCertificate.getTheSigningCertificateValidity();
		if (theSigningCertificateValidity != null) {

			if (theSigningCertificateValidity.isValid()) {

				final CertificateToken signingCertificateToken = theSigningCertificateValidity.getCertificateToken();
				return signingCertificateToken;
			}
		}
		final SigningCertificateValidity theBestCandidate = candidatesForSigningCertificate.getTheBestCandidate();
		return theBestCandidate == null ? null : theBestCandidate.getCertificateToken();
	}

	/**
	 * This method adds all timestamps to be validated.
	 *
	 * @param validationContext validationContext to which the timestamps must be added
	 */
	@Override
	public void prepareTimestamps(final ValidationContext validationContext) {

		// TODO: to be restored
		// this.timestampedReferences = getTimestampedReferences();

        /*
	     * This validates the signature timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getContentTimestamps()) {

			validationContext.addTimestampTokenForVerification(timestampToken);
		}

        /*
         * This validates the signature timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getSignatureTimestamps()) {
			validationContext.addTimestampTokenForVerification(timestampToken);
		}

        /*
         * This validates the SigAndRefs timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getTimestampsX1()) {
			validationContext.addTimestampTokenForVerification(timestampToken);
		}

        /*
         * This validates the RefsOnly timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getTimestampsX2()) {
			validationContext.addTimestampTokenForVerification(timestampToken);
		}

        /*
         * This validates the archive timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getArchiveTimestamps()) {
			validationContext.addTimestampTokenForVerification(timestampToken);
		}
	}

	/**
	 * This method adds all timestamps to be validated.
	 *
	 */
	@Override
	public void validateTimestamps() {

        /*
	     * This validates the signature timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getContentTimestamps()) {

			final byte[] timestampBytes = getContentTimestampData(timestampToken);
			timestampToken.matchData(timestampBytes);
		}

        /*
         * This validates the signature timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getSignatureTimestamps()) {

			final byte[] timestampBytes = getSignatureTimestampData(timestampToken);
			timestampToken.matchData(timestampBytes);
		}

        /*
         * This validates the SigAndRefs timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getTimestampsX1()) {

			final byte[] timestampBytes = getTimestampX1Data(timestampToken);
			timestampToken.matchData(timestampBytes);
		}

        /*
         * This validates the RefsOnly timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getTimestampsX2()) {

			final byte[] timestampBytes = getTimestampX2Data(timestampToken);
			timestampToken.matchData(timestampBytes);
		}

        /*
         * This validates the archive timestamp tokensToProcess present in the signature.
         */
		for (final TimestampToken timestampToken : getArchiveTimestamps()) {

			final byte[] timestampData = getArchiveTimestampData(timestampToken);
			timestampToken.matchData(timestampData);
		}
	}
}

