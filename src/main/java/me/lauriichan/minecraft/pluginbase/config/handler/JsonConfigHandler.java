package me.lauriichan.minecraft.pluginbase.config.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.util.Json;

public final class JsonConfigHandler implements IConfigHandler {

    public static final JsonConfigHandler JSON = new JsonConfigHandler();

    private final Json json = new Json(new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().disableHtmlEscaping().create());

    private JsonConfigHandler() {}

    public Json json() {
        return json;
    }

    @Override
    public void load(final Configuration configuration, final IDataSource source) throws Exception {
        JsonElement element;
        try (BufferedReader reader = source.openReader()) {
            element = json.asJson(reader);
        }
        if (!element.isJsonObject()) {
            throw new IllegalStateException("Config source doesn't contain a JsonObject");
        }
        loadToConfig(element.getAsJsonObject(), configuration);
    }

    private void loadToConfig(final JsonObject object, final Configuration configuration) {
        for (final String key : object.keySet()) {
            final JsonElement element = object.get(key);
            if (element.isJsonNull()) {
                continue;
            }
            if (element.isJsonObject()) {
                loadToConfig(element.getAsJsonObject(), configuration.getConfiguration(key, true));
                continue;
            }
            if (element.isJsonArray()) {
                configuration.set(key, deserialize(element.getAsJsonArray()));
                continue;
            }
            configuration.set(key, deserialize(element.getAsJsonPrimitive()));
        }
    }

    @SuppressWarnings({
        "rawtypes",
        "unchecked"
    })
    private List deserialize(final JsonArray array) {
        final ObjectArrayList list = new ObjectArrayList();
        for (final JsonElement arrayElement : array) {
            if (arrayElement.isJsonObject() || arrayElement.isJsonNull()) {
                continue;
            }
            if (arrayElement.isJsonArray()) {
                list.add(deserialize(arrayElement.getAsJsonArray()));
                continue;
            }
            list.add(deserialize(arrayElement.getAsJsonPrimitive()));
        }
        return list;
    }

    private Object deserialize(final JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            try {
                return primitive.getAsBigInteger();
            } catch (final NumberFormatException nfe) {
                return primitive.getAsBigDecimal();
            }
        }
        return primitive.getAsString();
    }

    @Override
    public void save(final Configuration configuration, final IDataSource source) throws Exception {
        final JsonObject root = new JsonObject();
        saveToObject(root, configuration);
        try (BufferedWriter writer = source.openWriter()) {
            writer.write(json.asString(root));
        }
    }

    private void saveToObject(final JsonObject object, final Configuration configuration) {
        JsonElement json;
        for (final String key : configuration.keySet()) {
            if (configuration.isConfiguration(key)) {
                final JsonObject child = new JsonObject();
                saveToObject(child, configuration.getConfiguration(key));
                object.add(key, child);
                continue;
            }
            json = serialize(configuration.get(key));
            if (json == null) {
                continue;
            }
            object.add(key, json);
        }
    }

    private JsonElement serialize(final Object object) {
        if (object instanceof final List<?> list) {
            final JsonArray array = new JsonArray();
            for (final Object elem : list) {
                array.add(serialize(elem));
            }
            return array;
        }
        if (object instanceof final String string) {
            return new JsonPrimitive(string);
        }
        if (object instanceof final Number number) {
            return new JsonPrimitive(number);
        }
        if (object instanceof final Character character) {
            return new JsonPrimitive(character);
        }
        if (object instanceof final Boolean bool) {
            return new JsonPrimitive(bool);
        }
        return null;
    }

}
