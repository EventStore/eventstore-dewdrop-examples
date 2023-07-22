package com.eventstore.events.user;

import events.dewdrop.read.readmodel.annotation.CreationEvent;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@CreationEvent
public class UserSignedUp extends UserEvent {
    String email;

    public UserSignedUp(UUID userId, String email) {
        super(userId);
        this.email = email;
    }
}
