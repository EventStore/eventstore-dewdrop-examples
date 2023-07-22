package com.eventstore.readmodels.user;

import com.eventstore.events.user.UserClaimedUsername;
import com.eventstore.queries.user.GetUserByIdQuery;
import com.eventstore.queries.user.RandomUserQuery;
import com.eventstore.queries.user.SearchUsernameQuery;
import events.dewdrop.api.result.Result;
import events.dewdrop.read.readmodel.annotation.DewdropCache;
import events.dewdrop.read.readmodel.annotation.EventHandler;
import events.dewdrop.read.readmodel.annotation.ReadModel;
import events.dewdrop.read.readmodel.annotation.Stream;
import events.dewdrop.read.readmodel.query.QueryHandler;
import events.dewdrop.read.readmodel.stream.StreamType;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ReadModel
@Stream(name = "UserAggregate", streamType = StreamType.CATEGORY, subscribed = true)
@Getter
public class UsersReadModel {
    @DewdropCache
    Map<UUID, User> cache;
    private Set<String> usernames = new HashSet<>();
    Random generator = new Random();

    @EventHandler
    public void on(UserClaimedUsername userClaimedUsername) {
        this.usernames.add(userClaimedUsername.getUsername());
    }

    @QueryHandler
    public User findById(GetUserByIdQuery userById) {
        User user = cache.get(userById.getUserId());
        return user;
    }

    @QueryHandler
    public Boolean usernameClaimed(SearchUsernameQuery username) {
        boolean contains = usernames.contains(username.getUsername());
        return contains;
    }

    @QueryHandler
    public Result<User> randomUser(RandomUserQuery randomUserQuery) {
        Optional<UUID> key = cache.keySet()
            .stream()
            .skip(ThreadLocalRandom.current()
                .nextInt(cache.size()))
            .findAny();
        if (key.isPresent()) {
            return Result.of(cache.get(key.get()));
        }
        return Result.empty();
    }
}
