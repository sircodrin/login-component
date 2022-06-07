package dot.cpp.login.models.user.entity;

import dev.morphia.annotations.Entity;
import dot.cpp.login.constants.UserStatus;
import dot.cpp.login.enums.UserRole;
import dot.cpp.repository.constants.Patterns;
import dot.cpp.repository.models.BaseEntity;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
public class User extends BaseEntity {

  @NotBlank private String userName;

  @NotNull
  @Pattern(regexp = Patterns.ALPHA_PASS_MIN8, message = "constraints.field.invalid")
  private String password;

  @NotNull private UserRole role;

  private List<String> groups;

  private UserStatus status;

  @NotNull
  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  @Pattern(regexp = Patterns.UUID, message = "constraints.field.invalid")
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

  public UserStatus getStatus() {
    return status;
  }

  public boolean isActive() {
    return getStatus() == UserStatus.ACTIVE;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }
}
