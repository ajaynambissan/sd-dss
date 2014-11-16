package eu.europa.ec.markt.dss.applet.component.model;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.parser.XSOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

/**
 * Created by kaczmani on 10/04/2014.
 */
public abstract class XmlDomTreeModelAdapter implements TreeModel {
    protected List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    //XmlDom doc to view as a tree
    private Document document;
    private final Map<XsdNode, Object> xsdTree;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    //constructor used to set the document to view
    public XmlDomTreeModelAdapter(Document doc, URL xsdTree) {
        document = doc;
        this.xsdTree = getXsdElements(xsdTree);
    }

    public Map<XsdNode, Object> getXsdTree() {
        return xsdTree;
    }

    /**
     * Get XSD schema in hashMap tree
     *
     * @return HashMap<String, Object>
     * @param xsdUrl
     */
    private Map<XsdNode, Object> getXsdElements(URL xsdUrl) {
        XSSchema xsSchema = loadXsd(xsdUrl);
        //---
        Map<XsdNode, Object> result = new LinkedHashMap<XsdNode, Object>();
        Map<XsdNode, Object> hashMap = new LinkedHashMap<XsdNode, Object>();
        Iterator<XSElementDecl> itre = xsSchema.iterateElementDecls();
        //---
        String path = "";
        while (itre.hasNext()) {
            XSElementDecl xsElementDecl = itre.next();

            final XsdNode xsdNode = new XsdNode(xsElementDecl.getName(), XsdNodeType.ELEMENT, XsdNodeCardinality.ONCE_EXACTLY);
            result.put(xsdNode, hashMap);
            path = xsElementDecl.getName();
            XSComplexType xsComplexType = xsElementDecl.getType().asComplexType();
            if (xsComplexType != null) {
                XSContentType xsContentType = xsComplexType.getContentType();
                XSParticle xsParticle = xsContentType.asParticle();
                getElementsRecursively(path, hashMap, xsParticle);
            }
        }
        return result;
    }

    /*
     * recursive helper method of getXmlElements
     * note that since we don't know the "deepness" of the
     * schema a recursive way of implementation was necessary
     */
    private void getElementsRecursively(String path, Map<XsdNode, Object> hm, XSParticle xsParticle) {
        if (xsParticle != null) {
            XSTerm term = xsParticle.getTerm();
            if (term.isElementDecl()) {
                final XSElementDecl xsElementDecl = term.asElementDecl();
                final String xsElementDeclName = xsElementDecl.getName();
                path = path + "/" + xsElementDeclName;
                XSComplexType xsComplexType = xsElementDecl.getType().asComplexType();
                //---
                if (xsComplexType == null) {
                    XsdNodeCardinality xsdNodeCardinality = getXmlItemCardinality(xsParticle);

                    // subchild has a text node
                    final XsdNode childNode = new XsdNode(path, XsdNodeType.ELEMENT, xsdNodeCardinality);
                    if (xsElementDecl.getType().isSimpleType()) {
                        final Map<XsdNode, Object> subMap = new LinkedHashMap<XsdNode, Object>(1);
                        subMap.put(new XsdNode(path, XsdNodeType.TEXT, XsdNodeCardinality.ONCE_EXACTLY), null);
                        hm.put(childNode, subMap);
                    } else {
                        hm.put(childNode, null);
                    }


                } else {
                    XSContentType xscont = xsComplexType.getContentType();
                    XSParticle particle = xscont.asParticle();
                    Map<XsdNode, Object> newHm = new LinkedHashMap<XsdNode, Object>();


                    final XsdNode childNode = new XsdNode(path, XsdNodeType.ELEMENT, getXmlItemCardinality(xsParticle));
                    //Attributes
                    Collection<? extends XSAttributeUse> attributeList = xsComplexType.getAttributeUses();
                    for (XSAttributeUse attr : attributeList) {
                        XSAttributeDecl attrInfo = attr.getDecl();
                        final XsdNode grandChildNode = new XsdNode(path + "/" + attrInfo.getName(), XsdNodeType.ATTRIBUTE, XsdNodeCardinality.ONCE_OPTIONALY);
                        newHm.put(grandChildNode, null);
                    }

                    getElementsRecursively(path, newHm, particle);

                    if (((ComplexTypeImpl) xsComplexType).getType().getBaseType().getName().equalsIgnoreCase("string")) {
                        newHm.put(new XsdNode(path, XsdNodeType.TEXT, XsdNodeCardinality.ONCE_EXACTLY), null);
                    }

                    hm.put(childNode, newHm);


                }
                //---
            } else if (term.isModelGroup()) {
                XSModelGroup model = term.asModelGroup();
                XSParticle[] parr = model.getChildren();
                for (XSParticle partemp : parr) {
                    getElementsRecursively(path, hm, partemp);
                }
            }
        }
    }

