package jp.gr.java_conf.tmatz.mushroom_safeincloud.db;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class DatabaseParser {

    private static final String LABEL = "label";
    private static final String CARD = "card";
    private static final String FIELD = "field";
    private static final String NOTES = "notes";
    private static final String LABEL_ID = "label_id";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String TITLE = "title";
    private static final String TEMPLATE = "template";
    private static final String TRUE = "true";

    public Database parse(InputStream inputStream) {
        try {
            final Database database = new Database();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            SAXParser parser = factory.newSAXParser();

            parser.parse(inputStream, new DefaultHandler() {

                private Label mLabel;
                private Card mCard;
                private Field mField;
                private LabelId mLabelId;
                private Note mNote;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    switch (qName) {
                        case LABEL:
                            mLabel = new Label();
                            mLabel.setName(attributes.getValue(NAME));
                            mLabel.setId(attributes.getValue(ID));
                            mLabel.setType(attributes.getValue(TYPE));
                            break;

                        case CARD:
                            mCard = new Card();
                            mCard.setTitle(attributes.getValue(TITLE));
                            mCard.setTemplate(TRUE.equals(attributes.getValue(TEMPLATE)));
                            break;

                        case FIELD:
                            mField = new Field();
                            mField.setTitle(attributes.getValue(NAME));
                            mField.setType(attributes.getValue(TYPE));
                            break;

                        case LABEL_ID:
                            mLabelId = new LabelId();
                            mLabelId.setId(attributes.getValue(ID));
                            break;

                        case NOTES:
                            mNote = new Note();
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    switch (qName) {
                        case LABEL:
                            if (mLabel != null) {
                                database.addLabel(mLabel);
                                mLabel = null;
                            }
                            break;

                        case CARD:
                            if (mCard != null) {
                                database.addCard(mCard);
                                mCard = null;
                            }
                            break;

                        case FIELD:
                            if (mCard != null && mField != null) {
                                mCard.addField(mField);
                                mField = null;
                            }
                            break;

                        case LABEL_ID:
                            if (mCard != null && mLabelId != null) {
                                mCard.addLabelId(mLabelId);
                                mLabelId = null;
                            }
                            break;

                        case NOTES:
                            if (mCard != null && mNote != null) {
                                mCard.addNote(mNote);
                                mNote = null;
                            }
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (mField != null) {
                        mField.setValue(new String(ch, start, length));
                    } else if (mLabelId != null) {
                        mLabelId.setId(new String(ch, start, length));
                    } else if (mNote != null) {
                        mNote.setText(new String(ch, start, length));
                    }
                }
            });

            return database;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
