package com.example.data

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val ENCRYPTION_KEY = "OpenClawSecureEncryptionKeySecret_3081"

    private fun getSecretKeySpec(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(ENCRYPTION_KEY.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun getIv(): IvParameterSpec {
        val ivBytes = ByteArray(16)
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(ENCRYPTION_KEY.toByteArray(Charsets.UTF_8))
        System.arraycopy(bytes, 0, ivBytes, 0, 16)
        return IvParameterSpec(ivBytes)
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(), getIv())
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(), getIv())
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText
        }
    }
}
