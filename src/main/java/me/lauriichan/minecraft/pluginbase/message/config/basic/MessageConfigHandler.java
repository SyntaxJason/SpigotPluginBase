package me.lauriichan.minecraft.pluginbase.message.config.basic;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.util.Json;

public final class MessageConfigHandler implements IConfigHandler {

    public static final MessageConfigHandler MESSAGE = new MessageConfigHandler();

    private final Json json = JsonConfigHandler.JSON.json();

    private MessageConfigHandler() {}

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
                final StringBuilder builder = new StringBuilder();
                boolean first = true;
                for (final JsonElement arrayElement : element.getAsJsonArray()) {
                    if (!arrayElement.isJsonPrimitive()) {
                        continue;
                    }
                    if (first) {
                        first = false;
                    } else {
                        builder.append("\n");
                    }
                    builder.append(arrayElement.getAsString());
                }
                configuration.set(key, builder.toString());
                continue;
            }
            configuration.set(key, element.getAsString());
        }
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
        String value;
        String[] lines;
        for (final String key : configuration.keySet()) {
            if (configuration.isConfiguration(key)) {
                final JsonObject child = new JsonObject();
                saveToObject(child, configuration.getConfiguration(key));
                object.add(key, child);
                continue;
            }
            value = configuration.get(key, String.class);
            if (value == null) {
                continue;
            }
            if (value.contains("\n")) {
                lines = value.split("\n");
                final JsonArray array = new JsonArray();
                for (final String line : lines) {
                    array.add(line);
                }
                object.add(key, array);
                continue;
            }
            object.addProperty(key, value);
        }
    }

}
