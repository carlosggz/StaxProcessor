package services;

import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StaxProcessorImpl implements StaxProcessor {
    private XMLEventReader eventReader;
    private XMLEvent currentXmlEvent = null;

    @Getter
    private int currentLevel;

    public StaxProcessorImpl(java.io.Reader reader) throws XMLStreamException {
        this.eventReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
        this.currentLevel = 0;
    }

    @Override
    public boolean hasNext() {
        return Objects.nonNull(eventReader) && this.eventReader.hasNext();
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        if (!hasNext()) {
            currentXmlEvent = null;
            currentLevel = 0;
        }
        else {
            currentXmlEvent = eventReader.nextEvent();

            if (currentXmlEvent.isStartElement()) {
                currentLevel++;
            }
            else if (currentXmlEvent.isEndElement()) {
                currentLevel--;
            }
            else if (currentXmlEvent.isEndDocument()) {
                currentXmlEvent = null;
                currentLevel = 0;
            }
        }

        return currentXmlEvent;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        var canIterate = hasNext();

        while (canIterate) {
            nextEvent();
            canIterate = hasNext() && !currentXmlEvent.isStartElement();
        }

        return currentXmlEvent;
    }

    @Override
    public XMLEvent getCurrentEvent() {
        return currentXmlEvent;
    }

    @Override
    public boolean navigateToDescendent(String nodeName) throws XMLStreamException {
        return navigate(nodeName, currentLevel+1);
    }

    @Override
    public boolean navigateToSibling(String nodeName) throws XMLStreamException {

        if (isStartingNode(nodeName)) { //skip current node
            var currenTagLevel = currentLevel;

            while (hasNext() && currenTagLevel <= currentLevel) {
                nextEvent();
            }
        }

        return navigate(nodeName, currentLevel);
    }

    @Override
    public void writeOuterXml(OutputStream outputStream) throws IOException, XMLStreamException {
        if (Objects.isNull(eventReader) || Objects.isNull(currentXmlEvent) || !currentXmlEvent.isStartElement()) return;

        outputStream.write(getCurrentTagAsStart());

        var currenTagLevel = currentLevel;

        while (hasNext() && currenTagLevel <= currentLevel) {
            nextEvent();
            switch (currentXmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT -> outputStream.write(getCurrentTagAsStart());
                case XMLStreamConstants.END_ELEMENT -> outputStream.write(getCurrentTagAsEnd());
                case XMLStreamConstants.CHARACTERS -> outputStream.write(getCurrentTagAsData());
            }
        }
    }

    @Override
    public void close() throws XMLStreamException {
        if (Objects.isNull(eventReader)) {
            return;
        }

        eventReader.close();
        eventReader = null;
        currentXmlEvent = null;
        currentLevel = 0;
    }

    @Override
    public boolean isStartingNode(String nodeName) {
        return !StringUtils.isBlank(nodeName) &&
                Objects.nonNull(currentXmlEvent) &&
                currentXmlEvent.isStartElement() &&
                currentXmlEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase(nodeName);
    }

    @Override
    public boolean navigateToNextTag(String nodeName) throws XMLStreamException {

        while (nextTag() != null) {
            if (isStartingNode(nodeName)) {
                return true;
            }
        }

        return false;
    }

    private String getCurrentNodeName() {
        return currentXmlEvent.isStartElement()
                ? currentXmlEvent.asStartElement().getName().getLocalPart()
                : currentXmlEvent.asEndElement().getName().getLocalPart();
    }

    private byte[] getCurrentTagAsStart() {
        val sb = new StringBuilder("<" + getCurrentNodeName());
        val attributes = currentXmlEvent.asStartElement().getAttributes();

        while (attributes.hasNext()) {
            val attr = attributes.next();
            sb.append(String.format(" %s=\"%s\"", attr.getName(), attr.getValue()));
        }

        sb.append(">");
        return (sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private byte[] getCurrentTagAsEnd() {
        return ("</" + getCurrentNodeName() + ">").getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getCurrentTagAsData() {
        return currentXmlEvent.asCharacters().getData().getBytes(StandardCharsets.UTF_8);
    }

    private boolean navigate(String nodeName, int level) throws XMLStreamException {
        if (StringUtils.isBlank(nodeName) || Objects.isNull(eventReader)) return false;

        if (isStartingNode(nodeName)) {
            return true;
        }

        while (eventReader.hasNext()) {
            nextEvent();

            if (isStartingNode(nodeName) && level <= currentLevel) {
                return true;
            }

            if (currentXmlEvent.isEndElement() && level > currentLevel) {
                return false;
            }
        }

        return false;
    }
}

