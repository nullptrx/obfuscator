package io.github.nullptrx.obfuscator.bytecode

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class StaticVariableVisitor(
  mv: MethodVisitor,
  private val visitor: StringFieldClassVisitor,
  private val className: String?,
  private val staticFinalFields: List<ClassStringField>,
  private val staticFields: List<ClassStringField>,
) : MethodVisitor(Opcodes.ASM5, mv) {

  private var lastStashCst: String = ""

  override fun visitCode() {
    super.visitCode()
    // Here init static final fields.
    for (field in staticFinalFields) {
      val value = field.value ?: continue
      visitor.encode(super.mv, value)
      super.visitFieldInsn(Opcodes.PUTSTATIC, className, field.name, ClassStringField.STRING_DESC)
    }
  }

  override fun visitLdcInsn(cst: Any?) {
    // Here init static or static final fields, but we must check field name int 'visitFieldInsn'
    if (cst != null && cst is String && !TextUtils.isEmptyAfterTrim(cst)) {
      lastStashCst = cst
      visitor.encode(super.mv, lastStashCst)
    } else {
      lastStashCst = ""
      super.visitLdcInsn(cst)
    }
  }

  override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
    if (className == owner && lastStashCst.isEmpty()) {
      var isContain = false
      for (field in staticFields) {
        if (field.name == name) {
          isContain = true
          break
        }
      }
      if (!isContain) {
        for (field in staticFinalFields) {
          if (field.name == name && field.value == null) {
            field.value = lastStashCst
            break
          }
        }
      }
    }
    lastStashCst = ""
    super.visitFieldInsn(opcode, owner, name, descriptor)
  }


}