package services;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;

public interface StaxProcessor {
    boolean hasNext();
    XMLEvent nextEvent() throws XMLStreamException;
    XMLEvent nextTag() throws XMLStreamException;
    boolean navigateToNextTag(String nodeName) throws XMLStreamException;
    XMLEvent getCurrentEvent();
    int getCurrentLevel();
    boolean navigateToDescendent(String nodeName) throws XMLStreamException;
    boolean navigateToSibling(String nodeName) throws XMLStreamException;
    void writeOuterXml(OutputStream outputStream) throws IOException, XMLStreamException;
    boolean isStartingNode(String nodeName);
    void close() throws XMLStreamException;
}
