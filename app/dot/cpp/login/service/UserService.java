package dot.cpp.login.service;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import com.typesafe.config.Config;
import dot.cpp.login.constants.Error;
import dot.cpp.login.constants.UserStatus;
import dot.cpp.login.enums.UserRole;
import dot.cpp.login.exceptions.UserException;
import dot.cpp.login.models.user.entity.User;
import dot.cpp.login.models.user.repository.UserRepository;
import dot.cpp.login.models.user.request.AcceptInviteRequest;
import dot.cpp.login.models.user.request.ResetPasswordRequest;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService extends EntityService<User> {
  private final Config config;
  private final String passwordPepper;
  private final Argon2Function argon2 = Argon2Function.getInstance(1000, 4, 2, 32, Argon2.ID, 19);

  @Inject
  public UserService(UserRepository repository, Config config) {
    super(repository);
    this.config = config;
    this.passwordPepper = config.getString("password.pepper");
  }

  public String generateUserWithResetPassword(String email, UserRole userRole) {
    User user = new User();
    final String resetPasswordUuid = UUID.randomUUID().toString();

    user.setEmail(email);
    user.setRole(userRole);
    user.setUserName("temporary");
    user.setPassword("temporary");
    user.setStatus(UserStatus.INACTIVE);
    user.setResetPasswordUuid(resetPasswordUuid);

    repository.save(user);

    return resetPasswordUuid;
  }

  public String generateResetPasswordUuid(String email) throws UserException {
    final User user = repository.findByField("email", email, User.class);

    if (user == null) {
      throw new UserException(Error.USER_EMAIL_NOT_FOUND);
    }

    if (!user.isActive()) {
      throw new UserException(Error.ACCOUNT_INACTIVE);
    }

    final String resetPasswordUuid = UUID.randomUUID().toString();
    user.setResetPasswordUuid(resetPasswordUuid);
    repository.save(user);

    logger.debug("{}", user);
    return resetPasswordUuid;
  }

  public User resetPassword(ResetPasswordRequest resetPasswordRequest, String resetPasswordUuid)
      throws UserException {
    logger.debug("{}", resetPasswordRequest);
    logger.debug("{}", resetPasswordUuid);

    final var user = repository.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    if (user == null) {
      throw new UserException(Error.NOT_FOUND);
    }

    final Hash hashedPassword =
        Password.hash(resetPasswordRequest.getPassword())
            .addRandomSalt(32)
            .addPepper(passwordPepper)
            .with(argon2);

    logger.debug("{}", hashedPassword);
    user.setPassword(hashedPassword.getResult());
    user.setResetPasswordUuid("");

    logger.debug("{}", user);
    repository.save(user);
    return user;
  }

  public boolean checkPassword(String hashedPassword, String password) {
    final String pepper = config.getString("password.pepper");

    boolean verified = Password.check(password, hashedPassword).addPepper(pepper).with(argon2);

    logger.debug("verified {}", verified);
    return verified;
  }

  public void userIsActiveAndHasRole(String userId, UserRole userRole) throws UserException {
    final var user = repository.findById(userId, User.class);

    if (user == null) {
      throw new UserException(Error.NOT_FOUND);
    } else {
      logger.debug("{}", user);

      if (!user.isActive()) {
        throw new UserException(Error.ACCOUNT_INACTIVE);
      }
      if (user.getRole() != userRole && userRole != UserRole.ALL) {
        throw new UserException(Error.USER_ROLE_MISMATCH);
      }
    }
  }

  public User acceptInvitation(AcceptInviteRequest acceptInviteRequest, String resetPasswordUuid) {
    logger.debug("{}", acceptInviteRequest);
    logger.debug("{}", resetPasswordUuid);

    final var user = repository.findByField("resetPasswordUuid", resetPasswordUuid, User.class);

    final Hash hashedPassword =
        Password.hash(acceptInviteRequest.getPassword())
            .addRandomSalt(32)
            .addPepper(passwordPepper)
            .with(argon2);

    user.setPassword(hashedPassword.getResult());
    user.setUserName(acceptInviteRequest.getUsername());
    user.setResetPasswordUuid("");
    user.setStatus(UserStatus.ACTIVE);

    logger.debug("{}", user);
    repository.save(user);
    return user;
  }
}
