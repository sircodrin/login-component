package dot.cpp.login.enums;

public enum UserRole {
  USER("User"),
  ADMIN("Admin"),
  ALL("All");

  private final String value;

  UserRole(final String value) {

    this.value = value;
  }

  public String getValue() {

    return this.value;
  }
}
