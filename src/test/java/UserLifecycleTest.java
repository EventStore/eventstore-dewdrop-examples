import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;
import static org.awaitility.pollinterval.FibonacciPollInterval.fibonacci;

import com.eventstore.commands.user.CsrClaimUsernameCommand;
import com.eventstore.commands.user.UserClaimUsernameCommand;
import com.eventstore.commands.user.UserCommand;
import com.eventstore.commands.user.UserSearchUsernameCommand;
import com.eventstore.commands.user.UserSignupCommand;
import com.eventstore.events.user.CsrSearchedUsernameCommand;
import com.eventstore.queries.user.GetUserByIdQuery;
import com.eventstore.queries.user.SearchUsernameQuery;
import com.eventstore.readmodels.user.User;
import com.github.dhiraj072.randomwordgenerator.RandomWordGenerator;
import com.github.dhiraj072.randomwordgenerator.exceptions.DataMuseException;
import events.dewdrop.Dewdrop;
import events.dewdrop.api.result.Result;
import events.dewdrop.api.validators.ValidationException;
import events.dewdrop.config.DewdropProperties;
import events.dewdrop.config.DewdropSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log4j2
public class UserLifecycleTest {
    String username = "IAmEventStore!";
    DewdropProperties properties = DewdropProperties.builder()
        .packageToScan("com.eventstore")
        .connectionString("esdb://localhost:2113?tls=false")
        .create();

    Dewdrop dewdrop;

    @BeforeEach
    void setup() {
        dewdrop = DewdropSettings.builder()
            .properties(properties)
            .create()
            .start();
    }

    @Test
    User signup(Optional<String> defaultUsername) throws ValidationException {

        // Create user
        UserSignupCommand userSignupCommand = createUser();

        // search for available usernames
        String availableUsername = searchAvailableUsername(userSignupCommand.getUserId(), defaultUsername.orElse(username));

        // claim username
        claimUsername(userSignupCommand.getUserId(), availableUsername);

        // find user
        User user = findUser(userSignupCommand.getUserId());

        log.info("USER:{}", user);
        return user;
    }

//    @Test
    void reportUsername() throws ValidationException, DataMuseException {
        //Create user with a filthy nasty username
        User user = signup(Optional.of("OhMyThatIsVeryBad..."));

        // Report user with filthy nasty username
//        ReportNastyFilthyUsernameCommand command = new ReportNastyFilthyUsernameCommand(user.getUserId(), user.getUsername());
//        dewdrop.executeCommand(command);
//        Find User with filthy nasty username
//        user = (User) dewdrop.executeQuery(new FindNastyFilterUsernameQuery()).get();

        // generate random word
        String randomWord = RandomWordGenerator.getRandomWord()
            .replaceAll(" ", "-");
        UUID csrUserId = UUID.randomUUID();

        // search if username is already claimed
        String newUsername = searchAvailableUsername(user.getUserId(), csrUserId, randomWord);

        // csr claims username on behalf of user
        CsrClaimUsernameCommand csrClaimedUsername = new CsrClaimUsernameCommand(user.getUserId(), csrUserId, newUsername);
        dewdrop.executeCommand(csrClaimedUsername);

        // fetch user again
        user = findUser(user.getUserId());

        log.info("USER:{}", user);
    }

    private User findUser(UUID userId) {
        GetUserByIdQuery getUserById = new GetUserByIdQuery(userId);
        final List<User> result = new ArrayList<>();
        retryUntilComplete(getUserById, (userResult) -> {
            log.info("userResult: {}", userResult);
            if (!userResult.isValuePresent()) {return false;}
            User user = (User) userResult.get();
            if (StringUtils.isNotEmpty(user.getUsername()) && user.getUserId()
                .equals(userId)) {
                result.add(user);
                return true;
            }
            return false;
        });
        return result.get(0);
    }

    private UserSignupCommand createUser() throws ValidationException {
        UserSignupCommand userSignupCommand = new UserSignupCommand(UUID.randomUUID(), "matt@eventstore.com");
        dewdrop.executeCommand(userSignupCommand);
        return userSignupCommand;
    }

    private String searchAvailableUsername(UUID userId, String username) throws ValidationException {
        return searchAvailableUsername(userId, null, username);
    }

    private String searchAvailableUsername(UUID userId, UUID csrUserId, String username) throws ValidationException {
        SearchUsernameQuery usernameQuery = new SearchUsernameQuery(username);
        Result<Boolean> hasUsername = Result.of(true);
        int count = -1;
        String attemptedUsername = "";
        while (hasUsername.get()) {
            attemptedUsername = username + "-" + ++count;
            usernameQuery.setUsername(attemptedUsername);

            UserCommand userSearchedUsername = csrUserId == null ? new UserSearchUsernameCommand(userId, attemptedUsername) : new CsrSearchedUsernameCommand(userId, csrUserId, attemptedUsername);
            dewdrop.executeCommand(userSearchedUsername);

            hasUsername = retryUntilComplete(usernameQuery, (result) -> {
                log.info("result: {}", result);
                return result.isValuePresent();
            });
        }

        return attemptedUsername;
    }

    private UserClaimUsernameCommand claimUsername(UUID userId, String availableUsername) throws ValidationException {
        UserClaimUsernameCommand claimUsernameCommand = new UserClaimUsernameCommand(userId, availableUsername);
        dewdrop.executeCommand(claimUsernameCommand);
        return claimUsernameCommand;
    }

    private <T> Result<T> retryUntilComplete(Object query, Predicate<Result<T>> predicate) {
        final List<Result<T>> finalResult = new ArrayList<>();
        with().pollInterval(fibonacci(SECONDS))
            .await()
            .timeout(100000L, SECONDS)
            .until(() -> {
                Result<T> result = dewdrop.executeQuery(query);
                boolean test = predicate.test(result);
                if (test) {
                    finalResult.add(result);
                }
                return test;
            });
        return finalResult.get(0);
    }
}
