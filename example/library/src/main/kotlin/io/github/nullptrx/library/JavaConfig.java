package io.github.nullptrx.library;

public interface JavaConfig {
  String username = "example";
  String email = "example@github.com";
  String tel = "+10000000000";

  default void hello() {
    String desc = "This is a test.";
  }
}
