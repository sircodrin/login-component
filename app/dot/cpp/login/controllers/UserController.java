package dot.cpp.login.controllers;

import dot.cpp.login.annotations.Authentication;
import dot.cpp.login.constants.UserStatus;
import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.helpers.CookieHelper;
import dot.cpp.login.models.session.entity.Session;
import dot.cpp.login.models.session.repository.SessionRepository;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import dot.cpp.login.models.user.request.AcceptInviteRequest;
import dot.cpp.login.models.user.request.InviteUserRequest;
import dot.cpp.login.models.user.request.LoginRequest;
import dot.cpp.login.service.LoginService;
import dot.cpp.login.service.RequestErrorService;
import java.util.UUID;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Call;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;

public class UserController extends Controller {

  private static final Class<User> clazz = User.class;

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final FormFactory formFactory;
  private final MessagesApi messagesApi;

  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;
  private final LoginService loginService;
  private final RequestErrorService requestErrorService;

  private final MailerClient mailerClient;

  @Inject
  public UserController(
      UserRepository user,
      LoginService loginService,
      RequestErrorService requestErrorService,
      FormFactory formFactory,
      MessagesApi messagesApi,
      SessionRepository sessionRepository,
      MailerClient mailerClient) {
    this.userRepository = user;
    this.loginService = loginService;
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
    this.sessionRepository = sessionRepository;
    this.mailerClient = mailerClient;
    this.requestErrorService = requestErrorService;
  }

  public void sendInviteEmail(String emailAddress, String uuid) {
    Email email =
        new Email()
            .setSubject("WaW Invite")
            .setFrom("Mister WaW <alshopcontact@gmail.com>")
            .addTo(emailAddress)
            .setBodyText("A WaW invitation: http://localhost:9000/accept-invite/" + uuid);

    mailerClient.send(email);
  }

  public Result register(Request request) {
    try {
      logger.debug("{}", request);
      return ok("registered");

    } catch (Exception e) {
      logger.error("", e);
      return badRequest("registration failed");
    }
  }

  public Result loginPage(Http.Request request, boolean userLoggedIn) {
    final var loginPage =
        ok(
            dot.cpp.login.views.html.login.render(
                formFactory.form(LoginRequest.class), request, messagesApi.preferred(request)));
    return userLoggedIn ? loginPage : CookieHelper.discardAuthorizationCookies(loginPage);
  }

  public Result login(Http.Request request) {
    var form = formFactory.form(LoginRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      requestErrorService.handleFormErrorWithRefresh(request, form); // todo
    }

    try {
      final var loginRequest = form.get();
      logger.debug("{}", request);
      final String clientIp = request.remoteAddress();
      logger.debug("{}", clientIp);
      final var session =
          loginService.login(loginRequest.getUsername(), loginRequest.getPassword(), clientIp);
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
    user.setEmail("none@yahoo.com");
    user.setUserName("john");
    user.setRole(UserRole.ADMIN);
    user.setPassword("liquidDnb!1");
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
    return ok(user.toString());
  }

  public Result findByValue(String field, String value) {
    return ok(userRepository.findByField(field, value, clazz).toString());
  }

  public Result get(String id) {
    return ok(userRepository.findById(id, clazz).toString());
  }

  @Authentication(redirectUrl = "/login", userRole = UserRole.ADMIN)
  public Result inviteUserPage(Http.Request request) {
    final var messages = messagesApi.preferred(request);
    var form = formFactory.form(InviteUserRequest.class);
    var accessCookie = request.getCookie("access_token");

    if (accessCookie.isPresent()) {
      var accessToken = accessCookie.get().value();
      var session = sessionRepository.findByField("accessToken", accessToken, Session.class);
      if (session != null) {
        logger.debug("{}", session);
        var user = userRepository.findById(session.getUserId().toString(), User.class);
        if (user != null) {
          logger.debug("{}", user);
          if (user.getRole() == UserRole.ADMIN) {
            return ok(dot.cpp.login.views.html.inviteUser.render(form, request, messages));
          }
        }
      }
    }

    return redirect("sus-login");
  }

  @Authentication(redirectUrl = "/login", userRole = UserRole.ADMIN)
  public Result inviteUser(Http.Request request) {
    final var form = formFactory.form(InviteUserRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    var email = form.get().getEmail();
    var user = new User();

    user.setRole(UserRole.ADMIN);
    user.setEmail(email);
    user.setUserName("temporary");
    user.setPassword("temporary");
    user.setResetPasswordUuid(UUID.randomUUID().toString());
    user.setStatus(UserStatus.INACTIVE);

    userRepository.save(user);
    logger.debug("{}", user);
    sendInviteEmail(email, user.getResetPasswordUuid());

    return ok("invited");
  }

  public Result forgotPasswordPage(Http.Request request) {
    /*
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(AcceptInviteRequest.class);
    final var user = userRepository.findByField("resetPasswordUuid", resetPasswordUuid, User.class);
    logger.debug("{}", user);

    if (user == null) {
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }

    return ok(
        dot.cpp.login.views.html.acceptInvite.render(form, resetPasswordUuid, request, messages));
     */
    return ok("not ready");
  }

  public Result forgotPassword(Http.Request request) {
    /*
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(AcceptInviteRequest.class);
    final var user = userRepository.findByField("resetPasswordUuid", resetPasswordUuid, User.class);
    logger.debug("{}", user);

    if (user == null) {
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }

    return ok(
        dot.cpp.login.views.html.acceptInvite.render(form, resetPasswordUuid, request, messages));
     */
    return ok("not ready");
  }

  /**
   * Activate account page for admin.
   *
   * @param request implicit request, must contain body with password and repeat password
   * @return accept invitation page
   */
  public Result registerByInvitePage(Http.Request request, String resetPasswordUuid) {
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(AcceptInviteRequest.class);
    final var user = userRepository.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    logger.debug("{}", user);

    if (user == null) {
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }

    return ok(
        dot.cpp.login.views.html.acceptInvite.render(form, resetPasswordUuid, request, messages));
  }

  /**
   * Activates account.
   *
   * @param request Request
   * @return accept invitation
   */
  public Result registerByInvite(Http.Request request, String resetPasswordUuid) {
    logger.debug("START");
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(AcceptInviteRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    var acceptInviteRequest = form.get();
    logger.debug("setPasswordRequest: {}", acceptInviteRequest);
    logger.debug("messages: {}", messages);

    try {
      final var user = acceptInvitation(acceptInviteRequest, resetPasswordUuid);
      final String clientIp = request.remoteAddress();
      final var session = loginService.login(user.getUserName(), user.getPassword(), clientIp);
      logger.debug("{}", session);
      return ok(session.toString()).withCookies(CookieHelper.getAccessCookie(session));
    } catch (Exception e) {
      Call call = new Call("GET", "login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }
  }

  private User acceptInvitation(AcceptInviteRequest acceptInviteRequest, String resetPasswordUuid) {
    logger.debug("{}", acceptInviteRequest);
    logger.debug("{}", resetPasswordUuid);

    final var user = userRepository.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    user.setPassword(acceptInviteRequest.getPassword());
    user.setUserName(acceptInviteRequest.getUsername());
    user.setResetPasswordUuid("");
    user.setStatus(UserStatus.ACTIVE);

    logger.debug("{}", user);

    userRepository.save(user);
    return user;
  }
}
