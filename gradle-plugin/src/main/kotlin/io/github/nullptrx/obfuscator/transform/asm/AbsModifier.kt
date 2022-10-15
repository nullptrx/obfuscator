package io.github.nullptrx.obfuscator.transform.asm

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.*

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.*
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

abstract class AbsModifier : IModifier {
  companion object {
    private val ZERO = FileTime.fromMillis(0)
    private val FILE_SEP = File.separator
  }

  protected var urlClassLoader: ClassLoader? = null

  fun modifyJar(inputJar: File, outputJar: File) {
    val inputZip = ZipFile(inputJar)
    val outputZip = ZipOutputStream(
      BufferedOutputStream(
        Files.newOutputStream(outputJar.toPath())
      )
    )
    val inEntries = inputZip.entries()
    while (inEntries.hasMoreElements()) {
      val entry = inEntries.nextElement()
      val originalFile =
        BufferedInputStream(inputZip.getInputStream(entry))
      val outEntry = ZipEntry(entry.getName())
      val newEntryContent: ByteArray
      // separator of entry name is always '/', even in windows
      if (!isModifiableClass(outEntry.getName().replace("/", "."))) {
        newEntryContent = IOUtils.toByteArray(originalFile)
      } else {
        newEntryContent = modifySingleClassToByteArray(originalFile)
      }
      val crc32 = CRC32()
      crc32.update(newEntryContent)
      outEntry.setCrc(crc32.getValue())
      outEntry.setMethod(ZipEntry.STORED)
      outEntry.setSize(newEntryContent.size.toLong())
      outEntry.setCompressedSize(newEntryContent.size.toLong())
      outEntry.setLastAccessTime(ZERO)
      outEntry.setLastModifiedTime(ZERO)
      outEntry.setCreationTime(ZERO)
      outputZip.putNextEntry(outEntry)
      outputZip.write(newEntryContent)
      outputZip.closeEntry()
    }
    outputZip.flush()
    outputZip.close()
  }

  fun modifySingleClassToFile(inputFile: File, outputFile: File, inputDir: String) {
    var inputBaseDir = inputDir
    if (!inputBaseDir.endsWith(FILE_SEP)) inputBaseDir = inputBaseDir + FILE_SEP
    if (isModifiableClass(
        inputFile.getAbsolutePath().replace(inputBaseDir, "").replace(FILE_SEP, ".")
      )
    ) {
      FileUtils.touch(outputFile)
      val inputStream = FileInputStream(inputFile)
      val bytes = modifySingleClassToByteArray(inputStream)
      val fos = FileOutputStream(outputFile)
      fos.write(bytes)
      fos.close()
      inputStream.close()
    } else {
      if (inputFile.isFile()) {
        FileUtils.touch(outputFile)
        FileUtils.copyFile(inputFile, outputFile)
      }
    }
  }


  override fun modifySingleClassToByteArray(inputStream: InputStream): ByteArray {
    val classReader = ClassReader(inputStream)
    val classWriter = ASMClassWriter(ClassWriter.COMPUTE_MAXS, urlClassLoader)
    val classWriterWrapper = wrapClassWriter(classWriter)
    classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES)
    return classWriter.toByteArray()
  }


  fun setClassLoader(classloader: ClassLoader) {
    this.urlClassLoader = classloader
  }

  protected open fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
    return classWriter
  }

  /**
   * @param filePath fullQualifiedClassName
   */
  override fun isModifiableClass(filePath: String): Boolean {
    return filePath.endsWith(".class")
        && !filePath.contains("R\$")
        && !filePath.contains("R.class")
        && !filePath.contains("BuildConfig.class")
  }
}