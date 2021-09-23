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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StaxProcessorTestSingleParser {
    XmlMapper xmlMapper = new XmlMapper();
    StaxProcessor processor;

    @BeforeEach
    @SneakyThrows
    void setup(){
        processor = XmlUtils.getProcessor("products.xml");
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        processor.close();
    }

    @Test
    @SneakyThrows
    void parseXmlHeaderGetHeaderValue() {
        processor.navigateToDescendent("header");
        val headerString = XmlUtils.getInnerData(processor);
        val header = xmlMapper.readValue(headerString, ProductHeader.class);

        assertNotNull(header);
        assertEquals(5, header.getCount());
        assertEquals(75, header.getTotal());
    }

    @Test
    @SneakyThrows
    void parseXmlItemsGetTheItems() {
        val items = new ArrayList<ProductItem>();

        processor.navigateToDescendent("header");
        processor.navigateToSibling("items");
        var canIterate = processor.navigateToDescendent("item");

        while (canIterate) {
            val itemString = XmlUtils.getInnerData(processor);
            items.add(xmlMapper.readValue(itemString, ProductItem.class));
            canIterate = processor.isStartingNode("item") || processor.navigateToSibling("item");
        }

        assertEquals(5, items.size());
        assertEquals(75, items.stream().map(ProductItem::getPrice).reduce(0f, Float::sum));
    }
}

