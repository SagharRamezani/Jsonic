package com.saghar.jsonicdb.json;

public sealed interface JsonValue permits JsonObject, JsonArray, JsonString, JsonNumber, JsonBoolean, JsonNull {
}
