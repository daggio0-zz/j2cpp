package me.pixodro.j2cpp.core;

import java.util.ArrayList;
import java.util.List;

public class JavaToCppConverter {
  public static List<String> excludedJavaMethods = new ArrayList<String>();

  static {
    excludedJavaMethods.add("equals");
    excludedJavaMethods.add("hashCode");
  }
}
