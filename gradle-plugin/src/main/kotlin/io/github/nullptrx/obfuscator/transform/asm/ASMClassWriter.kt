package io.github.nullptrx.obfuscator.transform.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.IOException
import java.io.InputStream

class ASMClassWriter(
  flags: Int,
  private val urlClassLoader: ClassLoader?
) : ClassWriter(flags) {

  companion object {
    private const val OBJECT = "java/lang/Object"
  }


  /**
   * https://github.com/Moniter123/pinpoint/blob/40106ffe6cc4d6aea9d59b4fb7324bcc009483ee/profiler/src/main/java/com/navercorp/pinpoint/profiler/instrument/ASMClassWriter.java
   */
  override fun getCommonSuperClass(type1: String?, type2: String?): String {
    if (type1 == null || type1.equals(OBJECT) || type2 == null || type2.equals(OBJECT)) {
      return OBJECT
    }

    if (type1.equals(type2)) {
      return type1
    }

    val type1ClassReader = getClassReader(type1)
    val type2ClassReader = getClassReader(type2)
    if (type1ClassReader == null || type2ClassReader == null) {
      return OBJECT
    }

    if (isInterface(type1ClassReader)) {
      var interfaceName = type1
      if (isImplements(interfaceName, type2ClassReader)) {
        return interfaceName
      }
      if (isInterface(type2ClassReader)) {
        interfaceName = type2
        if (isImplements(interfaceName, type1ClassReader)) {
          return interfaceName
        }
      }
      return OBJECT
    }

    if (isInterface(type2ClassReader)) {
      var interfaceName = type2
      if (isImplements(interfaceName, type1ClassReader)) {
        return interfaceName
      }
      return OBJECT
    }

    val superClassNames = mutableSetOf<String>()
    superClassNames.add(type1)
    superClassNames.add(type2)

    var type1SuperClassName = type1ClassReader.getSuperName()
    if (!superClassNames.add(type1SuperClassName)) {
      return type1SuperClassName
    }

    var type2SuperClassName = type2ClassReader.getSuperName()
    if (!superClassNames.add(type2SuperClassName)) {
      return type2SuperClassName
    }

    while (type1SuperClassName != null || type2SuperClassName != null) {
      if (type1SuperClassName != null) {
        type1SuperClassName = getSuperClassName(type1SuperClassName)
        if (type1SuperClassName != null) {
          if (!superClassNames.add(type1SuperClassName)) {
            return type1SuperClassName
          }
        }
      }

      if (type2SuperClassName != null) {
        type2SuperClassName = getSuperClassName(type2SuperClassName)
        if (type2SuperClassName != null) {
          if (!superClassNames.add(type2SuperClassName)) {
            return type2SuperClassName
          }
        }
      }
    }

    return OBJECT
  }


  private fun isImplements(interfaceName: String, classReader: ClassReader): Boolean {
    var classInfo: ClassReader? = classReader

    while (classInfo != null) {
      val interfaceNames = classInfo.getInterfaces()
      for (name in interfaceNames) {
        if (name != null && name.equals(interfaceName)) {
          return true
        }
      }

      for (name in interfaceNames) {
        if (name != null) {
          val interfaceInfo = getClassReader(name)
          if (interfaceInfo != null) {
            if (isImplements(interfaceName, interfaceInfo)) {
              return true
            }
          }
        }
      }

      val superClassName = classInfo.getSuperName()
      if (superClassName == null || superClassName.equals(OBJECT)) {
        break
      }
      classInfo = getClassReader(superClassName)
    }

    return false
  }

  private fun isInterface(classReader: ClassReader): Boolean {
    return (classReader.access and Opcodes.ACC_INTERFACE) != 0
  }


  private fun getSuperClassName(className: String): String? {
    val classReader = getClassReader(className) ?: return null
    return classReader.superName
  }


  private fun getClassReader(className: String): ClassReader? {
    val inputStream = urlClassLoader?.getResourceAsStream("$className.class")
    try {
      if (inputStream != null) {
        return ClassReader(inputStream)
      }
    } catch (_: IOException) {
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close()
        } catch (_: IOException) {
        }
      }
    }
    return null
  }
}