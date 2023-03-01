//package com.django.poc.service;
//
//import com.django.poc.config.RestClientConfigDjango;
//import com.django.poc.model.DjangoFile;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.nio.file.FileVisitOption;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class DjangoFolderImportService {
//    Logger logger = LoggerFactory.getLogger(this.getClass());
//    @Autowired
//    RestClientConfigDjango restClientConfigDjango;
//
//    List<DjangoFile> djangoFiles = new ArrayList<>();
//    Path rootPath = Paths.get("/Users/ymou/Desktop/yassine/projects/django/django-core/alert");
//    //Path rootPath = Paths.get("/Users/ymou/Desktop/yassine/data");
//
//    private Long nextIndex = 1l;
//
//    @Autowired
//    public DjangoFolderImportService(RestClientConfigDjango restClientConfigDjango) {
//        this.restClientConfigDjango = restClientConfigDjango;
//    }
//
//    public void importFolders() throws Exception {
//        Files.walk(rootPath, 10, FileVisitOption.FOLLOW_LINKS)
//                .filter(path -> !path.toFile().getPath().contains("/target/"))
//                .filter(path -> !path.toFile().getPath().contains("/.git/"))
//                .filter(path -> !path.toFile().getPath().contains("/.idea/"))
//                .filter(Files::isRegularFile)
//                .filter(Files::isReadable)
//                .forEach(this::importFile);
//
//        restClientConfigDjango.indexAllFiles();
//    }
//
//    private void importFile(Path path) {
//        try {
//            DjangoFile djangoFile = new DjangoFile(nextIndex++, path);
//            logger.info(djangoFile.toString()); // use Open Search client to add the file
////            restClientConfig.indexFile(djangoFile);
//            restClientConfigDjango.addBulkOperation(djangoFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void deleteFolders(String index, String id) throws Exception {
//        restClientConfigDjango.deleteDocument(index, id);
//    }
//
//    public boolean hasEmportedFolders() {
//        return !djangoFiles.isEmpty();
//    }
//}
