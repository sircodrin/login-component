package dot.cpp.login.constants;

public enum Error {
  NOT_FOUND("Not found"),
  INCORRECT_PASSWORD("Incorrect password");

  final String message;

  Error(String message) {
    this.message = message;
  }
}
