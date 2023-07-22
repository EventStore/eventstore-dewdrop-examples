package com.eventstore.events.user;

import com.eventstore.commands.user.UserSearchUsernameCommand;
import java.util.UUID;
import lombok.Data;

@Data
public class CsrSearchedUsernameCommand extends UserSearchUsernameCommand {
    private UUID csrId;
    public CsrSearchedUsernameCommand(UUID csrId, UUID userId, String username) {
        super(userId, username);
        this.csrId = csrId;
    }
}
