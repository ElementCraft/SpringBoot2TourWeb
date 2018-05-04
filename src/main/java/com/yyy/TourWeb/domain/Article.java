package com.yyy.TourWeb.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    Long id;

    String title;

    String content;

    Integer clickCount;

    Integer commentCount;

    String userAccount;

    String imgPath;

    Boolean isForeign;

    ZonedDateTime gmtCreate;
}
