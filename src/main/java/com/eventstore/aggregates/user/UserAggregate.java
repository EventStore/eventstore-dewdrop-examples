package com.eventstore.aggregates.user;

import com.eventstore.commands.user.CsrClaimUsernameCommand;
import com.eventstore.commands.user.UserClaimUsernameCommand;
import com.eventstore.commands.user.UserSearchUsernameCommand;
import com.eventstore.commands.user.UserSignupCommand;
import com.eventstore.events.user.CsrClaimedUsername;
import com.eventstore.events.user.UserClaimedUsername;
import com.eventstore.events.user.UserSearchedUsername;
import com.eventstore.events.user.UserSignedUp;
import events.dewdrop.aggregate.annotation.Aggregate;
import events.dewdrop.aggregate.annotation.AggregateId;
import events.dewdrop.api.validators.ValidationException;
import events.dewdrop.command.CommandHandler;
import events.dewdrop.read.readmodel.annotation.EventHandler;
import events.dewdrop.structure.api.validator.DewdropValidator;
import java.util.UUID;
import lombok.Data;

@Aggregate
@Data
public class UserAggregate {
    @AggregateId
    UUID userId;
    private String username;
    private String email;

    public UserAggregate() {}

    @CommandHandler
    public UserSignedUp signup(UserSignupCommand command) throws ValidationException {
        DewdropValidator.validate(command);
        return new UserSignedUp(command.getUserId(), command.getEmail());
    }

    @CommandHandler
    public UserClaimedUsername userClaimedUsername(UserClaimUsernameCommand command) throws ValidationException {
        DewdropValidator.validate(command);
        return new UserClaimedUsername(command.getUserId(), command.getUsername());
    }
    @CommandHandler
    public UserSearchedUsername userClaimedUsername(UserSearchUsernameCommand command) throws ValidationException {
        DewdropValidator.validate(command);
        return new UserSearchedUsername(command.getUserId(), command.getUsername());
    }

    @CommandHandler
    public CsrClaimedUsername csrClaimedUsername(CsrClaimUsernameCommand command) throws ValidationException {
        DewdropValidator.validate(command);
        return new CsrClaimedUsername(command.getUserId(), command.getCsrId(), command.getUsername());
    }

    // reportoffensive


    @EventHandler
    public void on(UserSignedUp userSignedup) {
        this.userId = userSignedup.getUserId();
        this.email = userSignedup.getEmail();
    }

    @EventHandler
    public void on(UserClaimedUsername userClaimedUsername) {
        this.userId = userClaimedUsername.getUserId();
        this.username = userClaimedUsername.getUsername();
    }

    @EventHandler
    public void on(CsrClaimedUsername csrClaimedUsername) {
        this.userId = csrClaimedUsername.getUserId();
        this.username = csrClaimedUsername.getUsername();
    }

    // reported offensive
}
