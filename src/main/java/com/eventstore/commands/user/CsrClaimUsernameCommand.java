package com.eventstore.commands.user;

import java.util.UUID;
import lombok.Data;

@Data
public class CsrClaimUsernameCommand extends UserClaimUsernameCommand {
    private UUID csrId;
    public CsrClaimUsernameCommand(UUID userId, UUID csrId, String username) {
        super(userId, username);
        this.csrId = csrId;
    }

}
