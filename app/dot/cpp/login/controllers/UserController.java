package dot.cpp.login.controllers;

import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.helpers.CookieHelper;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import dot.cpp.login.models.user.request.SetPasswordRequest;
import dot.cpp.login.service.LoginService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private final LoginService loginService;

  private final MailerClient mailerClient;

  @Inject
  public UserController(UserRepository user, LoginService loginService,
                        FormFactory formFactory, MessagesApi messagesApi,
                        MailerClient mailerClient) {
    this.userRepository = user;
    this.loginService = loginService;
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
    this.mailerClient = mailerClient;
  }

  public Result send() {
    logger.error("hi man");
    Email email =
        new Email()
            .setSubject("WaW")
            .setFrom("Mister Waw <alshopcontact@gmail.com>")
            .addTo("Drew <andrewtookay@gmail.com>")
            .setBodyText("A WaW text message");
    //    mailerClient.send(email);

    return ok("Sent");
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

  /**
   * Activate account page for admin.
   *
   * @param request implicit request, must contain body with password and repeat password
   * @return accept invitation page
   */
  public Result acceptInvitationPage(Http.Request request, String resetPasswordUuid) {

    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(SetPasswordRequest.class);
    final var repoCtx = request.attrs().get(ForKidsAttributes.REPO_CONTEXT);

    if (!userProfileManager.resetPasswordUuidIsValid(
            repoCtx, resetPasswordUuid, ForKidsEntityType.ADMIN)) {
      return redirect(getLoginPage())
              .flashing("alert-danger", messages.apply("userProfile.passwordUuid.alreadyUsed"));
    }

    return ok(
            dot.cpp.login.views.html.setPassword.render(
                    form, resetPasswordUuid, request, messages));
  }

  /**
   * Activates admin account.
   *
   * @param request Request
   * @return accept invitation
   */
  public Result acceptInvitation(Http.Request request, String resetPasswordUuid) {
    logger.debug("START");
    final var messages = messagesApi.preferred(request);
    final var form = formFactory.form(SetPasswordRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorHandlerService.handleFormErrorWithRefresh(request, form);
    }

    var setPasswordRequest = form.get();
    logger.debug("setPasswordRequest: {}", setPasswordRequest);

    try {
      final var dbAdminProfile =
              userProfileManager.acceptInvitation(
                      setPasswordRequest, resetPasswordUuid, ForKidsEntityType.ADMIN);

      final var session =
              userProfileManager.login(
                      request,
                      dbAdminProfile.getEmail(),
                      setPasswordRequest.getPassword(),
                      UserProfileRole.ADMIN,
                      ForKidsEntityType.ADMIN,
                      ForKidsApplication.ADMIN);

      return getSuccessfulResult(
              routes.AwpDashboardController.dashboard(),
              messages.apply("userProfile.newPasswordSuccess"))
              .withCookies(
                      CookieHelper.getAccessCookie(session, ForKidsApplication.ADMIN),
                      CookieHelper.getRefreshCookie(session, ForKidsApplication.ADMIN));
    } catch (ForKidsComponentException | AuthenticationException e) {
      return requestErrorHandlerService.handleXaftException(getLoginPage(), request, e);
    }
  }
}
