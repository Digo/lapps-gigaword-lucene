package edu.cmu.lti.oaqa.lapps;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * @author Di Wang.
 */
class GwDoc {
    protected static final String DOC = "DOC";
    protected static final String TEXT = "TEXT";
    protected static final String P = "P";
    protected static final String HEADLINE = "HEADLINE";
    protected static final String TYPE = "type";
    protected static final String ID = "id";
    String headline;
    String id;
    String text;
    String type;

    Document getLuceneDoc() {
        Document doc = new Document();
        if (headline != null) {
            doc.add(new TextField(HEADLINE, headline, Field.Store.YES));
        }
        doc.add(new TextField(TEXT, text, Field.Store.YES));
        doc.add(new StringField(ID, id, Field.Store.YES));
        return doc;
    }

    @Override
    public String toString() {
        return "GwDoc{" +
                "headline='" + headline + '\'' +
                ", id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
