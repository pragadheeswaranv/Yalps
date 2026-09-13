package com.example.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Standard RFC 6238 TOTP (Time-based One-Time Password) Authentication Engine.
 * Compatible with Google Authenticator (Google OTP), Microsoft Authenticator,
 * Android Phone OTP System, and any RFC 6238 compliant authenticator.
 */
class TotpAuthService private constructor() {

  companion object {
    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val TIME_STEP_SECONDS = 30L

    @Volatile
    private var instance: TotpAuthService? = null

    fun getInstance(): TotpAuthService {
      return instance ?: synchronized(this) {
        instance ?: TotpAuthService().also { instance = it }
      }
    }
  }

  /**
   * Encodes raw byte array into standard Base32 string (RFC 4648).
   */
  fun encodeBase32(data: ByteArray): String {
    val out = StringBuilder()
    var buffer = 0
    var bitsLeft = 0
    for (b in data) {
      buffer = (buffer shl 8) or (b.toInt() and 0xFF)
      bitsLeft += 8
      while (bitsLeft >= 5) {
        bitsLeft -= 5
        out.append(BASE32_ALPHABET[(buffer shr bitsLeft) and 0x1F])
      }
    }
    if (bitsLeft > 0) {
      buffer = buffer shl (5 - bitsLeft)
      out.append(BASE32_ALPHABET[buffer and 0x1F])
    }
    return out.toString()
  }

  /**
   * Decodes a Base32 string into raw bytes.
   */
  fun decodeBase32(base32: String): ByteArray {
    val clean = base32.uppercase(Locale.ROOT).replace(" ", "").replace("-", "").replace("=", "")
    val out = ArrayList<Byte>()
    var buffer = 0
    var bitsLeft = 0
    for (c in clean) {
      val valIndex = BASE32_ALPHABET.indexOf(c)
      if (valIndex < 0) continue
      buffer = (buffer shl 5) or valIndex
      bitsLeft += 5
      if (bitsLeft >= 8) {
        bitsLeft -= 8
        out.add(((buffer shr bitsLeft) and 0xFF).toByte())
      }
    }
    val result = ByteArray(out.size)
    for (i in out.indices) {
      result[i] = out[i]
    }
    return result
  }

  /**
   * Derives a deterministic, persistent 16-character Base32 TOTP secret
   * for a user phone number or Google account.
   */
  fun getSecretForIdentifier(identifier: String): String {
    val clean = identifier.trim().lowercase(Locale.ROOT)
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(("YALPS_TOTP_KEY_2026_" + clean).toByteArray())
    val keyBytes = hash.copyOf(10) // 10 bytes = 16 Base32 characters
    return encodeBase32(keyBytes)
  }

  /**
   * Generates standard RFC 6238 6-digit TOTP code for the given secret key.
   */
  fun generateTotp(secret: String, timeSeconds: Long = System.currentTimeMillis() / 1000): String {
    return try {
      val keyBytes = decodeBase32(secret)
      if (keyBytes.isEmpty()) return "000000"

      val timeStep = timeSeconds / TIME_STEP_SECONDS
      val timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array()

      val mac = Mac.getInstance("HmacSHA1")
      mac.init(SecretKeySpec(keyBytes, "HmacSHA1"))
      val hash = mac.doFinal(timeBytes)

      val offset = (hash[hash.size - 1].toInt() and 0x0F)
      val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
        ((hash[offset + 1].toInt() and 0xFF) shl 16) or
        ((hash[offset + 2].toInt() and 0xFF) shl 8) or
        (hash[offset + 3].toInt() and 0xFF)

      val otp = binary % 1000000
      String.format(Locale.US, "%06d", otp)
    } catch (e: Exception) {
      "000000"
    }
  }

  /**
   * Calculates seconds remaining in the current 30-second TOTP window.
   */
  fun getSecondsRemainingInWindow(): Int {
    val currentSeconds = (System.currentTimeMillis() / 1000) % TIME_STEP_SECONDS
    return (TIME_STEP_SECONDS - currentSeconds).toInt()
  }

  /**
   * Validates entered 6-digit OTP code against the RFC 6238 algorithm
   * allowing a +/- 1 step (30 seconds) window for time drift tolerance.
   */
  fun verifyTotp(
    secret: String,
    enteredCode: String,
    windowSteps: Int = 1
  ): Boolean {
    val cleanCode = enteredCode.trim().replace(" ", "").replace("-", "")
    if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
      return false
    }

    val currentSeconds = System.currentTimeMillis() / 1000
    for (i in -windowSteps..windowSteps) {
      val checkTime = currentSeconds + (i * TIME_STEP_SECONDS)
      val expectedCode = generateTotp(secret, checkTime)
      if (expectedCode == cleanCode) {
        return true
      }
    }
    return false
  }

  /**
   * Returns the standard otpauth:// URI for Google Authenticator / Phone OTP.
   */
  fun getTotpUri(identifier: String, secret: String): String {
    val cleanId = Uri.encode(identifier.trim())
    return "otpauth://totp/YALPS:$cleanId?secret=$secret&issuer=YALPS&algorithm=SHA1&digits=6&period=30"
  }

  /**
   * Launches Google Authenticator or default Phone OTP app with the otpauth:// URI.
   */
  fun launchAuthenticatorApp(context: Context, identifier: String, secret: String): Boolean {
    val uriString = getTotpUri(identifier, secret)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      try {
        val chooser = Intent.createChooser(intent, "Open in Google Authenticator or Phone OTP")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
        true
      } catch (e2: Exception) {
        false
      }
    }
  }

  /**
   * Copies the TOTP Base32 secret key to clipboard.
   */
  fun copySecretToClipboard(context: Context, secret: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("YALPS TOTP Secret", secret)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "TOTP Secret Key copied to clipboard! Paste into Google Authenticator or Phone OTP app.", Toast.LENGTH_SHORT).show()
  }
}
