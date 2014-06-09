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

package eu.europa.ec.markt.dss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.europa.ec.markt.dss.exception.DSSException;
import eu.europa.ec.markt.dss.signature.DSSDocument;

/**
 * Utility class that contains some XML related method.
 *
 * @version $Revision: 2221 $ - $Date: 2013-06-11 11:53:27 +0200 (Tue, 11 Jun 2013) $
 */

public final class DSSXMLUtils {

	public static final String ID_ATTRIBUTE_NAME = "Id";

	private static DocumentBuilderFactory dbFactory;


	private static final XPathFactory factory = XPathFactory.newInstance();

	private static final NamespaceContext namespacePrefixMapper;

	private static final Map<String, String> namespaces;

	static {

		Init.init();

		namespaces = new HashMap<String, String>();
		namespaces.put("ds", XMLSignature.XMLNS);
		namespaces.put("dsig", XMLSignature.XMLNS);
		namespaces.put("xades", "http://uri.etsi.org/01903/v1.3.2#");
		namespaces.put("xades141", "http://uri.etsi.org/01903/v1.4.1#");
		namespaces.put("xades122", "http://uri.etsi.org/01903/v1.2.2#");
		namespaces.put("xades111", "http://uri.etsi.org/01903/v1.1.1#");

		namespacePrefixMapper = new NamespaceContextMap(namespaces);
	}

	/**
	 * This class is an utility class and cannot be instantiated.
	 */
	private DSSXMLUtils() {

	}

	/**
	 * @param xpathString XPath query string
	 * @return
	 */
	private static XPathExpression createXPathExpression(final String xpathString) {

      /* XPath */
		final XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(namespacePrefixMapper);

		try {
			final XPathExpression expr = xpath.compile(xpathString);
			return expr;
		} catch (XPathExpressionException ex) {
			throw new DSSException(ex);
		}
	}

	/**
	 * Return the Element corresponding to the XPath query.
	 *
	 * @param xmlNode     The node where the search should be performed.
	 * @param xPathString XPath query string
	 * @return
	 */
	public static Element getElement(final Node xmlNode, final String xPathString) {

		return (Element) getNode(xmlNode, xPathString);
	}

	/**
	 * Return the Node corresponding to the XPath query.
	 *
	 * @param xmlNode     The node where the search should be performed.
	 * @param xPathString XPath query string
	 * @return
	 */
	public static Node getNode(final Node xmlNode, final String xPathString) {

		final NodeList list = getNodeList(xmlNode, xPathString);
		if (list.getLength() > 1) {
			throw new DSSException("More than one result for XPath: " + xPathString);
		}
		return list.item(0);
	}

	/**
	 * This method returns the list of children's names for a given {@code Node}.
	 *
	 * @param xmlNode     The node where the search should be performed.
	 * @param xPathString XPath query string
	 * @return {@code List} of children's names
	 */
	public static List<String> getChildrenNames(final Node xmlNode, final String xPathString) {

		ArrayList<String> childrenNames = new ArrayList<String>();

		final Element element = DSSXMLUtils.getElement(xmlNode, xPathString);
		if (element != null) {

			final NodeList unsignedProperties = element.getChildNodes();
			for (int ii = 0; ii < unsignedProperties.getLength(); ++ii) {

				final Node node = unsignedProperties.item(ii);
				childrenNames.add(node.getLocalName());
			}
		}
		return childrenNames;
	}

	/**
	 * Returns the NodeList corresponding to the XPath query.
	 *
	 * @param xmlNode     The node where the search should be performed.
	 * @param xPathString XPath query string
	 * @return
	 * @throws XPathExpressionException
	 */
	public static NodeList getNodeList(final Node xmlNode, final String xPathString) {

		try {

			final XPathExpression expr = createXPathExpression(xPathString);
			final NodeList evaluated = (NodeList) expr.evaluate(xmlNode, XPathConstants.NODESET);
			return evaluated;
		} catch (XPathExpressionException e) {

			throw new DSSException(e);
		}
	}

