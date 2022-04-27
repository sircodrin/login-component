package dot.cpp.login.service;

import dot.cpp.login.constants.Error;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.models.session.entity.Session;
import dot.cpp.login.models.session.repository.SessionRepository;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoginService {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;

  @Inject
  public LoginService(UserRepository userRepository, SessionRepository sessionRepository) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
  }

  public Session login(String userName, String password) throws LoginException {
    var user = userRepository.findByField("userName", userName, User.class);

    if (user == null) {
      throw new LoginException(Error.NOT_FOUND);
    } else {
      if (!user.getPassword().equals(password)) {
        throw new LoginException(Error.INCORRECT_PASSWORD);
      }

      var token = UUID.randomUUID().toString();
      var session = new Session();
      session.setAccessToken(token);
      session.setUserId(user.getId());
      session.setCreateTime(Instant.now().toEpochMilli());
      sessionRepository.save(session);

      logger.debug("{}", user);
      return session;
    }
  }

  public boolean logout() {
    return false;
  }
}
