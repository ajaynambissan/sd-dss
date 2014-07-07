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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.europa.ec.markt.dss.DSSXMLUtils;
import eu.europa.ec.markt.dss.validation102853.xades.XAdESSignature;
import eu.europa.ec.markt.dss.validation102853.xades.XPathQueryHolder;

/**
 *
 */
public class XAdESSignatureScopeFinder implements SignatureScopeFinder<XAdESSignature> {

    private final List<String> transformationToIgnore = new ArrayList<String>();

    private final Map<String, String> presentableTransformationNames = new HashMap<String, String>();

    public XAdESSignatureScopeFinder() {
        // @see http://www.w3.org/TR/xmldsig-core/#sec-TransformAlg

        // those transformations don't change the content of the document in a meaningfull way for the signature
        transformationToIgnore.add("http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        transformationToIgnore.add("http://www.w3.org/2000/09/xmldsig#base64");
        transformationToIgnore.add("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
        transformationToIgnore.add("http://www.w3.org/2006/12/xml-c14n11#WithComments");
        transformationToIgnore.add("http://www.w3.org/2001/10/xml-exc-c14n#WithComments");


        // those transformations change the document and must be reported
        presentableTransformationNames.put("http://www.w3.org/2002/06/xmldsig-filter2", "XPath filtering");
        presentableTransformationNames.put("http://www.w3.org/TR/1999/REC-xpath-19991116", "XPath filtering");
        presentableTransformationNames.put("http://www.w3.org/TR/1999/REC-xslt-19991116", "XSLT Transform");

        presentableTransformationNames.put("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "Canonical XML 1.0 (omits comments)");
        presentableTransformationNames.put("http://www.w3.org/2006/12/xml-c14n11#", "Canonical XML 1.1 (omits comments)");
        presentableTransformationNames.put("http://www.w3.org/2001/10/xml-exc-c14n#", "Exclusive Canonical XML (omits comments)");
    }


    @Override
    public List<SignatureScope> findSignatureScope(final XAdESSignature xadesSignature) {

        List<SignatureScope> result = new ArrayList<SignatureScope>();

        Set<Element> unsignedObjects = new HashSet<Element>();
        unsignedObjects.addAll(getSignatureObjects(xadesSignature));
        Set<Element> signedObjects = new HashSet<Element>();

        final List<Element> signatureReferences = xadesSignature.getSignatureReferences();
        for (final Element signatureReference : signatureReferences) {
            final String type = DSSXMLUtils.getValue(signatureReference, "@Type");
            final String uri = DSSXMLUtils.getValue(signatureReference, "@URI");
            final List<String> transformations = getTransformationNames(signatureReference);

            if (StringUtils.isBlank(uri)) {
                // self contained document
                result.add(new XmlRootSignatureScope(transformations));
            } else if (StringUtils.startsWith(uri, "#")) {
                // internal reference
                final String xmlIdOfSignedElement = uri.substring(1);
                Element signedElement = DSSXMLUtils.getElement(xadesSignature.getSignatureElement(), XPathQueryHolder.XPATH_OBJECT + "[@Id='" + xmlIdOfSignedElement + "']");
                if (signedElement != null) {
                    if (unsignedObjects.remove(signedElement)) {
                        signedObjects.add(signedElement);
                        result.add(new XmlElementSignatureScope(xmlIdOfSignedElement, transformations));
                    }
                } else {
                    signedElement = DSSXMLUtils
                          .getElement(xadesSignature.getSignatureElement().getOwnerDocument().getDocumentElement(), "//*" + "[@Id='" + xmlIdOfSignedElement + "']");
                    if (signedElement != null) {

                        final String namespaceURI = signedElement.getNamespaceURI();
                        if (namespaceURI == null || (!namespaceURI.equals("http://uri.etsi.org/01903/v1.3.2#") && !namespaceURI.equals("http://www.w3.org/2000/09/xmldsig#"))) {
                            signedObjects.add(signedElement);
                            result.add(new XmlElementSignatureScope(xmlIdOfSignedElement, transformations));
                        }
                    }
                }
            } else {
                // detached file
                result.add(new FullSignatureScope(uri));
            }
        }
        return result;
    }

    private List<String> getTransformationNames(final Element signatureReference) {

	    final NodeList nodeList = DSSXMLUtils.getNodeList(signatureReference, "./ds:Transforms/ds:Transform");
        List<String> algorithms = new ArrayList<String>(nodeList.getLength());
        for (int ii = 0; ii < nodeList.getLength(); ii++) {
            Element transformation = (Element) nodeList.item(ii);
            final String algorithm = DSSXMLUtils.getValue(transformation, "@Algorithm");
            if (transformationToIgnore.contains(algorithm)) {
                continue;
            }
            if (presentableTransformationNames.containsKey(algorithm)) {
                algorithms.add(presentableTransformationNames.get(algorithm));
            } else {
                algorithms.add(algorithm);
            }
        }
        return algorithms;
    }

    public List<Element> getSignatureObjects(final XAdESSignature xAdESSignature) {

	    final NodeList list = DSSXMLUtils.getNodeList(xAdESSignature.getSignatureElement(), XPathQueryHolder.XPATH_OBJECT);
        List<Element> references = new ArrayList<Element>(list.getLength());
        for (int ii = 0; ii < list.getLength(); ii++) {
            final Node node = list.item(ii);
            final Element element = (Element) node;
            if (DSSXMLUtils.getElement(element, "./xades:QualifyingProperties/xades:SignedProperties") != null) {
                // ignore signed properties
                continue;
            }
            references.add(element);
        }
        return references;
    }
}
