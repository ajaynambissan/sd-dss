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

package eu.europa.ec.markt.dss.validation102853.xades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.SignedInfo;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.tsp.TimeStampToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.europa.ec.markt.dss.DSSUtils;
import eu.europa.ec.markt.dss.DSSXMLUtils;
import eu.europa.ec.markt.dss.DigestAlgorithm;
import eu.europa.ec.markt.dss.EncryptionAlgorithm;
import eu.europa.ec.markt.dss.SignatureAlgorithm;
import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.exception.DSSNotETSICompliantException;
import eu.europa.ec.markt.dss.exception.DSSNullReturnedException;
import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.signature.SignatureLevel;
import eu.europa.ec.markt.dss.validation102853.crl.CRLRef;
import eu.europa.ec.markt.dss.validation102853.certificate.CertificateRef;
import eu.europa.ec.markt.dss.validation102853.ocsp.OCSPRef;
import eu.europa.ec.markt.dss.validation102853.SignatureForm;
import eu.europa.ec.markt.dss.validation102853.SignaturePolicy;
import eu.europa.ec.markt.dss.validation102853.crl.OfflineCRLSource;
import eu.europa.ec.markt.dss.validation102853.ocsp.OfflineOCSPSource;
import eu.europa.ec.markt.dss.validation102853.AdvancedSignature;
import eu.europa.ec.markt.dss.validation102853.ArchiveTimestampType;
import eu.europa.ec.markt.dss.validation102853.CertificatePool;
import eu.europa.ec.markt.dss.validation102853.CertificateToken;
import eu.europa.ec.markt.dss.validation102853.DefaultAdvancedSignature;
import eu.europa.ec.markt.dss.validation102853.TimestampReference;
import eu.europa.ec.markt.dss.validation102853.TimestampReferenceCategory;
import eu.europa.ec.markt.dss.validation102853.TimestampToken;
import eu.europa.ec.markt.dss.validation102853.TimestampType;
import eu.europa.ec.markt.dss.validation102853.bean.CertifiedRole;
import eu.europa.ec.markt.dss.validation102853.bean.CommitmentType;
import eu.europa.ec.markt.dss.validation102853.bean.SignatureCryptographicVerification;
import eu.europa.ec.markt.dss.validation102853.bean.SignatureProductionPlace;
import eu.europa.ec.markt.dss.validation102853.bean.SigningCertificateValidity;
import eu.europa.ec.markt.dss.validation102853.toolbox.XPointerResourceResolver;

// import javax.xml.crypto.dsig.XMLSignature;
// import javax.xml.crypto.dsig.XMLSignatureException;
// import javax.xml.crypto.dsig.XMLSignatureFactory;

/**
 * Parse an XAdES structure
 *
 * @version $Revision: 1825 $ - $Date: 2013-03-28 15:57:37 +0100 (Thu, 28 Mar 2013) $
 */

public class XAdESSignature extends DefaultAdvancedSignature {

    private static final Logger LOG = LoggerFactory.getLogger(XAdESSignature.class);

    /**
     * This variable contains the list of {@code XPathQueryHolder} adapted to the specific signature schema.
     */
    private final List<XPathQueryHolder> xPathQueryHolders;

    /**
     * This variable contains the XPathQueryHolder adapted to the signature schema.
     */
    protected XPathQueryHolder xPathQueryHolder;

    /**
     * This is the default canonicalization method for XMLDSIG used for timestamps. Another complication arises because of the way that the default canonicalization algorithm
     * handles namespace declarations; frequently a signed XML document needs to be embedded in another document; in this case the original canonicalization algorithm will not
     * yield the same result as if the document is treated alone. For this reason, the so-called Exclusive Canonicalization, which serializes XML namespace declarations
     * independently of the surrounding XML, was created.
     */
    public static final String DEFAULT_TIMESTAMP_CREATION_CANONICALIZATION_METHOD = CanonicalizationMethod.EXCLUSIVE;
    public static final String DEFAULT_TIMESTAMP_VALIDATION_CANONICALIZATION_METHOD = CanonicalizationMethod.INCLUSIVE;

    private final Element signatureElement;

    /**
     * Indicates the id of the signature. If not existing this attribute is auto calculated.
     */
    private String signatureId;

    private XAdESCertificateSource certificatesSource;

    /**
     * This is the reference to the global (external) pool of certificates. All encapsulated certificates in the signature are added to this pool. See {@link
     * eu.europa.ec.markt.dss.validation102853.CertificatePool}
     */
    private CertificatePool certPool;

    /**
     * This attribute is used when validate the ArchiveTimeStamp (XAdES-A).
     */
    private ByteArrayOutputStream referencesDigestOutputStream = new ByteArrayOutputStream();

    /**
     * This list represents all digest algorithms used to calculate the digest values of certificates.
     */
    private Set<DigestAlgorithm> usedCertificatesDigestAlgorithms = new HashSet<DigestAlgorithm>();

    static {

        Init.init();

        /**
         * Adds the support of ECDSA_RIPEMD160 for XML signature. Used by AT.
         * The BC provider must be previously added.
         */
        final JCEMapper.Algorithm algorithm = new JCEMapper.Algorithm("", SignatureAlgorithm.ECDSA_RIPEMD160.getJCEId(), "Signature");
        final String xmlId = SignatureAlgorithm.ECDSA_RIPEMD160.getXMLId();
        JCEMapper.register(xmlId, algorithm);
        try {
            org.apache.xml.security.algorithms.SignatureAlgorithm.register(xmlId, SignatureECDSARIPEMD160.class);
        } catch (Exception e) {
            LOG.error("ECDSA_RIPEMD160 algorithm initialisation failed.", e);
        }

        /**
         * Adds the support of not standard algorithm name: http://www.w3.org/2001/04/xmldsig-more/rsa-ripemd160. Used by some AT signature providers.
         * The BC provider must be previously added.
         */

        final JCEMapper.Algorithm notStandardAlgorithm = new JCEMapper.Algorithm("", SignatureAlgorithm.RSA_RIPEMD160.getJCEId(), "Signature");
        JCEMapper.register(SignatureRSARIPEMD160AT.XML_ID, notStandardAlgorithm);
        try {
            org.apache.xml.security.algorithms.SignatureAlgorithm.register(SignatureRSARIPEMD160AT.XML_ID, SignatureRSARIPEMD160AT.class);
        } catch (Exception e) {
            LOG.error("ECDSA_RIPEMD160AT algorithm initialisation failed.", e);
        }
    }

    /**
     * This constructor is used when creating the signature. The default {@code XPathQueryHolder} is set.
     *
     * @param signatureElement w3c.dom <ds:Signature> element
     * @param certPool         can be null
     */
    public XAdESSignature(final Element signatureElement, final CertificatePool certPool) {

        this(signatureElement, (new ArrayList<XPathQueryHolder>() {{
            add(new XPathQueryHolder());
        }}), certPool);
    }

    /**
     * The default constructor for XAdESSignature.
     *
     * @param signatureElement  w3c.dom <ds:Signature> element
     * @param xPathQueryHolders List of {@code XPathQueryHolder} to use when handling signature
     * @param certPool          can be null
     */
    public XAdESSignature(final Element signatureElement, final List<XPathQueryHolder> xPathQueryHolders, final CertificatePool certPool) {

        if (signatureElement == null) {

            throw new DSSException("DOM signature element is null, it must be provided!");
        }
        this.signatureElement = signatureElement;
        this.xPathQueryHolders = xPathQueryHolders;
        this.certPool = certPool;
        initialiseSettings();
    }

    /**
     * This method os called when creating a new instance of the {@code XAdESSignature} with unknown schema.
     */
    private void initialiseSettings() {

        recursiveNamespaceBrowser(signatureElement);
        if (xPathQueryHolder == null) {

            throw new DSSException("There is no suitable XPathQueryHolder to manage the signature.");
        }
    }

