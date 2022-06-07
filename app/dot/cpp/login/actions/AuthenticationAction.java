package dot.cpp.login.actions;

import dot.cpp.login.annotations.Authentication;
import dot.cpp.login.attributes.GeneralAttributes;
import dot.cpp.login.constants.Constants;
import dot.cpp.login.constants.Patterns;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.exceptions.UserException;
import dot.cpp.login.helpers.CookieHelper;
import dot.cpp.login.models.session.entity.Session;
import dot.cpp.login.service.LoginService;
import io.jsonwebtoken.JwtException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;

public class AuthenticationAction extends Action<Authentication> {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationAction.class);
  private static final String defaultRedirectUrl = "/login";

  @Inject private MessagesApi languageService;
  @Inject private LoginService loginService;

  @Override
  public CompletionStage<Result> call(Request request) {

    final var messages = languageService.preferred(request);
    // final var refreshToken = request.getCookie(Constants.REFRESH_TOKEN); // separate method
    final var authCookie = CookieHelper.getCookieString(request, Constants.ACCESS_TOKEN);
    final var authHeader = request.header(Http.HeaderNames.AUTHORIZATION).orElse("");

    logger.debug("Authentication");
    logger.debug("request: {}", request);
    logger.debug("authHeader: {}", authHeader);
    logger.debug("authCookie: {}", authCookie);

    String accessToken;

    if (authHeader.equals("")) {
      if (authCookie != null && authCookie.equals("")) {
        logger.error("Missing authorization token");
        return getResultInvalidToken(messages);
      } else {
        accessToken = authCookie;
      }
    } else {
      accessToken = authHeader;
    }

    logger.debug("{}", accessToken);
    if (accessToken != null && !accessToken.matches(Patterns.JWT_TOKEN)) {
      logger.error("Invalid authorization token");
      return getResultInvalidToken(messages);
    }

    try {
      final String userId = loginService.authorizeRequest(accessToken, configuration.userRole());
      return delegate.call(request.addAttr(GeneralAttributes.USER_ID, userId));
    } catch (JwtException | LoginException | UserException e) {
      logger.debug("{}", e.getMessage());
      return CompletableFuture.completedFuture(
          redirectWithError(messages, "general.session.expired"));
    }
  }

  private CompletableFuture<Result> getResultInvalidToken(Messages messages) {
    if (configuration.redirectUrl().isEmpty()) {
      return getResultIfRedirectUrlMissing(messages);
    } else {
      return CompletableFuture.completedFuture(redirect(configuration.redirectUrl()));
    }
  }

  private CompletableFuture<Result> getResultIfRedirectUrlMissing(Messages messages) {
    logger.warn("Missing redirect url so using default {}", defaultRedirectUrl);
    return CompletableFuture.completedFuture(
        redirectWithNoRedirectUrl(messages, defaultRedirectUrl));
  }

  private CompletionStage<Result> getSuccessfulResult(Request request, Session session) {
    logger.debug("session: {}", session.getSessionId());
    logger.debug("session.userId: {}", session.getUserId());

    request = request.addAttr(Constants.SESSION, session); // is this required?
    return delegate
        .call(request)
        .thenApply(
            result ->
                result.withCookies(
                    CookieHelper.getAccessCookie(session), CookieHelper.getRefreshCookie(session)));
  }

  private Result redirectWithError(Messages messages, String key) {
    return redirect(configuration.redirectUrl()).flashing("alert-danger", messages.apply(key));
  }

  private Result redirectWithNoRedirectUrl(Messages messages, String defaultRedirectUrl) {
    return redirect(defaultRedirectUrl)
        .flashing("alert-danger", messages.apply("general.application.error"));
  }
}
