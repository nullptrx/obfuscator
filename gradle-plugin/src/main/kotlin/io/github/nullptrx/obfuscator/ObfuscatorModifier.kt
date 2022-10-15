package io.github.nullptrx.obfuscator

import io.github.nullptrx.obfuscator.bytecode.StringFieldClassVisitor
import io.github.nullptrx.obfuscator.extension.StrExtension
import io.github.nullptrx.obfuscator.transform.asm.AbsModifier
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.util.logging.Logger

class ObfuscatorModifier(private val extension: StrExtension) : AbsModifier() {

  override fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
    return StringFieldClassVisitor(classWriter, extension)
  }

  /**
   * @param filePath fullQualifiedClassName
   */
  override fun isModifiableClass(filePath: String): Boolean {
    if (!extension.enabled) return false
    val isModifiable = super.isModifiableClass(filePath)
    if (!isModifiable) return false
    for (mark in extension.packages) {
      if (filePath.startsWith(mark)) {
        return true
      }
    }
    return false
  }
}