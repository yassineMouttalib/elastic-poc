package com.django.poc.service.hero;

import com.django.poc.config.RestClientConfigHero;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HeroBuilder {

    public static final String FILENAME = "/Users/ymou/Desktop/yassine/data/heros.json";
    @Autowired
    private RestClientConfigHero restClientConfigHero;

    @Autowired
    public static void insertData(RestClientConfigHero restClientConfigHero) {
        JsonArray jsonArray = importJsonFile();
        for (JsonValue jsonValue : jsonArray) {
            restClientConfigHero.addBulkOperation(jsonValue);
        }

        restClientConfigHero.indexAllObjects();
    }

    private static JsonArray importJsonFile() {
        try {
            InputStream inputStream = new FileInputStream(FILENAME);
            var jsonReader = Json.createReader(inputStream);
            return jsonReader.readArray();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
