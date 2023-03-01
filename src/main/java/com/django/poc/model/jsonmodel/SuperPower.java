package com.django.poc.model.jsonmodel;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class SuperPower {
    private  Long id;
    private  String name;
    private String type;
    private  String description;
}
