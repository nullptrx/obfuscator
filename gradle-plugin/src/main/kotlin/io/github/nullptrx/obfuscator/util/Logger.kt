package io.github.nullptrx.obfuscator.util

import java.util.logging.Logger

object Logger {
  private const val LOGGER_NAME = "obfuscator"

  @JvmStatic
  private val logger = Logger.getLogger(LOGGER_NAME)

  fun info(msg: String) {
    try {
      logger.severe(msg)
    } catch (_: Exception) {
    }
  }

}