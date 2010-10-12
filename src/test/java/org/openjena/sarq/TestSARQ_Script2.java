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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.junit.QueryTest;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;

public class TestSARQ_Script2 {

    static final String root = "src/test/resources/SARQ/" ;
    private EmbeddedSolrServer server;
    
    @Before public void setUp() throws Exception {
    	System.setProperty("solr.solr.home", "solr/sarq");
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer = initializer.initialize();
		server = new EmbeddedSolrServer(coreContainer, "");
    }
    
    @After public void tearDown() throws SolrServerException, IOException {
    	server.deleteByQuery("*:*");
    	server.commit(false, false);
    	server = null;
    }
    
    static void runTestScript(String queryFile, String dataFile, String resultsFile, IndexBuilderModel builder) {
        Query query = QueryFactory.read(root+queryFile) ;
        Model model = ModelFactory.createDefaultModel() ; 
        model.register(builder) ;    
        FileManager.get().readModel(model, root+dataFile) ;
        model.unregister(builder) ;
        builder.commit();
        
        SolrServer server = builder.getSolrServer() ;
        SARQ.setDefaultIndex(server) ;
        
        QueryExecution qe = QueryExecutionFactory.create(query, model) ;
        ResultSetRewindable rsExpected = ResultSetFactory.makeRewindable(ResultSetFactory.load(root+resultsFile)) ;
        ResultSetRewindable rsActual = ResultSetFactory.makeRewindable(qe.execSelect()) ;
        boolean b = QueryTest.resultSetEquivalent(query, rsActual, rsExpected) ;
        if ( ! b ) {
            rsActual.reset() ;
            rsExpected.reset() ;
            System.out.println("==== Different (SARQ)") ;
            System.out.println("== Actual") ;
            ResultSetFormatter.out(rsActual) ;
            System.out.println("== Expected") ;
            ResultSetFormatter.out(rsExpected) ;
        }
        
        assertTrue(b) ;
        qe.close() ; 
        SARQ.removeDefaultIndex() ;
    }
    
    @Test public void test_larq_1() { 
    	runTestScript("larq-q-1.rq", "data-1.ttl", "results-1.srj", new IndexBuilderString(server)) ; 
    }

    @Test public void test_larq_2() { 
    	runTestScript("larq-q-2.rq", "data-1.ttl", "results-2.srj", new IndexBuilderString(DC.title, server)) ; 
    }

    @Test public void test_larq_3() { 
    	runTestScript("larq-q-3.rq", "data-1.ttl", "results-3.srj", new IndexBuilderSubject(DC.title, server)) ; 
    }
    
    @Test public void test_larq_4() { 
    	runTestScript("larq-q-4.rq", "data-1.ttl", "results-4.srj", new IndexBuilderString(server)) ; 
    }
    
    @Test public void test_larq_5() { 
    	runTestScript("larq-q-5.rq", "data-1.ttl", "results-5.srj", new IndexBuilderString(server)) ; 
    }

    @Test public void test_larq_6() { 
    	runTestScript("larq-q-6.rq", "data-1.ttl", "results-6.srj", new IndexBuilderString(server)) ; 
   	}

    @Test public void test_larq_7() { 
    	runTestScript("larq-q-7.rq", "data-1.ttl", "results-7.srj", new IndexBuilderString(server)) ; 
    }

}
