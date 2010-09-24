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

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public abstract class IndexBuilderModel extends StatementListener {

    protected IndexBuilderNode builder ;
    
    public IndexBuilderModel(String url) { 
    	builder = new IndexBuilderNode(url) ; 
    }

    @Override
    public void addedStatement(Statement s) { 
    	indexStatement(s) ; 
    }
    
    @Override
    public void removedStatement(Statement s) { 
    	unindexStatement(s) ; 
    }

    public void indexStatements(StmtIterator sIter) {
        for ( ; sIter.hasNext() ; ) {
            indexStatement(sIter.nextStatement()) ;
        }
    }

    public void unindexStatement(Statement s) { 
    	throw new java.lang.UnsupportedOperationException("unindexStatement") ; 
    }
    
    public abstract void indexStatement(Statement s) ;

    public void rollback() { 
    	builder.rollback() ; 
    }
    
    public void commit() { 
    	builder.commit() ; 
    }

    public void optimize() { 
    	builder.optimize() ; 
    }

    public SolrServer getSolrServer() { 
    	return builder.getSolrServer() ; 
    }

}
