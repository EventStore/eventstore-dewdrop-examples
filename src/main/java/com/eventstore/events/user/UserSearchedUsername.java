package com.eventstore.events.user;

import events.dewdrop.read.readmodel.annotation.CreationEvent;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSearchedUsername extends UserEvent {
    String username;

    public UserSearchedUsername(UUID userId, String username) {
        super(userId);
        this.username = username;
    }
}
