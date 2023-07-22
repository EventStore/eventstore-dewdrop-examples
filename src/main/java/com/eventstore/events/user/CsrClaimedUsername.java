package com.eventstore.events.user;

import events.dewdrop.read.readmodel.annotation.CreationEvent;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@CreationEvent
public class CsrClaimedUsername extends UserClaimedUsername {
    private UUID csrId;

    public CsrClaimedUsername(UUID userId, UUID csrId, String username) {
        super(userId, username);
        this.csrId = csrId;
    }
}
