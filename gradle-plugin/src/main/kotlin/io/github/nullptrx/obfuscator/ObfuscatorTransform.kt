package io.github.nullptrx.obfuscator

import io.github.nullptrx.obfuscator.extension.StrExtension
import io.github.nullptrx.obfuscator.transform.ByteTransform
import org.gradle.api.Project

class ObfuscatorTransform(project: Project, isApp: Boolean = true) : ByteTransform(project, isApp) {

  init {
    val obfuscator = project.extensions.create("obfuscator", ObfuscatorExtension::class.java)
    val str = obfuscator.extensions.create("str", StrExtension::class.java)
    // obfuscator.extensions.create("black", BlackExtension::class.java)
    bytecodeModifier = ObfuscatorModifier(str)
  }


}