package net.brentwalther.javacash.util;

public class StringUtil {
  /** Returns string {@code s} repeated {@code count} times or empty string if count is 0. */
  public static String repeatString(String s, int count) {
    if (count == 0) {
      return "";
    } else if (count == 1) {
      return s;
    }
    StringBuilder builder = new StringBuilder();
    while (count-- > 0) {
      builder.append(s);
    }
    return builder.toString();
  }
}
