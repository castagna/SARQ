package org.openjena.sarq;

public class IndexBuilderBase implements IndexBuilder {

	private SolrServer index = null;

    public IndexBuilderBase(String url) {
   		index = new SolrServer(url);
    }

    @Override
    public void rollback() { 
        try {
            index.getSolrUpdateServer().rollback();
        } catch (Exception ex) { 
        	throw new SARQException("commit", ex) ; 
        }
    }

    @Override
    public void commit() { 
        try {
            index.getSolrUpdateServer().commit();
        } catch (Exception ex) { 
        	throw new SARQException("commit", ex) ; 
        }
    }

    @Override 
    public void optimize() { 
        try {
            index.getSolrUpdateServer().optimize();
        } catch (Exception ex) { 
        	throw new SARQException("optimize", ex) ; 
        }
    }
    
    public SolrServer getSolrServer() {
        return index ;
    }

}