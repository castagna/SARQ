SARQ - Free Text Indexing for SPARQL
------------------------------------

SARQ is a combination of ARQ and Solr. It gives ARQ the ability to perform
free text searches using a remote Solr server. Lucene indexes in Solr are 
additional information for accessing the RDF graph, not storage for the 
graph itself.

This is *experimental* (and unsupported).

See also:

 - http://openjena.org/ARQ/lucene-arq.html

 
TODO
====

 - Fix the failing test and add more tests.
 - Double check the id as unique key, does it make sense?
 - ...