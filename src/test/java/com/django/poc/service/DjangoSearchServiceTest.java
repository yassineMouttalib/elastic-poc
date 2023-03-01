//package com.django.poc.service;
//
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import com.django.poc.DisableSSLVerification;
//import com.django.poc.config.RestClientConfig;
//import com.django.poc.model.DjangoFile;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class DjangoSearchServiceTest {
//    RestClientConfig restClientConfig = new RestClientConfig();
//    DjangoSearchService djangoSearchService = new DjangoSearchService(restClientConfig);
//
//
//    DisableSSLVerification disableSSLVerification =  new DisableSSLVerification();
//
//    @Test
//    void getDocument() {
//        System.out.println("hello");
//        DjangoFile djangoFile =  djangoSearchService.getDocument("3");
//        System.out.println(djangoFile.toString());
//        assertEquals("hello",djangoFile.getFileName());
//    }
//
//    @Test
//    void searchDocument() {
//    }
//}