import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.StaxProcessor;
import utils.XmlUtils;


import javax.xml.stream.events.XMLEvent;

import static org.junit.jupiter.api.Assertions.*;

class StaxProcessorImplTest {

    StaxProcessor processor;

    @BeforeEach
    @SneakyThrows
    void setup(){
        processor = XmlUtils.getProcessor("heroes.xml");
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        processor.close();
    }

    @Test
    @SneakyThrows
    void hasNextReturnsTrueWhenNodesAreAvailable() {
        processor.nextEvent(); //xml declaration
        processor.nextEvent(); //heroes
        assertTrue(processor.hasNext());
    }

    @Test
    @SneakyThrows
    void hasNextReturnsFalseWhenNodesAreNotAvailable() {
        XMLEvent xmlEvent = null;

        while (processor.hasNext()) {
            xmlEvent = processor.nextEvent();
        }

        assertFalse(processor.hasNext());
        assertNull(xmlEvent);
    }

    @Test
    @SneakyThrows
    void nextEventNavigateToTheNextNodeWhenAvailable() {

        while (processor.hasNext()) {
            processor.nextEvent();
        }

        assertFalse(processor.hasNext());
        assertNull(processor.nextEvent());
    }

    @Test
    @SneakyThrows
    void nextTagNavigatesToTheNextStarterNode() {
        processor.nextEvent(); //xml declaration
        processor.nextEvent(); //heroes
        val heroTag = processor.nextTag();

        assertNotNull(heroTag);
        assertTrue(heroTag.isStartElement());
        assertEquals("Hero", heroTag.asStartElement().getName().getLocalPart());
    }

    @Test
    @SneakyThrows
    void getCurrentElementReturnsTheCurrentElement() {
        processor.nextEvent(); //xml declaration
        processor.nextEvent(); //heroes

        val current = processor.getCurrentEvent();
        assertNotNull(current);
        assertTrue(current.isStartElement());
        assertEquals("Heroes", current.asStartElement().getName().getLocalPart());
    }

    @Test
    @SneakyThrows
    void navigatesToDescendentMovesToTheNodeWhenAvailable() {
        val found = processor.navigateToDescendent("Hero");

        assertTrue(found);
        checkHeroNode("Hero", processor.getCurrentEvent());
        checkHeroAttr("1", processor.getCurrentEvent());
        assertEquals(2, processor.getCurrentLevel());
    }

    @Test
    @SneakyThrows
    void navigatesToDescendentReturnsFalseIfNodeIsNotAvailable() {
        processor.navigateToDescendent("Hero");

        val found = processor.navigateToDescendent("InvalidNodeName");

        assertFalse(found);
        assertEquals(2, processor.getCurrentLevel());
    }

    @Test
    @SneakyThrows
    void navigateToSiblingMovesToTheNodeWhenAvailable() {
        processor.navigateToDescendent("Hero");

        val found = processor.navigateToSibling("Hero");

        assertTrue(found);
        checkHeroNode("Hero", processor.getCurrentEvent());
        checkHeroAttr("2", processor.getCurrentEvent());
        assertEquals(2, processor.getCurrentLevel());
    }

    @Test
    @SneakyThrows
    void navigateToSiblingReturnsFalseIfNodeIsNotAvailable() {
        processor.navigateToDescendent("Hero");
        processor.navigateToSibling("Hero");
        val thirdNodeFound = processor.navigateToSibling("Hero");
        val thirdNodeElement = processor.getCurrentEvent();

        val found = processor.navigateToSibling("Hero");

        assertTrue(thirdNodeFound);
        checkHeroNode("Hero", thirdNodeElement);
        checkHeroAttr("3", thirdNodeElement);

        assertFalse(found);
        assertTrue(processor.getCurrentEvent().isEndElement());
        assertEquals("Heroes", processor.getCurrentEvent().asEndElement().getName().getLocalPart());
        assertEquals(0, processor.getCurrentLevel());
    }

    @Test
    @SneakyThrows
    void writeOuterXmlReturnsNodeContentAsString() {
        processor.navigateToDescendent("Address");

        assertEquals("<Address>New York</Address>", XmlUtils.getInnerData(processor));
    }

    @Test
    @SneakyThrows
    void closeReleaseTheStream() {
        processor.close();

        assertFalse(processor.hasNext());
    }

    @Test
    @SneakyThrows
    void isStartingNodeChecksIfTheCurrentNodeIsAStartingNodeWithTheName(){
        processor.navigateToDescendent("Hero");
        assertTrue(processor.isStartingNode("Hero"));
        assertFalse(processor.isStartingNode("Other"));
    }

    private void checkHeroNode(String name, XMLEvent current) {
        assertTrue(current.isStartElement());
        assertEquals(name, current.asStartElement().getName().getLocalPart());
    }

    private void checkHeroAttr(String id, XMLEvent current) {
        val idAttr = current.asStartElement().getAttributes().next();
        assertEquals("id", idAttr.getName().getLocalPart());
        assertEquals(id, idAttr.getValue());
    }
}