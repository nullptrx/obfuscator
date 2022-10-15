package io.github.nullptrx.obfuscator.transform.asm

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.gradle.TestedExtension
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.net.URLClassLoader

object ASMHelper {
  fun getClassLoader(
    inputs: Collection<TransformInput>,
    referencedInputs: Collection<TransformInput>,
    project: Project
  ): URLClassLoader {
    val urls = ImmutableList.Builder<URL>()
    val androidJarPath = getAndroidJarPath(project)
    val file = File(androidJarPath)
    val androidJarURL = file.toURI().toURL()
    urls.add(androidJarURL)
    for (totalInputs in Iterables.concat(inputs, referencedInputs)) {
      for (directoryInput in totalInputs.directoryInputs) {
        if (directoryInput.file.isDirectory) {
          urls.add(directoryInput.file.toURI().toURL())
        }
      }
      for (jarInput in totalInputs.jarInputs) {
        if (jarInput.file.isFile) {
          urls.add(jarInput.file.toURI().toURL())
        }
      }
    }
    val allUrls = urls.build()
    val classLoaderUrls = allUrls.toArray(arrayOf<URL>())
    return URLClassLoader(classLoaderUrls)
  }

  /**
   * Users/su/Library/Android/SDK/platforms/android-28/android.jar
   */
  private fun getAndroidJarPath(project: Project): String {
    val appExtension = project.properties.get("android") as TestedExtension
    var sdkDirectory = appExtension.sdkDirectory.absolutePath
    val compileSdkVersion = appExtension.compileSdkVersion
    sdkDirectory = sdkDirectory + File.separator + "platforms" + File.separator
    return sdkDirectory + compileSdkVersion + File.separator + "android.jar"
  }

}