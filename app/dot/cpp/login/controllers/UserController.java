package dot.cpp.login.controllers;

import static dot.cpp.login.helpers.CookieHelper.getCookie;

import com.google.gson.JsonObject;
import dot.cpp.login.annotations.Authentication;
import dot.cpp.login.attributes.GeneralAttributes;
import dot.cpp.login.constants.Constants;
import dot.cpp.login.constants.UserStatus;
import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.ApplicationException;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.exceptions.UserException;
import dot.cpp.login.helpers.CookieHelper;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.request.AcceptInviteRequest;
import dot.cpp.login.models.user.request.ForgotPasswordRequest;
import dot.cpp.login.models.user.request.InviteUserRequest;
import dot.cpp.login.models.user.request.LoginRequest;
import dot.cpp.login.models.user.request.ResetPasswordRequest;
import dot.cpp.login.service.LoginService;
import dot.cpp.login.service.RequestErrorService;
import dot.cpp.login.service.UserService;
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
import play.mvc.Result;

public class UserController extends Controller {

  private static final Class<User> clazz = User.class;

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final FormFactory formFactory;
  private final MessagesApi messagesApi;
  private final UserService userService;
  private final LoginService loginService;
  private final RequestErrorService requestErrorService;
  private final MailerClient mailerClient;

  @Inject
  public UserController(
      UserService userService,
      LoginService loginService,
      RequestErrorService requestErrorService,
      FormFactory formFactory,
      MessagesApi messagesApi,
      MailerClient mailerClient) {
    this.userService = userService;
    this.loginService = loginService;
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
    this.mailerClient = mailerClient;
    this.requestErrorService = requestErrorService;
  }

  public void sendInviteEmail(String emailAddress, String uuid) {
    Email email =
        new Email()
            .setSubject("dot.cpp Invite")
            .setFrom("dot.cpp <alshopcontact@gmail.com>")
            .addTo(emailAddress)
            .setBodyText(
                "Create your dot.cpp account by going to: http://localhost:9000/accept-invite/"
                    + uuid);

    mailerClient.send(email);
  }

  public void sendResetPasswordEmail(String emailAddress, String uuid) {
    Email email =
        new Email()
            .setSubject("dot.cpp Reset Password")
            .setFrom("dot.cpp <alshopcontact@gmail.com>")
            .addTo(emailAddress)
            .setBodyText(
                "Reset your dot.cpp password by going to: http://localhost:9000/reset-password/"
                    + uuid);

    mailerClient.send(email);
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
      requestErrorService.handleFormErrorWithRefresh(request, form);
      // todo needs different behaviour?
    }

