package dot.cpp.login.helpers;

import dot.cpp.login.constants.Constants;
import dot.cpp.login.models.session.entity.Session;
import play.mvc.Http;
import play.mvc.Result;

public class CookieHelper {

  /** Get access cookie for an application. */
  public static Http.Cookie getAccessCookie(Session session) {
    return getCookie(Constants.ACCESS_TOKEN, session.getAccessToken());
  }

  /** Get refresh cookie for an application. */
  public static Http.Cookie getRefreshCookie(Session session) {
    return getCookie(Constants.REFRESH_TOKEN, session.getRefreshToken());
  }

  /**
   * Get cookie for an application.
   *
   * @param session {@link Session}
   * @return {@link Http.Cookie} the cookie containing authorization
   */
  private static Http.Cookie getCookie(String cookieName, String session) {
    return Http.Cookie.builder(cookieName, session).withHttpOnly(true).build();
  }

  /**
   * Discard authorization cookie for an application.
   *
   * @param result {@link Result}
   * @return {@link Http.Cookie} the result without the authorization cookie
   */
  public static Result discardAuthorizationCookies(Result result) {
    return result
        .discardingCookie(Constants.ACCESS_TOKEN)
        .discardingCookie(Constants.REFRESH_TOKEN);
  }
}
