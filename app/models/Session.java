package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import play.mvc.Http;
import play.mvc.Http.Cookie;

public class Session extends BaseEntity {

  private String sessionId;
  private String userId;
  private String sessionIp;

  private Long expiryDate;
  private String userAgent;
  private String tokenType;
  private String accessToken;
  private String refreshToken;

  private Long expiresIn;
  private Long createTime;

  @JsonIgnore private Cookie cookie;

  /**
   * PerformedLogout.
   *
   * @return boolean
   */
  public boolean performedLogout() {
    return expiryDate == null || expiryDate == 0;
  }

  /**
   * Returns "Authorisation" cookie specific to this Session.
   *
   * @return Cookie
   */
  @JsonIgnore
  public Cookie getAuthorisationCookie() {
    if (cookie == null) {
      cookie =
          Cookie.builder(Http.HeaderNames.AUTHORIZATION, accessToken)
              .withSecure(true)
              .withHttpOnly(true)
              .build();
    }
    return cookie;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getSessionIp() {
    return sessionIp;
  }

  public void setSessionIp(String sessionIp) {
    this.sessionIp = sessionIp;
  }

  public Long getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Long expiryDate) {
    this.expiryDate = expiryDate;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public Long getExpires() {
    return createTime + expiresIn;
  }

  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }
}
