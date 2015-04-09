package edu.cmu.lti.oaqa.lapps;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

/**
 * @author Di Wang.
 */
public class GwDocIterator implements Iterator<GwDoc> {

    File file;
    XMLStreamReader reader;
    GwDoc nextDoc;
    boolean finished = false;

    public GwDocIterator(File file) throws IOException, XMLStreamException {
        this.file = file;
        Enumeration<InputStream> streams = Collections.enumeration(
                Arrays.asList(
                        new ByteArrayInputStream("<!DOCTYPE GWENG[<!ENTITY AMP  \"&#38;#38;\">]> <GWENG>".getBytes()),
                        new GZIPInputStream((new FileInputStream(file)), 65536),
                        new ByteArrayInputStream("</GWENG>".getBytes())));
        SequenceInputStream seqInputStream = new SequenceInputStream(streams);

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        this.reader = factory.createXMLStreamReader(seqInputStream);
    }

    @Override
    public boolean hasNext() {
        if (finished) {
            return false;
        }
        if (nextDoc == null) {
            nextDoc = parseNext();
        }
        finished = (nextDoc == null);
        return !finished;
    }

    @Override
    public GwDoc next() {
        if (nextDoc != null) {
            GwDoc doc = nextDoc;
            nextDoc = null;
            return doc;
        } else {
            return parseNext();
        }
    }


    public GwDoc parseNext() {
        String tagContent = null;
        StringBuilder textBuf = null;
        GwDoc gwDoc = null;
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case GwDoc.DOC:
                                gwDoc = new GwDoc();
                                gwDoc.id = reader.getAttributeValue(null, GwDoc.ID);
                                gwDoc.type = reader.getAttributeValue(null, GwDoc.TYPE);
                                break;
                            case GwDoc.TEXT:
                                textBuf = new StringBuilder();
                                break;
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case GwDoc.DOC:
                                return gwDoc;
                            case GwDoc.P:
                                textBuf.append(tagContent).append("\n\n");
                                break;
                            case GwDoc.TEXT:
                                textBuf.append(tagContent);
                                gwDoc.text = textBuf.toString().trim();
                                break;
                            case GwDoc.HEADLINE:
                                gwDoc.headline = tagContent;
                                break;
                        }
                        break;
                }
            }
        } catch (XMLStreamException e) {
            System.err.println("Error when parsing file: " + this.file.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
