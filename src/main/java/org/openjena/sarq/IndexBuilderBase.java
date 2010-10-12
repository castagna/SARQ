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

public class IndexBuilderBase implements IndexBuilder {

	private SolrServer index = null;

	public IndexBuilderBase(org.apache.solr.client.solrj.SolrServer server) {
		index = new SolrServer(server);
	}
	
    public IndexBuilderBase(String url) {
   		index = new SolrServer(url);
    }

    @Override
    public void rollback() { 
        index.rollback();
    }

    @Override
    public void commit() { 
        index.commit();
    }

    @Override 
    public void optimize() { 
    	index.optimize();
    }
    
    public SolrServer getSolrServer() {
        return index ;
    }

}