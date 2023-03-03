package com.django.poc;

import com.django.poc.config.RestClientConfigHero;
//import com.django.poc.service.DjangoFolderImportService;
//import com.django.poc.service.DjangoSearchService;
import com.django.poc.security.DisableSSLVerification;
import com.django.poc.service.hero.HeroSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CmdRunner implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CUSTOM_INDEX = "django-core";
    private static final String separator = "-------------------------------------------------";

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
        //HeroBuilder.insertData(restClientConfigHero);
//        heroSearchService.searchDocument("Superman");
        heroSearchService.seachData();
    }
}
