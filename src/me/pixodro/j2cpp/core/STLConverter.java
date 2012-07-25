package me.pixodro.j2cpp.core;

/**
 * Created with IntelliJ IDEA.
 * User: bquenin
 * Date: 7/11/12
 * Time: 8:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class STLConverter {
  static boolean isSTLType(final String typeName) {
    if ("set".equalsIgnoreCase(typeName)) {
      return true;
    } else if ("list".equalsIgnoreCase(typeName)) {
      return true;
    } else if ("map".equalsIgnoreCase(typeName)) {
      return true;
    }
    return false;
  }
}
