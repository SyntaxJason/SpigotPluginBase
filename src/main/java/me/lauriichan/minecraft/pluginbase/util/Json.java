package me.lauriichan.minecraft.pluginbase.util;

import java.io.Reader;
import java.io.StringReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Json {

    private final Gson gson;

    public Json(final Gson gson) {
        this.gson = gson;
    }

    public Gson gson() {
        return gson;
    }

    public JsonElement asJson(String string) {
        return asJson(new StringReader(string));
    }

    public JsonElement asJson(Reader reader) {
        return JsonParser.parseReader(gson.newJsonReader(reader));
    }
    
    public String asString(JsonElement element) {
        return gson.toJson(element);
    }

}