	/**
	 * Returns the String value of the corresponding to the XPath query.
	 *
	 * @param xmlNode     The node where the search should be performed.
	 * @param xPathString XPath query string
	 * @return string value of the XPath query
	 * @throws XPathExpressionException
	 */
	public static String getValue(final Node xmlNode, final String xPathString) {

		try {

			final XPathExpression xPathExpression = createXPathExpression(xPathString);
			final String string = (String) xPathExpression.evaluate(xmlNode, XPathConstants.STRING);
			return string.trim();
		} catch (XPathExpressionException e) {

			throw new DSSException(e);
		}
	}

	/**
	 * Returns the number of found elements based on the XPath query.
	 *
	 * @param xmlNode
	 * @param xPathString
	 * @return
	 */
	public static int count(final Node xmlNode, final String xPathString) {

		try {

			final XPathExpression xPathExpression = createXPathExpression(xPathString);
			final Double number = (Double) xPathExpression.evaluate(xmlNode, XPathConstants.NUMBER);
			return number.intValue();
		} catch (XPathExpressionException e) {

			throw new DSSException(e);
		}
	}

	/**
	 * Document Object Model (DOM) Level 3 Load and Save Specification See: http://www.w3.org/TR/2004/REC-DOM-Level-3-LS-20040407/
	 *
	 * @param xmlNode The node to be serialized.
	 * @return
	 */
	public static byte[] serializeNode(final Node xmlNode) {

		try {

			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			final LSOutput output = impl.createLSOutput();
			output.setByteStream(buffer);
			writer.write(xmlNode, output);

			final byte[] bytes = buffer.toByteArray();
			return bytes;
		} catch (ClassNotFoundException e) {
			throw new DSSException(e);
		} catch (InstantiationException e) {
			throw new DSSException(e);
		} catch (IllegalAccessException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * An ID attribute can only be dereferenced if it is declared in the validation context. This behaviour is caused by the fact that the attribute does not have attached type of
	 * information. Another solution is to parse the XML against some DTD or XML schema. This process adds the necessary type of information to each ID attribute.
	 *
	 * @param context
	 * @param element
	 */
	public static void recursiveIdBrowse(final DOMValidateContext context, final Element element) {

		for (int ii = 0; ii < element.getChildNodes().getLength(); ii++) {

			final Node node = element.getChildNodes().item(ii);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				final Element childElement = (Element) node;
				if (childElement.hasAttribute(ID_ATTRIBUTE_NAME)) {

					// System.out.println("ID: " + childElement.getTagName() + "/" + childElement.getNamespaceURI());
					context.setIdAttributeNS(childElement, null, ID_ATTRIBUTE_NAME);
				}
				recursiveIdBrowse(context, childElement);
			}
		}
	}

	/**
	 * An ID attribute can only be dereferenced if it is declared in the validation context. This behaviour is caused by the fact that the attribute does not have attached type of
	 * information. Another solution is to parse the XML against some DTD or XML schema. This process adds the necessary type of information to each ID attribute.
	 *
	 * @param element
	 */
	public static void recursiveIdBrowse(final Element element) {

		for (int ii = 0; ii < element.getChildNodes().getLength(); ii++) {

			final Node node = element.getChildNodes().item(ii);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				final Element childElement = (Element) node;
				if (childElement.hasAttribute(ID_ATTRIBUTE_NAME)) {
					// System.out.println("ID: " + childElement.getTagName() + "/" + childElement.getNamespaceURI());
					childElement.setIdAttribute(ID_ATTRIBUTE_NAME, true);
				}
				recursiveIdBrowse(childElement);
			}
		}
	}

	/**
	 * Guarantees that the xmlString builder has been created.
	 *
	 * @throws ParserConfigurationException
	 */
	private static void ensureDocumentBuilder() throws DSSException {

		if (dbFactory != null) {
			return;
		}
		dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
	}

