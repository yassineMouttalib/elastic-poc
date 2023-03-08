package com.django.poc;

import com.django.poc.config.RestClientConfigHero;
import com.django.poc.security.DisableSSLVerification;
import com.django.poc.service.hero.HeroBuilder;
import com.django.poc.service.hero.HeroSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CmdRunner implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CUSTOM_INDEX = "django-core";
    private static final String separator = "-------------------------------------------------";
    @Value("${FILENAME_HEROS_JSON_MODEL}")
    private String FILENAME_HEROS_JSON_MODEL;

    @Value("${FILENAME_HEROS_ROOT_REF_PATH_MODEL}")
    private String FILENAME_HEROS_ROOT_REF_PATH_MODEL;
    @Autowired
    DisableSSLVerification disableSSLVerification;
    //    @Autowired
//    DjangoFolderImportService service;
//    @Autowired
//    DjangoSearchService djangoSearchService;
    @Autowired
    RestClientConfigHero restClientConfigHero;
    @Autowired
    HeroSearchService heroSearchService;

    @Override
    public void run(String... args) throws Exception {
        //System.out.println("hello");
        //service.deleteFolders(CUSTOM_INDEX, "T8S4MIYBZOgJM4SSdYqv");
        //FileGenerator.generateFiles(100);
        //service.importFolders();
//        logger.info(separator);
//        djangoSearchService.getDocument("3");
//        logger.info(separator);
//        djangoSearchService.searchDocument("removableTypes");
//        logger.info(separator);
//        djangoSearchService.searchDocument("addToRemovableTypesIfExisting removableTypes");
//        logger.info(separator);
        //HeroBuilder.insertData(restClientConfigHero, FILENAME_HEROS_JSON_MODEL);
//        heroSearchService.searchDocument("Superman");
//        heroSearchService.seachData("Superman");

        //HeroBuilder.insertData(restClientConfigHero, FILENAME_HEROS_ROOT_REF_PATH_MODEL);
        heroSearchService.seachData("Flying");
    }
}
