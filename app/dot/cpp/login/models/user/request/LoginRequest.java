package dot.cpp.login.models.user.request;

import com.google.gson.Gson;
import dot.cpp.login.constants.Patterns;
import javax.validation.constraints.NotBlank;
import play.data.validation.Constraints.Pattern;

public class LoginRequest {

  @NotBlank(message = "constraints.field.not.empty")
  private String password;

  @NotBlank(message = "constraints.field.invalid")
  @Pattern(value = Patterns.NAME_OR_EMAIL, message = "constraints.field.invalid")
  private String username;

  @Override
  public String toString() {
    return new Gson().toJson(this); // todo check this
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username; // todo trimming
  }
}