	/**
	 * Creates the new empty Document.
	 *
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document buildDOM() {

		ensureDocumentBuilder();

		try {
			return dbFactory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method returns the {@link org.w3c.dom.Document} created based on the XML string.
	 *
	 * @param xmlString The string representing the dssDocument to be created.
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document buildDOM(final String xmlString) throws DSSException {

		final InputStream input = new ByteArrayInputStream(DSSUtils.getUtf8Bytes(xmlString));
		return buildDOM(input);
	}

	/**
	 * This method returns the {@link org.w3c.dom.Document} created based on byte array.
	 *
	 * @param bytes The bytes array representing the dssDocument to be created.
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document buildDOM(final byte[] bytes) throws DSSException {

		final InputStream input = new ByteArrayInputStream(bytes);
		return buildDOM(input);
	}

	/**
	 * This method returns the {@link org.w3c.dom.Document} created based on the XML inputStream.
	 *
	 * @param inputStream The inputStream stream representing the dssDocument to be created.
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document buildDOM(final InputStream inputStream) throws DSSException {

		try {
			ensureDocumentBuilder();

			final Document rootElement = dbFactory.newDocumentBuilder().parse(inputStream);
			return rootElement;
		} catch (SAXParseException e) {
			throw new DSSException(e);
		} catch (SAXException e) {
			throw new DSSException(e);
		} catch (IOException e) {
			throw new DSSException(e);
		} catch (ParserConfigurationException e) {
			throw new DSSException(e);			
		} finally {
			DSSUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * This method returns the {@link org.w3c.dom.Document} created based on the {@link eu.europa.ec.markt.dss.signature.DSSDocument}.
	 *
	 * @param dssDocument The DSS representation of the document from which the dssDocument is created.
	 * @return
	 * @throws DSSException
	 */
	public static Document buildDOM(final DSSDocument dssDocument) throws DSSException {

		final InputStream input = dssDocument.openStream();
		try {

			final Document doc = buildDOM(input);
			return doc;
		} finally {

			DSSUtils.closeQuietly(input);
		}
	}

	/**
	 * This method writes formatted {@link org.w3c.dom.Node} to the outputStream.
	 *
	 * @param node
	 * @param out
	 */
	public static void printDocument(final Node node, final OutputStream out) {

		try {

			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

			final DOMSource xmlSource = new DOMSource(node);
			final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			final StreamResult outputTarget = new StreamResult(writer);
			transformer.transform(xmlSource, outputTarget);
		} catch (Exception e) {

			// Ignore
		}
	}

	/**
	 * This method writes formatted {@link org.w3c.dom.Node} to the outputStream.
	 *
	 * @param dssDocument
	 * @param out
	 */
	public static void printDocument(final DSSDocument dssDocument, final OutputStream out) {

		try {

			final byte[] bytes = dssDocument.getBytes();
			final Document document = DSSXMLUtils.buildDOM(bytes);

			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

			final DOMSource xmlSource = new DOMSource(document);
			final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			final StreamResult outputTarget = new StreamResult(writer);
			transformer.transform(xmlSource, outputTarget);
		} catch (Exception e) {

			// Ignore
		}
	}

