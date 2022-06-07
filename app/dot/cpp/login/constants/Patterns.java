package dot.cpp.login.constants;

public class Patterns {
  public static final String NAME = "^([ \\u00c0-\\u01ffa-zA-Z'\\-])+$";

  public static final String PHONE_NUMBER =
      "(?=^.{10,60}$)^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$";

  public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,10}$";

  public static final String NAME_OR_EMAIL =
      "^([ \\u00c0-\\u01ffa-zA-Z'\\-])+$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,10}$";

  public static final String JWT_TOKEN = "(^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$)";

  public static final String GPS_COORDINATES =
      "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?"
          + "(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$";

  public static final String UUID =
      "^$|[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
}
