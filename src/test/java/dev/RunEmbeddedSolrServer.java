package dev;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

public class RunEmbeddedSolrServer {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		  System.setProperty("solr.solr.home", "solr/sarq");
		  CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		  CoreContainer coreContainer = initializer.initialize();
		  EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "sarq");
	}

}
