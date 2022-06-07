package dot.cpp.login.attributes;

import play.libs.typedmap.TypedKey;

/** Attribute keys to store into request.attrs() map */
public final class GeneralAttributes {

  public static final TypedKey<String> USER_ID = TypedKey.create("USER_ID");
}