    /**
     * This method sets the namespace which will determinate the {@code XPathQueryHolder} to use. The content of the Transform element is ignored.
     *
     * @param element
     */
    public void recursiveNamespaceBrowser(final Element element) {

        for (int ii = 0; ii < element.getChildNodes().getLength(); ii++) {

            final Node node = element.getChildNodes().item(ii);
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                final Element childElement = (Element) node;
                final String namespaceURI = childElement.getNamespaceURI();
                // final String tagName = childElement.getTagName();
                final String localName = childElement.getLocalName();
                // final String nodeName = childElement.getNodeName();
                // System.out.println(tagName + "-->" + namespaceURI);
                if (XPathQueryHolder.XMLE_TRANSFORM.equals(localName) && XPathQueryHolder.XMLDSIG_NAMESPACE.equals(namespaceURI)) {
                    continue;
                } else if (XPathQueryHolder.XMLE_QUALIFYING_PROPERTIES.equals(localName)) {

                    setXPathQueryHolder(namespaceURI);
                    return;
                }
                recursiveNamespaceBrowser(childElement);
            }
        }
    }

    private void setXPathQueryHolder(String namespaceURI) {

        for (final XPathQueryHolder xPathQueryHolder : xPathQueryHolders) {

            final boolean canUseThisXPathQueryHolder = xPathQueryHolder.canUseThisXPathQueryHolder(namespaceURI);
            if (canUseThisXPathQueryHolder) {

                this.xPathQueryHolder = xPathQueryHolder;
            }
        }
    }

    /**
     * This getter returns the {@code XPathQueryHolder}
     *
     * @return
     */
    public XPathQueryHolder getXPathQueryHolder() {
        return xPathQueryHolder;
    }

    /**
     * This method returns the certificate pool used by this instance to handle encapsulated certificates.
     *
     * @return
     */
    public CertificatePool getCertPool() {
        return certPool;
    }

    /**
     * Returns the w3c.dom encapsulated signature element.
     *
     * @return the signatureElement
     */
    public Element getSignatureElement() {

        return signatureElement;
    }

    @Override
    public SignatureForm getSignatureForm() {

        return SignatureForm.XAdES;
    }

    @Override
    public EncryptionAlgorithm getEncryptionAlgo() {

        final String xmlName = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_METHOD).getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
        final SignatureAlgorithm signatureAlgo = SignatureAlgorithm.forXML(xmlName, null);
        if (signatureAlgo == null) {
            return null;
        }
        return signatureAlgo.getEncryptionAlgo();
    }

    @Override
    public DigestAlgorithm getDigestAlgo() {

        final String xmlName = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_METHOD).getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
        final SignatureAlgorithm signatureAlgo = SignatureAlgorithm.forXML(xmlName, null);
        if (signatureAlgo == null) {
            return null;
        }
        return signatureAlgo.getDigestAlgo();
    }

    @Override
    public XAdESCertificateSource getCertificateSource() {

        if (certificatesSource == null) {

            certificatesSource = new XAdESCertificateSource(signatureElement, xPathQueryHolder, certPool);
        }
        return certificatesSource;
    }

    /**
     * This method resets the source of certificates. It must be called when any certificate is added to the KeyInfo or CertificateValues.
     */
    public void resetSources() {

        certificatesSource = null;
    }

    @Override
    public OfflineCRLSource getCRLSource() {

        final XAdESCRLSource xadesCRLSource = new XAdESCRLSource(signatureElement, xPathQueryHolder);
        return xadesCRLSource;
    }

    @Override
    public OfflineOCSPSource getOCSPSource() {

        final XAdESOCSPSource xadesOCSPSource = new XAdESOCSPSource(signatureElement, xPathQueryHolder);
        return xadesOCSPSource;
    }

    @Override
    public SigningCertificateValidity getSigningCertificateValidity() {

        if (signingCertificateValidity == null) {

            signingCertificateValidity = new SigningCertificateValidity();
            /**
             * The ../SignedProperties/SignedSignatureProperties/SigningCertificate element MAY contain references and
             * digests values of other certificates (that MAY form a chain up to the point of trust).
             */

            final NodeList list = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_SIGNING_CERTIFICATE_CERT);
            for (int ii = 0; ii < list.getLength(); ii++) {

                final Element element = (Element) list.item(ii);

                final Element digestMethodEl = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__DIGEST_METHOD);
                if (digestMethodEl == null) {
                    continue;
                }
                final String xmlAlgoName = digestMethodEl.getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
                final DigestAlgorithm digestAlgorithm = DigestAlgorithm.forXML(xmlAlgoName);

                final Element digestValueEl = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__CERT_DIGEST_DIGEST_VALUE);
                if (digestValueEl == null) {
                    continue;
                }
                final byte[] storedBase64DigestValue = DSSUtils.base64StringToBase64Binary(digestValueEl.getTextContent());

                /**
                 * 5.1.4.1 XAdES processing<br>
                 * <i>Candidates for the signing certificate extracted from ds:KeyInfo element</i> shall be checked
                 * against all references present in the ds:SigningCertificate property, if present, since one of these
                 * references shall be a reference to the signing certificate.
                 */
                final XAdESCertificateSource certSource = getCertificateSource();
                for (final CertificateToken token : certSource.getKeyInfoCertificates()) {

                    /**
                     * Step 1:<br>
                     * Take the first child of the property and check that the content of ds:DigestValue matches the
                     * result of digesting <i>the candidate for</i> the signing certificate with the algorithm indicated
                     * in ds:DigestMethod. If they do not match, take the next child and repeat this step until a matching
                     * child element has been found or all children of the element have been checked. If they do match,
                     * continue with step 2. If the last element is reached without finding any match, the validation of
                     * this property shall be taken as failed and INVALID/FORMAT_FAILURE is returned.
                     */
                    final byte[] recalculatedBase64DigestValue;
                    if (digestAlgorithm.equals(DigestAlgorithm.RIPEMD160)) {

                        final RIPEMD160Digest digest = new RIPEMD160Digest();
                        final byte[] message = token.getEncoded();
                        digest.update(message, 0, message.length);
                        final byte[] digestValue = new byte[digest.getDigestSize()];
                        digest.doFinal(digestValue, 0);
                        recalculatedBase64DigestValue = DSSUtils.base64BinaryEncode(digestValue);
                    } else {

                        final byte[] digest = DSSUtils.digest(digestAlgorithm, token.getEncoded());
                        recalculatedBase64DigestValue = DSSUtils.base64BinaryEncode(digest);
                    }
                    signingCertificateValidity.setDigestMatch(false);
                    if (Arrays.equals(recalculatedBase64DigestValue, storedBase64DigestValue)) {

                        final Element issuerNameEl = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__X509_ISSUER_NAME);
                        final X500Principal issuerName = new X500Principal(issuerNameEl.getTextContent());
                        final X500Principal candidateIssuerName = token.getIssuerX500Principal();

                        // final boolean issuerNameMatches = candidateIssuerName.equals(issuerName);
                        final boolean issuerNameMatches = DSSUtils.equals(candidateIssuerName, issuerName);
                        if (!issuerNameMatches) {

                            final String c14nCandidateIssuerName = candidateIssuerName.getName(X500Principal.CANONICAL);
                            LOG.info("candidateIssuerName: " + c14nCandidateIssuerName);
                            final String c14nIssuerName = issuerName.getName(X500Principal.CANONICAL);
                            LOG.info("issuerName         : " + c14nIssuerName);
                        }

                        final Element serialNumberEl = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__X509_SERIAL_NUMBER);
                        final BigInteger serialNumber = new BigInteger(serialNumberEl.getTextContent());
                        final BigInteger candidateSerialNumber = token.getSerialNumber();
                        final boolean serialNumberMatches = candidateSerialNumber.equals(serialNumber);

                        signingCertificateValidity.setDigestMatch(true);
                        signingCertificateValidity.setSerialNumberMatch(serialNumberMatches);
                        signingCertificateValidity.setNameMatch(issuerNameMatches);
                        signingCertificateValidity.setCertToken(token);
                        return signingCertificateValidity;
                    }
                }
            }
        }
        return signingCertificateValidity;
    }

    @Override
    public Date getSigningTime() {

        try {

            final Element signingTimeEl = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNING_TIME);
            if (signingTimeEl == null) {
                return null;
            }
            final String text = signingTimeEl.getTextContent();
            final DatatypeFactory factory = DatatypeFactory.newInstance();
            final XMLGregorianCalendar cal = factory.newXMLGregorianCalendar(text);
            return cal.toGregorianCalendar().getTime();
        } catch (DOMException e) {
            throw new RuntimeException(e);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SignaturePolicy getPolicyId() {

        final Element policyIdentifier = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_POLICY_IDENTIFIER);
        if (policyIdentifier != null) {

         /* There is a policy */
            final Element policyId = DSSXMLUtils.getElement(policyIdentifier, xPathQueryHolder.XPATH__POLICY_ID);
            if (policyId != null) {
            /* Explicit policy */
                final String policyIdString = policyId.getTextContent();
                final SignaturePolicy signaturePolicy = new SignaturePolicy(policyIdString);
                final Node policyDigestMethod = DSSXMLUtils.getNode(policyIdentifier, xPathQueryHolder.XPATH__POLICY_DIGEST_METHOD);
                final String policyDigestMethodString = policyDigestMethod.getTextContent();
                final DigestAlgorithm digestAlgorithm = DigestAlgorithm.forXML(policyDigestMethodString);
                signaturePolicy.setDigestAlgorithm(digestAlgorithm);
                final Element policyDigestValue = DSSXMLUtils.getElement(policyIdentifier, xPathQueryHolder.XPATH__POLICY_DIGEST_VALUE);
                final String digestValue = policyDigestValue.getTextContent().trim();
                signaturePolicy.setDigestValue(digestValue);
                return signaturePolicy;
            } else {
                /* Implicit policy */
                final Element signaturePolicyImplied = DSSXMLUtils.getElement(policyIdentifier, xPathQueryHolder.XPATH__SIGNATURE_POLICY_IMPLIED);
                if (signaturePolicyImplied != null) {
                    return new SignaturePolicy();
                }
            }
        }
        return null;

    }

    @Override
    public SignatureProductionPlace getSignatureProductionPlace() {

        final NodeList list = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_PRODUCTION_PLACE);
        if (list.getLength() == 0) {

            return null;
        }
        final SignatureProductionPlace signatureProductionPlace = new SignatureProductionPlace();
        for (int ii = 0; ii < list.getLength(); ii++) {

            final String name = list.item(ii).getNodeName();
            if (XPathQueryHolder.XMLE_CITY.equals(name)) {

                signatureProductionPlace.setCity(name);
            } else if (XPathQueryHolder.XMLE_STATE_OR_PROVINCE.equals(name)) {

                signatureProductionPlace.setStateOrProvince(name);
            } else if (XPathQueryHolder.XMLE_POSTAL_CODE.equals(name)) {

                signatureProductionPlace.setPostalCode(name);
            } else if (XPathQueryHolder.XMLE_COUNTRY_NAME.equals(name)) {

                signatureProductionPlace.setCountryName(name);
            }
        }
        return signatureProductionPlace;
    }

    @Override
    public String[] getClaimedSignerRoles() {

        final NodeList nodeList = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_CLAIMED_ROLE);
        if (nodeList.getLength() == 0) {

            return null;
        }
        final String[] roles = new String[nodeList.getLength()];
        for (int ii = 0; ii < nodeList.getLength(); ii++) {

            roles[ii] = nodeList.item(ii).getTextContent();
        }
        return roles;
    }

    @Override
    public List<CertifiedRole> getCertifiedSignerRoles() {

        /**
         * <!-- Start EncapsulatedPKIDataType-->
         * <xsd:element name="EncapsulatedPKIData" type="EncapsulatedPKIDataType"/>
         * <xsd:complexType name="EncapsulatedPKIDataType">
         * <xsd:simpleContent>
         * <xsd:extension base="xsd:base-64Binary">
         * <xsd:attribute name="Id" type="xsd:ID" use="optional"/>
         * <xsd:attribute name="Encoding" type="xsd:anyURI" use="optional"/>
         * </xsd:extension>
         * </xsd:simpleContent>
         * </xsd:complexType>
         * <!-- End EncapsulatedPKIDataType -->
         */
        final NodeList nodeList = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_CERTIFIED_ROLE);
        if (nodeList.getLength() == 0) {

            return null;
        }
        final List<CertifiedRole> roles = new ArrayList<CertifiedRole>();
        for (int ii = 0; ii < nodeList.getLength(); ii++) {

            final Element certEl = (Element) nodeList.item(ii);
            final String textContent = certEl.getTextContent();
            final X509Certificate x509Certificate = DSSUtils.loadCertificateFromBase64EncodedString(textContent);
            if (!roles.contains(x509Certificate)) {

                roles.add(new CertifiedRole());
            }
        }
        return roles;
    }

    @Override
    public String getContentType() {

        return "text/xml";
    }

    @Override
    public String getContentIdentifier() {
        return null;
    }

    @Override
    public String getContentHints() {
        return null;
    }

    private TimestampToken makeTimestampToken(int id, Element element, TimestampType timestampType) throws DSSException {

        final Element timestampTokenNode = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__ENCAPSULATED_TIMESTAMP);
        try {

            final String textContent = timestampTokenNode.getTextContent();
            final byte[] tokenBytes = DSSUtils.base64Decode(textContent);
            final CMSSignedData signedData = new CMSSignedData(tokenBytes);
            final TimeStampToken timeStampToken = new TimeStampToken(signedData);
            final TimestampToken timestampToken = new TimestampToken(timeStampToken, timestampType, certPool);
            timestampToken.setDSSId(id);
            timestampToken.setHashCode(element.hashCode());
            return timestampToken;
        } catch (Exception e) {

            throw new DSSException(e);
        }
    }

    public Node getSignatureValue() {

        return DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_VALUE);
    }

    public Element getObject() {

        return DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_OBJECT);
    }

    /**
     * This method returns the list of ds:Object elements for the current signature element.
     *
     * @return
     */
    public NodeList getObjects() {

        return DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_OBJECT);
    }

    public Element getCompleteCertificateRefs() {

        return DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_CERTIFICATE_REFS);
    }

    public Element getCompleteRevocationRefs() {

        return DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_REVOCATION_REFS);
    }

    public NodeList getSigAndRefsTimeStamp() {

        return DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_SIG_AND_REFS_TIMESTAMP);
    }

    public Element getCertificateValues() {

        return DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_CERTIFICATE_VALUES);
    }

    public Element getRevocationValues() {

        return DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_REVOCATION_VALUES);
    }

    /**
     * Checks the presence of ... segment in the signature, what is the proof -B profile existence
     *
     * @return
     */
    public boolean hasBProfile() {

        final int count = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_SIGNED_SIGNATURE_PROPERTIES);
        return count > 0;
    }

    /**
     * Checks the presence of SignatureTimeStamp segment in the signature, what is the proof -T profile existence
     *
     * @return
     */
    public boolean hasTProfile() {

        final int count = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_SIGNATURE_TIMESTAMP);
        return count > 0;
    }

    /**
     * Checks the presence of CompleteCertificateRefs & CompleteRevocationRefs segments in the signature, what is the proof -C profile existence
     *
     * @return
     */
    public boolean hasCProfile() {

        final boolean certRefs = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_COMPLETE_CERTIFICATE_REFS) > 0;
        final boolean revocationRefs = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_COMPLETE_REVOCATION_REFS) > 0;
        return certRefs || revocationRefs;
    }

    /**
     * Checks the presence of SigAndRefsTimeStamp segment in the signature, what is the proof -X profile existence
     *
     * @return true if the -X extension is present
     */
    public boolean hasXProfile() {

        boolean signAndRefs = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_SIG_AND_REFS_TIMESTAMP) > 0;
        return signAndRefs;
    }

    /**
     * Checks the presence of CertificateValues and RevocationValues segments in the signature, what is the proof -XL profile existence
     *
     * @return true if -XL extension is present
     */
    public boolean hasXLProfile() {

        final boolean certValues = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_CERTIFICATE_VALUES) > 0;
        final boolean revocationValues = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_REVOCATION_VALUES) > 0;
        return certValues || revocationValues;
    }

    /**
     * Checks the presence of CertificateValues and RevocationValues segments in the signature, what is the proof -LT profile existence
     *
     * @return true if -LT extension is present
     */
    public boolean hasLTProfile() {

        return hasXLProfile();
    }

    /**
     * Checks the presence of CertificateValues and RevocationValues segments in the signature, what is the proof -A profile existence
     *
     * @return true if -A extension is present
     */
    public boolean hasAProfile() {

        final boolean archiveTimestamp = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_ARCHIVE_TIMESTAMP) > 0;
        final boolean archiveTimestamp141 = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_ARCHIVE_TIMESTAMP_141) > 0;
        final boolean archiveTimestampV2 = DSSXMLUtils.count(signatureElement, xPathQueryHolder.XPATH_COUNT_ARCHIVE_TIMESTAMP_V2) > 0;
        return archiveTimestamp || archiveTimestamp141 || archiveTimestampV2;
    }

    /**
     * Checks the presence of CertificateValues and RevocationValues segments in the signature, what is the proof -LTA profile existence
     *
     * @return true if -LTA extension is present
     */
    public boolean hasLTAProfile() {

        return hasAProfile();
    }

    @Override
    public List<TimestampToken> getContentTimestamps() {

        final List<TimestampToken> contentTimestamps = new ArrayList<TimestampToken>();
        final NodeList timestampsNodes = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_ALL_DATA_OBJECT_TIMESTAMP);
        for (int ii = 0; ii < timestampsNodes.getLength(); ii++) {

            final TimestampToken timestampToken = makeTimestampToken(ii, (Element) timestampsNodes.item(ii), TimestampType.CONTENT_TIMESTAMP);
            if (timestampToken != null) {

                contentTimestamps.add(timestampToken);
            }
        }

        return contentTimestamps;
    }

    @Override
    public byte[] getContentTimestampData(final TimestampToken timestampToken) {
        return null;
    }

    @Override
    public List<TimestampToken> getSignatureTimestamps() {

        final List<TimestampToken> signatureTimestamps = new ArrayList<TimestampToken>();
        final NodeList timestampsNodes = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_TIMESTAMP);
        for (int ii = 0; ii < timestampsNodes.getLength(); ii++) {

            final Element timestampElement = (Element) timestampsNodes.item(ii);
            final TimestampToken timestampToken = makeTimestampToken(ii, timestampElement, TimestampType.SIGNATURE_TIMESTAMP);
            if (timestampToken != null) {

                setTimestampCanonicalizationMethod(timestampElement, timestampToken);

                final List<TimestampReference> references = new ArrayList<TimestampReference>();
                final TimestampReference signatureReference = new TimestampReference();
                signatureReference.setCategory(TimestampReferenceCategory.SIGNATURE);
                signatureReference.setSignatureId(getId());
                references.add(signatureReference);
                final NodeList list = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_CERT_DIGEST);
                for (int jj = 0; jj < list.getLength(); jj++) {

                    final Element element = (Element) list.item(jj);
                    final TimestampReference signingCertReference = createCertificateTimestampReference(element);
                    references.add(signingCertReference);
                }

                timestampToken.setTimestampedReferences(references);
                signatureTimestamps.add(timestampToken);
            }
        }
        return signatureTimestamps;
    }

    @Override
    public List<TimestampToken> getTimestampsX1() {

        final List<TimestampToken> signatureTimestamps = new ArrayList<TimestampToken>();
        final NodeList timestampsNodes = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_SIG_AND_REFS_TIMESTAMP);
        for (int ii = 0; ii < timestampsNodes.getLength(); ii++) {

            final Element timestampElement = (Element) timestampsNodes.item(ii);
            final TimestampToken timestampToken = makeTimestampToken(ii, timestampElement, TimestampType.VALIDATION_DATA_TIMESTAMP);
            if (timestampToken != null) {

                setTimestampCanonicalizationMethod(timestampElement, timestampToken);

                final List<TimestampReference> references = getTimestampedReferences();
                final TimestampReference signatureReference = new TimestampReference();
                signatureReference.setCategory(TimestampReferenceCategory.SIGNATURE);
                signatureReference.setSignatureId(getId());
                references.add(0, signatureReference);
                timestampToken.setTimestampedReferences(references);
                signatureTimestamps.add(timestampToken);
            }
        }
        return signatureTimestamps;
    }

    @Override
    public List<TimestampToken> getTimestampsX2() {

        final List<TimestampToken> signatureTimestamps = new ArrayList<TimestampToken>();
        final NodeList timestampsNodes = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_REFS_ONLY_TIMESTAMP);
        for (int ii = 0; ii < timestampsNodes.getLength(); ii++) {

            final Element timestampElement = (Element) timestampsNodes.item(ii);
            final TimestampToken timestampToken = makeTimestampToken(ii, timestampElement, TimestampType.VALIDATION_DATA_REFSONLY_TIMESTAMP);
            if (timestampToken != null) {

                setTimestampCanonicalizationMethod(timestampElement, timestampToken);

                timestampToken.setTimestampedReferences(getTimestampedReferences());
                signatureTimestamps.add(timestampToken);
            }
        }
        return signatureTimestamps;
    }

    @Override
    public List<TimestampToken> getArchiveTimestamps() {

        if (archiveTimestamps == null) {

            archiveTimestamps = new ArrayList<TimestampToken>();
            final NodeList timestampsNodes = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_ARCHIVE_TIMESTAMP);
            addArchiveTimestamps(archiveTimestamps, timestampsNodes, ArchiveTimestampType.XAdES);
            final NodeList timestampsNodes141 = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_ARCHIVE_TIMESTAMP_141);
            addArchiveTimestamps(archiveTimestamps, timestampsNodes141, ArchiveTimestampType.XAdES_141);
            final NodeList timestampsNodesV2 = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_ARCHIVE_TIMESTAMP_V2);
            addArchiveTimestamps(archiveTimestamps, timestampsNodesV2, ArchiveTimestampType.XAdES_141_V2);
        }
        return archiveTimestamps;
    }

    private void addArchiveTimestamps(final List<TimestampToken> signatureTimestamps, final NodeList timestampsNodes, final ArchiveTimestampType archiveTimestampType) {

        for (int ii = 0; ii < timestampsNodes.getLength(); ii++) {

            final Element timestampElement = (Element) timestampsNodes.item(ii);
            final TimestampToken timestampToken = makeTimestampToken(ii, timestampElement, TimestampType.ARCHIVE_TIMESTAMP);
            if (timestampToken != null) {

                timestampToken.setArchiveTimestampType(archiveTimestampType);
                setTimestampCanonicalizationMethod(timestampElement, timestampToken);

                final List<TimestampReference> references = getTimestampedReferences();
                final TimestampReference signatureReference = new TimestampReference();
                signatureReference.setCategory(TimestampReferenceCategory.SIGNATURE);
                signatureReference.setSignatureId(getId());
                references.add(0, signatureReference);
                timestampToken.setTimestampedReferences(references);
                signatureTimestamps.add(timestampToken);
            }
        }
    }

    private void setTimestampCanonicalizationMethod(Element timestampElement, TimestampToken timestampToken) {
        final Element canonicalizationMethodElement = DSSXMLUtils.getElement(timestampElement, xPathQueryHolder.XPATH__CANONICALIZATION_METHOD);
        String canonicalizationMethod = DEFAULT_TIMESTAMP_VALIDATION_CANONICALIZATION_METHOD;
        if (canonicalizationMethodElement != null) {

            canonicalizationMethod = canonicalizationMethodElement.getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
        }
        timestampToken.setCanonicalizationMethod(canonicalizationMethod);
    }

    /*
     * Returns an unmodifiable list of all certificate tokens encapsulated in the signature
     *
     * @see eu.europa.ec.markt.dss.validation.AdvancedSignature#getCertificates()
     */
    @Override
    public List<CertificateToken> getCertificates() {

        return getCertificateSource().getCertificates();
    }

    /*
     * Returns the list of certificates encapsulated in the KeyInfo segment
     */
    public List<CertificateToken> getKeyInfoCertificates() {

        return getCertificateSource().getKeyInfoCertificates();
    }

    /*
     * Returns the list of certificates encapsulated in the KeyInfo segment
     */
    public List<CertificateToken> getTimestampCertificates() {

        return getCertificateSource().getTimestampCertificates();
    }

    @Override
    public SignatureCryptographicVerification checkIntegrity(final DSSDocument detachedDocument) {

        final SignatureCryptographicVerification scv = new SignatureCryptographicVerification();

        final CertificateToken certificateToken = getSigningCertificateToken();
        if (certificateToken == null) {

            final SigningCertificateValidity signingCertificateValidity = getSigningCertificateValidity();
            final CertificateToken candidateCertificateToken = signingCertificateValidity.getCertToken();
            if (candidateCertificateToken != null) {

                final String subjectName = candidateCertificateToken.getSubjectX500Principal().getName();
                scv.setErrorMessage("There is no signed reference to the found signing certificate: " + candidateCertificateToken.getAbbreviation() + ":" + subjectName);
            } else {
                scv.setErrorMessage("There is no signing certificate within the signature.");
            }
            return scv;
        }
        final PublicKey publicKey = certificateToken.getCertificate().getPublicKey();
//        final KeySelector keySelector = KeySelector.singletonKeySelector(publicKey);

        final Document document = signatureElement.getOwnerDocument();
        final Element rootElement = document.getDocumentElement();
        if (rootElement.hasAttribute(DSSXMLUtils.ID_ATTRIBUTE_NAME)) {

            rootElement.setIdAttribute(DSSXMLUtils.ID_ATTRIBUTE_NAME, true);
        }

        DSSXMLUtils.recursiveIdBrowse(rootElement);

        /**
         * Creating a Validation Context<br>
         * We create an XMLValidateContext instance containing input parameters for validating the signature. Since we
         * are using DOM, we instantiate a DOMValidateContext instance (a subclass of XMLValidateContext), and pass it
         * two parameters, a KeyValueKeySelector object and a reference to the Signature element to be validated (which
         * is the first entry of the NodeList we generated earlier):
         */
        // final DOMValidateContext valContext = new DOMValidateContext(keySelector, signatureElement);
        try {

            // final URIDereferencer dereferencer = new ExternalFileURIDereferencer(detachedDocument);
            // valContext.setURIDereferencer(dereferencer);
            /**
             * This property controls whether or not the digested Reference objects will cache the dereferenced content
             * and pre-digested input for subsequent retrieval via the Reference.getDereferencedData and
             * Reference.getDigestInputStream methods. The default value if not specified is Boolean.FALSE.
             */
            // valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);

            /**
             * Unmarshalling the XML Signature<br>
             * We extract the contents of the Signature element into an XMLSignature object. This process is called
             * unmarshalling. The Signature element is unmarshalled using an XMLSignatureFactory object. An application
             * can obtain a DOM implementation of XMLSignatureFactory by calling the following line of code:
             */

            /**
             * These providers do not support ECDSA algorithm
             * factory = XMLSignatureFactory.getInstance("DOM");
             * factory = XMLSignatureFactory.getInstance("DOM", "XMLDSig");
             * factory = XMLSignatureFactory.getInstance("DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());
             * ---> org.jcp.xml.dsig.internal.dom
             */

            // This provider support ECDSA signature
            /**
             * ApacheXMLDSig / Apache Santuario XMLDSig (DOM XMLSignatureFactory; DOM KeyInfoFactory; C14N 1.0, C14N
             * 1.1, Exclusive C14N, Base64, Enveloped, XPath, XPath2, XSLT TransformServices)<br>
             * If this library is used than the same library must be used for the URIDereferencer.
             * ---> org.apache.jcp.xml.dsig.internal.dom
             */
            //** final XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM", xmlProvider);

            /**
             * We then invoke the unmarshalXMLSignature method of the factory to unmarshal an XMLSignature object, and
             * pass it the validation context we created earlier:
             */
            //** final XMLSignature signature = factory.unmarshalXMLSignature(valContext);
            //System.out.println("XMLSignature class: " + signature.getClass());

            /**
             * This does not work for RIPEMD160
             * --> Some clues:
             *
             *     xmlFact.newSignatureMethod("http://www.w3.org/2007/05/xmldsig-more#ecdsa-ripemd160", MyECDSARipemd160Provider());
             *     http://stackoverflow.com/questions/11984025/unsupported-signaturemethod-algorithm-but-the-algorithm-is-listed-as-available
             *
             *
             */

            /**
             * Austrian specific signature
             * org.apache.xml.security.signature.XMLSignature signature_ = null;
             * try {
             *    signature_ = new org.apache.xml.security.signature.XMLSignature(signatureElement, "");
             * } catch (Exception e) {
             *    throw new DSSException(e);
             * }
             * signature.addResourceResolver(new XPointerResourceResolver(signatureElement));
             * signature_.getSignedInfo().verifyReferences();*getVerificationResult(1);
             */

            org.apache.xml.security.signature.XMLSignature signature_ = null;
                /*
                final Provider[] providers = Security.getProviders();
                for (final Provider provider : providers) {

                    System.out.println("PROVIDER: " + provider.getName());
                    final Set<Provider.Service> services = provider.getServices();
                    for (final Provider.Service service : services) {

                        System.out.println("\tALGORITHM: " + service.getAlgorithm() + " / " + service.getType() + " / " + service.getClassName());
                    }
                }
                */
            // as second parameter: f.toURI().toURL().toString();
            signature_ = new org.apache.xml.security.signature.XMLSignature(signatureElement, "");

            signature_.addResourceResolver(new XPointerResourceResolver(signatureElement));
            signature_.addResourceResolver(new OfflineResolver(detachedDocument));
            //signature_.addResourceResolver(new XPointerResourceResolver(signatureElement));
            /**
             * In case of org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI() provider, the ID attributes need to be set
             * manually.<br>
             * The DSSXMLUtils.recursiveIdBrowse(...) method do not take into account the XML outside of the Signature
             * tag. It prevents some signatures to be validated.<br>
             *
             * Solution: the following lines where added:
             */

            // final Document document = signatureElement.getOwnerDocument();
            // final Element rootElement = document.getDocumentElement();
            // if (rootElement.hasAttribute(DSSXMLUtils.ID_ATTRIBUTE_NAME)) {

            //    valContext.setIdAttributeNS(rootElement, null, DSSXMLUtils.ID_ATTRIBUTE_NAME);
            // }
            // DSSXMLUtils.recursiveIdBrowse(valContext, rootElement);

            /**
             * Validating the XML Signature<br>
             * Now we are ready to validate the signature. We do this by invoking the validate method on the
             * XMLSignature object, and pass it the validation context as follows:
             */
            boolean coreValidity = signature_.checkSignatureValue(publicKey);

            /** OLD CODE:
             * try {
             *
             *    coreValidity = signature.validate(valContext);
             * } catch (XMLSignatureException e) {
             *
             *    scv.setErrorMessage("Signature validation: " + e.getMessage());
             * }
             */
            boolean signatureValidity = coreValidity;

            /**
             * If the XMLSignature.validate method returns false, we can try to narrow down the cause of the failure.
             * There are two phases in core XML Signature validation: <br>
             * - Signature validation (the cryptographic verification of the signature)<br>
             * - Reference validation (the verification of the digest of each reference in the signature)<br>
             * Each phase must be successful for the signature to be valid. To check if the signature failed to
             * cryptographically validate, we can check the status, as follows:
             */

            /**
             * This line was commented cos of the implementation of the validation process. The signature itself can be valid even if the references are bad.
             * It can be amended by introducing another variable: cryptographicallyValid
             * signatureValidity = signature.getSignatureValue().validate(valContext);
             *            try {
             *
             *               signature.getSignatureValue().validate(valContext);
             *          } catch (XMLSignatureException e) {
             *
             *               scv.setErrorMessage(e.getMessage());
             *          }
             */

            /** OLD CODE:
             *
             * final List<Reference> references = signature.getSignedInfo().getReferences();
             * for (final Reference reference : references) {
             *
             *    boolean refHashValidity = false;
             *    try {
             *
             *       refHashValidity = reference.validate(valContext);
             *    } catch (XMLSignatureException e) {
             *
             *       scv.setErrorMessage(reference.getURI() + ": " + e.getMessage());
             *    }
             *    referenceDataHashValid = referenceDataHashValid && refHashValidity;
             *    if (LOG.isInfoEnabled()) {
             *       LOG.info("Reference hash validity checked: " + reference.getURI() + "=" + refHashValidity);
             *    }
             *    final Data data = reference.getDereferencedData();
             *    dataFound = dataFound && (data != null);
             *
             *    final InputStream digestInputStream = reference.getDigestInputStream();
             *    if (data != null && digestInputStream != null) {
             *
             *       // The references are saved for later treatment in -A level.
             *       try {
             *
             *          DSSUtils.copy(digestInputStream, referencesDigestOutputStream);
             *       } catch (IOException e) {
             *          LOG.debug(e.getMessage(), e);
             *       }
             *    }
             * }
             */

            boolean referenceDataFound = true;
            boolean referenceDataHashValid = true;

            final SignedInfo signedInfo = signature_.getSignedInfo();
            final int length = signedInfo.getLength();
            for (int ii = 0; ii < length; ii++) {

                // referenceDataHashValid = referenceDataHashValid && signedInfo.getVerificationResult(ii);
                final Reference reference = signedInfo.item(ii);
                referenceDataHashValid = referenceDataHashValid && reference.verify();
                final byte[] referencedBytes = reference.getReferencedBytes();
                referenceDataFound = referenceDataFound && (referencedBytes != null);
                /**
                 * It returns null if This method only works works after a call to verify.
                 * final XMLSignatureInput transformsOutput = reference.getTransformsOutput();
                 * InputStream referenceOctetStream = transformsOutput.getOctetStream();
                 * referenceOctetStream.reset(); // Must be reset!!
                 */
                final InputStream referencedInputStream = DSSUtils.toInputStream(referencedBytes);
                DSSUtils.copy(referencedInputStream, referencesDigestOutputStream);
            }
            scv.setReferenceDataFound(referenceDataFound);
            scv.setReferenceDataIntact(referenceDataHashValid);
            scv.setSignatureIntact(signatureValidity);
            //** } catch (MarshalException e) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            StackTraceElement[] stackTrace = e.getStackTrace();
            final String name = XAdESSignature.class.getName();
            int lineNumber = 0;
            for (int ii = 0; ii < stackTrace.length; ii++) {

                final String className = stackTrace[ii].getClassName();
                if (className.equals(name)) {

                    lineNumber = stackTrace[ii].getLineNumber();
                    break;
                }
            }
            scv.setErrorMessage(e.getMessage() + "/ XAdESSignature/Line number/" + lineNumber);
        }
        return scv;
    }

    @Override
    public List<AdvancedSignature> getCounterSignatures() {

        // see ETSI TS 101 903 V1.4.2 (2010-12) pp. 38/39/40

        //  try {
        NodeList counterSigs = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_COUNTER_SIGNATURE);
        if (counterSigs == null) {
            return null;
        }

        List<AdvancedSignature> xadesList = new ArrayList<AdvancedSignature>();

        for (int i = 0; i < counterSigs.getLength(); i++) {

            Element counterSigEl = (Element) counterSigs.item(i);
            Element signatureEl = DSSXMLUtils.getElement(counterSigEl, xPathQueryHolder.XPATH_SIGNATURE);

            // Verify that the element is a proper signature by trying to build a XAdESSignature out of it
            XAdESSignature xCounterSig = new XAdESSignature(signatureEl, xPathQueryHolders, certPool);

            /*
             * Verify that there is a ds:Reference element with a Type set to:
             * http://uri.etsi.org/01903#CountersignedSignature (as per the XAdES spec)
             */
/*
                XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
                XMLSignature signature = factory.unmarshalXMLSignature(new DOMStructure(signatureEl));

                LOG.info("Verifying countersignature References");
                for (Object refobj : signature.getSignedInfo().getReferences()) {

                    Reference ref = (Reference) refobj;
                    if (ref.getType() != null && ref.getType().equals(xPathQueryHolder.XADES_COUNTERSIGNED_SIGNATURE)) {

                        // Ok, this seems to be a CounterSignature
                        // Verify that the digest is that of the signature value
                        CertificateToken certToken = xCounterSig.getSigningCertificateToken();
                        PublicKey publicKey = certToken.getCertificate().getPublicKey();
                        if (ref.validate(new DOMValidateContext(publicKey, DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_VALUE)))) {

                            LOG.info("Reference verification succeeded, adding countersignature");
                            xadesList.add(xCounterSig);
                        } else {

                            LOG.warn("Skipping countersignature because the Reference doesn't contain a hash of the embedding SignatureValue");
                        }
                        break;
                    }
                }
*/
        }
        return xadesList;
