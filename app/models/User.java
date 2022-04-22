package models;

import constants.Constants;
import constants.Patterns;
import enums.UserRole;
import java.util.List;
import javax.validation.constraints.Pattern;
import play.data.validation.Constraints;

public class User extends BaseEntity {

  private String userName;

  private String password;

  private UserRole role;

  /** Array of groups the user belongs to. */
  private List<String> groups;

  @Constraints.Required
  @Constraints.MaxLength(
      value = Constants.EMAIL_FIELD_LENGTH,
      message = "constraints.field.invalid")
  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  @Constraints.Pattern(value = Patterns.UUID, message = "constraints.field.invalid")
  private String resetPasswordUuid;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getResetPasswordUuid() {
    return resetPasswordUuid;
  }

  public void setResetPasswordUuid(String resetPasswordUuid) {
    this.resetPasswordUuid = resetPasswordUuid;
  }
}
