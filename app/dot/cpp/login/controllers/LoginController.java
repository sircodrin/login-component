package dot.cpp.login.controllers;

import static dot.cpp.login.helpers.CookieHelper.getCookie;

import com.google.gson.JsonObject;
import dot.cpp.login.annotations.Authentication;
import dot.cpp.login.attributes.GeneralAttributes;
import dot.cpp.login.constants.Constants;
import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.ApplicationException;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.exceptions.UserException;
import dot.cpp.login.helpers.CookieHelper;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.request.AcceptInviteRequest;
import dot.cpp.login.models.user.request.ForgotPasswordRequest;
import dot.cpp.login.models.user.request.LoginRequest;
import dot.cpp.login.models.user.request.ResetPasswordRequest;
import dot.cpp.login.service.EmailService;
import dot.cpp.login.service.LoginService;
import dot.cpp.login.service.RequestErrorService;
import dot.cpp.login.service.UserService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Call;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class LoginController extends Controller {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final FormFactory formFactory;
  private final MessagesApi messagesApi;
  private final UserService userService;
  private final LoginService loginService;
  private final EmailService emailService;
  private final RequestErrorService requestErrorService;

  @Inject
  public LoginController(
      UserService userService,
      LoginService loginService,
      RequestErrorService requestErrorService,
      FormFactory formFactory,
      MessagesApi messagesApi,
      EmailService emailService) {
    this.userService = userService;
    this.loginService = loginService;
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
    this.emailService = emailService;
    this.requestErrorService = requestErrorService;
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
    }

    try {
      final var loginRequest = form.get();
      final var tokens = loginService.login(loginRequest.getUsername(), loginRequest.getPassword());
      return getOkWithCookies(tokens);
    } catch (LoginException e) {
      logger.error("", e);
      return badRequest("bad");
    }
  }

  @Authentication(userRole = UserRole.ALL)
  public Result logout(Http.Request request) {
    logger.debug("{}", request);

    try {
      loginService.logout(request.attrs().get(GeneralAttributes.USER_ID));
      return ok("logged out");
    } catch (ApplicationException e) {
      return requestErrorService.handleGenericErrors(getLoginPage(), request);
    }
  }

  /**
   * Activate account page.
   *
   * @param request implicit request, must contain body with username, password and repeat password
   * @return accept invitation page
   */
  public Result registerByInvitePage(Http.Request request, String resetPasswordUuid) {
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(AcceptInviteRequest.class);
    final var user = userService.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    logger.debug("{}", user);

    if (user == null) {
      return requestErrorService.handleGenericErrors(getLoginPage(), request);
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
      final var tokens = loginService.login(user.getUserName(), acceptInviteRequest.getPassword());
      return getOkWithCookies(tokens);
    } catch (Exception e) {
      logger.error("", e);
      return requestErrorService.handleGenericErrors(getLoginPage(), request);
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
      requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    try {
      final String resetPasswordUuid = userService.generateResetPasswordUuid(form.get().getEmail());
      emailService.sendResetPasswordEmail(form.get().getEmail(), resetPasswordUuid);
    } catch (UserException e) {
      logger.error("", e);
      return requestErrorService.handleGenericErrors(getLoginPage(), request);
    }

    return ok("password reset");
  }

  public Result resetPasswordPage(Http.Request request, String resetPasswordUuid) {
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(ResetPasswordRequest.class);
    final var user = userService.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    logger.debug("{}", user);

    if (user == null) {
      return requestErrorService.handleGenericErrors(getLoginPage(), request);
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
      final var tokens = loginService.login(user.getUserName(), resetPasswordRequest.getPassword());
      return getOkWithCookies(tokens);
    } catch (Exception e) {
      logger.error("", e);
      return requestErrorService.handleGenericErrors(getLoginPage(), request);
    }
  }

  private Call getLoginPage() {
    return routes.LoginController.login();
  }

  private Result getOkWithCookies(JsonObject tokens) {
    return ok().withCookies(
            getCookie(Constants.ACCESS_TOKEN, tokens.get(Constants.ACCESS_TOKEN).getAsString()),
            getCookie(Constants.REFRESH_TOKEN, tokens.get(Constants.REFRESH_TOKEN).getAsString()));
  }
}
