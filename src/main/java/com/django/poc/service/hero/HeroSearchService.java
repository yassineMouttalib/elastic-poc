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
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
            GetResponse<SuperHeroJsonModel> response = client.get(g -> g
                            .index(restClientConfigHero.CUSTOM_INDEX)
                            .id(fileId),
                    SuperHeroJsonModel.class
            );

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
//    SearchRequest searchRequest = new SearchRequest.

//    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//searchSourceBuilder.query(QueryBuilders.boolQuery()
//        .should(QueryBuilders.matchQuery("name", "Superman"))
//            .should(QueryBuilders.nestedQuery("powers", QueryBuilders.matchQuery("powers.description", "Superman"), ScoreMode.None))
//            );
//searchSourceBuilder.fetchSource(new String[] {"name", "description", "powers.description"}, null);
//
//searchRequest.source(searchSourceBuilder);

    public List<Hit<SuperHeroJsonModel>> searchDocument(String searchText) {

//        Query byName = MatchQuery.of(m -> m
//                .field("name")
//                .query(searchText)
//        )._toQuery();
//        Query byPower= MatchQuery.of(m -> m
//                .field("powers")
//                .query(searchText)
//        )._toQuery();
//
//        NestedQuery nestedQuery = NestedQuery.of(a -> a.query( q ->q.));
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<Hit<SuperHeroJsonModel>> hits;
        try {
            SearchResponse<SuperHeroJsonModel> response = client.search(s -> s
                            .index(RestClientConfigHero.CUSTOM_INDEX)
//                            .highlight(h -> h.highlightQuery(a -> a.match(v -> v.field("*").query(searchText))))
//                            .highlight(h -> h.highlightQuery(q -> q.match(m -> m.field("*").query(searchText))))
                            //.highlight(h -> h.fields(searchText, f -> f.matchedFields("*")))
                            .query(q -> q
                                            .matchAll(f -> f.queryName(searchText))
//                                    .bool(b -> b.must(m -> m.matchAll(a -> a.queryName(searchText))))
//                                    .match(t -> t
//                                            .field("name")
//                                            .query(searchText)
//                                    )
                            )
//                            .highlight(h -> h.highlightQuery(q -> q.matchAll(m -> m.queryName(searchText))))
//                            .highlight(h -> h.fields("*", f -> f.matchedFields("*")))
                    ,
                    SuperHeroJsonModel.class
            );
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


    public void seachData() throws IOException {
        String url = "https://localhost:9200/django-core/_search?pretty=true&q=Superman";
        String json = """
                {
                  "query": {
                    "multi_match": {
                      "query": "Superman",
                      "fields": ["*"]
                    }
                  },
                  "_source": true,
                  "highlight": {
                    "fields": {
                      "*": {}
                    },
                    "pre_tags": ["<mark>"],
                    "post_tags": ["</mark>"]
                  }
                }""";
        // Create an HttpClient instance that ignores SSL certificate validation
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(securityConfig.getSSLConnectionSocket())
                .setDefaultCredentialsProvider(securityConfig.getProvider()).build()) {

            HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            HttpPost request = new HttpPost(url);
            request.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(request);

            // Extract the response body
            HttpEntity entityResponse = response.getEntity();
//            String responseBody = EntityUtils.toString(entityResponse);
            if (entity != null) {
                InputStream inputStream = null;
                JsonReader jsonReader = null;
                try {
                    inputStream = entityResponse.getContent();
                    jsonReader = Json.createReader(inputStream);
                    JsonObject jsonResponse = jsonReader.readObject();
                    JsonArray hits = jsonResponse.getJsonObject("hits").getJsonArray("hits");

                    for (int i = 0; i < hits.size(); i++) {
                        JsonObject hit = hits.getJsonObject(i);
                        String id = hit.getString("_id");
                        String name = hit.getJsonObject("_source").getString("name");

                        JsonObject highlight = hit.getJsonObject("highlight");
                        for (String fieldName : highlight.keySet()) {
                            JsonArray fragments = highlight.getJsonArray(fieldName);
                            for (int j = 0; j < fragments.size(); j++) {
                                String highlightedValue = fragments.getString(j);
                                System.out.println("ID: " + id + ", Name: " + name + ", Highlighted Field: " + fieldName + ", Highlighted Value: " + highlightedValue);
                            }
                        }
                    }
                    response.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // close the InputStream and JsonReader manually
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
}
