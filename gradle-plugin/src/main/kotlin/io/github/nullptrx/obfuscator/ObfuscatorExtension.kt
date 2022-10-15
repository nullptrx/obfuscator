package io.github.nullptrx.obfuscator

import io.github.nullptrx.obfuscator.extension.BlackExtension
import io.github.nullptrx.obfuscator.extension.StrExtension
import org.gradle.api.plugins.ExtensionAware

abstract class ObfuscatorExtension : ExtensionAware {

  lateinit var str: StrExtension

  lateinit var black: BlackExtension

}