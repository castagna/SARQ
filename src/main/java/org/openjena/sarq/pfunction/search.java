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

package org.openjena.sarq.pfunction;

import org.openjena.sarq.SolrServer;
import org.openjena.sarq.SARQ;
import org.openjena.sarq.SolrSearch;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;

/** 
 * Property function to search using a remote Solr server (which is set by {@link SARQ#setDefaultIndex(SolrServer) })
 */

public class search extends SolrSearch {
    private SolrServer index = null ;

    @Override
    protected SolrServer getIndex(ExecutionContext execCxt) { 
        if ( index == null ) {
            index = SARQ.getDefaultIndex(execCxt.getContext()) ;
        }

        return index ; 
    }

}
