package dot.cpp.login.controllers;

import dot.cpp.login.annotations.Authentication;
import dot.cpp.login.enums.UserRole;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.request.InviteUserRequest;
import dot.cpp.login.service.EmailService;
import dot.cpp.login.service.RequestErrorService;
import dot.cpp.login.service.UserService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class UserController extends Controller {

  private static final Class<User> clazz = User.class;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final FormFactory formFactory;
  private final MessagesApi messagesApi;
  private final UserService userService;
  private final EmailService emailService;
  private final RequestErrorService requestErrorService;

  @Inject
  public UserController(
      UserService userService,
      RequestErrorService requestErrorService,
      FormFactory formFactory,
      MessagesApi messagesApi,
      EmailService emailService) {
    this.userService = userService;
    this.emailService = emailService;
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
    this.requestErrorService = requestErrorService;
  }

  public Result modifyUser(String id) {
    User user = userService.findById(id, clazz);
    return ok(user.toString());
  }

  public Result generateAdmin(String email) {
    return ok(userService.generateUserWithInvitation(email, UserRole.ADMIN));
  }

  public Result findByValue(String field, String value) {
    return ok(userService.findByField(field, value, clazz).toString());
  }

  public Result get(String id) {
    return ok(userService.findById(id, clazz).toString());
  }

  @Authentication(userRole = UserRole.ADMIN)
  public Result inviteUserPage(Http.Request request) {
    final var messages = messagesApi.preferred(request);
    var form = formFactory.form(InviteUserRequest.class);

    return ok(dot.cpp.login.views.html.inviteUser.render(form, request, messages));
  }

  @Authentication(userRole = UserRole.ADMIN)
  public Result inviteUser(Http.Request request) {
    final var form = formFactory.form(InviteUserRequest.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return requestErrorService.handleFormErrorWithRefresh(request, form);
    }

    var email = form.get().getEmail();
    final String resetPasswordUuid =
        userService.generateUserWithInvitation(email, form.get().getUserRole());
    emailService.sendInviteEmail(email, resetPasswordUuid);

    return ok("invited");
  }
}
