package com.django.poc.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.django.poc.config.RestClientConfigDjango;
import com.django.poc.model.DjangoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class DjangoSearchService {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ElasticsearchClient client;
    private RestClientConfigDjango restClientConfigDjango;

    @Autowired
    public DjangoSearchService(RestClientConfigDjango restClientConfigDjango) {
        this.restClientConfigDjango = restClientConfigDjango;
        this.client = restClientConfigDjango.getClient();

    }

    public DjangoFile getDocument(String fileId) {
        DjangoFile djangoFile = null;
        try {
            GetResponse<DjangoFile> response = client.get(g -> g
                            .index(restClientConfigDjango.CUSTOM_INDEX)
                            .id(fileId),
                    DjangoFile.class
            );

            if (response.found()) {
                 djangoFile = response.source();
                logger.info(djangoFile.toString());
            } else {
                logger.info("File not found");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return djangoFile;
    }

    public List<Hit<DjangoFile>> searchDocument(String searchText) {
        List<Hit<DjangoFile>> hits;
        try {
            SearchResponse<DjangoFile> response = client.search(s -> s
                            .index(RestClientConfigDjango.CUSTOM_INDEX)
                            .query(q -> q
                                    .match(t -> t
                                            .field(RestClientConfigDjango.FILE_CONTENT)
                                            .query(searchText)
                                    )
                            ),
                    DjangoFile.class
            );

            TotalHits total = response.hits().total();
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

            if (isExactResult) {
                logger.info("There are " + total.value() + " results");
            } else {
                logger.info("There are more than " + total.value() + " results");
            }

            hits = response.hits().hits();
            for (Hit<DjangoFile> hit : hits) {
                DjangoFile djangoFile = hit.source();
                logger.info("Found File " + djangoFile.getFileName() + ", score " + hit.score());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return hits;
    }
}
