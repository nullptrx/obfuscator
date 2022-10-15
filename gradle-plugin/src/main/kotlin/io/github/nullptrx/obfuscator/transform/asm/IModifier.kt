package io.github.nullptrx.obfuscator.transform.asm

import java.io.InputStream

interface IModifier {
  /**
   * Check a certain file is modifiable
   * @param filePath class路径
   * @return 是否需要修改字节码
   */
  fun isModifiableClass(filePath: String): Boolean

  /**
   * Modify single class to byte array
   * @param inputStream
   */
  fun modifySingleClassToByteArray(inputStream: InputStream): ByteArray
}