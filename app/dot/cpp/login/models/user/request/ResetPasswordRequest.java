package dot.cpp.login.models.user.request;

import com.google.gson.Gson;
import dot.cpp.core.constants.Patterns;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;

@Validate
public class ResetPasswordRequest implements Validatable<String> {

  @Pattern(value = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Constraints.MinLength(value = 1, message = "constraints.field.invalid")
  private String password;

  @Pattern(value = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Constraints.MinLength(value = 1, message = "constraints.field.invalid")
  private String confirmPassword;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  @Override
  public String validate() {
    if (!password.equals(confirmPassword)) {
      return "general.passwords.not.match";
    }
    return null;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
