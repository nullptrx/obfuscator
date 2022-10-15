package io.github.nullptrx.obfuscator.kg

import java.security.SecureRandom

open class RandomKeyGenerator
@JvmOverloads constructor(private val mLength: Int = DEFAULT_LENGTH) : IKeyGenerator() {

    companion object {
        private const val DEFAULT_LENGTH = 2
    }

    private val mSecureRandom: SecureRandom = SecureRandom()
    override fun get(): String {
        if (mLength < 0) {
            return ""
        }
        val key = ByteArray(mLength)
        mSecureRandom.nextBytes(key)
        return String(key)
    }


}