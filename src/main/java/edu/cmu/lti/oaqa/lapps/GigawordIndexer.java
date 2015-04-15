package edu.cmu.lti.oaqa.lapps;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 * @author Di Wang.
 */
abstract public class GigawordIndexer {


    protected static final String DEFAULT_DOC_PATH = "./gw-data/";

    protected static final String DEFAULT_SOLR_URL = "http://localhost:8983/solr/gweng/";
    protected static final String DEFAULT_LUCENE_INDEX_PATH = "./index/";

    protected static final String DEFAULT_INDEXER = "solr"; //solr or lucene

    public static void main(String[] args) throws IOException, XMLStreamException, SolrServerException {

        String docsPath = System.getProperty("docs", DEFAULT_DOC_PATH);
        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Please check documents path: " + docDir.getAbsolutePath());
            System.exit(1);
        }

        String indexerName = System.getProperty("indexer", DEFAULT_INDEXER);
        switch (indexerName) {
            case "solr":
                indexDocsSolr(docDir);
                break;
            case "lucene":
                indexDocsLucene(docDir);
                break;
            default:
                System.err.println("Indexer is not supported: " + indexerName);
                System.err.println("Try -Dindexer=solr or -Dindexer=lucene");
        }

    }

    static void indexDocsSolr(File docDir) throws IOException, SolrServerException {
        String solrUrl = System.getProperty("solrUrl", DEFAULT_SOLR_URL);
        SolrClient solrClient = new HttpSolrClient(solrUrl);
        indexDocs(docDir, (doc) -> {
            solrClient.add(getSolrDoc(doc));
        });
        solrClient.commit();
    }

    public static final Analyzer ANALYZER = new EnglishAnalyzer();

    static void indexDocsLucene(File docDir) throws IOException {
        String index_path = System.getProperty("index", DEFAULT_LUCENE_INDEX_PATH);
        Directory dir = FSDirectory.open(new File(index_path));
        IndexWriterConfig iwc = new IndexWriterConfig(ANALYZER.getVersion(), ANALYZER);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);
        indexDocs(docDir, (doc) -> {
            writer.addDocument(getLuceneDoc(doc));
        });
        writer.close();
    }


    interface DocIndexer {
        void indexDoc(GwDoc doc) throws IOException, SolrServerException;
    }

    static void indexDocs(File file, DocIndexer writer) {
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    indexDocs(new File(file, files[i]), writer);
                }
            }
        } else {
            try {
                GwDocIterator docIter = new GwDocIterator(file);
                while (docIter.hasNext()) {
                    GwDoc doc = docIter.next();
                    writer.indexDoc(doc);
                }
            } catch (Exception e) {
                System.err.println("Error indexing file: " + file.getAbsolutePath());
                e.printStackTrace();
            }

        }
    }


    static Document getLuceneDoc(GwDoc gwDoc) {
        Document doc = new Document();
        if (gwDoc.headline != null) {
            doc.add(new TextField(GwDoc.HEADLINE, gwDoc.headline, Field.Store.YES));
        }
        doc.add(new TextField(GwDoc.TEXT, gwDoc.text, Field.Store.YES));
        doc.add(new StringField(GwDoc.ID, gwDoc.id, Field.Store.YES));
        return doc;
    }

    static SolrInputDocument getSolrDoc(GwDoc gwDoc) {
        SolrInputDocument doc = new SolrInputDocument();
        if (gwDoc.headline != null) {
            doc.addField(GwDoc.HEADLINE, gwDoc.headline);
        }
        doc.addField(GwDoc.TEXT, gwDoc.text);
        doc.addField(GwDoc.ID, gwDoc.id);
        return doc;
    }
}
