package com.eventstore.queries.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchUsernameQuery {
    private String username;

}
