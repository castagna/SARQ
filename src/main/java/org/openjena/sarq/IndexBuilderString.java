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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

public class IndexBuilderString extends IndexBuilderLiteral {
    private Property property = null ;

    public IndexBuilderString(org.apache.solr.client.solrj.SolrServer server) { 
    	super(server) ; 
    }
    
    public IndexBuilderString(String url) { 
    	super(url) ; 
    }

    public IndexBuilderString(Property property, org.apache.solr.client.solrj.SolrServer server) { 
    	super(server) ; 
    	setProperty(property) ; 
    }

    public IndexBuilderString(Property property, String url) { 
    	super(url) ; 
    	setProperty(property) ; 
    }

    @Override
    protected boolean indexThisStatement(Statement stmt) { 
        if ( property == null ) {
            return true ;
        }
        return stmt.getPredicate().equals(property) ;
    }

    private void setProperty(Property p) { 
    	property = p ; 
    }
    
    @Override
    protected boolean indexThisLiteral(Literal literal) { 
    	return SARQ.isString(literal) ; 
    }

}
