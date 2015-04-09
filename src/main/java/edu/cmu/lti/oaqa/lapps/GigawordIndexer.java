package edu.cmu.lti.oaqa.lapps;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 * @author Di Wang.
 */
public class GigawordIndexer {

    public static final Analyzer ANALYZER = new EnglishAnalyzer();

    public static void main(String[] args) throws IOException, XMLStreamException {

        String docsPath = "./gw-data/";
        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Please check documents path: " + docDir.getAbsolutePath());
            System.exit(1);
        }

        Directory dir = FSDirectory.open(new File("./index/"));
        IndexWriterConfig iwc = new IndexWriterConfig(ANALYZER.getVersion(), ANALYZER);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        indexDocs(writer, docDir);

        writer.close();
    }


    static void indexDocs(final IndexWriter writer, File file) {
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    indexDocs(writer, new File(file, files[i]));
                }
            }
        } else {
            try {
                GwDocIterator docIter = new GwDocIterator(file);
                while (docIter.hasNext()) {
                    GwDoc doc = docIter.next();
                    writer.addDocument(doc.getLuceneDoc());
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
