package dot.cpp.login.service;

import dot.cpp.login.models.session.entity.Session;
import dot.cpp.login.models.session.repository.SessionRepository;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EntityServiceFactory {

  private final EntityService<User> userService;
  private final EntityService<Session> sessionService;

  @Inject
  public EntityServiceFactory(UserRepository userRepository, SessionRepository sessionRepository) {
    this.userService = new EntityService<>(userRepository);
    this.sessionService = new EntityService<>(sessionRepository);
  }

  public EntityService<User> getUserService() {
    return userService;
  }

  public EntityService<Session> getSessionService() {
    return sessionService;
  }
}
