package io.github.nullptrx.obfuscator.extension

import io.github.nullptrx.obfuscator.kg.HardCodeKeyGenerator
import io.github.nullptrx.obfuscator.kg.IKeyGenerator
import io.github.nullptrx.obfuscator.kg.RandomKeyGenerator
import java.lang.reflect.Method


open class StrExtension {
    open var enabled: Boolean = true
    open var packages: Array<String> = arrayOf()
    open var implementation: String = ""

    open var password: IKeyGenerator = RandomKeyGenerator()

    fun getImplClass(): String {
        try {
            return implementation.replace(".", "/")
        } catch (e: Exception) {
            throw  RuntimeException(e)
        }
    }

    fun getImplMethod(): Method {
        try {
            val clazz = Class.forName(implementation)
            if (password.get().isEmpty()) {
                try {
                    return clazz.getMethod("e", ByteArray::class.java)
                } catch (_: Exception) {
                    return clazz.getMethod("e", ByteArray::class.java, String::class.java)
                }
            } else {
                return clazz.getMethod("e", ByteArray::class.java, String::class.java)
            }
        } catch (e: Exception) {
            if (e is ClassNotFoundException) {
                throw ClassNotFoundException(e.localizedMessage)
            } else if (e is NoSuchMethodException) {
                throw NoSuchMethodException(e.localizedMessage)
            } else {
                throw e
            }
        }
    }

    fun getPassword(): String {
        return password.get()
    }

    fun random(length: Int = 0): Any {
        return RandomKeyGenerator(length)
    }

    fun hardcode(key: String? = null): Any {
        return HardCodeKeyGenerator(key)
    }
}