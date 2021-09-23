import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import lombok.val;
import models.ProductHeader;
import models.ProductItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.StaxProcessor;
import utils.XmlUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaxProcessorTestComposedParser {
    XmlMapper xmlMapper = new XmlMapper();
    StaxProcessor processor;

    @BeforeEach
    @SneakyThrows
    void setup(){
        processor = XmlUtils.getProcessor("composed.xml");
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        processor.close();
    }

    @Test
    @SneakyThrows
    void parseXmlItemsGetTheItems() {
        val groups = new HashMap<Integer, List<ProductItem>>();

        processor.navigateToDescendent("header");
        val header = xmlMapper.readValue(XmlUtils.getInnerData(processor), ProductHeader.class);

        var index = 0;
        var canIterateGroups = processor.navigateToSibling("items");

        while (canIterateGroups) {
            groups.put(++index, getGroup());
            canIterateGroups = processor.navigateToNextTag("items");
        }

        assertEquals(3, groups.size());
        assertEquals(3, groups.get(1).size());
        assertEquals(4, groups.get(2).size());
        assertEquals(5, groups.get(3).size());
        val allItems = groups.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        assertEquals(header.getCount(), allItems.size());
        assertEquals(header.getTotal(), allItems.stream().map(ProductItem::getPrice).reduce(0f, Float::sum));
    }

    private List<ProductItem> getGroup() throws XMLStreamException, IOException {
        val groupName = "item";
        val currentGroup = new ArrayList<ProductItem>();
        var canIterateItem = processor.navigateToDescendent(groupName);

        while (canIterateItem) {
            val itemString = XmlUtils.getInnerData(processor);
            currentGroup.add(xmlMapper.readValue(itemString, ProductItem.class));
            canIterateItem = processor.isStartingNode(groupName) || processor.navigateToSibling(groupName);
        }

        return currentGroup;
    }
}

