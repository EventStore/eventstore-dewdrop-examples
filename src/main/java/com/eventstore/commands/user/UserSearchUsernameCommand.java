package com.eventstore.commands.user;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSearchUsernameCommand extends UserCommand {

    @NotBlank(message = "Username is required")
    String username;
    public UserSearchUsernameCommand(UUID userId, String username) {
        super(userId);
        this.username = username;
    }

}
