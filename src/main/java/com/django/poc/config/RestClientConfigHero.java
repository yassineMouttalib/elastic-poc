package com.django.poc.config;//package com.django.poc.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.django.poc.model.DjangoFile;
import com.django.poc.service.hero.HeroBuilder;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;


@SuppressWarnings("checkstyle:JavadocType")
@Component
public class RestClientConfigHero {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private ElasticsearchClient client;
    public static final String CUSTOM_INDEX = "django-core";
    public static final String ID = "id";
    public static final String FILE_NAME = "fileName";
    public static final String FILE_CONTENT = "content";
    public static final String EXTENSION = "extension";
    private static final String PATH = "path";
    public static final String CREATION_TIME = "creationTime";
    public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
    public static final String TOTAL_FIELDS_LIMIT = "4000";
    public static final String TOTAL_FIELDS_LIMIT_NAME = "index.mapping.total_fields.limit";
    public static final int TIMEOUT = 600000;
    ElasticsearchTransport transport;
    BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

    public RestClientConfigHero() {
        setUp();
    }

    private void setUp() {
        try {
            BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
            credsProv.setCredentials(
                    AuthScope.ANY, new UsernamePasswordCredentials("elastic", "admin")
            );

            RestClient restClient = RestClient
                    .builder(new HttpHost("localhost", 9200, "https"))
                    .setHttpClientConfigCallback(hc -> hc
                            .setDefaultCredentialsProvider(credsProv)
                    ).setRequestConfigCallback(a -> a.setSocketTimeout(TIMEOUT))
                    .build();


            // Create the transport with a Jackson mapper
            transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());

            // And create the API client
            client = new ElasticsearchClient(transport);
            BooleanResponse exists = client.indices().exists(a -> a.index(CUSTOM_INDEX));
            if (!exists.value()) {
                client.indices().create(c -> c.index(CUSTOM_INDEX));
            }
            client.indices().putSettings(a -> a.settings(b -> b.otherSettings(TOTAL_FIELDS_LIMIT_NAME,
                    JsonData.of(TOTAL_FIELDS_LIMIT))));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * index a file in OpenSearch database. to use only if we need to index 1 file.
     *
     * @param dFile file to index
     */
    public void indexFile(DjangoFile dFile) {
        try {
            //Index some data
            IndexRequest<DjangoFile> indexRequest = new IndexRequest.Builder<DjangoFile>().index(CUSTOM_INDEX)
                    .id(dFile.getId().toString()).document(dFile).build();
            client.index(indexRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void indexAllObjects() {
        try {
            BulkResponse result = client.bulk(bulkRequestBuilder.build());
            // Log errors, if any
            if (result.errors()) {
                logger.error("Bulk had errors");
                for (BulkResponseItem item : result.items()) {
                    if (item.error() != null) {
                        logger.error(item.error().reason());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addBulkOperation(JsonValue jsonObj)  {
        try {
            bulkRequestBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(CUSTOM_INDEX)
                            .id(jsonObj.asJsonObject().getString(ID))
                            .document(jsonObj)
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ElasticsearchClient getClient() {
        return client;
    }
}