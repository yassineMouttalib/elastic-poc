package com.django.poc.model.jsonmodel;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class SuperHeroJsonModel {
    private  Long id;
    private  String name;
    private String type;
    private  String description;
    private List<SuperPower> powers;

}
