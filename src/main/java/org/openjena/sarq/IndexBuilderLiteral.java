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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;

public abstract class IndexBuilderLiteral extends IndexBuilderModel {
    
    public IndexBuilderLiteral(String url) { 
    	super(url) ; 
    }

    protected abstract boolean indexThisLiteral(Literal literal) ;

    protected abstract boolean indexThisStatement(Statement stmt) ;

    @Override
    public void unindexStatement(Statement s) { 
        if ( ! indexThisStatement(s) )
            return ;

        if ( s.getObject().isLiteral() )
        {
            Node node = s.getObject().asNode() ;
            if ( indexThisLiteral(s.getLiteral())) {
            	builder.unindex(node, node.getLiteralLexicalForm()) ;
            }
        }
    }
    
    @Override
    public void indexStatement(Statement s) {
        if ( ! indexThisStatement(s) ) {
            return ;
        }
        
        try {
            if ( s.getObject().isLiteral() ) {
                Node node = s.getObject().asNode() ;
                if ( indexThisLiteral(s.getLiteral())) {
                	builder.index(node, node.getLiteralLexicalForm()) ;
                }
            }
        } catch (Exception e) { 
        	throw new SARQException("indexStatement", e) ; 
        }
    }

}
