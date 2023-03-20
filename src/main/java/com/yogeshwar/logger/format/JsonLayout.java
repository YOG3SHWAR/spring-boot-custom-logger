package com.yogeshwar.logger.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import static java.nio.charset.StandardCharsets.UTF_8;

@Plugin(name = "JsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class JsonLayout extends AbstractStringLayout {

    private final boolean prettyPrint;
    private final ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();

    protected JsonLayout(boolean prettyPrint) {
        super(UTF_8);
        this.prettyPrint = prettyPrint;
    }

    @PluginFactory
    public static JsonLayout createLayout(@PluginAttribute(value = "prettyPrint", defaultString = "false") boolean prettyPrint) {
        return new JsonLayout(prettyPrint);
    }

    @Override
    public String toSerializable(final LogEvent event) {
        String message = "";
        try {
            if (prettyPrint)
                message = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
            else message = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("Could not write log event: " + event); // NOSONAR
        }

        return message + System.lineSeparator();
    }
}
