package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.TotpAuthService
import com.example.auth.UserSessionManager
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import com.example.ui.theme.YalpsTertiary
import kotlinx.coroutines.delay

enum class LoginTab {
  PHONE_GOOGLE_TOTP,
  GMAIL_PASSWORD
}

data class CountryCode(val code: String, val country: String, val flag: String)

@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  onSkipToGuest: () -> Unit = onLoginSuccess,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val sessionManager = remember { UserSessionManager.getInstance() }
  val totpService = remember { TotpAuthService.getInstance() }

  var selectedTab by remember { mutableStateOf(LoginTab.PHONE_GOOGLE_TOTP) }

  // Phone / Google TOTP State
  val countryCodes = listOf(
    CountryCode("+91", "India", "🇮🇳"),
    CountryCode("+1", "USA", "🇺🇸"),
    CountryCode("+44", "UK", "🇬🇧"),
    CountryCode("+65", "Singapore", "🇸🇬"),
    CountryCode("+971", "UAE", "🇦🇪"),
    CountryCode("+60", "Malaysia", "🇲🇾"),
    CountryCode("+94", "Sri Lanka", "🇱🇰")
  )
  var selectedCountry by remember { mutableStateOf(countryCodes[0]) }
  var showCountryDropdown by remember { mutableStateOf(false) }
  var isPhoneInputMode by remember { mutableStateOf(true) }
  var phoneNumber by remember { mutableStateOf("") }
  var googleAccountInput by remember { mutableStateOf("") }

  var isOtpSetupStep by remember { mutableStateOf(false) }
  var otpDigits by remember { mutableStateOf("") }
  var secondsRemainingInCycle by remember { mutableIntStateOf(totpService.getSecondsRemainingInWindow()) }

  // Gmail & Password State
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }

  var isLoading by remember { mutableStateOf(false) }

  // Real-time RFC 6238 30-second cycle countdown
  LaunchedEffect(isOtpSetupStep) {
    while (true) {
      secondsRemainingInCycle = totpService.getSecondsRemainingInWindow()
      delay(500)
    }
  }

  val activeIdentifier = if (isPhoneInputMode) {
    "${selectedCountry.code} $phoneNumber".trim()
  } else {
    googleAccountInput.trim()
  }
  val totpSecret = remember(activeIdentifier) { totpService.getSecretForIdentifier(activeIdentifier) }
  val formattedSecret = totpSecret.chunked(4).joinToString(" ")

  Surface(
    modifier = modifier.fillMaxSize(),
    color = YalpsBackground
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .imePadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {

        // Top Brand Header
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 16.dp)
        ) {
          // Glowing Yalps Emblem
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFEC4899))
                )
              )
              .border(2.dp, YalpsPrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Headphones,
              contentDescription = "Yalps Audio",
              tint = Color.White,
              modifier = Modifier.size(38.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "YALPS",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(YalpsPrimary.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "LOSSLESS MASTER",
                color = YalpsPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
            Text(
              text = "• 24-Bit Bit-Perfect",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Sign in to stream bit-perfect lossless discographies & Dolby Atmos mixes.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Authentication Container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(YalpsSurfaceContainerLowest)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(20.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {

            // Tab Switcher: Phone / Google OTP (TOTP) vs Gmail Password
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(YalpsSurfaceContainerHigh)
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Phone & Google TOTP Tab
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (selectedTab == LoginTab.PHONE_GOOGLE_TOTP) YalpsPrimary else Color.Transparent
                  )
                  .clickable {
                    selectedTab = LoginTab.PHONE_GOOGLE_TOTP
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (selectedTab == LoginTab.PHONE_GOOGLE_TOTP) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = "Phone / Google OTP",
                    color = if (selectedTab == LoginTab.PHONE_GOOGLE_TOTP) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Gmail & Password Tab
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (selectedTab == LoginTab.GMAIL_PASSWORD) YalpsPrimary else Color.Transparent
                  )
                  .clickable {
                    selectedTab = LoginTab.GMAIL_PASSWORD
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = if (selectedTab == LoginTab.GMAIL_PASSWORD) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = "Gmail / Pass",
                    color = if (selectedTab == LoginTab.GMAIL_PASSWORD) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content Tab 1: Phone & Google TOTP Mode (RFC 6238)
            if (selectedTab == LoginTab.PHONE_GOOGLE_TOTP) {
              if (!isOtpSetupStep) {
                // Step 1: Input Identifier (Phone or Google Account)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(YalpsSurfaceContainer)
                    .padding(3.dp),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isPhoneInputMode) YalpsPrimary.copy(alpha = 0.25f) else Color.Transparent)
                      .clickable { isPhoneInputMode = true }
                      .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "Phone Number",
                      color = if (isPhoneInputMode) YalpsPrimary else Color.Gray,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (!isPhoneInputMode) YalpsPrimary.copy(alpha = 0.25f) else Color.Transparent)
                      .clickable { isPhoneInputMode = false }
                      .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "Google Account",
                      color = if (!isPhoneInputMode) YalpsPrimary else Color.Gray,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isPhoneInputMode) {
                  Text(
                    text = "MOBILE PHONE NUMBER",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                  )
                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    // Country Code Picker
                    Box {
                      Row(
                        modifier = Modifier
                          .clip(RoundedCornerShape(12.dp))
                          .background(YalpsSurfaceContainer)
                          .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                          .clickable { showCountryDropdown = true }
                          .padding(horizontal = 12.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Text(text = selectedCountry.flag, fontSize = 16.sp)
                        Text(
                          text = selectedCountry.code,
                          color = Color.White,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }

                      DropdownMenu(
                        expanded = showCountryDropdown,
                        onDismissRequest = { showCountryDropdown = false },
                        modifier = Modifier.background(YalpsSurfaceContainerHigh)
                      ) {
                        countryCodes.forEach { cc ->
                          DropdownMenuItem(
                            text = {
                              Text(
                                text = "${cc.flag} ${cc.code} (${cc.country})",
                                color = Color.White,
                                fontSize = 13.sp
                              )
                            },
                            onClick = {
                              selectedCountry = cc
                              showCountryDropdown = false
                            }
                          )
                        }
                      }
                    }

                    // Phone Input Field
                    OutlinedTextField(
                      value = phoneNumber,
                      onValueChange = { phoneNumber = it.filter { ch -> ch.isDigit() }.take(10) },
                      placeholder = { Text("Enter 10-digit number", color = Color.Gray, fontSize = 14.sp) },
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                      ),
                      keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YalpsPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = YalpsSurfaceContainer,
                        unfocusedContainerColor = YalpsSurfaceContainer,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                      ),
                      shape = RoundedCornerShape(12.dp),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("login_phone_input")
                    )
                  }
                } else {
                  Text(
                    text = "GOOGLE / GMAIL ACCOUNT",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                  )
                  Spacer(modifier = Modifier.height(8.dp))

                  OutlinedTextField(
                    value = googleAccountInput,
                    onValueChange = { googleAccountInput = it },
                    placeholder = { Text("your.email@gmail.com", color = Color.Gray, fontSize = 14.sp) },
                    singleLine = true,
                    leadingIcon = {
                      Icon(Icons.Default.Email, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(
                      keyboardType = KeyboardType.Email,
                      imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = YalpsPrimary,
                      unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                      focusedContainerColor = YalpsSurfaceContainer,
                      unfocusedContainerColor = YalpsSurfaceContainer,
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("login_google_input")
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1E2E))
                    .padding(10.dp)
                ) {
                  Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(16.dp))
                  Text(
                    text = "RFC 6238 TOTP Protocol • Compatible with Google Authenticator & Phone OTP System",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                  )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Continue to TOTP Button
                Button(
                  onClick = {
                    if (isPhoneInputMode && phoneNumber.length < 10) {
                      Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                    } else if (!isPhoneInputMode && !googleAccountInput.contains("@")) {
                      Toast.makeText(context, "Please enter a valid Google/Gmail account", Toast.LENGTH_SHORT).show()
                    } else {
                      isOtpSetupStep = true
                      otpDigits = ""
                    }
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_totp_btn"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = YalpsPrimary,
                    contentColor = Color.Black
                  ),
                  shape = RoundedCornerShape(14.dp)
                ) {
                  Text(
                    text = "Continue with TOTP Protocol",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              } else {
                // Step 2: TOTP Protocol Verification
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  IconButton(
                    onClick = { isOtpSetupStep = false },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = "TOTP PROTOCOL VERIFICATION",
                      color = YalpsPrimary,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = "Account: $activeIdentifier",
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TOTP Secret Card
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = Color(0xFF1E1E2E),
                  border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.3f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(14.dp))
                        Text(
                          text = "TOTP SECRET KEY (RFC 6238)",
                          color = YalpsPrimary,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = FontFamily.Monospace
                        )
                      }
                      IconButton(
                        onClick = { totpService.copySecretToClipboard(context, totpSecret) },
                        modifier = Modifier.size(24.dp)
                      ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Secret", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                      }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                      text = formattedSecret,
                      color = Color.White,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Black,
                      fontFamily = FontFamily.Monospace,
                      letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action: Open in Google Authenticator / Phone OTP App
                    OutlinedButton(
                      onClick = {
                        val launched = totpService.launchAuthenticatorApp(context, activeIdentifier, totpSecret)
                        if (!launched) {
                          totpService.copySecretToClipboard(context, totpSecret)
                          Toast.makeText(context, "Copied key! Open Google Authenticator or Phone OTP and add token manually.", Toast.LENGTH_LONG).show()
                        }
                      },
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, YalpsPrimary),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Icon(Icons.Default.OpenInNew, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Add to Google Authenticator / Phone OTP",
                        color = YalpsPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time 30-second cycle progress
                Column(modifier = Modifier.fillMaxWidth()) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Icon(Icons.Default.Timer, contentDescription = null, tint = YalpsSecondary, modifier = Modifier.size(12.dp))
                      Text(
                        text = "Google OTP / Phone System Cycle",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                      )
                    }
                    Text(
                      text = "${secondsRemainingInCycle}s left",
                      color = YalpsSecondary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  LinearProgressIndicator(
                    progress = { secondsRemainingInCycle / 30f },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(4.dp)
                      .clip(RoundedCornerShape(2.dp)),
                    color = YalpsSecondary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                  text = "ENTER 6-DIGIT CODE FROM AUTHENTICATOR",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // OTP Code Input
                OutlinedTextField(
                  value = otpDigits,
                  onValueChange = { otpDigits = it.filter { ch -> ch.isDigit() }.take(6) },
                  placeholder = { Text("6-Digit Code (e.g. 839201)", color = Color.Gray, fontSize = 14.sp) },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                  ),
                  keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YalpsPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = YalpsSurfaceContainer,
                    unfocusedContainerColor = YalpsSurfaceContainer,
                    focusedTextColor = YalpsPrimary,
                    unfocusedTextColor = Color.White
                  ),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("totp_digit_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Verify and Sign In Button
                Button(
                  onClick = {
                    if (otpDigits.length != 6) {
                      Toast.makeText(context, "Please enter the 6-digit TOTP code from your Google OTP or Phone Authenticator", Toast.LENGTH_SHORT).show()
                    } else {
                      val isValid = totpService.verifyTotp(totpSecret, otpDigits)
                      if (!isValid) {
                        Toast.makeText(context, "Invalid TOTP code. Ensure your device clock is synced and check Google Authenticator / Phone OTP.", Toast.LENGTH_LONG).show()
                      } else {
                        isLoading = true
                        if (isPhoneInputMode) {
                          sessionManager.loginWithPhone(activeIdentifier)
                        } else {
                          sessionManager.loginWithGmail(activeIdentifier)
                        }
                        Toast.makeText(context, "Verified via TOTP! Welcome to Yalps Lossless Audio.", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        onLoginSuccess()
                      }
                    }
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_totp_btn"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = YalpsPrimary,
                    contentColor = Color.Black
                  ),
                  shape = RoundedCornerShape(14.dp)
                ) {
                  Text(
                    text = "Verify & Sign In",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Content Tab 2: Gmail & Password Mode
            if (selectedTab == LoginTab.GMAIL_PASSWORD) {
              Text(
                text = "GMAIL / EMAIL ADDRESS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = { Text("Enter your email address", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = YalpsSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Email,
                  imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = YalpsPrimary,
                  unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                  focusedContainerColor = YalpsSurfaceContainer,
                  unfocusedContainerColor = YalpsSurfaceContainer,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("login_email_input")
              )

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = "PASSWORD",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                placeholder = { Text("Enter your account password", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = YalpsPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                },
                trailingIcon = {
                  IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                      imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                      contentDescription = "Toggle Password Visibility",
                      tint = Color.Gray
                    )
                  }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Password,
                  imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = YalpsPrimary,
                  unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                  focusedContainerColor = YalpsSurfaceContainer,
                  unfocusedContainerColor = YalpsSurfaceContainer,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("login_password_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Text(
                  text = "Forgot Password?",
                  color = YalpsSecondary,
                  fontSize = 12.sp,
                  modifier = Modifier.clickable {
                    Toast.makeText(context, "Password reset link sent to $emailInput", Toast.LENGTH_SHORT).show()
                  }
                )
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Sign in button
              Button(
                onClick = {
                  if (!emailInput.contains("@")) {
                    Toast.makeText(context, "Please enter a valid Gmail / Email address", Toast.LENGTH_SHORT).show()
                  } else {
                    val result = sessionManager.loginWithPassword(emailInput, passwordInput)
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    if (result.success) {
                      onLoginSuccess()
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("signin_email_btn"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = YalpsPrimary,
                  contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
              ) {
                Text(
                  text = "Sign In",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer: Terms
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
        ) {
          Text(
            text = "By signing in, you agree to Yalps Audio Terms of Service & Privacy Policy.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

