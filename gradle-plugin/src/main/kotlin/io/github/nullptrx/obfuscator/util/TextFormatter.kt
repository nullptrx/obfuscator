package io.github.nullptrx.obfuscator.util

import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

class TextFormatter : SimpleFormatter() {
  @Synchronized
  override fun format(record: LogRecord): String {
    val result = StringBuffer()
    result.append(record.message).append("\n")
    return result.toString()
  }
}