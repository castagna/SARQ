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

package dev;

import java.io.File;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.openjena.sarq.IndexBuilderModel;
import org.openjena.sarq.IndexBuilderString;
import org.openjena.sarq.SARQ;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Run {

	public static File PATH_SOLR_INDEX = new File ("target/data/solr");
	public static String TDB_ASSEMBLER_FILENAME = "src/test/resources/tdb.ttl";
	
	public static void main(String[] args) throws Exception {
		PATH_SOLR_INDEX.mkdirs();
		
        System.setProperty("solr.solr.home", "solr/sarq");
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");
		
		// Load data into TDB
		Dataset dataset = TDBFactory.assembleDataset(TDB_ASSEMBLER_FILENAME);
		Model model = dataset.getDefaultModel();
		model.add(RDF.first, RDF.first, "text");
		model.add(ResourceFactory.createResource(), RDFS.label, "foo");
		model.add(ResourceFactory.createResource("foo"), FOAF.knows, ResourceFactory.createResource("bar"));
		model.add(ResourceFactory.createResource(), ResourceFactory.createProperty(":p1"), "text");
		model.add(ResourceFactory.createResource(), ResourceFactory.createProperty(":p2"), "ciao");
		
		
		// Build Solr index
		IndexBuilderModel builder = new IndexBuilderString(server);
		// IndexBuilderModel builder = new IndexBuilderString("http://127.0.0.1:8983/solr/sarq");
        // IndexBuilderModel builder = new IndexBuilderSubject("http://127.0.0.1:8983/solr");
        
        builder.indexStatements(model.listStatements());
        builder.commit();
        
        SARQ.setDefaultIndex(builder.getSolrServer());

        // Perform a query
        Query q = QueryFactory.create(
                "PREFIX sarq:     <http://openjena.org/SARQ/property#>" +
                "" +
                "select * where {" +
                "?doc ?p ?lit ." +
                "(?lit ?score ) sarq:search '+text' ." +
                "}"
                );
        
        Op op = Algebra.compile(q) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        
        QueryExecution qe = QueryExecutionFactory.create(q, dataset);
        ResultSet res = qe.execSelect();
        ResultSetFormatter.out(res);
        qe.close();	
        
        server = null;
        System.exit(0);
	}

}
