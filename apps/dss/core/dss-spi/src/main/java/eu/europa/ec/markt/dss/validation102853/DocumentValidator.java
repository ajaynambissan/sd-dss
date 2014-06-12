package eu.europa.ec.markt.dss.validation102853;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import eu.europa.ec.markt.dss.signature.DSSDocument;
import eu.europa.ec.markt.dss.validation102853.report.DetailedReport;
import eu.europa.ec.markt.dss.validation102853.report.DiagnosticData;
import eu.europa.ec.markt.dss.validation102853.report.Reports;
import eu.europa.ec.markt.dss.validation102853.report.SimpleReport;

/**
 * TODO
 * <p/>
 * <p/>
 * DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public interface DocumentValidator {

	/**
	 * The document to validate, in the case of ASiC-S container this method returns the signature.
	 *
	 * @return
	 */
	DSSDocument getDocument();

	/**
	 * This method returns the signed document in the case of the detached signatures.
	 *
	 * @return
	 */
	DSSDocument getExternalContent();

	/**
	 * Retrieves the signatures found in the document
	 *
	 * @return a list of AdvancedSignatures for validation purposes
	 */
	List<AdvancedSignature> getSignatures();

	void setCertificateVerifier(final CertificateVerifier certVerifier);

	void setExternalContent(final DSSDocument externalContent);

	/**
	 * This method allows to define the signing certificate. It is useful in the case of ,non AdES signatures.
	 *
	 * @param x509Certificate
	 */
	void defineSigningCertificate(final X509Certificate x509Certificate);

	void setPolicyFile(final File policyDocument);

	void setPolicyFile(final String signatureId, final File policyDocument);

	Reports validateDocument();

	Reports validateDocument(final URL validationPolicyURL);

	Reports validateDocument(final String policyResourcePath);

	Reports validateDocument(final File policyFile);

	Reports validateDocument(final InputStream policyDataStream);

	DiagnosticData getDiagnosticData();

	SimpleReport getSimpleReport();

	DetailedReport getDetailedReport();

	Reports getReports() ;

	void printReports();
}
