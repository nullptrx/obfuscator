package io.github.nullptrx.obfuscator.kg

open class HardCodeKeyGenerator
@JvmOverloads
constructor(private val mKey: String? = null) : IKeyGenerator() {
    override fun get(): String {
        if (mKey == null) {
            return ""
        }
        return mKey
    }
}