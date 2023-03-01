package com.django.poc.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
@Getter
@Setter
public class DjangoFile implements Serializable {

    private  final Long id;
    private  String fileName;
    private  String content;
    private  Path path;
    private String extension;
    private String creationTime;
    private String lastModifiedTime;

    public DjangoFile(Long id, String fileName, String content, Path path) {
        this.fileName = fileName;
        this.content = content;
        this.id = id;
        this.path = path;

    }

    public DjangoFile(Long id, Path filePath) {
        this(id, filePath.getFileName().toString(), loadFile(filePath), filePath);
        setup();
    }

    public DjangoFile() {
        this.fileName = "";
        this.content = "";
        this.id = 0l;
        this.path = null;
        this.extension = "";
        this.creationTime = "";
        this.lastModifiedTime = "";
    }

    @Override
    public String toString() {
        return "DjangoFile{" +
                "\n id=" + id +
                "\n fileName='" + fileName + '\'' +
                "\n path=" + path +
                "\n extension='" + extension + '\'' +
                "\n creationTime='" + creationTime + '\'' +
                "\n lastModifiedTime='" + lastModifiedTime + '\'' +
                "\n content='" + content.length() + '\'' +
                '}';
    }

    private static String loadFile(Path filePath) {
        try {
            return String.join("\n", Files.readAllLines(filePath));
        } catch (IOException e) {
            return "No readable data";
        }
    }

    private void setup() {
        try {
            BasicFileAttributes attr =
                    Files.readAttributes(path, BasicFileAttributes.class);
            extension = FilenameUtils.getExtension(fileName);
            creationTime = attr.creationTime().toString();
            lastModifiedTime = attr.lastModifiedTime().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
