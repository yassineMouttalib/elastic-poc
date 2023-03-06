package com.django.poc.service.hero;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import com.django.poc.config.RestClientConfigHero;
import com.django.poc.model.jsonmodel.SuperHeroJsonModel;
import com.django.poc.security.SecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HeroSearchService {
    public static final String JSON_TEMPLATE_PATH = "src/main/resources/q1.json";
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ElasticsearchClient client;
    RestClientConfigHero restClientConfigHero;
    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    public HeroSearchService(RestClientConfigHero restClientConfigHero) {
        this.restClientConfigHero = restClientConfigHero;
        this.client = restClientConfigHero.getClient();
    }

    public SuperHeroJsonModel getDocument(String fileId) {
        SuperHeroJsonModel superHeroJsonModel = null;
        try {
            GetResponse<SuperHeroJsonModel> response = client.get(g -> g.index(restClientConfigHero.CUSTOM_INDEX).id(fileId), SuperHeroJsonModel.class);

            if (response.found()) {
                superHeroJsonModel = response.source();
                logger.info(superHeroJsonModel.toString());
            } else {
                logger.info("Object not found");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return superHeroJsonModel;
    }

    public List<Hit<SuperHeroJsonModel>> searchDocument(String searchText) {

        List<Hit<SuperHeroJsonModel>> hits;
        try {
            SearchResponse<SuperHeroJsonModel> response = client.search(s -> s.index(RestClientConfigHero.CUSTOM_INDEX).query(q -> q.matchAll(f -> f.queryName(searchText)))

                    , SuperHeroJsonModel.class);
            TotalHits total = response.hits().total();
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

            if (isExactResult) {
                logger.info("There are " + total.value() + " results");
            } else {
                logger.info("There are more than " + total.value() + " results");
            }
            //System.out.println(response.hits());
            hits = response.hits().hits();
            for (Hit<SuperHeroJsonModel> hit : hits) {
                SuperHeroJsonModel superHeroJsonModel = hit.source();
                Map<String, List<String>> highlightFields = hit.highlight();
                System.out.println(superHeroJsonModel);
//                printHighlightedFields(hit.fields(), highlightFields);
                logger.info("Found Object " + superHeroJsonModel.getName() + ", score " + hit.score());
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return hits;
    }

    private static void printHighlightedFields(Map<String, JsonData> sourceAsMap, Map<String, List<String>> highlightFields) {
        for (Map.Entry<String, JsonData> entry : sourceAsMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (highlightFields.containsKey(key)) {
                String highlightValue = ((String) highlightFields.get(key).toString());
                System.out.println("Key: " + key + " Highlighted Value: " + highlightValue);
            } else if (value instanceof Map) {
                printHighlightedFields((Map<String, JsonData>) value, highlightFields);
            }
        }
    }


    public void seachData(String query) throws IOException {
        String url = "https://localhost:9200/django-core/_search?pretty=true&q=".concat(query);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(JSON_TEMPLATE_PATH));

        // Replace "Superman" with query in the JSON object
        ObjectNode multiMatch = (ObjectNode) root.path("query").path("multi_match");
        String originalQuery = multiMatch.path("query").asText();
        String updatedQuery = originalQuery.replace("Superman", query);
        multiMatch.put("query", updatedQuery);

        // Create an HttpClient instance that ignores SSL certificate validation
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(securityConfig.getSSLConnectionSocket())
                .setDefaultCredentialsProvider(securityConfig.getProvider()).build()) {

            HttpEntity entity = new StringEntity(root.toString(), ContentType.APPLICATION_JSON);
            HttpPost request = new HttpPost(url);
            request.setEntity(entity);
            Long consumedTime = System.currentTimeMillis();
            CloseableHttpResponse response = httpClient.execute(request);
            logger.info("searching server made {} milliseconds to get receive Data",
                    String.valueOf(System.currentTimeMillis() - consumedTime));
            // Extract the response body
            HttpEntity entityResponse = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                JsonReader jsonReader = null;
                try {
                    inputStream = entityResponse.getContent();
                    jsonReader = Json.createReader(inputStream);
                    JsonObject jsonResponse = jsonReader.readObject();
                    StringBuilder fieldNames = new StringBuilder();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(jsonResponse.toString()).get("hits").get("hits");
                    getFieldsContainingValue(rootNode, query, fieldNames, "");
                    logger.info(fieldNames.toString());
                    logger.info("request treated in {} milliseconds",
                            String.valueOf(System.currentTimeMillis() - consumedTime));
                    response.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (jsonReader != null) {
                        jsonReader.close();
                    }
                }
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getFieldsContainingValue(JsonNode node, String searchValue, StringBuilder fieldNames, String path) {

        if (node.isObject()) {
            Iterator<String> fieldNamesIterator = node.fieldNames();
            while (fieldNamesIterator.hasNext()) {
                String fieldName = fieldNamesIterator.next();
                JsonNode fieldValueNode = node.get(fieldName);
                if (fieldValueNode != null) {
                    String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    if (fieldValueNode.isContainerNode()) {
                        getFieldsContainingValue(fieldValueNode, searchValue, fieldNames, fieldPath);
                    } else if (fieldValueNode.isValueNode() && fieldValueNode.asText().toLowerCase().contains(searchValue.toLowerCase())) {
                        fieldNames.append(fieldPath).append("\n");
                    }
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode arrayElement = node.get(i);
                String id = "";
                if (arrayElement.has("_index")) {
                    JsonNode nod = arrayElement.get("_source");
                    String fieldFormat = "Hit: Id = %s in fields\n";
                    fieldNames = fieldNames.append(String.format("Hit: Id = %s in fields\n", nod.get("id").asText()), 0, fieldFormat.length() - 1);
                    getFieldsContainingValue(arrayElement.get("_source"), searchValue, fieldNames, path);
                } else {
                    if (arrayElement.has("id")) {
                        id = arrayElement.get("id").asText();
                        String elementPath = path.isEmpty() ? "[" + id + "]" : path + "[id=" + id + "]";
                        getFieldsContainingValue(arrayElement, searchValue, fieldNames, elementPath);
                    }
                }
            }
        }
    }
}

