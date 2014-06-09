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

package eu.europa.ec.markt.dss.validation102853.pades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.EncryptionAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.SignatureLevel;
import eu.europa.ec.markt.dss.signature.pdf.PdfDocTimestampInfo;
import eu.europa.ec.markt.dss.signature.pdf.PdfSignatureInfo;
import eu.europa.ec.markt.dss.signature.pdf.PdfSignatureOrDocTimestampInfo;
import eu.europa.ec.markt.dss.signature.pdf.pdfbox.PdfDssDict;
import eu.europa.ec.markt.dss.validation102853.AdvancedSignature;
import eu.europa.ec.markt.dss.validation102853.CAdESCertificateSource;
import eu.europa.ec.markt.dss.validation102853.CertificatePool;
import eu.europa.ec.markt.dss.validation102853.CertificateToken;
import eu.europa.ec.markt.dss.validation102853.DefaultAdvancedSignature;
import eu.europa.ec.markt.dss.validation102853.SignatureForm;
import eu.europa.ec.markt.dss.validation102853.SignaturePolicy;
import eu.europa.ec.markt.dss.validation102853.TimestampReference;
import eu.europa.ec.markt.dss.validation102853.TimestampToken;
import eu.europa.ec.markt.dss.validation102853.TimestampType;
import eu.europa.ec.markt.dss.validation102853.bean.CandidatesForSigningCertificate;
import eu.europa.ec.markt.dss.validation102853.bean.CertifiedRole;
import eu.europa.ec.markt.dss.validation102853.bean.CommitmentType;
import eu.europa.ec.markt.dss.validation102853.bean.SignatureCryptographicVerification;
import eu.europa.ec.markt.dss.validation102853.bean.SignatureProductionPlace;
import eu.europa.ec.markt.dss.validation102853.cades.CAdESSignature;
import eu.europa.ec.markt.dss.validation102853.certificate.CertificateRef;
import eu.europa.ec.markt.dss.validation102853.crl.CRLRef;
import eu.europa.ec.markt.dss.validation102853.crl.OfflineCRLSource;
import eu.europa.ec.markt.dss.validation102853.ocsp.OCSPRef;
import eu.europa.ec.markt.dss.validation102853.ocsp.OfflineOCSPSource;

/**
 * Implementation of AdvancedSignature for PAdES
 *
 * @version $Revision: 1849 $ - $Date: 2013-04-04 17:51:32 +0200 (Thu, 04 Apr 2013) $
 */
public class PAdESSignature extends DefaultAdvancedSignature {

    private static final Logger LOG = LoggerFactory.getLogger(PAdESSignature.class);

    private final DSSDocument document;
    private final PdfDssDict pdfCatalog;

    private final PdfDssDict outerCatalog;

    private final CAdESSignature cadesSignature;
    private final List<TimestampToken> cadesTimestamps;
    private final List<TimestampToken> cadesArchiveTimestamps;

    private final PdfSignatureInfo pdfSignature;

    private PAdESCertificateSource padesCertSources;

    /**
     * This is the reference to the global (external) pool of certificates. All encapsulated certificates in the signature are added
     * to this pool. See {@link eu.europa.ec.markt.dss.validation102853.CertificatePool}
     */
    private CertificatePool certPool;

    /**
     * This list represents all digest algorithms used to calculate the digest values of certificates.
     */
    private Set<DigestAlgorithm> usedCertificatesDigestAlgorithms = new HashSet<DigestAlgorithm>();

    /**
     * The default constructor for PAdESSignature.
     *
     * @param document
     * @param pdfSignatureInfo
     * @throws eu.europa.ec.markt.dss.exception.DSSException
     */
    protected PAdESSignature(DSSDocument document, final PdfSignatureInfo pdfSignatureInfo, final CertificatePool certPool) throws DSSException {
        this.document = document;
        this.pdfCatalog = pdfSignatureInfo.getDocumentDictionary();
        this.outerCatalog = pdfSignatureInfo.getOuterCatalog();
        this.certPool = certPool;
        this.cadesSignature = pdfSignatureInfo.getCades();
        this.cadesTimestamps = cadesSignature.getSignatureTimestamps();
        this.cadesArchiveTimestamps = cadesSignature.getArchiveTimestamps();
        this.pdfSignature = pdfSignatureInfo;
    }

