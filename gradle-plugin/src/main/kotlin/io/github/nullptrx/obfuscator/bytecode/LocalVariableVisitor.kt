package io.github.nullptrx.obfuscator.bytecode

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes;

class LocalVariableVisitor(
  methodVisitor: MethodVisitor,
  private val visitor: StringFieldClassVisitor,
  private val className: String?,
  private val staticFinalFields: List<ClassStringField>,
  private val finalFields: List<ClassStringField>,
) :
  MethodVisitor(Opcodes.ASM5, methodVisitor) {


  override fun visitLdcInsn(cst: Any?) {
    if (cst != null && cst is String && !TextUtils.isEmptyAfterTrim(cst)) {
      // If the value is a static final field
      for (field in staticFinalFields) {
        if (cst == field.value) {
          super.visitFieldInsn(
            Opcodes.GETSTATIC,
            className,
            field.name,
            ClassStringField.STRING_DESC
          )
          return
        }
      }
      // If the value is a final field (not static)
      for (field in finalFields) {
        // if the value of a final field is null, we ignore it
        if (cst == field.value) {
          super.visitVarInsn(Opcodes.ALOAD, 0)
          super.visitFieldInsn(
            Opcodes.GETFIELD,
            className,
            field.name,
            ClassStringField.STRING_DESC
          )
          return
        }
      }
      // local variables
      visitor.encode(super.mv, cst)
      return
    }
    super.visitLdcInsn(cst)
  }
}
