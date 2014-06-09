package eu.europa.ec.markt.dss.parameter;

import eu.europa.ec.markt.dss.validation102853.SignatureForm;

/**
 * This class regroups the signature parameters related to ASiC form.
 * <p/>
 * <p/>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public class ASiCParameters {

	/**
	 * Indicates if the ZIP comment should be used to store the signed content mime-type.
	 */
	private boolean zipComment = false;

	/**
	 * Indicates the mime-type to be set within the mimetype file. If null the stored mime-type is that of the signed content.
	 */
	private String mimeType = null;

	/**
	 * The default signature form to use within the ASiC containers.
	 */
	private SignatureForm asicSignatureForm = SignatureForm.XAdES;

	public ASiCParameters() {
	}

	public ASiCParameters(final ASiCParameters source) {

		zipComment = source.zipComment;
		mimeType = source.mimeType;
		asicSignatureForm = source.asicSignatureForm;

	}

	public boolean isZipComment() {
		return zipComment;
	}

	/**
	 * This method allows to indicate if the zip comment will contain the mime type.
	 *
	 * @param zipComment
	 */
	public void setZipComment(final boolean zipComment) {
		this.zipComment = zipComment;
	}

	public String getMimeType() {
		return mimeType;
	}

	/**
	 * This method allows to set the mime-type within the mimetype file.
	 *
	 * @param mimeType the mimetype to  store
	 */
	public void setMimeType( final String mimeType) {
		this.mimeType = mimeType;
	}

	public SignatureForm getAsicSignatureForm() {
		return asicSignatureForm;
	}

	/**
	 * Sets the signature form associated with an ASiC container. Only two forms are acceptable: XAdES and CAdES.
	 *
	 * @param asicSignatureForm signature form to associate with the ASiC container.
	 */
	public void setAsicSignatureForm(final SignatureForm asicSignatureForm) {
		this.asicSignatureForm = asicSignatureForm;
	}

}
