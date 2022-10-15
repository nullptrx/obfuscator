package io.github.nullptrx.obfuscator

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension


class ObfuscatorPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.getByType(TestedExtension::class.java)
    if (extension is AppExtension) {
      extension.registerTransform(ObfuscatorTransform(project, true))
    } else if (extension is LibraryExtension) {
      extension.registerTransform(ObfuscatorTransform(project, false))
    }
  }
}