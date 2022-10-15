package io.github.nullptrx.obfuscator.bytecode

object TextUtils {
  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  fun isEmpty(str: String?): Boolean {
    return str == null || str.isEmpty()
  }

  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  fun isEmptyAfterTrim(str: String?): Boolean {
    return str == null || str.isEmpty() || str.trim().isEmpty()
  }
}