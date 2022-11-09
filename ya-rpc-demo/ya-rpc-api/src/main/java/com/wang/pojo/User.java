package com.wang.pojo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    private int userId;
    private String userName;
}
