package io.github.nullptrx.obfuscator.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import io.github.nullptrx.obfuscator.ObfuscatorModifier
import io.github.nullptrx.obfuscator.transform.asm.ASMHelper
import io.github.nullptrx.obfuscator.transform.concurrent.Schedulers
import io.github.nullptrx.obfuscator.transform.concurrent.Worker
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URLClassLoader

abstract class ByteTransform(
  protected val project: Project,
  isApp: Boolean = true
) : Transform() {

  private val logger: Logger

  private val SCOPES = mutableSetOf<QualifiedContent.Scope>()

  private val worker: Worker
  protected var emptyRun: Boolean = false
  protected lateinit var bytecodeModifier: ObfuscatorModifier
  // protected lateinit var redundantModifier:RedundantModifier

  init {
    if (isApp) {
      SCOPES.add(QualifiedContent.Scope.PROJECT)
      SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS)
      SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    } else {
      SCOPES.add(QualifiedContent.Scope.PROJECT)
    }
    this.logger = project.logger
    this.worker = Schedulers.IO
  }

  override fun getName(): String {
    return javaClass.simpleName
  }

  override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
    return TransformManager.CONTENT_CLASS
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return SCOPES
  }

  override fun isIncremental(): Boolean {
    return true
  }

  override fun isCacheable(): Boolean {
    return true
  }


  protected fun getTaskVariant(): TaskVariant {
    return TaskVariant.ALWAYS
  }

  protected fun inDuplicatedClassSafeMode(): Boolean {
    return false
  }

  @Deprecated("Deprecated in Java")
  override fun transform(
    context: Context,
    inputs: MutableCollection<TransformInput>,
    referencedInputs: MutableCollection<TransformInput>,
    outputProvider: TransformOutputProvider,
    isIncremental: Boolean
  ) {
    val taskVariant = getTaskVariant()
    if ("debug".equals(context.variantName)) {
      emptyRun = taskVariant == TaskVariant.RELEASE || taskVariant == TaskVariant.NEVER
    } else if ("release".equals(context.variantName)) {
      emptyRun = taskVariant == TaskVariant.DEBUG || taskVariant == TaskVariant.NEVER
    }
    // logger.warn(
    //   name + " isIncremental = " + isIncremental + ", taskVariant = "
    //       + taskVariant + ", emptyRun = " + emptyRun + ", inDuplicatedClassSafeMode = " + inDuplicatedClassSafeMode()
    // )
    // val startTime = System.currentTimeMillis()
    if (!isIncremental) {
      outputProvider.deleteAll()
    }
    val urlClassLoader = ASMHelper.getClassLoader(inputs, referencedInputs, project)
    this.bytecodeModifier.setClassLoader(urlClassLoader)
    var flagForCleanDexBuilderFolder = false
    for (input in inputs) {
      for (jarInput in input.jarInputs) {
        val status = jarInput.status
        status ?: continue
        val dest = outputProvider.getContentLocation(
          jarInput.file.absolutePath,
          jarInput.contentTypes,
          jarInput.scopes,
          Format.JAR
        )
        if (isIncremental && !emptyRun) {
          when (status) {
            Status.NOTCHANGED -> {}
            Status.ADDED, Status.CHANGED -> {
              transformJar(jarInput.file, dest, status)
            }
            Status.REMOVED -> {
              if (dest.exists()) {
                FileUtils.forceDelete(dest)
              }
            }
          }
        } else {
          //Forgive me!, Some project will store 3rd-party aar for several copies in dexbuilder folder,unknown issue.
          if (inDuplicatedClassSafeMode() && !isIncremental && !flagForCleanDexBuilderFolder) {
            cleanDexBuilderFolder(dest)
            flagForCleanDexBuilderFolder = true
          }
          transformJar(jarInput.file, dest, status)
        }
      }

      for (directoryInput in input.directoryInputs) {
        val dest = outputProvider.getContentLocation(
          directoryInput.name,
          directoryInput.contentTypes, directoryInput.scopes,
          Format.DIRECTORY
        )

        FileUtils.forceMkdir(dest)
        if (isIncremental && !emptyRun) {
          val srcDirPath = directoryInput.file.absolutePath
          val destDirPath = dest.absolutePath
          val fileStatusMap = directoryInput.changedFiles
          for (changedFile in fileStatusMap.entries) {
            val status = changedFile.value
            status ?: continue
            val inputFile = changedFile.key
            val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            when (status) {
              Status.NOTCHANGED -> {}
              Status.REMOVED -> {
                if (destFile.exists()) {
                  //noinspection ResultOfMethodCallIgnored
                  destFile.delete()
                }
              }
              Status.ADDED, Status.CHANGED -> {
                try {
                  FileUtils.touch(destFile)
                } catch (_: IOException) {
                  //maybe mkdirs fail for some strange reason, try again.
                  FileUtils.forceMkdir(destFile)
                }
                transformSingleFile(inputFile, destFile, srcDirPath)
              }
            }
          }
        } else {
          transformDir(directoryInput.file, dest)
        }

      }
    }

    worker.await()
    // val costTime = System.currentTimeMillis() - startTime
    // logger.warn((name + " costed " + costTime + "ms"))
  }


  private fun transformSingleFile(inputFile: File, outputFile: File, srcBaseDir: String) {
    worker.submit {
      bytecodeModifier.modifySingleClassToFile(inputFile, outputFile, srcBaseDir)
    }
  }

  private fun transformDir(inputDir: File, outputDir: File) {
    if (emptyRun) {
      FileUtils.copyDirectory(inputDir, outputDir)
      return
    }
    val inputDirPath = inputDir.absolutePath
    val outputDirPath = outputDir.absolutePath

    if (inputDir.isDirectory) {
      worker.submit {
        for (file in com.android.utils.FileUtils.getAllFiles(inputDir)) {
          val filePath = file.absolutePath
          val outputFile = File(filePath.replace(inputDirPath, outputDirPath))
          bytecodeModifier.modifySingleClassToFile(file, outputFile, inputDirPath)
        }
      }
    }
  }

  private fun transformJar(srcJar: File, destJar: File, status: Status) {
    worker.submit {
      if (emptyRun) {
        FileUtils.copyFile(srcJar, destJar)
      } else {
        bytecodeModifier.modifyJar(srcJar, destJar)
      }
    }
  }

  private fun cleanDexBuilderFolder(dest: File) {
    worker.submit {
      try {
        val dexBuilderDir = replaceLastPart(dest.absolutePath, name, "dexBuilder")
        //intermediates/transforms/dexBuilder/debug
        val file = File(dexBuilderDir).parentFile
        project.logger.warn("clean dexBuilder folder = " + file.absolutePath)
        if (file.exists() && file.isDirectory) {
          FileUtils.deleteDirectory(file)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun replaceLastPart(
    originString: String,
    replacement: String,
    toreplace: String
  ): String {
    val start = originString.lastIndexOf(replacement)
    val builder = StringBuilder()
    builder.append(originString, 0, start)
    builder.append(toreplace)
    builder.append(originString.substring(start + replacement.length))
    return builder.toString()
  }


}