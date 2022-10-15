package io.github.nullptrx.obfuscator.bytecode

import io.github.nullptrx.obfuscator.extension.StrExtension
import org.objectweb.asm.*

class StringFieldClassVisitor(cw: ClassVisitor, val extension: StrExtension) :
  ClassVisitor(Opcodes.ASM5, cw) {

  private var isClInitExists: Boolean = false

  private val staticFinalFields: MutableList<ClassStringField> = mutableListOf()
  private val staticFields: MutableList<ClassStringField> = mutableListOf()
  private val finalFields: MutableList<ClassStringField> = mutableListOf()
  private val fields: MutableList<ClassStringField> = mutableListOf()

  private var className: String? = null
  private var fieldName: String? = null


  fun encode(mv: MethodVisitor, str: String) {
    val key = extension.getPassword()
    val method = extension.getImplMethod()
    val length = method.parameterCount
    val enc: ByteArray
    if (length == 1) {
      enc = method.invoke(null, str.toByteArray() as Any) as ByteArray
    } else {
      enc = method.invoke(null, str.toByteArray() as Any, key as Any) as ByteArray
    }
    val len = enc.size
    mv.visitIntInsn(Opcodes.SIPUSH, len)
    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE)
    for (i in 0 until len) {
      mv.visitInsn(Opcodes.DUP)
      mv.visitIntInsn(Opcodes.SIPUSH, i)
      mv.visitIntInsn(Opcodes.BIPUSH, enc[i].toInt())
      mv.visitInsn(Opcodes.BASTORE)
    }
    if (length == 1) {
      mv.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        extension.getImplClass(),
        "d",
        "([B)Ljava/lang/String;",
        false
      )
    } else {
      mv.visitLdcInsn(key)
      mv.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        extension.getImplClass(),
        "d",
        "([BLjava/lang/String;)Ljava/lang/String;",
        false
      )
    }
  }

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    this.className = name
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
    return super.visitAnnotation(descriptor, visible)
  }

  override fun visitField(
    access: Int,
    name: String?,
    desc: String?,
    signature: String?,
    value: Any?
  ): FieldVisitor {
    this.fieldName = name
    var newValue: Any? = value
    if (ClassStringField.STRING_DESC.equals(desc) && name != null) {
      // static final, in this condition, the value is null or not null.
      if ((access and Opcodes.ACC_STATIC) != 0 && (access and Opcodes.ACC_FINAL) != 0) {
        staticFinalFields.add(ClassStringField(name, value as? String))
        newValue = null
      }
      // static, in this condition, the value is null.
      if ((access and Opcodes.ACC_STATIC) != 0 && (access and Opcodes.ACC_FINAL) == 0) {
        staticFields.add(ClassStringField(name, value as? String))
        newValue = null
      }

      // final, in this condition, the value is null or not null.
      if ((access and Opcodes.ACC_STATIC) == 0 && (access and Opcodes.ACC_FINAL) != 0) {
        finalFields.add(ClassStringField(name, value as? String))
        newValue = null
      }

      // normal, in this condition, the value is null.
      if ((access and Opcodes.ACC_STATIC) != 0 && (access and Opcodes.ACC_FINAL) != 0) {
        fields.add(ClassStringField(name, value as? String))
        newValue = null
      }
    }
    return super.visitField(access, name, desc, signature, newValue)
  }

  override fun visitMethod(
    access: Int,
    name: String?,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor {
    var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
    if (mv != null) {
      if ("<clinit>" == name) { // 处理静态成员变量
        isClInitExists = true
        // If clinit exists meaning the static fields (not final) would have be inited here.
        mv = StaticVariableVisitor(mv, this, className, staticFinalFields, staticFields)
      } else if ("<init>" == name) { // 处理成员变量
        // Here init final(not static) and normal fields
        mv = MemberVariableVisitor(mv, this)
      } else { // 处理局部变量
        mv = LocalVariableVisitor(mv, this, className, staticFinalFields, finalFields)
      }

    }

    return mv

  }


  override fun visitEnd() {
    if (!isClInitExists && staticFinalFields.isNotEmpty()) {
      val mv = super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
      mv.visitCode()
      // Here init static final fields.
      for (field in staticFinalFields) {
        val value = field.value ?: continue // It could not be happened
        encode(mv, value)
        mv.visitFieldInsn(Opcodes.PUTSTATIC, className, field.name, ClassStringField.STRING_DESC)
      }
      mv.visitInsn(Opcodes.RETURN)
      mv.visitMaxs(1, 0)
      mv.visitEnd()
    }

    super.visitEnd()
  }

}