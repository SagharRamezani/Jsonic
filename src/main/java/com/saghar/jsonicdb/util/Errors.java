package com.saghar.jsonicdb.util;

public final class Errors {
    private Errors() {
    }

    public static String invalidCommandFormat() {
        return "Invalid command format";
    }

    public static String invalidCommand(String action) {
        return "Invalid command: " + action;
    }

    public static String invalidJsonFormat() {
        return "Invalid JSON format";
    }

    public static String dataTypeAlreadyExists(String name) {
        return "Data type already exists: " + name;
    }

    public static String dataTypeNotFound(String name) {
        return "Data type not found: " + name;
    }

    public static String emptyFields() {
        return "Fields section is empty";
    }

    public static String duplicateField(String field) {
        return "Duplicate field: " + field;
    }

    public static String invalidDataType(String spec) {
        return "Invalid data type: " + spec;
    }

    public static String fieldNotFound(String field) {
        return "Field not found: " + field;
    }

    public static String missingRequiredField(String field) {
        return "Missing required field: " + field;
    }

    public static String uniqueViolation(String field) {
        return "Duplicate value for unique field: " + field;
    }

    public static String invalidValue(String field) {
        return "Invalid value for field: " + field;
    }

    public static String invalidFilter(String details) {
        if (details == null || details.isBlank()) return "Invalid filter";
        return "Invalid filter: " + details;
    }

    // Backward-compatible aliases (older code paths)
    public static String invalidJson() {
        return invalidJsonFormat();
    }

    public static String invalidSyntax(String cmd) {
        return "Invalid " + cmd + " syntax";
    }

    public static String invalidValueForField(String field) {
        return invalidValue(field);
    }

    public static String invalidTypeSpec(String spec) {
        return invalidDataType(spec);
    }

    // More aliases for older call sites
    public static String typeAlreadyExists(String name) {
        return dataTypeAlreadyExists(name);
    }

    public static String typeNotFound(String name) {
        return dataTypeNotFound(name);
    }

    public static String missingRequired(String field) {
        return missingRequiredField(field);
    }

    public static String duplicateUnique(String field) {
        return uniqueViolation(field);
    }

    public static String fieldsEmpty() {
        return emptyFields();
    }
}
