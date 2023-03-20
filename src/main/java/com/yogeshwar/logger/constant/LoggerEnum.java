package com.yogeshwar.logger.constant;

import lombok.Getter;

@Getter
public enum LoggerEnum {

    CLASS("Class"),
    FILE("File"),
    FUNCTION("Function"),
    LINE_NUMBER("LineNumber");

    private final String value;

    LoggerEnum(String value) {
        this.value = value;
    }
}