/*        } catch (MarshalException e) {

            throw new DSSEncodingException(MSG.COUNTERSIGNATURE_ENCODING, e);
        } catch (XMLSignatureException e) {

            throw new DSSEncodingException(MSG.COUNTERSIGNATURE_ENCODING, e);
        }
*/
    }

    @Override
    public List<CertificateRef> getCertificateRefs() {

        Element signingCertEl = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_CERT_REFS);
        if (signingCertEl == null) {

            return null;
        }
        List<CertificateRef> certIds = new ArrayList<CertificateRef>();
        NodeList certIdnodes = DSSXMLUtils.getNodeList(signingCertEl, "./xades:Cert");
        for (int i = 0; i < certIdnodes.getLength(); i++) {

            Element certId = (Element) certIdnodes.item(i);
            Element issuerNameEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__X509_ISSUER_NAME);
            Element issuerSerialEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__X509_SERIAL_NUMBER);
            Element digestAlgorithmEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__DIGEST_METHOD);
            Element digestValueEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__CERT_DIGEST_DIGEST_VALUE);

            CertificateRef genericCertId = new CertificateRef();
            if (issuerNameEl != null && issuerSerialEl != null) {
                genericCertId.setIssuerName(issuerNameEl.getTextContent());
                genericCertId.setIssuerSerial(issuerSerialEl.getTextContent());
            }

            String xmlName = digestAlgorithmEl.getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
            genericCertId.setDigestAlgorithm(DigestAlgorithm.forXML(xmlName).getName());

            genericCertId.setDigestValue(DSSUtils.base64Decode(digestValueEl.getTextContent()));
            certIds.add(genericCertId);
        }

        return certIds;

    }

    @Override
    public List<CRLRef> getCRLRefs() {

        final List<CRLRef> certIds = new ArrayList<CRLRef>();
        final Element signingCertEl = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_REVOCATION_CRL_REFS);
        if (signingCertEl != null) {

            final NodeList crlRefNodes = DSSXMLUtils.getNodeList(signingCertEl, xPathQueryHolder.XPATH__CRL_REF);
            for (int i = 0; i < crlRefNodes.getLength(); i++) {

                final Element certId = (Element) crlRefNodes.item(i);
                final Element digestAlgorithmEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__DAAV_DIGEST_METHOD);
                final Element digestValueEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__DAAV_DIGEST_VALUE);

                final String xmlName = digestAlgorithmEl.getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
                final DigestAlgorithm digestAlgo = DigestAlgorithm.forXML(xmlName);

                final CRLRef ref = new CRLRef();
                ref.setDigestAlgorithm(digestAlgo);
                ref.setDigestValue(DSSUtils.base64Decode(digestValueEl.getTextContent()));
                certIds.add(ref);
            }
        }
        return certIds;
    }

    @Override
    public List<OCSPRef> getOCSPRefs() {

        final List<OCSPRef> certIds = new ArrayList<OCSPRef>();
        final Element signingCertEl = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_OCSP_REFS);
        if (signingCertEl != null) {

            final NodeList ocspRefNodes = DSSXMLUtils.getNodeList(signingCertEl, xPathQueryHolder.XPATH__OCSPREF);
            for (int i = 0; i < ocspRefNodes.getLength(); i++) {

                final Element certId = (Element) ocspRefNodes.item(i);
                final Element digestAlgorithmEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__DAAV_DIGEST_METHOD);
                final Element digestValueEl = DSSXMLUtils.getElement(certId, xPathQueryHolder.XPATH__DAAV_DIGEST_VALUE);

                if (digestAlgorithmEl == null || digestValueEl == null) {
                    throw new DSSNotETSICompliantException(DSSNotETSICompliantException.MSG.XADES_DIGEST_ALG_AND_VALUE_ENCODING);
                }

                final String xmlName = digestAlgorithmEl.getAttribute(XPathQueryHolder.XMLE_ALGORITHM);
                final DigestAlgorithm digestAlgo = DigestAlgorithm.forXML(xmlName);

                final String digestValue = digestValueEl.getTextContent();
                final byte[] base64EncodedDigestValue = DSSUtils.base64Decode(digestValue);
                final OCSPRef ocspRef = new OCSPRef(digestAlgo, base64EncodedDigestValue, false);
                certIds.add(ocspRef);
            }
        }
        return certIds;
    }

    @Override
    public byte[] getSignatureTimestampData(final TimestampToken timestampToken) {

        final String canonicalizationMethod = getCanonicalizationMethod(timestampToken);
        final Node signatureValue = getSignatureValue();
        final byte[] canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, signatureValue);
        // *** Log signature timestamp canonicalised string
        if (LOG.isTraceEnabled()) {
            LOG.trace("| Canonicalization Method:" + canonicalizationMethod);
            LOG.trace(new String(canonicalizedValue) + "\n");
        }
        return canonicalizedValue;
    }

    private String getCanonicalizationMethod(final TimestampToken timestampToken) {

        String canonicalizationMethod;
        if (timestampToken != null) {

            canonicalizationMethod = timestampToken.getCanonicalizationMethod();
        } else {
            canonicalizationMethod = DEFAULT_TIMESTAMP_CREATION_CANONICALIZATION_METHOD;
        }
        return canonicalizationMethod;
    }

    @Override
    public byte[] getTimestampX1Data(final TimestampToken timestampToken) {

        final String canonicalizationMethod = getCanonicalizationMethod(timestampToken);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {

            getSignatureValue();
            final Element signatureValue = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_VALUE);
            byte[] canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, signatureValue);
            buffer.write(canonicalizedValue);

            final NodeList signatureTimeStampNode = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_TIMESTAMP);
            if (signatureTimeStampNode != null) {

                for (int ii = 0; ii < signatureTimeStampNode.getLength(); ii++) {

                    final Node item = signatureTimeStampNode.item(ii);
                    canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, item);
                    buffer.write(canonicalizedValue);
                }
            }

            final Node completeCertificateRefsNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_CERTIFICATE_REFS);
            if (completeCertificateRefsNode != null) {

                canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, completeCertificateRefsNode);
                buffer.write(canonicalizedValue);
            }
            final Node completeRevocationRefsNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_REVOCATION_REFS);
            if (completeRevocationRefsNode != null) {

                canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, completeRevocationRefsNode);
                buffer.write(canonicalizedValue);
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("X1Timestamp canonicalised string:\n" + buffer.toString());
            }
            final byte[] bytes = buffer.toByteArray();
            return bytes;
        } catch (IOException e) {

            throw new DSSException("Error when computing the SigAndRefsTimeStamp", e);
        }
    }

    @Override
    public byte[] getTimestampX2Data(final TimestampToken timestampToken) {

        final String canonicalizationMethod = getCanonicalizationMethod(timestampToken);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {

            final Node completeCertificateRefsNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_CERTIFICATE_REFS);
            if (completeCertificateRefsNode != null) {

                final byte[] canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, completeCertificateRefsNode);
                buffer.write(canonicalizedValue);
            }
            final Node completeRevocationRefsNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_REVOCATION_REFS);
            if (completeRevocationRefsNode != null) {

                final byte[] canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, completeRevocationRefsNode);
                buffer.write(canonicalizedValue);
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("ArchiveTimestamp canonicalised string:\n" + buffer.toString());
            }
            final byte[] bytes = buffer.toByteArray();
            return bytes;
        } catch (IOException e) {

            throw new DSSException("Error when computing the TimestampX2Data", e);
        }
    }

    /**
     * Creates the hash sent to the TSA (messageImprint) computed on the XAdES-X-L or -A form of the electronic signature and the signed data objects<br>
     *
     * @param timestampToken null when adding a new archive timestamp
     * @return
     */
    @Override
    public byte[] getArchiveTimestampData(final TimestampToken timestampToken) {

        if (LOG.isTraceEnabled()) {
            LOG.trace("--->Get archive timestamp data:" + (timestampToken == null ? "--> CREATION" : "--> VALIDATION"));
        }
        final String canonicalizationMethod = getCanonicalizationMethod(timestampToken);
        /**
         * 8.2.1 Not distributed case<br>
         *
         * When xadesv141:ArchiveTimeStamp and all the unsigned properties covered by its time-stamp token have the same
         * parent, this property uses the Implicit mechanism for all the time-stamped data objects. The input to the
         * computation of the digest value MUST be built as follows:
         */
        try {

            /**
             * 1) Initialize the final octet stream as an empty octet stream.
             */
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            /**
             * 2) Take all the ds:Reference elements in their order of appearance within ds:SignedInfo referencing whatever
             * the signer wants to sign including the SignedProperties element. Process each one as indicated below:<br>
             * - Process the retrieved ds:Reference element according to the reference processing model of XMLDSIG.<br>
             * - If the result is a XML node set, canonicalize it. If ds:Canonicalization is present, the algorithm
             * indicated by this element is used. If not, the standard canonicalization method specified by XMLDSIG is
             * used.<br>
             * - Concatenate the resulting octets to the final octet stream.
             */

            /**
             * The references are already calculated {@see #checkIntegrity()}
             */

            final InputStream decodedInput = new ByteArrayInputStream((referencesDigestOutputStream).toByteArray());

            DSSUtils.copy(decodedInput, buffer);
            /**
             * 3) Take the following XMLDSIG elements in the order they are listed below, canonicalize each one and
             * concatenate each resulting octet stream to the final octet stream:<br>
             * - The ds:SignedInfo element.<br>
             * - The ds:SignatureValue element.<br>
             * - The ds:KeyInfo element, if present.
             */
            byte[] canonicalizedValue;

            final Element signedInfo = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNED_INFO);
            canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, signedInfo);
            buffer.write(canonicalizedValue);

            final Element signatureValue = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_SIGNATURE_VALUE);
            canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, signatureValue);
            buffer.write(canonicalizedValue);

            final Element keyInfo = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_KEY_INFO);
            canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, keyInfo);
            buffer.write(canonicalizedValue);

            /**
             * 4) Take the unsigned signature properties that appear before the current xadesv141:ArchiveTimeStamp in the
             * order they appear within the xades:UnsignedSignatureProperties, canonicalize each one and concatenate each
             * resulting octet stream to the final octet stream. While concatenating the following rules apply:
             */

            // System.out.println("///### -------------------------------------> ");
            // DSSXMLUtils.printDocument(signatureElement.getOwnerDocument(), System.out);
            // System.out.println("<------------------------------------- ");

            final Element unsignedSignaturePropertiesNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_UNSIGNED_SIGNATURE_PROPERTIES);
            if (unsignedSignaturePropertiesNode == null) {
                throw new DSSNullReturnedException(xPathQueryHolder.XPATH_UNSIGNED_SIGNATURE_PROPERTIES);
            }
            // The archive timestamp need to be identified to know if it must be taken into account or not.
            int archiveTimeStampCount = 0;

            final NodeList unsignedProperties = unsignedSignaturePropertiesNode.getChildNodes();
            for (int ii = 0; ii < unsignedProperties.getLength(); ii++) {

                Node node = unsignedProperties.item(ii);
                final String localName = node.getLocalName();
                // System.out.println("###: " + localName);
                // In the SD-DSS implementation when validating the signature the framework will not add missing data. To do so you the signature must be extended.
                // if (localName.equals("CertificateValues")) {

                /**
                 * - The xades:CertificateValues property MUST be added if it is not already present and the ds:KeyInfo
                 * element does not contain the full set of certificates used to validate the electronic signature.
                 */

                // } else if (localName.equals("RevocationValues")) {

                /**
                 * - The xades:RevocationValues property MUST be added if it is not already present and the ds:KeyInfo
                 * element does not contain the revocation information that has to be shipped with the electronic
                 * signature
                 */

                // } else if (localName.equals("AttrAuthoritiesCertValues")) {

                /**
                 * - The xades:AttrAuthoritiesCertValues property MUST be added if not already present and the following
                 * conditions are true: there exist an attribute certificate in the signature AND a number of
                 * certificates that have been used in its validation do not appear in CertificateValues. Its content
                 * will satisfy with the rules specified in clause 7.6.3.
                 */

                // } else if (localName.equals("AttributeRevocationValues")) {

                /**
                 * - The xades:AttributeRevocationValues property MUST be added if not already present and there the
                 * following conditions are true: there exist an attribute certificate AND some revocation data that have
                 * been used in its validation do not appear in RevocationValues. Its content will satisfy with the rules
                 * specified in clause 7.6.4.
                 */

                // } else
                if (XPathQueryHolder.XMLE_ARCHIVE_TIME_STAMP.equals(localName) || XPathQueryHolder.XMLE_ARCHIVE_TIME_STAMP_V2.equals(localName)) {

                    if (timestampToken != null && timestampToken.getDSSId() <= archiveTimeStampCount) {

                        break;
                    }
                    archiveTimeStampCount++;
                } else if (localName.equals("TimeStampValidationData")) {

                    /**
                     * ETSI TS 101 903 V1.4.2 (2010-12)
                     * 8.1 The new XAdESv141:TimeStampValidationData element
                     * ../..
                     * This element is specified to serve as an optional container for validation data required for carrying a full verification of
                     * time-stamp tokens embedded within any of the different time-stamp containers defined in the present document.
                     * ../..
                     * 8.1.1 Use of URI attribute
                     * ../..
                     * a new xadesv141:TimeStampValidationData element SHALL be created containing the missing
                     validation data information and it SHALL be added as a child of UnsignedSignatureProperties elements
                     immediately after the respective time-stamp token container element.
                     */

                    /**
                     * This is the work around for the name space problem: The issue was reported on: https://issues.apache.org/jira/browse/SANTUARIO-139 and considered as close.
                     * But for me (Bob) it still does not work!
                     */

                    final Document document = DSSXMLUtils.buildDOM();
                    final Element rootElement = document.createElementNS(xPathQueryHolder.XADES141_NAMESPACE, "xades141:toto");
                    document.appendChild(rootElement);

                    final Node node1 = node.cloneNode(true);
                    document.adoptNode(node1);
                    rootElement.appendChild(node1);
                    node = node1;

                    if (LOG.isTraceEnabled()) {
                        DSSXMLUtils.printDocument(node, System.out);
                    }
                }

                canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, node);
                if (LOG.isTraceEnabled()) {
                    LOG.trace(localName + ": Canonicalization: " + canonicalizationMethod);
                    LOG.trace(new String(canonicalizedValue) + "\n");
                }
                buffer.write(canonicalizedValue);
            }
            /**
             * 5) Take all the ds:Object elements except the one containing xades:QualifyingProperties element.
             * Canonicalize each one and concatenate each resulting octet stream to the final octet stream. If
             * ds:Canonicalization is present, the algorithm indicated by this element is used. If not, the standard
             * canonicalization method specified by XMLDSIG is used.
             */
            boolean xades141 = true;
            if (timestampToken != null && ArchiveTimestampType.XAdES.equals(timestampToken.getArchiveTimestampType())) {

                xades141 = false;
            }
            if (xades141) {

                NodeList objects = getObjects();
                for (int ii = 0; ii < objects.getLength(); ii++) {

                    Node node = objects.item(ii);
                    Node qualifyingProperties = DSSXMLUtils.getElement(node, xPathQueryHolder.XPATH__QUALIFYING_PROPERTIES);
                    if (qualifyingProperties != null) {

                        continue;
                    }
                    canonicalizedValue = DSSXMLUtils.canonicalizeSubtree(canonicalizationMethod, node);
                    buffer.write(canonicalizedValue);
                }
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("ArchiveTimestamp canonicalised string:\n" + buffer.toString());
            }
            final byte[] bytes = buffer.toByteArray();
            return bytes;
        } catch (IOException e) {

            throw new DSSException("Error when computing the archive data", e);
        }
    }

    @Override
    public String getId() {

        if (signatureId == null) {

            Node idElement = DSSXMLUtils.getNode(signatureElement, "./@Id");
            if (idElement != null) {

                signatureId = idElement.getTextContent();
            } else {

                final CertificateToken certificateToken = getSigningCertificateToken();
                final int dssId = certificateToken == null ? 0 : certificateToken.getDSSId();
                signatureId = DSSUtils.getDeterministicId(getSigningTime(), dssId);
            }
        }
        return signatureId;
    }

    @Override
    public List<TimestampReference> getTimestampedReferences() {

        final List<TimestampReference> references = new ArrayList<TimestampReference>();
        final NodeList certDigestList = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_CERT_DIGEST);
        for (int jj = 0; jj < certDigestList.getLength(); jj++) {

            final Element certDigestElement = (Element) certDigestList.item(jj);
            final TimestampReference certificateReference = createCertificateTimestampReference(certDigestElement);
            references.add(certificateReference);
        }

        final Node completeCertificateRefsNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_CERTIFICATE_REFS);
        if (completeCertificateRefsNode != null) {

            final NodeList nodes = DSSXMLUtils.getNodeList(completeCertificateRefsNode, xPathQueryHolder.XPATH__COMPLETE_CERTIFICATE_REFS__CERT_DIGEST);
            for (int ii = 0; ii < nodes.getLength(); ii++) {

                final Element certDigestElement = (Element) nodes.item(ii);
                final TimestampReference certificateReference = createCertificateTimestampReference(certDigestElement);
                references.add(certificateReference);
            }
        }
        final Node completeRevocationRefsNode = DSSXMLUtils.getElement(signatureElement, xPathQueryHolder.XPATH_COMPLETE_REVOCATION_REFS);
        if (completeRevocationRefsNode != null) {

            final NodeList nodes = DSSXMLUtils.getNodeList(completeRevocationRefsNode, "./*/*/xades:DigestAlgAndValue");
            for (int ii = 0; ii < nodes.getLength(); ii++) {

                final Element element = (Element) nodes.item(ii);
                String digestAlgorithm = DSSXMLUtils.getNode(element, xPathQueryHolder.XPATH__DIGEST_METHOD_ALGORITHM).getTextContent();
                digestAlgorithm = DigestAlgorithm.forXML(digestAlgorithm).getName();
                final String digestValue = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__DIGEST_VALUE).getTextContent();
                final TimestampReference revocationReference = new TimestampReference();
                revocationReference.setCategory(TimestampReferenceCategory.REVOCATION);
                revocationReference.setDigestAlgorithm(digestAlgorithm);
                revocationReference.setDigestValue(digestValue);
                references.add(revocationReference);
            }
        }
        return references;
    }

    /**
     * @param element
     * @return
     * @throws eu.europa.ec.markt.dss.exception.DSSException
     */
    private TimestampReference createCertificateTimestampReference(final Element element) throws DSSException {

        final String digestAlgorithm = DSSXMLUtils.getNode(element, xPathQueryHolder.XPATH__DIGEST_METHOD_ALGORITHM).getTextContent();
        final DigestAlgorithm digestAlgorithmObj = DigestAlgorithm.forXML(digestAlgorithm);
        if (!usedCertificatesDigestAlgorithms.contains(digestAlgorithmObj)) {

            usedCertificatesDigestAlgorithms.add(digestAlgorithmObj);
        }
        final Element digestValueElement = DSSXMLUtils.getElement(element, xPathQueryHolder.XPATH__DIGEST_VALUE);
        final String digestValue = (digestValueElement == null) ? "" : digestValueElement.getTextContent();
        final TimestampReference reference = new TimestampReference();
        reference.setCategory(TimestampReferenceCategory.CERTIFICATE);
        reference.setDigestAlgorithm(digestAlgorithmObj.getName());
        reference.setDigestValue(digestValue);
        return reference;
    }

    @Override
    public Set<DigestAlgorithm> getUsedCertificatesDigestAlgorithms() {

        return usedCertificatesDigestAlgorithms;
    }

    @Override
    public boolean isDataForSignatureLevelPresent(final SignatureLevel signatureLevel) {

        boolean dataForLevelPresent = true;
        switch (signatureLevel) {
            case XAdES_BASELINE_LTA:
            case XAdES_A:
                dataForLevelPresent = hasLTAProfile();
                break;
            case XAdES_BASELINE_LT:
                dataForLevelPresent &= hasLTProfile();
                break;
            case XAdES_BASELINE_T:
                dataForLevelPresent &= hasTProfile();
                break;
            case XAdES_BASELINE_B:
                dataForLevelPresent &= hasBProfile();
                break;
            case XAdES_XL:
                dataForLevelPresent &= hasXLProfile();
                break;
            case XAdES_X:
                dataForLevelPresent &= hasXProfile();
                break;
            case XAdES_C:
                dataForLevelPresent &= hasCProfile();
                break;
            default:
                throw new IllegalArgumentException("Unknown level " + signatureLevel);
        }
        return dataForLevelPresent;
    }

    public SignatureLevel[] getSignatureLevels() {

        return new SignatureLevel[]{SignatureLevel.XAdES_BASELINE_B, SignatureLevel.XAdES_BASELINE_T, SignatureLevel.XAdES_C, SignatureLevel.XAdES_X, SignatureLevel.XAdES_XL, SignatureLevel.XAdES_BASELINE_LT, SignatureLevel.XAdES_A, SignatureLevel.XAdES_BASELINE_LTA};
    }

    /**
     * This method returns the last timestamp validation data for an archive timestamp.
     *
     * @return
     */
    public Element getLastTimestampValidationData() {

        final List<TimestampToken> archiveTimestamps = getArchiveTimestamps();
        TimestampToken mostRecentTimestamp = null;
        for (final TimestampToken archiveTimestamp : archiveTimestamps) {

            if (mostRecentTimestamp == null) {

                mostRecentTimestamp = archiveTimestamp;
                continue;
            }
            final Date generationTime = archiveTimestamp.getGenerationTime();
            final Date mostRecentGenerationTime = mostRecentTimestamp.getGenerationTime();
            if (generationTime.after(mostRecentGenerationTime)) {

                mostRecentTimestamp = archiveTimestamp;
            }
        }
        final NodeList nodeList = DSSXMLUtils.getNodeList(signatureElement, xPathQueryHolder.XPATH_UNSIGNED_SIGNATURE_PROPERTIES + "/*");
        boolean found = false;
        for (int ii = 0; ii < nodeList.getLength(); ii++) {

            final Element unsignedSignatureElement = (Element) nodeList.item(ii);
            final int hashCode = mostRecentTimestamp.getHashCode();
            final int nodeHashCode = unsignedSignatureElement.hashCode();
            if (nodeHashCode == hashCode) {

                found = true;
            } else if (found) {

                final String nodeName = unsignedSignatureElement.getLocalName();
                if ("TimeStampValidationData".equals(nodeName)) {

                    return unsignedSignatureElement;
                }
            }
        }
        return null;
    }

    @Override
    public CommitmentType getCommitmentTypeIndication() {
        return null;
    }
}