package dot.cpp.login.constants;

import dot.cpp.login.models.session.entity.Session;
import play.libs.typedmap.TypedKey;

public class Constants {
  public static final int EMAIL_FIELD_LENGTH = 140;
  public static final int COMMON_FIELD_LENGTH = 70;
  public static final TypedKey<Session> SESSION = TypedKey.create("session");
  public static final String ACCESS_TOKEN = "access_token";
  public static final String REFRESH_TOKEN = "refresh_token";
}
