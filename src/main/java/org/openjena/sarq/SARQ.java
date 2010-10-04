/*
 * Copyright © 2010 Talis Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openjena.sarq;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.openjena.sarq.pfunction.search;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.Symbol;

public class SARQ {

	/** The SARQ property function library URI space */
    public static final String SARQPropertyFunctionLibraryURI = "http://openjena.org/SARQ/property#" ;
    
    static {
    	PropertyFunctionRegistry.get().put(SARQPropertyFunctionLibraryURI + "search", search.class);
    }

    public static final String fId                 = "id" ;

    // The field that is the index
    public static final String fText               = "text" ;

    // Object literals
    public static final String fLex                 = "lex" ;
    public static final String fLang                = "lang" ;
    public static final String fDataType            = "datatype" ;
    // Object URI
    public static final String fURI                 = "uri" ;
    // Object bnode
    public static final String fBNodeID             = "bnode" ;

    // The symbol used to register the index in the query context
    public static final Symbol indexKey     = ARQConstants.allocSymbol("solr") ;

    public static void setDefaultIndex(SolrServer index) { 
    	setDefaultIndex(ARQ.getContext(), index) ; 
    }
    
    public static void setDefaultIndex(Context context, SolrServer index) { 
    	context.set(SARQ.indexKey, index) ; 
    }
    
    public static SolrServer getDefaultIndex() { 
    	return getDefaultIndex(ARQ.getContext()) ; 
    }
    
    public static SolrServer getDefaultIndex(Context context) { 
    	return (SolrServer)context.get(SARQ.indexKey) ; 
    }
    
    public static void removeDefaultIndex() { 
    	removeDefaultIndex(ARQ.getContext()) ; 
    }
    
    public static void removeDefaultIndex(Context context) { 
    	context.unset(SARQ.indexKey) ; 
    }

    public static void index(SolrInputDocument doc, Node indexNode) {
        if ( ! indexNode.isLiteral() ) {
            throw new SARQException("Not a literal: " + indexNode) ;
        }
        index(doc, indexNode, indexNode.getLiteralLexicalForm()) ;
    }        
     
    public static void index(SolrInputDocument doc, Node node, String indexContent) {
    	doc.addField(SARQ.fId, hash(node, indexContent));
        doc.addField(SARQ.fText, indexContent);
    }        
     
    public static void index(SolrInputDocument doc, Node node, Reader indexContent) {
    	String content = read(indexContent);
    	doc.addField(SARQ.fId, hash(node, content));
        doc.addField(SARQ.fText, content) ;
    }

	public static String unindex(Node node, String indexStr) {
		return hash(node, indexStr);
	}

	public static String unindex(Node node, Reader indexContent) {
		return hash(node, indexContent);
	}
    
    public static void store(SolrInputDocument doc, Node node) {
        if ( node.isLiteral() ) {
            storeLiteral(doc, (Node_Literal)node) ;
        } else if ( node.isURI() ) {
            storeURI(doc, (Node_URI)node) ;
        } else if ( node.isBlank() ) {
            storeBNode(doc, (Node_Blank)node) ;
        } else {
            throw new SARQException("Can't store: "+node) ;
        }
    }

    public static Node build(SolrDocument doc) {
        String lex = (String)doc.getFirstValue(SARQ.fLex) ;
        if ( lex != null ) {
            return buildLiteral(doc) ;
        }
        String uri = (String)doc.getFirstValue(SARQ.fURI) ;
        if ( uri != null ) {
            return Node.createURI(uri) ;
        }
        String bnode = (String)doc.getFirstValue(SARQ.fBNodeID) ;
        if ( bnode != null ) {
            return Node.createAnon(new AnonId(bnode)) ;
        }
        throw new SARQException("Can't build: " + doc) ;
    }
    
    public static boolean isString(Literal literal) {
        RDFDatatype dtype = literal.getDatatype() ;
        if ( dtype == null ) {
            return true ;
        }
        if ( dtype.equals(XSDDatatype.XSDstring) ) {
            return true ;
        }
        return false ;
    }
    
    private static void storeURI(SolrInputDocument doc, Node_URI node) { 
        String x = node.getURI() ;
        // TODO - Think about this... should "text" be multi-valued?
        // doc.addField(SARQ.fText, x) ;
        doc.addField(SARQ.fURI, x) ;
    }

    private static void storeBNode(SolrInputDocument doc, Node_Blank node) { 
        String x = node.getBlankNodeLabel() ;
        // doc.addField(SARQ.fText, x) ;
        doc.addField(SARQ.fBNodeID, x) ;
    }
    
    private static void storeLiteral(SolrInputDocument doc, Node_Literal node) {
        String lex = node.getLiteralLexicalForm() ;
        String datatype = node.getLiteralDatatypeURI() ;
        String lang = node.getLiteralLanguage() ;

        doc.addField(SARQ.fLex, lex) ;
        
        if ( lang != null ) {
            doc.addField(SARQ.fLang, lang) ;
        }

        if ( datatype != null ) {
            doc.addField(SARQ.fDataType, datatype) ;
        }
    }
    
    private static Node buildLiteral(SolrDocument doc) {
        String lex = (String)doc.getFirstValue(SARQ.fLex) ;
        if ( lex == null ) {
            return null ;
        }
        String datatype = (String)doc.getFirstValue(SARQ.fDataType) ;
        String lang = (String)doc.getFirstValue(SARQ.fLang) ;
        return NodeFactory.createLiteralNode(lex, lang, datatype) ;
    }

    private static String hash (String str) 
    {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes("UTF8"));
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(hash);
            return bigInt.toString();
        } catch (NoSuchAlgorithmException e) {
        	new SARQException("hash", e);
        } catch (UnsupportedEncodingException e) {
        	new SARQException("hash", e);
        }

        return null;
    }
    
    private static String hash (Node node, String str) 
    {
        String lexForm = null ; 
        String datatypeStr = "" ;
        String langStr = "" ;
        
        if ( node.isURI() ) {
        	lexForm = node.getURI() ;
        } else if ( node.isLiteral() ) {
        	lexForm = node.getLiteralLexicalForm() ;
            datatypeStr = node.getLiteralDatatypeURI() ;
            langStr = node.getLiteralLanguage() ;
        } else if ( node.isBlank() ) {
        	lexForm = node.getBlankNodeLabel() ;
        } else {
        	throw new SARQException("Unable to hash node:"+node) ;
        }

        return hash (lexForm + "|" + langStr + "|" + datatypeStr + "|" + str);
    }
    
    private static String hash (Node node, Reader reader)
    {
        String lexForm = null ; 
        String datatypeStr = "" ;
        String langStr = "" ;
        
        if ( node.isURI() ) {
        	lexForm = node.getURI() ;
        } else if ( node.isLiteral() ) {
        	lexForm = node.getLiteralLexicalForm() ;
            datatypeStr = node.getLiteralDatatypeURI() ;
            langStr = node.getLiteralLanguage() ;
        } else if ( node.isBlank() ) {
        	lexForm = node.getBlankNodeLabel() ;
        } else {
        	throw new SARQException("Unable to hash node:"+node) ;
        }
    	
    	StringBuffer sb = new StringBuffer();
		try {
	        int charsRead;
			do {
		    	char[] buffer = new char[1024];
		        int offset = 0;
		        int length = buffer.length;
		        charsRead = 0;
				while (offset < buffer.length) {
					charsRead = reader.read(buffer, offset, length);
					if (charsRead == -1)
						break;
					offset += charsRead;
					length -= charsRead;
				}
				sb.append(buffer);
			} while (charsRead != -1);
			reader.reset();
		} catch (IOException e) {
			new SARQException("hash", e);
		}
		
		return hash (lexForm + "|" + langStr + "|" + datatypeStr + "|" + sb.toString());
    }
    
    private static String read(Reader reader) {
    	StringBuffer sb = new StringBuffer();
		try {
	        int charsRead;
			do {
		    	char[] buffer = new char[1024];
		        int offset = 0;
		        int length = buffer.length;
		        charsRead = 0;
				while (offset < buffer.length) {
					charsRead = reader.read(buffer, offset, length);
					if (charsRead == -1)
						break;
					offset += charsRead;
					length -= charsRead;
				}
				sb.append(buffer);
			} while (charsRead != -1);
			reader.reset();
		} catch (IOException e) {
			new SARQException("read", e);
		}

		return sb.toString();
    }
    
}