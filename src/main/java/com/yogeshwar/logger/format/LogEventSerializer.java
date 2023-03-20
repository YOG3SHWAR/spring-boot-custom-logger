package com.yogeshwar.logger.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static com.yogeshwar.logger.constant.LoggerEnum.*;

public class LogEventSerializer extends JsonSerializer<LogEvent> {

    public static final String MESSAGE = "Message";

    @Override
    public void serialize(
            final LogEvent logEvent,
            final JsonGenerator jsonGenerator,
            final SerializerProvider serializers)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField(
                "TimeStamp", String.valueOf(Instant.ofEpochMilli(logEvent.getTimeMillis())));

        final Message message = logEvent.getMessage();
        if (message instanceof SimpleMessage || message instanceof ParameterizedMessage) {
            writeLevelAndMessage(jsonGenerator, logEvent, message.getFormattedMessage());
        } else if (message instanceof ObjectMessage) {
            logForObjectMessage(logEvent, jsonGenerator, message);
        }
        if (logEvent.getThrown() != null) {
            jsonGenerator.writeObjectField(
                    "Exception",
                    ExceptionUtils.getStackTrace(logEvent.getThrown()).replaceAll("[\\n\\t]", " "));
        }
        writeLocation(logEvent, jsonGenerator);
        jsonGenerator.writeEndObject();
    }

    @SuppressWarnings("unchecked")
    private void logForObjectMessage(
            final LogEvent logEvent, final JsonGenerator jsonGenerator, final Message message)
            throws IOException {
        final Object[] parameters = message.getParameters();
        if (!ArrayUtils.isEmpty(parameters)) {
            if (parameters.length > 1) {
                writeLevelAndMessage(jsonGenerator, logEvent, parameters);
            } else {
                if (parameters[0] instanceof Map) {
                    jsonGenerator.writeObjectField("Level", logEvent.getLevel().getStandardLevel());
                    final Map<String, String> map = (Map<String, String>) parameters[0];
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (isNotEmpty(entry.getKey()) && isNotEmpty(entry.getValue())) {
                            jsonGenerator.writeObjectField(entry.getKey(), entry.getValue());
                        }
                    }
                } else {
                    if (isNotEmpty(parameters[0])) {
                        writeLevelAndMessage(jsonGenerator, logEvent, parameters[0]);
                    }
                }
            }
        }
    }

    private void writeLevelAndMessage(
            final JsonGenerator jsonGenerator, final LogEvent logEvent, final Object parameter)
            throws IOException {
        jsonGenerator.writeObjectField("Level", logEvent.getLevel().getStandardLevel());
        jsonGenerator.writeObjectField(MESSAGE, parameter);
    }

    private void writeLevelAndMessage(
            final JsonGenerator jsonGenerator, final LogEvent logEvent, final Object[] parameters)
            throws IOException {
        jsonGenerator.writeObjectField("Level", logEvent.getLevel().getStandardLevel());
        jsonGenerator.writeArrayFieldStart(MESSAGE);
        for (Object parameter : parameters) {
            if (isNotEmpty(parameter)) {
                jsonGenerator.writeObject(parameter);
            }
        }
        jsonGenerator.writeEndArray();
    }

    private void writeLocation(final LogEvent logEvent, final JsonGenerator jsonGenerator)
            throws IOException {
        if (logEvent.isIncludeLocation()) {
            final StackTraceElement source = logEvent.getSource();
            if (source != null) {
                if (isNotEmpty(source.getClassName())) {
                    jsonGenerator.writeObjectField(
                            CLASS.getValue(), source.getClassName());
                }
                if (isNotEmpty(source.getFileName())) {
                    jsonGenerator.writeObjectField(
                            FILE.getValue(), source.getFileName());
                }
                if (source.getLineNumber() != 0) {
                    jsonGenerator.writeObjectField(
                            LINE_NUMBER.getValue(), source.getLineNumber());
                }
                if (isNotEmpty(source.getMethodName())) {
                    jsonGenerator.writeObjectField(
                            FUNCTION.getValue(), source.getMethodName());
                }
            }
        }
    }

    private boolean isNotEmpty(final Object object) {
        return object != null && !String.valueOf(object).isEmpty();
    }
}
