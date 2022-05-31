package dot.cpp.login.service;

import dot.cpp.login.constants.Error;
import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.LoginException;
import dot.cpp.login.models.session.entity.Session;
import dot.cpp.login.models.session.repository.SessionRepository;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoginService {

  private final SecretKey key;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;

  @Inject
  public LoginService(UserRepository userRepository, SessionRepository sessionRepository) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;

    key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  }

  public Session login(String userName, String password, String clientIp) throws LoginException {
    var user = userRepository.findByField("userName", userName, User.class);

    if (user == null) {
      throw new LoginException(Error.NOT_FOUND);
    } else {
      if (!user.getPassword().equals(password)) {
        throw new LoginException(Error.INCORRECT_PASSWORD);
      }

      Date expirationDateAccess = new Date();
      Date expirationDateRefresh = new Date();
      expirationDateAccess.setTime(expirationDateAccess.getTime() + 1200000L); // 20 minutes
      expirationDateRefresh.setTime(expirationDateRefresh.getTime() + 86400000L); // one day

      logger.debug("{}", user.getId());

      String jws =
          Jwts.builder()
              .setSubject(user.getId().toString())
              .setExpiration(expirationDateAccess)
              .signWith(key)
              .compact();

      logger.debug("{}", jws);

      var session = new Session();
      var refreshToken = UUID.randomUUID().toString();
      session.setAccessToken(jws);
      session.setRefreshToken(refreshToken);
      session.setRefreshExpiryDate(expirationDateRefresh.getTime());
      session.setCreateTime(Instant.now().toEpochMilli());
      session.setUserId(user.getId());
      if (clientIp != null) {
        session.setSessionIp(clientIp);
      }
      sessionRepository.save(session);

      logger.debug("{}", session);
      return session;
    }
  }

  public boolean logout() {
    return false;
  }

  public void checkJwtAndUserRole(String jwtToken, UserRole userRole) throws LoginException {
    logger.debug("{}", jwtToken);

    final var jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
    final var expirationDate = jws.getBody().getExpiration();

    if (expirationDate.before(new Date()))
      throw new LoginException(Error.EXPIRED_ACCESS);

    final var user = userRepository.findById(jws.getBody().getSubject(), User.class);

    if (user == null) {
      throw new LoginException(Error.NOT_FOUND);
    } else {
      logger.debug("{}", user);
      if (user.getRole() != userRole) {
       throw new LoginException(Error.ROLE_NOT_ALLOWED);
      }
    }
  }
}
