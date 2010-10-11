SARQ - Free Text Indexing for SPARQL
====================================

SARQ is a combination of ARQ and Solr. It gives ARQ the ability to perform
free text searches using a remote Solr server. Lucene indexes in Solr are 
additional information for accessing the RDF graph, not storage for the 
graph itself.

This is *experimental* (and unsupported).


How to use it
-------------

This is how you build an index from a Jena Model:

    IndexBuilderModel builder = new IndexBuilderString("http://127.0.0.1:8983/solr/sarq");
    builder.indexStatements(model.listStatements());
    builder.commit();

This is how you configure ARQ to use Solr:
        
    SARQ.setDefaultIndex(builder.getSolrServer());

This is an example of a SPARQL query using the sarq:search property function: 

    PREFIX sarq:     <http://openjena.org/SARQ/property#>
    SELECT * WHERE {
        ?doc ?p ?lit .
        (?lit ?score ) sarq:search "+text" .
    }


Acknowledgement
---------------
        
The design and part of the code has been taken from LARQ, see:

 * [http://openjena.org/ARQ/lucene-arq.html](http://openjena.org/ARQ/lucene-arq.html)


TODO
----

 * Fix the failing test and add more tests. [DONE]
 * Double check the id as unique key, does it make sense? [DONE]
 * Consider using multiValue="true" when index by subject, is it possible/useful?
 * Test with Solr, add id=x title=foo then add id=x title=bar... if title is multi value field, what happens?
 * Add required="true" in the schema.xml where appropriate?
 * Add custom rank field and how to combined it with existing rank?
 * Investigate allowDups=true|false, when is it appropriate/safe to use?
 * Use [EmbeddedSolrServer](http://lucene.apache.org/solr/api/org/apache/solr/client/solrj/embedded/EmbeddedSolrServer.html) for testing
 * ...
