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
