package com.tosak.authos.common.utils

import org.springframework.stereotype.Component
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Component
class AESUtil(
    private val key: SecretKey
) {
    private val ENCRYPTION_ALGO: String = "AES/GCM/NoPadding"
    private val TAG_LENGTH_BIT: Int = 128;
    private val IV_LENGTH_BYTE: Int = 12;


    fun encrypt(plainText: String): ByteArray {
        val iv = generateIV()
        val cipher = Cipher.getInstance(ENCRYPTION_ALGO)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray())

        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        return combined
    }


    fun decrypt(cipherMessage: ByteArray): String {
        val iv = cipherMessage.copyOfRange(0, IV_LENGTH_BYTE)
        val cipherText = cipherMessage.copyOfRange(IV_LENGTH_BYTE, cipherMessage.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val plainText = cipher.doFinal(cipherText)
        return String(plainText)
    }


    private fun generateIV(): ByteArray {
        val srand = SecureRandom()
        val iv = ByteArray(IV_LENGTH_BYTE)
        srand.nextBytes(iv)
        return iv;
    }

}