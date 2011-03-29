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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.hp.hpl.jena.graph.Node;

public class SolrServer {
	
	private static CommonsHttpSolrServer queryServer = null;
	private static StreamingUpdateSolrServer updateServer = null;
	private org.apache.solr.client.solrj.SolrServer server = null;
	
	public SolrServer(String url) {
		try {
			if (!url.endsWith("/")) {
				url = url.concat("/");
			}
			if ( queryServer == null ) {
				queryServer = buildSolrQueryServer(url, true);
			}
			if ( updateServer == null ) {
				updateServer = buildSolrUpdateServer(url, true);
			}
		} catch (MalformedURLException e) {
			// TODO: message
			throw new SARQException(e.getMessage(), e);
		}
	}
	
	public SolrServer (org.apache.solr.client.solrj.SolrServer server) {
		this.server = server;
	}
	
    public Iterator<SolrDocument> search(String queryString) {
		SolrQuery solrQuery = new SolrQuery(queryString);
		solrQuery.setRows(Integer.MAX_VALUE);
		solrQuery.setStart(0);
		solrQuery.setFields("*", "score");
		try {
			QueryResponse response = null;
			if ( server != null ) {
				response = server.query(solrQuery, SolrRequest.METHOD.GET);
			} else {
				response = queryServer.query(solrQuery, SolrRequest.METHOD.GET);				
			}
			return response.getResults().iterator();
		} catch (SolrServerException e) {
			throw new SARQException(e.getMessage(), e);
		}
    }
    
    public SolrDocument contains(Node node, String queryString) {
        try {
            Iterator<SolrDocument> iter = search(queryString) ;
            for ( ; iter.hasNext() ; ) {
                SolrDocument x = iter.next();
                if ( x != null && SARQ.build(x).equals(node)) {
                    return x ;
                }
            }
            return null ;
        } catch (Exception e) { 
        	throw new SARQException("contains", e) ; 
        }
    }

    public void addDocument(SolrInputDocument doc) {
		try {
			if ( server != null ) {
				server.add(doc);
			} else {
				updateServer.add(doc);				
			}
		} catch (Exception e) {
			throw new SARQException(e.getMessage(), e);
		}
	}
    
    public org.apache.solr.client.solrj.SolrServer getSolrQueryServer() {
        if ( server != null ) {
            return server;
        } else {
            return queryServer;            
        }
    }
    
    public org.apache.solr.client.solrj.SolrServer getSolrUpdateServer() {
        if ( server != null ) {
            return server;            
        } else {
            return updateServer;            
        }
    }
    
	private CommonsHttpSolrServer buildSolrQueryServer (String url, boolean binary) throws MalformedURLException {
		if ( url == null ) {
			throw new IllegalArgumentException("URL cannot be null.");
		}
		
		MultiThreadedHttpConnectionManager cm = new MultiThreadedHttpConnectionManager();
		ResponseParser parser = null;
		if (binary) {
			parser = new BinaryResponseParser();
		} else {
			parser = new XMLResponseParser();
		}
		CommonsHttpSolrServer server = new CommonsHttpSolrServer(new URL(url), new HttpClient(cm), parser, false);
		server.setSoTimeout(1000);
		server.setConnectionTimeout(2000);
		server.setDefaultMaxConnectionsPerHost(10);
		server.setMaxTotalConnections(10);
		server.setFollowRedirects(false);
		server.setAllowCompression(true);
		server.setMaxRetries(1);
		if (binary) {
			server.setRequestWriter(new BinaryRequestWriter());
		} else {
			server.setRequestWriter(new RequestWriter());
		}

		return server;
	}
	
	private StreamingUpdateSolrServer buildSolrUpdateServer (String url, boolean binary) throws MalformedURLException {
		if ( url == null ) {
			throw new IllegalArgumentException("URL cannot be null.");
		}
		
		StreamingUpdateSolrServer server = new StreamingUpdateSolrServer(url, 100, 4);
		server.setDefaultMaxConnectionsPerHost(10);
		server.setMaxTotalConnections(10);
		server.setFollowRedirects(false);
		server.setAllowCompression(true);
		server.setMaxRetries(1);
//		if (binary) {
//			server.setRequestWriter(new BinaryRequestWriter());
//		} else {
//			server.setRequestWriter(new RequestWriter());
//		}
		
		return server;
	}

	public boolean hasMatch(String string) {
		return search(string).hasNext();
	}

	public void rollback() {
		try {
			if ( server != null ) {
				server.rollback();
			} else {
				updateServer.rollback();				
			}
        } catch (Exception ex) { 
        	throw new SARQException("rollback", ex) ; 
        }
	}

	public void commit() {
		try {
			if ( server != null ) {
				server.commit();
			} else {
				updateServer.commit();				
			}
        } catch (Exception ex) { 
        	throw new SARQException("commit", ex) ; 
        }
	}

	public void optimize() {
		try {
			if ( server != null ) {
				server.optimize();
			} else {
				updateServer.optimize();				
			}
        } catch (Exception ex) { 
        	throw new SARQException("optimize", ex) ; 
        }
	}

}