	/**
	 * This method canonicalizes the given array of bytes using the {@code canonicalizationMethod} parameter.
	 *
	 * @param canonicalizationMethod canonicalization method
	 * @param toCanonicalizeBytes    array of bytes to canonicalize
	 * @return array of canonicalized bytes
	 * @throws DSSException if any error is encountered
	 */
	public static byte[] canonicalize(final String canonicalizationMethod, final byte[] toCanonicalizeBytes) throws DSSException {

		try {

			final Canonicalizer c14n = Canonicalizer.getInstance(canonicalizationMethod);
			return c14n.canonicalize(toCanonicalizeBytes);
		} catch (InvalidCanonicalizerException e) {
			throw new DSSException(e);
		} catch (ParserConfigurationException e) {
			throw new DSSException(e);
		} catch (SAXException e) {
			throw new DSSException(e);
		} catch (CanonicalizationException e) {
			throw new DSSException(e);
		} catch (IOException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method canonicalizes the given {@code Node}.
	 *
	 * @param canonicalizationMethod canonicalization method
	 * @param node                   node to canonicalize
	 * @return array of canonicalized bytes
	 */
	public static byte[] canonicalizeSubtree(final String canonicalizationMethod, final Node node) {

		try {

			final Canonicalizer c14n = Canonicalizer.getInstance(canonicalizationMethod);
			final byte[] canonicalized = c14n.canonicalizeSubtree(node);
			return canonicalized;
		} catch (InvalidCanonicalizerException e) {
			throw new DSSException(e);
		} catch (CanonicalizationException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method creates and adds a new XML {@code Element} with text value
	 *
	 * @param document  root document
	 * @param parentDom parent node
	 * @param namespace namespace
	 * @param name      element name
	 * @param value     element text node value
	 * @return added element
	 */
	public static Element addTextElement(final Document document, final Element parentDom, final String namespace, final String name, final String value) {

		final Element dom = document.createElementNS(namespace, name);
		parentDom.appendChild(dom);
		final Text valueNode = document.createTextNode(value);
		dom.appendChild(valueNode);
		return dom;
	}

	/**
	 * This method creates and adds a new XML {@code Element}
	 *
	 * @param document  root document
	 * @param parentDom parent node
	 * @param namespace namespace
	 * @param name      element name
	 * @return added element
	 */
	public static Element addElement(final Document document, final Element parentDom, final String namespace, final String name) {

		final Element dom = document.createElementNS(namespace, name);
		parentDom.appendChild(dom);
		return dom;
	}

	public static byte[] transformDomToByteArray(final Document documentDom) {

		try {

			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(documentDom);

			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			final StreamResult streamResult = new StreamResult(byteArrayOutputStream);
			transformer.transform(source, streamResult);
			byte[] byteArray = byteArrayOutputStream.toByteArray();
			return byteArray;
		} catch (final TransformerException e) {
			throw new DSSException(e);
		}
	}

	/**
	 * This method sets a text node to the given DOM element.
	 *
	 * @param document  root document
	 * @param parentDom parent node
	 * @param text      text to be added
	 */
	public static void setTextNode(final Document document, final Element parentDom, final String text) {

		final Text textNode = document.createTextNode(text);
		parentDom.appendChild(textNode);

	}

	/**
	 * Creates a DOM Document object of the specified type with its document element.
	 *
	 * @param namespaceURI
	 * @param qualifiedName
	 * @param element
	 * @return
	 */
	public static Document createDocument(final String namespaceURI, final String qualifiedName, final Element element) {

		DOMImplementation domImpl;
		try {
			domImpl = dbFactory.newDocumentBuilder().getDOMImplementation();
		} catch (ParserConfigurationException e) {
			throw new DSSException(e);
		}
		final Document newDocument = domImpl.createDocument(namespaceURI, qualifiedName, null);
		final Element newElement = newDocument.getDocumentElement();
		newDocument.adoptNode(element);
		newElement.appendChild(element);

		return newDocument;
	}

	/**
	 * Converts a given {@code Date} to a new {@code XMLGregorianCalendar}.
	 *
	 * @param date the date to be converted
	 * @return the new {@code XMLGregorianCalendar} or null
	 */
	public static XMLGregorianCalendar createXMLGregorianCalendar(final Date date) {

		if (date == null) {
			return null;
		}

		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		try {
			XMLGregorianCalendar gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
			gc.setFractionalSecond(null);
			gc = gc.normalize(); // to UTC = Zulu
			return gc;
		} catch (DatatypeConfigurationException e) {

			// LOG.warn("Unable to properly convert a Date to an XMLGregorianCalendar",e);
		}

		return null;
	}
}