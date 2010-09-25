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

import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.webapp.WebAppContext;

public class SolrEmbeddedServer {
	
	// see: http://svn.codehaus.org/jetty/jetty/tags/jetty-6.1.3/examples/embedded/src/main/java/org/mortbay/jetty/example/LikeJettyXml.java
	
	private static String ROOT = ".";
	private static String SOLR_HOME = ROOT + "/solr";
	private static String JETTY_HOME = SOLR_HOME + "/jetty-6.1.25";
	private static int JETTY_PORT = 8983;
	private Server server = null;
	
	public SolrEmbeddedServer() throws Exception {
		System.setProperty("slave", "disabled");
		System.setProperty("solr.solr.home", SOLR_HOME);
		System.setProperty("java.util.logging.config.file", ROOT + "/solr/logging.properties");
		System.setProperty("jetty.home", JETTY_HOME);
		System.setProperty("jetty.logs", ROOT + "/solr/logs");
		System.setProperty("STOP.PORT", "9983");
		System.setProperty("STOP.KEY", "changeme");

		server = new Server(JETTY_PORT);
        
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/solr");
		webapp.setWar(JETTY_HOME + "/webapps/solr.war");
		server.addHandler(webapp);

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        NCSARequestLog requestLog = new NCSARequestLog(ROOT + "/solr/logs/jetty-yyyy_mm_dd.log");
        requestLog.setExtended(false);
        requestLogHandler.setRequestLog(requestLog);
        server.addHandler(requestLogHandler);
		
        server.setStopAtShutdown(true);
        server.setSendServerVersion(true);
		server.start();
	}
	
	public Server getServer() {
		return server;
	}
	
	public static void main(String[] args) throws Exception {
		SolrEmbeddedServer ses = new SolrEmbeddedServer();
		ses.getServer().join();
	}

}