    @Override
    public SignatureForm getSignatureForm() {

        return SignatureForm.PAdES;
    }

    @Override
    public EncryptionAlgorithm getEncryptionAlgo() {

        return cadesSignature.getEncryptionAlgo();
    }

    @Override
    public DigestAlgorithm getDigestAlgo() {

        return cadesSignature.getDigestAlgo();
    }

    @Override
    public PAdESCertificateSource getCertificateSource() {

        if (padesCertSources == null) {

            CAdESCertificateSource cadesCertSource = cadesSignature.getCertificateSource();
            padesCertSources = new PAdESCertificateSource(getDSSDictionary(), cadesCertSource, certPool);
        }
        return padesCertSources;
    }

    private PdfDssDict getDSSDictionary() {

        PdfDssDict catalog = outerCatalog != null ? outerCatalog : pdfCatalog;
        return catalog;
    }

    @Override
    public OfflineCRLSource getCRLSource() {

        final PdfDssDict dssDictionary = getDSSDictionary();
        final PAdESCRLSource padesCRLSource = new PAdESCRLSource(cadesSignature, dssDictionary);
        return padesCRLSource;
    }

    @Override
    public OfflineOCSPSource getOCSPSource() {

        final PdfDssDict dssDictionary = getDSSDictionary();
        final PAdESOCSPSource padesOCSPSource = new PAdESOCSPSource(cadesSignature, dssDictionary);
        return padesOCSPSource;
    }

    @Override
    public CandidatesForSigningCertificate getCandidatesForSigningCertificate() {

        return cadesSignature.getCandidatesForSigningCertificate();
    }

    @Override
    public Date getSigningTime() {

        Date date = null;
        if (pdfSignature.getSigningDate() != null) {
            date = pdfSignature.getSigningDate();
        }
        if (date == null) {
            return cadesSignature.getSigningTime();
        } else {
            return date;
        }
    }

    @Override
    public SignaturePolicy getPolicyId() {

        return cadesSignature.getPolicyId();
    }

    @Override
    public SignatureProductionPlace getSignatureProductionPlace() {

        String location = pdfSignature.getLocation();
        if (location == null || location.trim().length() == 0) {

            return cadesSignature.getSignatureProductionPlace();
        } else {
            SignatureProductionPlace signatureProductionPlace = new SignatureProductionPlace();
            signatureProductionPlace.setCountryName(location);
            return signatureProductionPlace;
        }
    }

    @Override
    public String getContentType() {

        return "application/pdf";
    }

    @Override
    public String getContentIdentifier() {
        return null;
    }

    @Override
    public String getContentHints() {
        return null;
    }

    @Override
    public String[] getClaimedSignerRoles() {

        return cadesSignature.getClaimedSignerRoles();
    }

    @Override
    public List<CertifiedRole> getCertifiedSignerRoles() {
        return null;
    }

    @Override
    public List<TimestampToken> getContentTimestamps() {

        final List<TimestampToken> contentTimestamps = cadesSignature.getContentTimestamps();
        return contentTimestamps;
    }

    @Override
    public byte[] getContentTimestampData(final TimestampToken timestampToken) {

        final byte[] contentTimestampData = cadesSignature.getContentTimestampData(timestampToken);
        return contentTimestampData;
    }

