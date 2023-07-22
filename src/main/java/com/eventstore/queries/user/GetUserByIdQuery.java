package com.eventstore.queries.user;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserByIdQuery {
    private UUID userId;
}
