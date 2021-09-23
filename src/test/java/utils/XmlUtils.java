package utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import services.StaxProcessor;
import services.StaxProcessorImpl;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@UtilityClass
public class XmlUtils {

    @SneakyThrows
    public StaxProcessor getProcessor(String fileName){
        val xmlResource = XmlUtils.class.getClassLoader().getResource(fileName);
        val xmlFile = new File(xmlResource.toURI());
        val reader = new FileReader(xmlFile);
        return new StaxProcessorImpl(reader);
    }

    public String getInnerData(StaxProcessor processor) throws XMLStreamException, IOException {
        val outputStream = new ByteArrayOutputStream();
        processor.writeOuterXml(outputStream);
        val value = outputStream.toString();
        outputStream.close();
        return value;
    }
}
