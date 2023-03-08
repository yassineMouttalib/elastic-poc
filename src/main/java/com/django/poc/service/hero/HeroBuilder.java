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


    @Autowired
    private RestClientConfigHero restClientConfigHero;

    @Autowired
    public static void insertData(RestClientConfigHero restClientConfigHero, String filePath) {
        JsonArray jsonArray = importJsonFile(filePath);
        for (JsonValue jsonValue : jsonArray) {
            restClientConfigHero.addBulkOperation(jsonValue);
        }

        restClientConfigHero.indexAllObjects();
    }

    private static JsonArray importJsonFile(String filePath) {
        try {
            InputStream inputStream = new FileInputStream(filePath);
            var jsonReader = Json.createReader(inputStream);
            return jsonReader.readArray();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
