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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDFS;

import dev.SolrEmbeddedServer;

public class TestSARQ_Code 
{

    static final String url = "http://127.0.0.1:8983/solr/sarq/";
    static final SolrServer server = new SolrServer(url);
	
    @BeforeClass public static void startSolrServer() {
    	try {
    		new SolrEmbeddedServer();
    	} catch (Throwable e) {
    		e.printStackTrace();
    	};

		SolrPingResponse response = null;
		int attempts = 0;
		do {
			try {
				response = server.getSolrQueryServer().ping();
			} catch (Throwable e) {
				attempts++;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} while ( ( ( response == null ) || ( response.getStatus() != 0 ) ) && ( attempts < 40 ) );
    }
    
    @Before public void setUp() throws Exception {
    	server.getSolrUpdateServer().deleteByQuery("*:*");
//    	server.getSolrUpdateServer().commit(true, true);
    }
    
    static final String datafile = "src/test/resources/SARQ/data-1.ttl" ;
    
    public void test_ext_1()
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        nIter = index.search("foo") ;
        Resource r2 = (Resource)nIter.next() ;
        assertEquals(r, r2) ;
    }
    
    @Test public void test_ext_2()
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Model model = ModelFactory.createDefaultModel() ;
        Literal lit = model.createLiteral("example") ;
        b.index(lit, "foo") ;
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        nIter = index.search("foo") ;
        Node lit2 = SARQ.build(nIter.next()) ;
        assertEquals(lit.asNode(), lit2) ;
    }

    @Test public void test_ext_3()
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource bnode = model.createResource() ;
        b.index(bnode, "foo") ;
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        nIter = index.search("foo") ;
        Node bnode2 = SARQ.build(nIter.next()) ;
        assertEquals(bnode.asNode(), bnode2) ;
        assertTrue(bnode2.isBlank()) ;
    }

    @Test public void test_ext_4()
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("bah") ;
        assertFalse(nIter.hasNext()) ;
    }
    
    @Test public void test_ext_5()
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        StringReader sr = new StringReader("foo") ;
        b.index(r, sr) ;
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        nIter = index.search("foo") ;
        Node r2 = SARQ.build(nIter.next()) ;
        assertEquals(r.asNode(), r2) ;
    }
    
    // Test what happens when the index is updated after a reader index (LARQIndex) is created
    @Test public void test_ext_6()
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r1 = model.createResource("http://example/r1") ;
        Resource r2 = model.createResource("http://example/r2") ;
        
        StringReader sr = new StringReader("R1") ;
        b.index(r1, sr) ;
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("R1") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        nIter = index.search("R2") ;
        assertEquals(0, TestSARQUtils.count(nIter)) ;
        
        // Add r2.
        b = new IndexBuilderNode(url) ;
        b.index(r2, new StringReader("R2")) ;
        b.commit() ;

        // Old index - can see R2... it's Solr!
        nIter = index.search("R2") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        
        // New index - can see R2
        index = b.getSolrServer() ;
        nIter = index.search("R2") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
    }
    
    @Test public void test_index_literal_1()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderString(url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search both DC title and RDFS label
        assertEquals(3,TestSARQUtils.count(nIter)) ;
    }

    @Test public void test_index_literal_2()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderString(DC.title, url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search just DC title
        assertEquals(2,TestSARQUtils.count(nIter)) ;
    } 
    
    @Test public void test_index_literal_3()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderString(url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search both DC title and RDFS label
        for ( ; nIter.hasNext(); )
        {
            Node n = SARQ.build(nIter.next()) ;
            assertTrue(n.isLiteral()) ;
            assertTrue(model.getGraph().contains(Node.ANY, Node.ANY, n)) ;
            boolean b = model.getGraph().contains(Node.ANY, DC.title.asNode(), n) ||
                        model.getGraph().contains(Node.ANY, RDFS.label.asNode(), n) ;
            assertTrue("DC.title or RDFS.label", b) ;
        }
    }

    @Test public void test_index_literal_4()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderString(DC.title, url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search both DC title and RDFS label
        for ( ; nIter.hasNext(); )
        {
        	Node n = SARQ.build(nIter.next()) ;
            assertTrue(n.isLiteral()) ;
            assertTrue(model.getGraph().contains(Node.ANY, DC.title.asNode(), n)) ;
            assertFalse(model.getGraph().contains(Node.ANY, RDFS.label.asNode(), n)) ;
        }
    }
    
    
    @Test public void test_index_subject_1()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderSubject(url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search both DC title and RDFS label
        assertEquals(3,TestSARQUtils.count(nIter)) ;
    }
    
    @Test public void test_index_subject_2()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderSubject(DC.title, url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search both DC title and RDFS label
        assertEquals(2,TestSARQUtils.count(nIter)) ;
    }

    @Test public void test_index_subject_3()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderSubject(url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        // Search both DC title and RDFS label
        for ( ; nIter.hasNext(); )
        {
        	Node n = SARQ.build(nIter.next()) ;
            assertTrue(n.isURI()) ;
            assertTrue(model.getGraph().contains(n, Node.ANY, Node.ANY)) ;
            boolean b = model.getGraph().contains(n, DC.title.asNode(), Node.ANY) ||
                        model.getGraph().contains(n, RDFS.label.asNode(), Node.ANY) ;
            assertTrue("subject with DC.title or RDFS.label", b) ;
        }
    }

    @Test public void test_index_subject_4()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderSubject(DC.title, url)) ; 
        Iterator<SolrDocument> nIter = index.search("+document") ;
        for ( ; nIter.hasNext(); )
        {
        	Node n = SARQ.build(nIter.next()) ;
            assertTrue(n.isURI()) ;
            assertTrue(model.getGraph().contains(n, Node.ANY, Node.ANY)) ;
            assertTrue(model.getGraph().contains(n, DC.title.asNode(), Node.ANY)) ;
        }
    }

    // Negative searches
    @Test public void test_negative_1()
    {
    	SolrServer index = TestSARQUtils.createIndex(datafile, new IndexBuilderString(url)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }

    @Test public void test_negative_2()
    {
    	SolrServer index = TestSARQUtils.createIndex(datafile, new IndexBuilderString(DC.title, url)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }

    @Test public void test_negative_3()
    {
    	SolrServer index = TestSARQUtils.createIndex(datafile, new IndexBuilderSubject(url)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }
    
    @Test public void test_negative_4()
    {
    	SolrServer index = TestSARQUtils.createIndex(datafile, new IndexBuilderSubject(DC.title, url)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }
    
    @Test public void test_search_index_registration_1()
    {
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderString(url)) ;
        assertFalse(ARQ.getContext().isDefined(SARQ.indexKey)) ;
        try {
            SARQ.setDefaultIndex(index) ;
            assertTrue(ARQ.getContext().isDefined(SARQ.indexKey)) ;
            QueryExecution qExec = TestSARQUtils.query(model, "{ ?lit sarq:search '+document' }") ;
            ResultSet rs = qExec.execSelect() ;
            assertEquals(3, TestSARQUtils.count(rs)) ;
            qExec.close() ;
            SARQ.removeDefaultIndex() ;
            assertFalse(ARQ.getContext().isDefined(SARQ.indexKey)) ;
        } finally { SARQ.removeDefaultIndex() ; }
    }
    
    @Test public void test_search_index_registration_2()
    {
        Model model = ModelFactory.createDefaultModel() ;
        SolrServer index = TestSARQUtils.createIndex(model, datafile, new IndexBuilderString(url)) ;
        
        assertFalse(ARQ.getContext().isDefined(SARQ.indexKey)) ;
        QueryExecution qExec = TestSARQUtils.query(model, "{ ?lit sarq:search '+document' }") ;
        
        try {
            SARQ.setDefaultIndex(qExec.getContext(), index) ;
            assertFalse(ARQ.getContext().isDefined(SARQ.indexKey)) ;
            assertTrue(qExec.getContext().isDefined(SARQ.indexKey)) ;
            
            ResultSet rs = qExec.execSelect() ;
            assertEquals(3, TestSARQUtils.count(rs)) ;
            qExec.close() ;

            SARQ.removeDefaultIndex(qExec.getContext()) ;
            assertFalse(qExec.getContext().isDefined(SARQ.indexKey)) ;
            assertFalse(ARQ.getContext().isDefined(SARQ.indexKey)) ;
        } finally { SARQ.removeDefaultIndex() ; }
    }
    
    @Test public void test_remove_1() 
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.unindex(r, "foo");
        b.commit();
        
        SolrServer index = b.getSolrServer() ;
        assertFalse(index.search("foo").hasNext()) ;
    }
    
    @Test public void test_remove_2() throws Exception 
    {
        IndexBuilderString b = new IndexBuilderString(url);
    	Model model = ModelFactory.createDefaultModel();
        model.register(b) ;
        FileManager.get().readModel(model, datafile) ;
        model.removeAll(ResourceFactory.createResource("http://example/doc3"), (Property)null, (RDFNode)null);
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        assertFalse(index.search("keyword").hasNext()) ;    	
    }

    @Test public void test_remove_3() throws IOException 
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        StringReader sr = new StringReader("foo") ;
        b.index(r, sr) ;
        sr = new StringReader("foo") ;
        b.unindex(r, sr);
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        assertFalse(index.search("foo").hasNext()) ;
    }

    @Test public void test_duplicates_1() 
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.index(r, "foo") ;
        b.commit() ;

        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
    }
    
    @Test public void test_duplicates_2() throws Exception 
    {
        IndexBuilderString b = new IndexBuilderString(url);
    	Model model = ModelFactory.createDefaultModel();
        model.register(b) ;
        model.add(model.createResource("http://example/r"), RDFS.label, "foo");
        model.add(model.createResource("http://example/r"), RDFS.label, "foo");
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
    }

    @Test public void test_duplicates_3() 
    {
        IndexBuilderNode b = new IndexBuilderNode(url) ;
        Resource r1 = ResourceFactory.createResource() ;
        Resource r2 = ResourceFactory.createResource() ;
        b.index(r1, "foo") ;
        b.index(r1, "foo") ;
        b.index(r1, "bar") ;
        b.index(r2, "foo") ;
        b.index(r2, "foo") ;
        b.index(r2, "bar") ;
        b.commit() ;

        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(2, TestSARQUtils.count(nIter)) ;
        nIter = index.search("bar") ;
        assertEquals(2, TestSARQUtils.count(nIter)) ;
    }
    
    @Test public void test_duplicates_4() throws Exception 
    {
        IndexBuilderString b = new IndexBuilderString(url);
    	Model model = ModelFactory.createDefaultModel();
        model.register(b) ;
        model.add(model.createResource(), RDFS.label, "foo");
        model.add(model.createResource(), RDFS.label, "foo");
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
    }
    
    @Test public void test_duplicates_5() throws Exception 
    {
        IndexBuilderNode b = new IndexBuilderNode(url);
        
        Resource blank = ResourceFactory.createResource() ;
        b.index(blank, "foo");
        b.index(blank, "foo");
        b.index(blank, "bar");
        b.commit() ;
        
        SolrServer index = b.getSolrServer() ;
        Iterator<SolrDocument> nIter = index.search("foo") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        assertEquals(blank.asNode(), SARQ.build(index.search("foo").next()));
        nIter = index.search("bar") ;
        assertEquals(1, TestSARQUtils.count(nIter)) ;
        assertEquals(blank.asNode(), SARQ.build(index.search("bar").next()));
    }

}