    @Override
    public List<TimestampToken> getSignatureTimestamps() {

        final List<TimestampToken> result = new ArrayList<TimestampToken>();
        result.addAll(cadesTimestamps);
        final Set<PdfSignatureOrDocTimestampInfo> outerSignatures = pdfSignature.getOuterSignatures();
        for (final PdfSignatureOrDocTimestampInfo outerSignature : outerSignatures) {

            if (outerSignature.isTimestamp() && (outerSignature instanceof PdfDocTimestampInfo)) {

                final PdfDocTimestampInfo pdfBoxTimestampInfo = (PdfDocTimestampInfo) outerSignature;
                // do not return this timestamp if it's an archive timestamp
                if (pdfBoxTimestampInfo.getTimestampToken().getTimeStampType() == TimestampType.SIGNATURE_TIMESTAMP) {

                    result.add(pdfBoxTimestampInfo.getTimestampToken());
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<TimestampToken> getTimestampsX1() {

      /* Not applicable for PAdES */
        return Collections.emptyList();
    }

    @Override
    public List<TimestampToken> getTimestampsX2() {

      /* Not applicable for PAdES */
        return Collections.emptyList();
    }

    @Override
    public List<TimestampToken> getArchiveTimestamps() {
        List<TimestampToken> result = new ArrayList<TimestampToken>();
        result.addAll(cadesArchiveTimestamps);
        final Set<PdfSignatureOrDocTimestampInfo> outerSignatures = pdfSignature.getOuterSignatures();
        for (final PdfSignatureOrDocTimestampInfo outerSignature : outerSignatures) {
            if (outerSignature.isTimestamp() && (outerSignature instanceof PdfDocTimestampInfo)) {
                PdfDocTimestampInfo pdfBoxTimestampInfo = (PdfDocTimestampInfo) outerSignature;
                // return this timestamp if it's an archive timestamp
                if (pdfBoxTimestampInfo.getTimestampToken().getTimeStampType() == TimestampType.ARCHIVE_TIMESTAMP) {
                    result.add(pdfBoxTimestampInfo.getTimestampToken());
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<CertificateToken> getCertificates() {
        return getCertificateSource().getCertificates();
    }

	@Override
	public SignatureCryptographicVerification checkIntegrity(final DSSDocument detachedDocument) {

		return checkIntegrity(detachedDocument, null);
	}
    @Override
    public SignatureCryptographicVerification checkIntegrity(final DSSDocument document, final CertificateToken providedSigningCertificate) {

	    SignatureCryptographicVerification scv = new SignatureCryptographicVerification();
        try {
            scv = pdfSignature.checkIntegrity();
        } catch (Exception e) {
            LOG.warn("Could not check integrity", e);
            scv.setErrorMessage(e.getMessage());
        }
        return scv;
    }

    @Override
    public List<AdvancedSignature> getCounterSignatures() {

      /* Not applicable for PAdES */
        return Collections.emptyList();
    }

    @Override
    public List<CertificateRef> getCertificateRefs() {
        return cadesSignature.getCertificateRefs();
    }

    @Override
    public List<CRLRef> getCRLRefs() {
        return getCAdESSignature().getCRLRefs();
    }

    @Override
    public List<OCSPRef> getOCSPRefs() {
        return cadesSignature.getOCSPRefs();
    }

    @Override
    public byte[] getSignatureTimestampData(final TimestampToken timestampToken) {
        if (cadesTimestamps.contains(timestampToken)) {
            return cadesSignature.getSignatureTimestampData(timestampToken);
        } else {
            for (final PdfSignatureOrDocTimestampInfo signatureInfo : pdfSignature.getOuterSignatures()) {
                if (signatureInfo instanceof PdfDocTimestampInfo) {
                    PdfDocTimestampInfo pdfTimestampInfo = (PdfDocTimestampInfo) signatureInfo;
                    if (pdfTimestampInfo.getTimestampToken().equals(timestampToken)) {
                        final byte[] signedDocumentBytes = pdfTimestampInfo.getSignedDocumentBytes();
                        return signedDocumentBytes;
                    }
                }
            }
        }
        throw new DSSException("Timestamp Data not found");
    }

    @Override
    public byte[] getTimestampX1Data(final TimestampToken timestampToken) {

      /* Not applicable for PAdES */
        return null;
    }

    @Override
    public byte[] getTimestampX2Data(final TimestampToken timestampToken) {

      /* Not applicable for PAdES */
        return null;
    }

    /**
     * @return the CAdES signature underlying this PAdES signature
     */
    public CAdESSignature getCAdESSignature() {

        return cadesSignature;
    }

    @Override
    public byte[] getArchiveTimestampData(TimestampToken timestampToken) {
        if (cadesArchiveTimestamps.contains(timestampToken)) {
            return cadesSignature.getArchiveTimestampData(timestampToken);
        } else {
            for (final PdfSignatureOrDocTimestampInfo signatureInfo : pdfSignature.getOuterSignatures()) {
                if (signatureInfo instanceof PdfDocTimestampInfo) {
                    PdfDocTimestampInfo pdfTimestampInfo = (PdfDocTimestampInfo) signatureInfo;
                    if (pdfTimestampInfo.getTimestampToken().equals(timestampToken)) {
                        final byte[] signedDocumentBytes = pdfTimestampInfo.getSignedDocumentBytes();
                        return signedDocumentBytes;
                    }
                }
            }
        }
        throw new DSSException("Timestamp Data not found");
    }

    @Override
    public String getId() {

        Date signingTime = getSigningTime();
        if (signingTime == null) {

            signingTime = new Date();
        }
        final byte[] timeBytes = Long.toString(signingTime.getTime()).getBytes();

        byte[] certificateBytes;
        final CertificateToken signingCertificateToken = getSigningCertificateToken();
        if (signingCertificateToken == null) {

            certificateBytes = DSSUtils.EMPTY_BYTE_ARRAY;
        } else {

            certificateBytes = signingCertificateToken.getEncoded();
        }
        final byte[] digestValue = DSSUtils.digest(DigestAlgorithm.MD5, timeBytes, certificateBytes);
        return DSSUtils.toHex(digestValue);
    }

    @Override
    public List<TimestampReference> getTimestampedReferences() {

      /* Not applicable for PAdES */
        return Collections.emptyList();
    }

    @Override
    public Set<DigestAlgorithm> getUsedCertificatesDigestAlgorithms() {

        return usedCertificatesDigestAlgorithms;
    }

    public boolean isDataForSignatureLevelPresent(SignatureLevel signatureLevel) {
        boolean dataForLevelPresent = true;
        final List<TimestampToken> signatureTimestamps = getSignatureTimestamps();
        switch (signatureLevel) {
            case PAdES_BASELINE_LTA:
                dataForLevelPresent = hasDocumentTimestampOnTopOfDSSDict();
                dataForLevelPresent &= (((signatureTimestamps != null) && (!signatureTimestamps.isEmpty())));
                break;
            case PAdES_102778_LTV:
                dataForLevelPresent = hasDocumentTimestampOnTopOfDSSDict();
                break;
            case PAdES_BASELINE_LT:
                dataForLevelPresent &= hasDSSDictionary();
                // break omitted purposely
            case PAdES_BASELINE_T:
                dataForLevelPresent &= (((signatureTimestamps != null) && (!signatureTimestamps.isEmpty())));
                // break omitted purposely
            case PAdES_BASELINE_B:
                dataForLevelPresent &= (pdfSignature != null);
                break;
            default:
                throw new IllegalArgumentException("Unknown level " + signatureLevel);
        }
        LOG.debug("Level {} found on document {} = {}", new Object[]{signatureLevel, document.getName(), dataForLevelPresent});
        return dataForLevelPresent;
    }

    public SignatureLevel[] getSignatureLevels() {
        return new SignatureLevel[]{SignatureLevel.PAdES_BASELINE_B, SignatureLevel.PAdES_BASELINE_T, SignatureLevel.PAdES_BASELINE_LT, SignatureLevel.PAdES_102778_LTV, SignatureLevel.PAdES_BASELINE_LTA};
    }

    private boolean hasDSSDictionary() {
        for (final PdfSignatureOrDocTimestampInfo outerSignature : pdfSignature.getOuterSignatures()) {
            if (outerSignature.getDocumentDictionary() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDocumentTimestampOnTopOfDSSDict() {
        for (final PdfSignatureOrDocTimestampInfo outerSignature : pdfSignature.getOuterSignatures()) {
            if (outerSignature.getDocumentDictionary() != null) {
                if (outerSignature.isTimestamp()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public CommitmentType getCommitmentTypeIndication() {
        return cadesSignature.getCommitmentTypeIndication();
    }

    public boolean hasOuterSignatures() {
        return !pdfSignature.getOuterSignatures().isEmpty();
    }

    public PdfSignatureInfo getPdfSignature() {
        return pdfSignature;
    }
}
