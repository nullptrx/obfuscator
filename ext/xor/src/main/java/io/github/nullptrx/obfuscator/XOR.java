package io.github.nullptrx.obfuscator;

public class XOR {

  public static byte[] e(byte[] bytes) {
    //根据默认编码获取字节数组

    int len = bytes.length;
    for (int i = 0; i < len; i++) {
      //对每个字节进行异或
      bytes[i] = (byte) (bytes[i] ^ 1);
    }

    return bytes;
  }

  public static String d(byte[] bytes) {
    int len = bytes.length;
    for (int i = 0; i < len; i++) {
      //对每个字节进行异或
      bytes[i] = (byte) (bytes[i] ^ 1);
    }

    return new String(bytes);
  }

  /**
   * 将字符串编码成16进制数字,适用于所有字符（包括中文）
   */
  public static byte[] e(byte[] bytes, String key) {
    //根据默认编码获取字节数组

    int len = bytes.length;
    int keyLen = key.length();
    for (int i = 0; i < len; i++) {
      //对每个字节进行异或
      bytes[i] = (byte) (bytes[i] ^ key.charAt(i % keyLen));
    }

    return bytes;
  }

  /**
   * 将16进制数字解码成字符串,适用于所有字符（包括中文）
   */
  public static String d(byte[] bytes, String key) {
    int len = bytes.length;
    int keyLen = key.length();
    for (int i = 0; i < len; i++) {
      //对每个字节进行异或
      bytes[i] = (byte) (bytes[i] ^ key.charAt(i % keyLen));
    }

    return new String(bytes);
  }
}