    try {
      final var loginRequest = form.get();
      logger.debug("{}", request);
      final String clientIp = request.remoteAddress();
      logger.debug("{}", clientIp);
      final var tokens =
          loginService.login(loginRequest.getUsername(), loginRequest.getPassword(), clientIp);
      return ok(tokens.toString())
          .withCookies(
              getCookie(Constants.ACCESS_TOKEN, tokens.get(Constants.ACCESS_TOKEN).getAsString()));
      // todo cookie will be replaced with auth token with React frontend
    } catch (LoginException e) {
      logger.error("", e);
      return badRequest("bad");
    }
  }

  public Result modifyUser(String id) {
    User user = userService.findById(id, clazz);
    return ok(user.toString());
  }

  public Result save() {
    final User user = new User();
    user.setEmail("none@yahoo.com");
    user.setUserName("john");
    user.setRole(UserRole.ADMIN);
    user.setPassword("liquidDnb!1");
    user.setStatus(UserStatus.ACTIVE);
    userService.save(user);
    return ok(user.toString());
  }

  public Result findByValue(String field, String value) {
    return ok(userService.findByField(field, value, clazz).toString());
  }

  public Result get(String id) {
    return ok(userService.findById(id, clazz).toString());
  }

  @Authentication(redirectUrl = "/login", userRole = UserRole.ADMIN)
  public Result inviteUserPage(Http.Request request) {
    final var messages = messagesApi.preferred(request);
    var form = formFactory.form(InviteUserRequest.class);

    return ok(dot.cpp.login.views.html.inviteUser.render(form, request, messages));
  }

  @Authentication(redirectUrl = "/login", userRole = UserRole.ADMIN)
  public Result inviteUser(Http.Request request) {
    final var form = formFactory.form(InviteUserRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    var email = form.get().getEmail();
    final String resetPasswordUuid =
        userService.generateUserWithResetPassword(email, UserRole.ADMIN);
    sendInviteEmail(email, resetPasswordUuid);

    return ok("invited");
  }

  @Authentication(redirectUrl = "/login", userRole = UserRole.ALL)
  public Result logout(Http.Request request) {
    logger.debug("{}", request);

    try {
      loginService.logout(request.attrs().get(GeneralAttributes.USER_ID));
      return ok("logged out");
    } catch (ApplicationException e) {
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }
  }

  public Result forgotPasswordPage(Http.Request request) {
    final var messages = messagesApi.preferred(request);
    var form = formFactory.form(ForgotPasswordRequest.class);

    return ok(dot.cpp.login.views.html.forgotPassword.render(form, request, messages));
  }

  public Result forgotPassword(Http.Request request) {
    var form = formFactory.form(ForgotPasswordRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      requestErrorService.handleFormErrorWithRefresh(request, form); // todo
    }

    try {
      final String resetPasswordUuid = userService.generateResetPasswordUuid(form.get().getEmail());
      sendResetPasswordEmail(form.get().getEmail(), resetPasswordUuid);
    } catch (UserException e) {
      logger.error(e.getMessage());
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }

    return ok("password reset");
  }

  public Result refreshAccessToken(Http.Request request) {
    // todo check expired access token as well?
    logger.debug("{}", request.body().asJson());

    final var refreshTokenNode = request.body().asJson().get("refreshToken");

    if (refreshTokenNode.isMissingNode()) {
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }

    final String oldRefreshToken = refreshTokenNode.asText();
    logger.debug("{}", oldRefreshToken);

    try {
      final JsonObject tokens = loginService.refreshTokens(oldRefreshToken);
      logger.debug("{}", tokens);
      return ok(tokens.toString()).withCookies(
          getCookie(Constants.ACCESS_TOKEN, tokens.get(Constants.ACCESS_TOKEN).getAsString()));
    } catch (ApplicationException e) {
      logger.error("{}", e.getMessage());
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }
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
    final var user = userService.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

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
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(AcceptInviteRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    var acceptInviteRequest = form.get();
    logger.debug("acceptInviteRequest: {}", acceptInviteRequest);
    logger.debug("messages: {}", messages);

    try {
      final var user = userService.acceptInvitation(acceptInviteRequest, resetPasswordUuid);
      final String clientIp = request.remoteAddress();
      final var tokens = loginService.login(user.getUserName(), user.getPassword(), clientIp);
      return ok(tokens.toString()).withCookies(
          getCookie(Constants.ACCESS_TOKEN, tokens.get(Constants.ACCESS_TOKEN).getAsString()));
    } catch (Exception e) {
      logger.error(e.getMessage());
      Call call = new Call("GET", "login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }
  }

  public Result resetPasswordPage(Http.Request request, String resetPasswordUuid) {
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(ResetPasswordRequest.class);
    final var user = userService.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    logger.debug("{}", user);

    if (user == null) {
      Call call = new Call("GET", "/login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }

    return ok(
        dot.cpp.login.views.html.resetPassword.render(form, resetPasswordUuid, request, messages));
  }

  /**
   * Activates account.
   *
   * @param request Request
   * @return accept invitation
   */
  public Result resetPassword(Http.Request request, String resetPasswordUuid) {
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(ResetPasswordRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    var resetPasswordRequest = form.get();
    logger.debug("resetPasswordRequest: {}", resetPasswordRequest);
    logger.debug("messages: {}", messages);

    try {
      final var user = userService.resetPassword(resetPasswordRequest, resetPasswordUuid);
      final String clientIp = request.remoteAddress();
      final var tokens = loginService.login(user.getUserName(), user.getPassword(), clientIp);
      return ok(tokens.toString()).withCookies(
          getCookie(Constants.ACCESS_TOKEN, tokens.get(Constants.ACCESS_TOKEN).getAsString()));
    } catch (Exception e) {
      logger.error(e.getMessage());
      Call call = new Call("GET", "login", "");
      return requestErrorService.handleGenericErrors(call, request);
    }
  }
}
