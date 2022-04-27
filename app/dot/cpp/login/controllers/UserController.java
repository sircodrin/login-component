package dot.cpp.login.controllers;

import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.helpers.CookieHelper;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import dot.cpp.login.service.LoginService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {

  private static final Class<User> clazz = User.class;

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final UserRepository userRepository;
  private final LoginService loginService;

  @Inject
  public UserController(UserRepository user, LoginService loginService) {
    this.userRepository = user;
    this.loginService = loginService;
  }

  public Result login() {
    try {
      var session = loginService.login("john", "liquidDnb!1");
      logger.debug("{}", session);
      return ok(session.toString()).withCookies(CookieHelper.getAccessCookie(session));

    } catch (LoginException e) {
      logger.error("", e);
      return badRequest("bad");
    }
  }

  public Result modifyUser(String id) {
    User user = userRepository.findById(id, clazz);
    return ok(user.toString());
  }

  public Result save() {
    final User user = new User();
    user.setId("asda1");
    user.setEmail("none@yahoo.com");
    user.setUserName("john");
    user.setRole(UserRole.ADMIN);
    user.setPassword("liquidDnb!1");
    userRepository.save(user);
    return ok(user.toString());
  }

  public Result findByValue(String field, String value) {
    return ok(userRepository.findByField(field, value, clazz).toString());
  }

  public Result get(String id) {
    return ok(userRepository.findById(id, clazz).toString());
  }
}
