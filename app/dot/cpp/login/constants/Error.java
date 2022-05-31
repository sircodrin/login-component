package dot.cpp.login.constants;

public enum Error {
  NOT_FOUND("Not found"),
  INCORRECT_PASSWORD("Incorrect password"),
  EXPIRED_ACCESS("Access token expired"),
  ROLE_NOT_ALLOWED("Role not allowed");
  final String message;

  Error(String message) {
    this.message = message;
  }
}
