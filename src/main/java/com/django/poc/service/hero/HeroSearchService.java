package com.django.poc.service.hero;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.django.poc.config.RestClientConfigHero;
import com.django.poc.model.jsonmodel.SuperHeroJsonModel;

import java.io.IOException;
import java.util.List;

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

    public List<Hit<SuperHeroJsonModel>> searchDocument(String searchText) {
        List<Hit<SuperHeroJsonModel>> hits;
        try {
            SearchResponse<SuperHeroJsonModel> response = client.search(s -> s
                            .index(RestClientConfigHero.CUSTOM_INDEX)
                            .query(q -> q
                                    .match(t -> t
                                            .field("name")
                                            .query(searchText)
                                    )
                            )
                            .fields(a -> a.field("*")),
                    SuperHeroJsonModel.class
            );
            TotalHits total = response.hits().total();
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

            if (isExactResult) {
                logger.info("There are " + total.value() + " results");
            } else {
                logger.info("There are more than " + total.value() + " results");
            }

            hits = response.hits().hits();
            for (Hit<SuperHeroJsonModel> hit : hits) {
                SuperHeroJsonModel superHeroJsonModel = hit.source();
                logger.info("Found Object " + superHeroJsonModel.getName() + ", score " + hit.score());
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return hits;
    }
}
