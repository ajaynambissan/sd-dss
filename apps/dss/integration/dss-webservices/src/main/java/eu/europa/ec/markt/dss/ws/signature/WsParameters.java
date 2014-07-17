
package eu.europa.ec.markt.dss.ws.signature;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for wsParameters complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="wsParameters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="asicMimeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="asicSignatureForm" type="{http://ws.dss.markt.ec.europa.eu/}signatureForm" minOccurs="0"/>
 *         &lt;element name="asicZipComment" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="certificateChainByteArrayList" type="{http://www.w3.org/2001/XMLSchema}base64Binary" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="certifiedSignerRoles" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="claimedSignerRole" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="commitmentTypeIndication" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="contentIdentifierPrefix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentIdentifierSuffix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="deterministicId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digestAlgorithm" type="{http://ws.dss.markt.ec.europa.eu/}digestAlgorithm" minOccurs="0"/>
 *         &lt;element name="encryptionAlgorithm" type="{http://ws.dss.markt.ec.europa.eu/}encryptionAlgorithm" minOccurs="0"/>
 *         &lt;element name="references" type="{http://ws.dss.markt.ec.europa.eu/}dssReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="signWithExpiredCertificate" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="signatureLevel" type="{http://ws.dss.markt.ec.europa.eu/}signatureLevel" minOccurs="0"/>
 *         &lt;element name="signaturePackaging" type="{http://ws.dss.markt.ec.europa.eu/}signaturePackaging" minOccurs="0"/>
 *         &lt;element name="signaturePolicy" type="{http://ws.dss.markt.ec.europa.eu/}policy" minOccurs="0"/>
 *         &lt;element name="signerLocation" type="{http://ws.dss.markt.ec.europa.eu/}signerLocation" minOccurs="0"/>
 *         &lt;element name="signingCertificateBytes" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="signingCertificateDigestAlgorithm" type="{http://ws.dss.markt.ec.europa.eu/}digestAlgorithm" minOccurs="0"/>
 *         &lt;element name="signingDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="timestampDigestAlgorithm" type="{http://ws.dss.markt.ec.europa.eu/}digestAlgorithm" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wsParameters", propOrder = {
    "asicMimeType",
    "asicSignatureForm",
    "asicZipComment",
    "certificateChainByteArrayList",
    "certifiedSignerRoles",
    "claimedSignerRole",
    "commitmentTypeIndication",
    "contentIdentifierPrefix",
    "contentIdentifierSuffix",
    "deterministicId",
    "digestAlgorithm",
    "encryptionAlgorithm",
    "references",
    "signWithExpiredCertificate",
    "signatureLevel",
    "signaturePackaging",
    "signaturePolicy",
    "signerLocation",
    "signingCertificateBytes",
    "signingCertificateDigestAlgorithm",
    "signingDate",
    "timestampDigestAlgorithm"
})
public class WsParameters {

    protected String asicMimeType;
    protected SignatureForm asicSignatureForm;
    protected boolean asicZipComment;
    @XmlElement(nillable = true)
    protected List<byte[]> certificateChainByteArrayList;
    @XmlElement(nillable = true)
    protected List<String> certifiedSignerRoles;
    @XmlElement(nillable = true)
    protected List<String> claimedSignerRole;
    @XmlElement(nillable = true)
    protected List<String> commitmentTypeIndication;
    protected String contentIdentifierPrefix;
    protected String contentIdentifierSuffix;
    protected String deterministicId;
    protected DigestAlgorithm digestAlgorithm;
    protected EncryptionAlgorithm encryptionAlgorithm;
    @XmlElement(nillable = true)
    protected List<DssReference> references;
    protected boolean signWithExpiredCertificate;
    protected SignatureLevel signatureLevel;
    protected SignaturePackaging signaturePackaging;
    protected Policy signaturePolicy;
    protected SignerLocation signerLocation;
    protected byte[] signingCertificateBytes;
    protected DigestAlgorithm signingCertificateDigestAlgorithm;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar signingDate;
    protected DigestAlgorithm timestampDigestAlgorithm;

    /**
     * Gets the value of the asicMimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsicMimeType() {
        return asicMimeType;
    }

    /**
     * Sets the value of the asicMimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsicMimeType(String value) {
        this.asicMimeType = value;
    }

    /**
     * Gets the value of the asicSignatureForm property.
     * 
     * @return
     *     possible object is
     *     {@link SignatureForm }
     *     
     */
    public SignatureForm getAsicSignatureForm() {
        return asicSignatureForm;
    }

    /**
     * Sets the value of the asicSignatureForm property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignatureForm }
     *     
     */
    public void setAsicSignatureForm(SignatureForm value) {
        this.asicSignatureForm = value;
    }

    /**
     * Gets the value of the asicZipComment property.
     * 
     */
    public boolean isAsicZipComment() {
        return asicZipComment;
    }

    /**
     * Sets the value of the asicZipComment property.
     * 
     */
    public void setAsicZipComment(boolean value) {
        this.asicZipComment = value;
    }

    /**
     * Gets the value of the certificateChainByteArrayList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the certificateChainByteArrayList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCertificateChainByteArrayList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * byte[]
     * 
     */
    public List<byte[]> getCertificateChainByteArrayList() {
        if (certificateChainByteArrayList == null) {
            certificateChainByteArrayList = new ArrayList<byte[]>();
        }
        return this.certificateChainByteArrayList;
    }

    /**
     * Gets the value of the certifiedSignerRoles property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the certifiedSignerRoles property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCertifiedSignerRoles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCertifiedSignerRoles() {
        if (certifiedSignerRoles == null) {
            certifiedSignerRoles = new ArrayList<String>();
        }
        return this.certifiedSignerRoles;
    }

    /**
     * Gets the value of the claimedSignerRole property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the claimedSignerRole property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClaimedSignerRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getClaimedSignerRole() {
        if (claimedSignerRole == null) {
            claimedSignerRole = new ArrayList<String>();
        }
        return this.claimedSignerRole;
    }

    /**
     * Gets the value of the commitmentTypeIndication property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the commitmentTypeIndication property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommitmentTypeIndication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCommitmentTypeIndication() {
        if (commitmentTypeIndication == null) {
            commitmentTypeIndication = new ArrayList<String>();
        }
        return this.commitmentTypeIndication;
    }

    /**
     * Gets the value of the contentIdentifierPrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentIdentifierPrefix() {
        return contentIdentifierPrefix;
    }

    /**
     * Sets the value of the contentIdentifierPrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentIdentifierPrefix(String value) {
        this.contentIdentifierPrefix = value;
    }

    /**
     * Gets the value of the contentIdentifierSuffix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentIdentifierSuffix() {
        return contentIdentifierSuffix;
    }

    /**
     * Sets the value of the contentIdentifierSuffix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentIdentifierSuffix(String value) {
        this.contentIdentifierSuffix = value;
    }

    /**
     * Gets the value of the deterministicId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeterministicId() {
        return deterministicId;
    }

    /**
     * Sets the value of the deterministicId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeterministicId(String value) {
        this.deterministicId = value;
    }

    /**
     * Gets the value of the digestAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link DigestAlgorithm }
     *     
     */
    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * Sets the value of the digestAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link DigestAlgorithm }
     *     
     */
    public void setDigestAlgorithm(DigestAlgorithm value) {
        this.digestAlgorithm = value;
    }

    /**
     * Gets the value of the encryptionAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link EncryptionAlgorithm }
     *     
     */
    public EncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    /**
     * Sets the value of the encryptionAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link EncryptionAlgorithm }
     *     
     */
    public void setEncryptionAlgorithm(EncryptionAlgorithm value) {
        this.encryptionAlgorithm = value;
    }

    /**
     * Gets the value of the references property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the references property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DssReference }
     * 
     * 
     */
    public List<DssReference> getReferences() {
        if (references == null) {
            references = new ArrayList<DssReference>();
        }
        return this.references;
    }

    /**
     * Gets the value of the signWithExpiredCertificate property.
     * 
     */
    public boolean isSignWithExpiredCertificate() {
        return signWithExpiredCertificate;
    }

    /**
     * Sets the value of the signWithExpiredCertificate property.
     * 
     */
    public void setSignWithExpiredCertificate(boolean value) {
        this.signWithExpiredCertificate = value;
    }

    /**
     * Gets the value of the signatureLevel property.
     * 
     * @return
     *     possible object is
     *     {@link SignatureLevel }
     *     
     */
    public SignatureLevel getSignatureLevel() {
        return signatureLevel;
    }

    /**
     * Sets the value of the signatureLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignatureLevel }
     *     
     */
    public void setSignatureLevel(SignatureLevel value) {
        this.signatureLevel = value;
    }

    /**
     * Gets the value of the signaturePackaging property.
     * 
     * @return
     *     possible object is
     *     {@link SignaturePackaging }
     *     
     */
    public SignaturePackaging getSignaturePackaging() {
        return signaturePackaging;
    }

    /**
     * Sets the value of the signaturePackaging property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignaturePackaging }
     *     
     */
    public void setSignaturePackaging(SignaturePackaging value) {
        this.signaturePackaging = value;
    }

    /**
     * Gets the value of the signaturePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link Policy }
     *     
     */
    public Policy getSignaturePolicy() {
        return signaturePolicy;
    }

    /**
     * Sets the value of the signaturePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Policy }
     *     
     */
    public void setSignaturePolicy(Policy value) {
        this.signaturePolicy = value;
    }

    /**
     * Gets the value of the signerLocation property.
     * 
     * @return
     *     possible object is
     *     {@link SignerLocation }
     *     
     */
    public SignerLocation getSignerLocation() {
        return signerLocation;
    }

    /**
     * Sets the value of the signerLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignerLocation }
     *     
     */
    public void setSignerLocation(SignerLocation value) {
        this.signerLocation = value;
    }

    /**
     * Gets the value of the signingCertificateBytes property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSigningCertificateBytes() {
        return signingCertificateBytes;
    }

    /**
     * Sets the value of the signingCertificateBytes property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSigningCertificateBytes(byte[] value) {
        this.signingCertificateBytes = ((byte[]) value);
    }

    /**
     * Gets the value of the signingCertificateDigestAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link DigestAlgorithm }
     *     
     */
    public DigestAlgorithm getSigningCertificateDigestAlgorithm() {
        return signingCertificateDigestAlgorithm;
    }

    /**
     * Sets the value of the signingCertificateDigestAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link DigestAlgorithm }
     *     
     */
    public void setSigningCertificateDigestAlgorithm(DigestAlgorithm value) {
        this.signingCertificateDigestAlgorithm = value;
    }

    /**
     * Gets the value of the signingDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSigningDate() {
        return signingDate;
    }

    /**
     * Sets the value of the signingDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSigningDate(XMLGregorianCalendar value) {
        this.signingDate = value;
    }

    /**
     * Gets the value of the timestampDigestAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link DigestAlgorithm }
     *     
     */
    public DigestAlgorithm getTimestampDigestAlgorithm() {
        return timestampDigestAlgorithm;
    }

    /**
     * Sets the value of the timestampDigestAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link DigestAlgorithm }
     *     
     */
    public void setTimestampDigestAlgorithm(DigestAlgorithm value) {
        this.timestampDigestAlgorithm = value;
    }

}