    private XsdNodeCardinality getXmlItemCardinality(XSParticle xsParticle) {
        if (xsParticle.getMinOccurs().compareTo(BigInteger.valueOf(0)) == 0) {
            // minOccurs == 0
            if (xsParticle.getMaxOccurs().compareTo(BigInteger.valueOf(XSParticle.UNBOUNDED)) != 0) {
                // maxOccurs != UNBOUNDED
                return XsdNodeCardinality.ONCE_OPTIONALY;
            } else {
                // maxOccurs == UNBOUNDED
                return XsdNodeCardinality.ZERO_OR_MORE;
            }
        } else {
            return XsdNodeCardinality.ONCE_EXACTLY;
        }
    }

    /**
     * Load xsd from file .xsd
     *
     * @return XSOM.XSSchema
     * @param xsdUrl
     */
    private XSSchema loadXsd(URL xsdUrl) {
        XSOMParser parser = new XSOMParser();
        XSSchemaSet xsSchemaSet = null;
        try {
            //			System.out.println("##########" + xsdUrl.toString());
            parser.parse(xsdUrl.openStream());
            xsSchemaSet = parser.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Object[] schemaArray = xsSchemaSet.getSchemas().toArray();
        XSSchema s = null;
        if (schemaArray.length > 1) {
            s = (XSSchema) xsSchemaSet.getSchemas().toArray()[1];
        }
        return s;
    }


    //override from TreeModel
    public Object getRoot() {
        if(document == null) return null;
        return new XmlDomAdapterNode(null, document.getDocumentElement(),false);
    }

    //override from TreeModel
    public Object getChild(Object parent, int index) {
        XmlDomAdapterNode node = (XmlDomAdapterNode) parent;
        return node.child(index);
    }

    //override from TreeModel
    public int getIndexOfChild(Object parent, Object child) {
        XmlDomAdapterNode node = (XmlDomAdapterNode) parent;
        return node.index((XmlDomAdapterNode) child);
    }

    //override from TreeModel
    public int getChildCount(Object parent) {
        XmlDomAdapterNode xmlDomNode = (XmlDomAdapterNode)parent;
        return xmlDomNode.childCount();
    }

    //override from TreeModel
    public boolean isLeaf(Object node) {
        boolean isLeaf = false;
        XmlDomAdapterNode xmlDomNode = (XmlDomAdapterNode)node;
        if(xmlDomNode.isAttribute()){
            return false;
        }
        if(xmlDomNode.node.getChildNodes() == null ){
            isLeaf = true;
        }else{
            if(xmlDomNode.node.getChildNodes().getLength() == 1){
                //String data = xmlDomNode.node.getFirstChild().getNodeValue();
                if(xmlDomNode.node instanceof Attr){
                    isLeaf = true;
                   // xmlDomNode.setText(true);
                }else if(xmlDomNode.node instanceof Element){
                    isLeaf = false;
                }

            }else {
                int nbAttribute = 0;
                if(xmlDomNode.node.getAttributes() != null){
                    nbAttribute = xmlDomNode.node.getAttributes().getLength();
                }
                isLeaf = (xmlDomNode.node.getChildNodes().getLength() + nbAttribute) == 0;
            }
        }
        return isLeaf;
    }

    //override from TreeModel
    public void valueForPathChanged(TreePath path, Object newValue) {
        // Null. We won't be making changes in the GUI
        // If we did, we would ensure the new value was really new,
        // adjust the model, and then fire a TreeNodesChanged event.
    }


    /*
     * Use these methods to add and remove event listeners.
     * (Needed to satisfy TreeModel interface, but not used.)
     */

    // override from TreeModel
    public void addTreeModelListener(TreeModelListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    // override from TreeModel
    public void removeTreeModelListener(TreeModelListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /*
	 * Invoke these methods to inform listeners of changes.
	  * Methods taken from TreeModelSupport class described at
	 * http://java.sun.com/products/jfc/tsc/articles/jtree/index.html That
	 * architecture (produced by Tom Santos and Steve Wilson) is more elegant.
	 */
    public void fireTreeNodesChanged(TreeModelEvent e) {
        Iterator listenersIt = listeners.iterator();
        while (listenersIt.hasNext()) {
            TreeModelListener listener = (TreeModelListener) listenersIt.next();
            listener.treeNodesChanged(e);
        }
    }
    public void fireTreeNodesInserted(TreeModelEvent e) {
        Iterator listenersIt = listeners.iterator();
        while (listenersIt.hasNext()) {
            TreeModelListener listener = (TreeModelListener) listenersIt.next();
            listener.treeNodesInserted(e);
        }
    }
    public void fireTreeNodesRemoved(TreeModelEvent e) {
        Iterator listenersIt = listeners.iterator();
        while (listenersIt.hasNext()) {
            TreeModelListener listener = (TreeModelListener) listenersIt.next();
            listener.treeNodesRemoved(e);
        }
    }
    public void fireTreeStructureChanged(TreeModelEvent e) {
        Iterator listenersIt = listeners.iterator();
        while (listenersIt.hasNext()) {
            TreeModelListener listener = (TreeModelListener) listenersIt.next();
            listener.treeStructureChanged(e);
        }
    }
}

