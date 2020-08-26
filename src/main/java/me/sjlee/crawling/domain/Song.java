package me.sjlee.crawling.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Song implements Serializable {

    private int grade;

    private String title;

    private String artist;
}
