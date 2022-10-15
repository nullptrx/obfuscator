package io.github.nullptrx.obfuscator.bytecode

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes;

class MemberVariableVisitor(
  mv: MethodVisitor,
  private val visitor: StringFieldClassVisitor,
) : MethodVisitor(Opcodes.ASM5, mv) {

  override fun visitLdcInsn(cst: Any?) {
    // We don't care about whether the field is final or normal
    if (cst != null && cst is String && !TextUtils.isEmptyAfterTrim(cst)) {
      visitor.encode(super.mv, cst)
    } else {
      super.visitLdcInsn(cst)
    }
  }
}
