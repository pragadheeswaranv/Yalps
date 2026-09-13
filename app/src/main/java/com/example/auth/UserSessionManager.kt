package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuthMethod {
  PHONE_OTP,
  GMAIL_PASSWORD,
  GUEST
}

data class UserProfile(
  val identifier: String, // Phone number or Gmail
  val displayName: String,
  val authMethod: AuthMethod,
  val isProMember: Boolean = false,
  val isAdminBackend: Boolean = false,
  val memberSince: String = "2026"
) {
  val isMasterBackendAdmin: Boolean
    get() = isAdminBackend || identifier.equals(UserSessionManager.ADMIN_BACKEND_EMAIL, ignoreCase = true)
}

data class AuthResult(
  val success: Boolean,
  val isBackendAdmin: Boolean,
  val message: String
)

class UserSessionManager private constructor() {

  companion object {
    const val ADMIN_BACKEND_EMAIL = "shwxrxn95@gmail.com"
    const val ADMIN_BACKEND_PASSWORD = "pragaveera"

    @Volatile
    private var instance: UserSessionManager? = null

    fun getInstance(): UserSessionManager {
      return instance ?: synchronized(this) {
        instance ?: UserSessionManager().also { instance = it }
      }
    }
  }

  private var sharedPrefs: SharedPreferences? = null

  private val _isLoggedIn = MutableStateFlow(true)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _currentUser = MutableStateFlow<UserProfile?>(
    UserProfile(
      identifier = "pragadheeeesh95@gmail.com",
      displayName = "Pragadheesh",
      authMethod = AuthMethod.GMAIL_PASSWORD,
      isProMember = false,
      isAdminBackend = false
    )
  )
  val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  fun initialize(context: Context) {
    if (sharedPrefs == null) {
      sharedPrefs = context.applicationContext.getSharedPreferences("yalps_user_session", Context.MODE_PRIVATE)
      loadSession()
    }
  }

  private fun loadSession() {
    val prefs = sharedPrefs ?: return
    val loggedIn = prefs.getBoolean("is_logged_in", true)
    _isLoggedIn.value = loggedIn
    val identifier = prefs.getString("user_identifier", "pragadheeeesh95@gmail.com") ?: "pragadheeeesh95@gmail.com"
    val displayName = prefs.getString("user_display_name", "Pragadheesh") ?: "Pragadheesh"
    val authMethodStr = prefs.getString("auth_method", AuthMethod.GMAIL_PASSWORD.name) ?: AuthMethod.GMAIL_PASSWORD.name
    val isPro = prefs.getBoolean("is_pro_member", false)
    val isAdmin = prefs.getBoolean("is_admin_backend", identifier.equals(ADMIN_BACKEND_EMAIL, ignoreCase = true))
    val memberSince = prefs.getString("member_since", "2026") ?: "2026"

    val method = try {
      AuthMethod.valueOf(authMethodStr)
    } catch (e: Exception) {
      AuthMethod.GMAIL_PASSWORD
    }

    _currentUser.value = UserProfile(
      identifier = identifier,
      displayName = displayName,
      authMethod = method,
      isProMember = isPro || isAdmin,
      isAdminBackend = isAdmin,
      memberSince = memberSince
    )
  }

  private fun saveSession() {
    val prefs = sharedPrefs ?: return
    val user = _currentUser.value
    prefs.edit()
      .putBoolean("is_logged_in", _isLoggedIn.value)
      .putString("user_identifier", user?.identifier ?: "")
      .putString("user_display_name", user?.displayName ?: "")
      .putString("auth_method", user?.authMethod?.name ?: AuthMethod.GUEST.name)
      .putBoolean("is_pro_member", user?.isProMember ?: false)
      .putBoolean("is_admin_backend", user?.isAdminBackend ?: false)
      .putString("member_since", user?.memberSince ?: "2026")
      .apply()
  }

  fun loginWithPhone(phoneNumber: String, name: String = "Yalps Listener") {
    _currentUser.value = UserProfile(
      identifier = phoneNumber,
      displayName = name.ifBlank { "User ${phoneNumber.takeLast(4)}" },
      authMethod = AuthMethod.PHONE_OTP,
      isProMember = false,
      isAdminBackend = false
    )
    _isLoggedIn.value = true
    saveSession()
  }

  fun loginWithPassword(email: String, password: String): AuthResult {
    val trimmedEmail = email.trim()
    val isBackendMaster = trimmedEmail.equals(ADMIN_BACKEND_EMAIL, ignoreCase = true) && password == ADMIN_BACKEND_PASSWORD

    if (trimmedEmail.equals(ADMIN_BACKEND_EMAIL, ignoreCase = true)) {
      if (password == ADMIN_BACKEND_PASSWORD) {
        _currentUser.value = UserProfile(
          identifier = ADMIN_BACKEND_EMAIL,
          displayName = "Backend Master Admin",
          authMethod = AuthMethod.GMAIL_PASSWORD,
          isProMember = true,
          isAdminBackend = true,
          memberSince = "Master Host"
        )
        _isLoggedIn.value = true
        saveSession()
        return AuthResult(
          success = true,
          isBackendAdmin = true,
          message = "Welcome Backend Administrator! Master backend controls unlocked."
        )
      } else {
        return AuthResult(
          success = false,
          isBackendAdmin = false,
          message = "Invalid password for Backend Admin ($ADMIN_BACKEND_EMAIL)."
        )
      }
    }

    // Standard user login
    val fallbackName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
    _currentUser.value = UserProfile(
      identifier = trimmedEmail,
      displayName = fallbackName,
      authMethod = AuthMethod.GMAIL_PASSWORD,
      isProMember = false,
      isAdminBackend = false
    )
    _isLoggedIn.value = true
    saveSession()
    return AuthResult(
      success = true,
      isBackendAdmin = false,
      message = "Signed in as $trimmedEmail"
    )
  }

  fun loginWithGmail(email: String, name: String = "") {
    val trimmedEmail = email.trim()
    val isAdmin = trimmedEmail.equals(ADMIN_BACKEND_EMAIL, ignoreCase = true)
    val fallbackName = if (isAdmin) "Backend Master Admin" else trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
    _currentUser.value = UserProfile(
      identifier = trimmedEmail,
      displayName = name.ifBlank { fallbackName },
      authMethod = AuthMethod.GMAIL_PASSWORD,
      isProMember = isAdmin,
      isAdminBackend = isAdmin
    )
    _isLoggedIn.value = true
    saveSession()
  }

  fun continueAsGuest() {
    _currentUser.value = UserProfile(
      identifier = "guest_listener",
      displayName = "Guest Listener",
      authMethod = AuthMethod.GUEST,
      isProMember = false,
      isAdminBackend = false
    )
    _isLoggedIn.value = true
    saveSession()
  }

  fun setProMembership(isPro: Boolean) {
    _currentUser.value = _currentUser.value?.copy(isProMember = isPro)
    saveSession()
  }

  fun logout() {
    _currentUser.value = null
    _isLoggedIn.value = false
    saveSession()
  }
}
