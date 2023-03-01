package com.django.poc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileGenerator {
    private static final String FILE_SOURCE = "/Users/ymou/Desktop/yassine/projects/django/django-core/alert/src/main/java/com/legisway/django/repository/change/ImplicitlyRemovableAlertTypesContributor.java";
    private static final String FILE_DEST = "/Users/ymou/Desktop/yassine/data";

    public static void generateFiles(int n) {

        try {
            Path sourceFIle = Paths.get(FILE_SOURCE);
            List<String> lines = Files.readAllLines(sourceFIle);
            Path destFile = Paths.get(FILE_DEST);

            for (int i= 0;i<n;i++) {
                Path path = Files.createFile(Paths.get(destFile+"\\"+i+".java"));
                Files.writeString(path, lines.stream().reduce("",(a,b) -> a.concat(b).concat("\n")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
