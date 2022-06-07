package dot.cpp.login.constants;

public enum UserStatus {
  INACTIVE("Inactive"),
  ACTIVE("Active"),
  SUSPENDED("Suspended");

  final String message;

  UserStatus(String message) {
    this.message = message;
  }
}
