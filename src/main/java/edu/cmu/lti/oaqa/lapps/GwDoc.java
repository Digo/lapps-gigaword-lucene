package edu.cmu.lti.oaqa.lapps;

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
