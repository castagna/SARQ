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

import java.io.Reader;

import org.apache.solr.common.SolrInputDocument;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class IndexBuilderNode extends IndexBuilderBase {

	public IndexBuilderNode(String url) { 
    	super(url) ; 
    }
    
    public void index(RDFNode rdfNode, String indexStr) {
    	SolrInputDocument doc = new SolrInputDocument() ;
        SARQ.store(doc, rdfNode.asNode()) ;
        SARQ.index(doc, rdfNode.asNode(), indexStr) ;
        getSolrServer().addDocument(doc) ;
    }
   
    public void index(RDFNode rdfNode, Reader indexStream) {
    	SolrInputDocument doc = new SolrInputDocument() ;
    	SARQ.store(doc, rdfNode.asNode()) ;
        SARQ.index(doc, rdfNode.asNode(), indexStream) ;
        getSolrServer().addDocument(doc) ;
    }
    
    public void index(Node node, String indexStr) {
    	SolrInputDocument doc = new SolrInputDocument() ;
        SARQ.store(doc, node) ;
        SARQ.index(doc, node, indexStr) ;
        getSolrServer().addDocument(doc) ;
    }
   
    public void index(Node node, Reader indexStream) {
    	SolrInputDocument doc = new SolrInputDocument() ;
        SARQ.store(doc, node) ;
        SARQ.index(doc, node, indexStream) ;
        getSolrServer().addDocument(doc) ;
    }

	public void unindex(RDFNode node, Reader indexStream) {
		unindex(node.asNode(), indexStream);
	}
    
	public void unindex(RDFNode node, String indexStr) {
		unindex(node.asNode(), indexStr);
	}
    
	public void unindex(Node node, Reader indexStream) {
		try {
			String id = SARQ.unindex(node, indexStream);
			getSolrServer().getSolrUpdateServer().deleteById(id);			
        } catch (Exception ex) { 
        	throw new SARQException("unindex", ex) ; 
        } 
	}

	public void unindex(Node node, String indexStr) {
		try {
			String id = SARQ.unindex(node, indexStr);
			getSolrServer().getSolrUpdateServer().deleteById(id);
        } catch (Exception ex) { 
        	throw new SARQException("unindex", ex) ; 
        } 
	}

}
