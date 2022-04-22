package ro.iss.yolacare.component.models.userProfile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import ro.iss.coreComponent.classes.AuditableBaseEntity;
import ro.iss.coreComponent.classes.JsonToStringBuilder;
import ro.iss.yolacare.component.classes.Constants;
import ro.iss.yolacare.component.classes.Patterns;
import ro.iss.yolacare.component.models.ForKidsEntity;
import ro.iss.yolacare.component.models.userProfile.enums.UserGroupPermissions;
import ro.iss.yolacare.component.models.userProfile.enums.UserProfileRole;
import ro.iss.yolacare.component.models.userProfile.enums.UserProfileStatus;

public abstract class UserProfile extends AuditableBaseEntity implements ForKidsEntity {

  @NotNull private String ownerId;

  @NotNull private String userName;

  @NotNull private UserProfileRole userProfileRole;

  @NotNull private UserGroupPermissions userGroupPermissions;

  private String userGroupId;

  private String lastUsedIp;

  private Long lastLogin;

  private UserProfileStatus userProfileStatus;

  @Size(min = 1, max = Constants.EMAIL_FIELD_LENGTH, message = "constraints.field.invalid")
  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  @Pattern(regexp = Patterns.UUID, message = "constraints.field.invalid")
  private String resetPasswordUuid;

  private Long resetPasswordUuidTimestamp;

  @Size(min = 1, max = Constants.COMMON_FIELD_LENGTH, message = "constraints.field.invalid")
  @Pattern(regexp = Patterns.NAME, message = "constraints.field.invalid")
  private String firstName;

  @Size(min = 1, max = Constants.COMMON_FIELD_LENGTH, message = "constraints.field.invalid")
  @Pattern(regexp = Patterns.NAME, message = "constraints.field.invalid")
  private String lastName;

  @NotBlank(message = "constraints.field.mandatory")
  private String dateOfBirth;

  /**
   * Returns the full name.
   *
   * @return the full name
   */
  @JsonIgnore
  @BsonIgnore
  public String getFullName() {
    return Stream.of(getFirstName(), StringUtils.defaultString(getLastName()))
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(" "));
  }

  /**
   * Returns the full name with last name to upper case.
   *
   * @return the full name with last name to upper case
   */
  @JsonIgnore
  @BsonIgnore
  public String getFullNameWithLastNameToUpper() {
    return Stream.of(getFirstName(), StringUtils.defaultString(getLastName()).toUpperCase())
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(" "));
  }

  /**
   * Checks if this userProfile is inactive.
   *
   * @return true if user is inactive, false otherwise
   */
  @JsonIgnore
  @BsonIgnore
  public boolean isInactive() {
    return UserProfileStatus.INACTIVE == userProfileStatus;
  }

  /**
   * Checks if this userProfile is active.
   *
   * @return true if user is active, false otherwise
   */
  @JsonIgnore
  @BsonIgnore
  public boolean isActiv() {
    return UserProfileStatus.ACTIVE == userProfileStatus;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    // make sure that email is saved lower case, to ensure duplicates are found
    this.email = StringUtils.trimToEmpty(StringUtils.lowerCase(email));
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public void setwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getLastUsedIp() {
    return lastUsedIp;
  }

  public void setLastUsedIp(String lastUsedIp) {
    this.lastUsedIp = lastUsedIp;
  }

  public Long getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Long lastLogin) {
    this.lastLogin = lastLogin;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = StringUtils.capitalize(firstName);
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = StringUtils.capitalize(lastName);
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public UserProfileStatus getUserProfileStatus() {
    return userProfileStatus;
  }

  public void setUserProfileStatus(UserProfileStatus userProfileStatus) {
    this.userProfileStatus = userProfileStatus;
  }

  public String getResetPasswordUuid() {
    return resetPasswordUuid;
  }

  public void setResetPasswordUuid(String resetPasswordUuid) {
    this.resetPasswordUuid = resetPasswordUuid;
  }

  public Long getResetPasswordUuidTimestamp() {
    return resetPasswordUuidTimestamp;
  }

  public void setResetPasswordUuidTimestamp(Long resetPasswordUuidTimestamp) {
    this.resetPasswordUuidTimestamp = resetPasswordUuidTimestamp;
  }

  public UserProfileRole getUserProfileRole() {
    return userProfileRole;
  }

  public void setUserProfileRole(UserProfileRole userProfileRole) {
    this.userProfileRole = userProfileRole;
  }

  public UserGroupPermissions getUserGroupPermissions() {
    return userGroupPermissions;
  }

  public void setUserGroupPermissions(UserGroupPermissions userGroupPermissions) {
    this.userGroupPermissions = userGroupPermissions;
  }

  public String getUserGroupId() {
    return userGroupId;
  }

  public void setUserGroupId(String userGroupId) {
    this.userGroupId = userGroupId;
  }

  @Override
  public String toString() {
    JsonToStringBuilder builder = new JsonToStringBuilder(this);
    addFieldsToBuilder(builder);
    return builder.toString();
  }

  /** Append the fields of UserProfile and AuditableBaseEntity to the json builder. */
  public void addFieldsToBuilder(JsonToStringBuilder builder) {
    builder.append("ownerId", ownerId);
    builder.append("userName", userName);
    builder.append("userProfileRole", userProfileRole);
    builder.append("userProfileStatus", userProfileStatus);
    builder.append("userPermissionsForAccess", userGroupPermissions);
    builder.append("lastUsedIp", lastUsedIp);
    builder.append("lastLogin", lastLogin);
    builder.append("email", email);
    builder.append("resetPasswordUuid", resetPasswordUuid);
    builder.append("resetPasswordUuidTimestamp", resetPasswordUuidTimestamp);
    builder.append("firstName", firstName);
    builder.append("lastName", lastName);
    builder.append("dateOfBirth", dateOfBirth);
    builder.append("createdById", createdById);
    builder.append("createdByApp", createdByApp);
    builder.append("modifiedById", modifiedById);
    builder.append("modifiedByApp", modifiedByApp);
    builder.append("id", id);
    builder.append("createdAt", createdAt);
    builder.append("createdBy", createdBy);
    builder.append("modifiedAt", modifiedAt);
    builder.append("modifiedBy", modifiedBy);
  }
}
