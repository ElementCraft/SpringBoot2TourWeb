package com.yyy.TourWeb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    String content;

    String userAccount;

    Long articleId;

    ZonedDateTime gmtCreate;
}
