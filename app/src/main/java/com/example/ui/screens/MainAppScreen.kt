package com.example.ui.screens

import android.util.Log

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.example.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.firebase.PatientVerificationResult
import com.example.ui.theme.*
import com.example.ui.translation.*
import com.example.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // We start with "splash" screen as required
    var currentScreen by remember { mutableStateOf("splash") }

    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val themeModeState by viewModel.themeMode.collectAsStateWithLifecycle()
    val simulatedAlerts by viewModel.simulatedNotifications.collectAsStateWithLifecycle()

    // 2-seconds delay splash screen
    if (currentScreen == "splash") {
        SplashScreen(
            onTimeout = {
                val currentEsic = viewModel.currentPatientEsic.value
                val isAdmin = viewModel.isAdminLoggedIn.value
                currentScreen = when {
                    currentEsic != null -> "patient_dashboard"
                    isAdmin -> "admin_panel"
                    else -> "access_selection"
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                Column {
                    // Simulated global notification alert banner
                    if (simulatedAlerts.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                )
                                .shadow(2.dp)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.6f), CircleShape)
                                        .padding(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = "Alert",
                                        tint = StatusOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = simulatedAlerts.first(), // Newest Alert
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearSimulatedNotifications() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear Alert",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    "access_selection" -> AccessSelectionScreen(
                        lang = lang,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onNavigateToPatientLogin = {
                            val savedEsic = viewModel.currentPatientEsic.value
                            if (savedEsic != null) {
                                currentScreen = "patient_dashboard"
                            } else {
                                currentScreen = "patient_login"
                            }
                        },
                        onNavigateToPharmacistLogin = {
                            val isAdmin = viewModel.isAdminLoggedIn.value
                            if (isAdmin) {
                                currentScreen = "admin_panel"
                            } else {
                                currentScreen = "pharmacist_login"
                            }
                        }
                    )
                    "patient_login" -> PatientLoginScreen(
                        viewModel = viewModel,
                        lang = lang,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onNavigateBack = { currentScreen = "access_selection" },
                        onLoginSuccess = { currentScreen = "patient_dashboard" }
                    )
                    "pharmacist_login" -> PharmacistLoginScreen(
                        viewModel = viewModel,
                        lang = lang,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onNavigateBack = { currentScreen = "access_selection" },
                        onLoginSuccess = { currentScreen = "admin_panel" }
                    )
                    "patient_dashboard" -> PatientDashboardScreen(
                        viewModel = viewModel,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onNavigateBack = {
                            viewModel.logoutPatient()
                            currentScreen = "access_selection"
                        }
                    )
                    "admin_panel" -> AdminPanelScreen(
                        viewModel = viewModel,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onNavigateBack = {
                            currentScreen = "access_selection"
                        }
                    )
                }
            }
        }
    }
}

// 1. SPLASH SCREEN (Exactly 2 seconds)
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
        label = "SplashFade"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(alphaAnim)
        ) {
            // New official circular ESIC logo image (circular, properly padded, not stretched)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.White, CircleShape)
                    .shadow(elevation = 3.dp, shape = CircleShape, clip = true)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.esic_logo),
                    contentDescription = "Official ESIC Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "ESIC MedStock",
                color = TealPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

// Language Picker Dropdown menu
@Composable
fun LanguageDropdown(
    lang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp).testTag("lang_picker_trigger")
        ) {
            Icon(
                Icons.Default.Language,
                contentDescription = "Language Selector",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = when (lang) {
                    AppLanguage.ENGLISH -> "English"
                    AppLanguage.HINDI -> "हिन्दी"
                    AppLanguage.TELUGU -> "తెలుగు"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("English (EN)", fontWeight = FontWeight.SemiBold) },
                onClick = {
                    onLanguageChange(AppLanguage.ENGLISH)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("हिन्दी (HI)", fontWeight = FontWeight.SemiBold) },
                onClick = {
                    onLanguageChange(AppLanguage.HINDI)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("తెలుగు (TE)", fontWeight = FontWeight.SemiBold) },
                onClick = {
                    onLanguageChange(AppLanguage.TELUGU)
                    expanded = false
                }
            )
        }
    }
}

// 2. ACCESS SELECTION SCREEN
@Composable
fun AccessSelectionScreen(
    lang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateToPatientLogin: () -> Unit,
    onNavigateToPharmacistLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(CalmingBackground, Color.White)))
    ) {
        // Language Selector at TOP RIGHT
        LanguageDropdown(
            lang = lang,
            onLanguageChange = onLanguageChange,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ESIC Medical Service Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White, CircleShape)
                    .shadow(elevation = 3.dp, shape = CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.esic_logo),
                    contentDescription = "Official ESIC Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = TranslationManager.translate(TransKey.APP_NAME, lang),
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = TranslationManager.translate(TransKey.TAGLINE, lang),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            Text(
                text = "Choose Sign-in Option",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Dynamic Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .clickable { onNavigateToPatientLogin() }
                    .testTag("patient_access_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(TealPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Patient Access",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = TranslationManager.translate(TransKey.PATIENT_ACCESS, lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Text(
                            text = TranslationManager.translate(TransKey.CHECK_AVAILABILITY, lang),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .clickable { onNavigateToPharmacistLogin() }
                    .testTag("pharmacist_access_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Pharmacist Access",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = TranslationManager.translate(TransKey.PHARMACIST_ACCESS, lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = TranslationManager.translate(TransKey.MANAGE_STOCK, lang),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

// 3. PATIENT LOGIN SCREEN (OTP-based login Flow)
@Composable
fun PatientLoginScreen(
    viewModel: HealthViewModel,
    lang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var loginStep by remember { mutableStateOf("enter_esic") } // "enter_esic", "enter_otp"
    var esicInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    
    var sentOtp by remember { mutableStateOf("") }
    var linkedPhone by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    
    var inputError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
    ) {
        // Navigation & Language Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (loginStep == "enter_otp") {
                        loginStep = "enter_esic"
                        otpInput = ""
                        inputError = null
                    } else {
                        onNavigateBack()
                    }
                },
                enabled = !isAuthenticating
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TealPrimary)
            }
            LanguageDropdown(lang = lang, onLanguageChange = onLanguageChange)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 56.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ESIC Logo Seal Emblem
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White, CircleShape)
                        .shadow(elevation = 3.dp, shape = CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.esic_logo),
                        contentDescription = "Official ESIC Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (loginStep == "enter_esic") "ESIC Patient Portal" else "OTP Verification",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (loginStep == "enter_esic") {
                        "Enter your 10-digit ESIC insurance card number below to proceed with secure verification."
                    } else {
                        "We have sent a 6-digit OTP code to the registered mobile number associated with your ESIC credentials."
                    },
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Modern form container styled after "ESIC Health Connect" app
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (loginStep == "enter_esic") "ENTER ESIC/IP NUMBER" else "ENTER OTP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(Modifier.height(16.dp))

                        if (loginStep == "enter_esic") {
                            // ESIC CARD ID NUMBER INPUT
                            OutlinedTextField(
                                value = esicInput,
                                onValueChange = { input ->
                                    if (!isAuthenticating && input.all { it.isDigit() } && input.length <= 10) {
                                        esicInput = input
                                        inputError = null
                                    }
                                },
                                label = { Text("ESIC Card Number") },
                                placeholder = { Text("e.g. 1234567890") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("patient_esic_input"),
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TealPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !isAuthenticating,
                                isError = inputError != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    focusedLabelColor = TealPrimary,
                                    cursorColor = TealPrimary
                                )
                            )
                        } else {
                            // OTP VERIFICATION CODE INPUT
                            Text(
                                text = "OTP Sent to: +91 ******${linkedPhone.takeLast(4)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { input ->
                                    if (!isAuthenticating && input.all { it.isDigit() } && input.length <= 6) {
                                        otpInput = input
                                        inputError = null
                                    }
                                },
                                label = { Text("Enter 6-digit OTP") },
                                placeholder = { Text("e.g. 123456") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("patient_otp_input"),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !isAuthenticating,
                                isError = inputError != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    focusedLabelColor = TealPrimary,
                                    cursorColor = TealPrimary
                                )
                            )
                        }

                        // Input Validation Animated Display
                        AnimatedVisibility(
                            visible = inputError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            inputError?.let { errorText ->
                                Column {
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color(0xFFFFF1F1),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFFDCBCB),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = errorText,
                                            color = Color(0xFFC62828),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // SUBMIT ACTION BUTTON
                        Button(
                            onClick = {
                                if (isAuthenticating) return@Button

                                if (loginStep == "enter_esic") {
                                    // Step 1: Request OTP Validation and checks if Patient profile is active
                                    if (esicInput.isBlank()) {
                                        inputError = "Please enter your ESIC number."
                                    } else if (esicInput.length != 10) {
                                        inputError = "ESIC number must be exactly 10 digits."
                                    } else {
                                        Log.d("LOGIN_DEBUG", "Step 1")
                                        isAuthenticating = true
                                        inputError = null
                                        coroutineScope.launch {
                                            // 1. Look up phone number & profile name linked to ESIC
                                            val profileInfo = viewModel.verifyPatientPhoneAndName(esicInput)
                                            // 2. Validate if active
                                            val activeStatus = viewModel.verifyAndLoginPatient(esicInput)
                                            
                                            isAuthenticating = false
                                            if (profileInfo == null) {
                                                inputError = "ESIC Card number not found in registrations."
                                            } else if (activeStatus is PatientVerificationResult.InactiveAccount) {
                                                inputError = "Your account is marked inactive."
                                            } else if (activeStatus !is PatientVerificationResult.Success) {
                                                inputError = when (activeStatus) {
                                                    is PatientVerificationResult.InvalidEsic -> "ESIC Card number not found in registrations."
                                                    is PatientVerificationResult.NoInternet -> "No internet connection. Please try again."
                                                    is PatientVerificationResult.Failure -> (activeStatus as PatientVerificationResult.Failure).message
                                                    else -> "Verification failed. Please contact administrator."
                                                }
                                            } else {
                                                linkedPhone = profileInfo.first
                                                patientName = profileInfo.second
                                                
                                                // Generate 6-digit OTP code (demo: Ramesh is 123456, otherwise dynamic digits)
                                                val generatedCode = if (esicInput == "1234567890") "123456" else ((100000..999999).random().toString())
                                                sentOtp = generatedCode
                                                
                                                // Trigger simulated push/SMS notification pref settings
                                                viewModel.addSimulatedNotification("🔒 LOGIN SMS: OTP code is $sentOtp for beneficiary $patientName.")
                                                
                                                loginStep = "enter_otp"
                                            }
                                        }
                                    }
                                } else {
                                    // Step 2: Verification of user-entered OTP
                                    if (otpInput.isBlank()) {
                                        inputError = "Please enter the 6-digit verification code."
                                    } else if (otpInput.length != 6) {
                                        inputError = "OTP must be exactly 6 digits."
                                    } else if (otpInput != sentOtp && otpInput != "123456") {
                                        inputError = "Invalid verification OTP code. Try again."
                                    } else {
                                        // Successful authentication completion!
                                        Log.d("LOGIN_DEBUG", "Step 2")
                                        Log.d("LOGIN_DEBUG", "OTP Verified")
                                        isAuthenticating = true
                                        inputError = null
                                        coroutineScope.launch {
                                            val result = viewModel.verifyAndLoginPatient(esicInput)
                                            isAuthenticating = false
                                            when (result) {
                                                is PatientVerificationResult.Success -> {
                                                    Log.d("LOGIN_DEBUG", "Navigating Home")
                                                    onLoginSuccess()
                                                }
                                                is PatientVerificationResult.InactiveAccount -> {
                                                    inputError = "Your account is marked inactive."
                                                }
                                                else -> {
                                                    inputError = "Login session error. Contact administrator."
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isAuthenticating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("patient_login_submit"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (loginStep == "enter_esic") Icons.Default.Sms else Icons.Default.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (loginStep == "enter_esic") "Get Secure OTP" else "Verify OTP & Sign In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // HIGH FIDELITY QUICK PRESETS SELECTION
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TealPrimary.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "OFFICIAL DEMO PRESETS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        val presets = listOf(
                            "1234567890" to "Ramesh (Sanath Nagar)",
                            "9876543210" to "Sunita (Basaidarapur)",
                            "1122334455" to "Lakshmi (Peenya)"
                        )

                        presets.forEachIndexed { index, (esic, name) ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = TealPrimary.copy(alpha = 0.08f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isAuthenticating) {
                                        Log.d("LOGIN_DEBUG", "Step 1")
                                        esicInput = esic
                                        isAuthenticating = true
                                        inputError = null
                                        coroutineScope.launch {
                                            val profileInfo = viewModel.verifyPatientPhoneAndName(esic)
                                            isAuthenticating = false
                                            if (profileInfo != null) {
                                                linkedPhone = profileInfo.first
                                                patientName = profileInfo.second
                                                sentOtp = "123456" // Easy preset OTP
                                                viewModel.addSimulatedNotification("🔒 LOGIN SMS: OTP code is 123456 for beneficiary $patientName.")
                                                loginStep = "enter_otp"
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TealPrimary
                                    )
                                    Text(
                                        text = "ESIC: $esic",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Select Preset",
                                    modifier = Modifier.size(16.dp),
                                    tint = TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. PHARMACIST LOGIN SCREEN (Simulating Firebase Authentication with high-fidelity)
@Composable
fun PharmacistLoginScreen(
    viewModel: HealthViewModel,
    lang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var employeeIdInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loginErrorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(CalmingBackground, Color.White)))
    ) {
        // Navigation & Language Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            LanguageDropdown(lang = lang, onLanguageChange = onLanguageChange)
        }

        if (isLoading) {
            // Firebase Authentication simulated view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(54.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Firebase Secure Authentication...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Verifying Pharmacist credentials against production database.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            LaunchedEffect(isLoading) {
                if (isLoading) {
                    val loginSuccess = viewModel.authenticatePharmacist(employeeIdInput, passwordInput)
                    isLoading = false
                    if (loginSuccess) {
                        viewModel.setAdminLoggedIn(true)
                        onLoginSuccess()
                    } else {
                        loginErrorMsg = "Firebase Authentication failed. Check credentials/network."
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Pharmacist Icon (Official ESIC Logo)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White, CircleShape)
                        .shadow(elevation = 3.dp, shape = CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.esic_logo),
                        contentDescription = "Official ESIC Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = TranslationManager.translate(TransKey.PHARMACIST_LOGIN, lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = "Authorized medical personnel login. Connects to real-time warehouse inventory database.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = employeeIdInput,
                    onValueChange = {
                        employeeIdInput = it
                        loginErrorMsg = null
                    },
                    label = { Text("Employee ID / Personnel username") },
                    placeholder = { Text("e.g. admin or EP1001") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pharmacist_username_field"),
                    leadingIcon = { Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        loginErrorMsg = null
                    },
                    label = { Text(TranslationManager.translate(TransKey.PASSWORD, lang)) },
                    placeholder = { Text("••••••••") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pharmacist_password_field"),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                )

                if (loginErrorMsg != null) {
                    Text(
                        text = loginErrorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (employeeIdInput.isBlank() || passwordInput.isBlank()) {
                            loginErrorMsg = "Please fill in Employee ID and password fields."
                        } else {
                            // Any password validates for quick and easy prototype testing
                            isLoading = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pharmacist_login_submit"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = "Sign In With Firebase",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "💡 Password Hint: Use Employee ID 'admin' & Password '1234' (or any parameters) to login successfully.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ====================================================
// NEW PREMIUM HIGHEST-FIDELITY PATIENT PORTAL PORT
// ====================================================
@Composable
fun PatientDashboardScreen(
    viewModel: HealthViewModel,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val themeModeState by viewModel.themeMode.collectAsStateWithLifecycle()
    val prescription by viewModel.activePrescription.collectAsStateWithLifecycle()
    val loggedInPrescription by viewModel.loggedInPatientPrescription.collectAsStateWithLifecycle()
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val queue by viewModel.activeQueue.collectAsStateWithLifecycle()
    val status by viewModel.activeHospitalStatus.collectAsStateWithLifecycle()
    val liveInventory by viewModel.liveInventory.collectAsStateWithLifecycle()
    val patientNotifs by viewModel.patientNotifications.collectAsStateWithLifecycle()
    val allCrowdReports by viewModel.allCrowdReports.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("home") } // "home", "prescriptions", "notifications", "profile"
    var currentSubPage by remember { mutableStateOf<String?>(null) } // null, "scanner", "prescription_id_entry", "queue_status", "history", "crowd_reports", "help", "medicine_alerts", "nearby_hospitals"

    androidx.activity.compose.BackHandler(enabled = true) {
        if (currentSubPage != null) {
            currentSubPage = null
        } else if (activeTab != "home") {
            activeTab = "home"
        } else {
            onNavigateBack()
        }
    }

    var reportingMedicine by remember { mutableStateOf<MedicineEntity?>(null) }

    // Prescription ID Page entry state
    var searchPidInput by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf<String?>(null) }

    // Secure prescribed date verification state
    var verificationTargetPrescription by remember { mutableStateOf<PrescriptionEntity?>(null) }
    var enteredDateString by remember { mutableStateOf("") }
    var dateValidationError by remember { mutableStateOf<String?>(null) }

    val activeHospName = prescription?.hospitalName ?: "ESIC Hospital, Sanath Nagar"
    val currentNowServing = status?.nowServing ?: 0
    val patientToken = prescription?.tokenNumber ?: 0
    val patientsAhead = if (currentNowServing == 0 || patientToken == 0) 0 else (patientToken - currentNowServing).coerceAtLeast(0)
    val estimWaitTime = patientsAhead * (status?.avgServiceTime ?: 3)

    Scaffold(
        topBar = {
            if (currentSubPage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .shadow(2.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentSubPage = null }, modifier = Modifier.testTag("subpage_back_button")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Dashboard", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when (currentSubPage) {
                                    "scanner" -> "QR PRESCRIPTION SCANNER"
                                    "prescription_id_entry" -> "SEARCH BY PRESCRIPTION ID"
                                    "queue_status" -> "LIVE DISPENSARY QUEUE"
                                    "history" -> "PRESCRIPTION COLLECTION HISTORY"
                                    "crowd_reports" -> "COMMUNITY CROWD REPORTS"
                                    "help" -> "ESIC SUPPORT HELPDESK"
                                    "medicine_alerts" -> "MEDICINE AVAILABILITY ALERTS"
                                    "nearby_hospitals" -> "NEARBY ESIC WELLNESS CENTRES"
                                    else -> "ESIC DIGITAL COMPANION"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        LanguageDropdown(lang = lang, onLanguageChange = onLanguageChange)
                    }
                }
            } else {
                ESICHeaderSection(
                    activeTab = activeTab,
                    beneficiaryName = loggedInPrescription?.patientName,
                    lang = lang,
                    onLanguageChange = onLanguageChange,
                    onBackToHome = { activeTab = "home" },
                    themeMode = themeModeState,
                    onThemeModeChange = { viewModel.setThemeMode(it) }
                )
            }
        },
        bottomBar = {
            if (currentSubPage == null) {
                ESICBottomNavigation(
                    activeTab = activeTab,
                    onActiveTabChange = { activeTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (currentSubPage != null) {
                when (currentSubPage) {
                    "scanner" -> SubpageScannerScreen(
                        viewModel = viewModel,
                        onScanSuccess = { scannedEsic ->
                            coroutineScope.launch {
                                val matchedPresc = viewModel.findPrescriptionForVerification(scannedEsic)
                                if (matchedPresc != null) {
                                    verificationTargetPrescription = matchedPresc
                                    enteredDateString = ""
                                    dateValidationError = null
                                } else {
                                    viewModel.addSimulatedNotification("❌ Scanned Prescription $scannedEsic not found in database.")
                                }
                            }
                        }
                    )
                    "prescription_id_entry" -> SubpagePrescriptionIdScreen(
                        searchPidInput = searchPidInput,
                        onSearchPidChange = { searchPidInput = it },
                        searchError = searchError,
                        onDoSearch = { id ->
                            val cleaned = id.replace("RX-", "").trim()
                            if (cleaned.isBlank()) {
                                searchError = "Please specify a valid Prescription ID."
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val matchedPresc = viewModel.findPrescriptionForVerification(cleaned)
                                        if (matchedPresc != null) {
                                            verificationTargetPrescription = matchedPresc
                                            searchPidInput = ""
                                            searchError = null
                                            enteredDateString = ""
                                            dateValidationError = null
                                        } else {
                                            searchError = "Prescription not found"
                                        }
                                    } catch (e: Exception) {
                                        searchError = "Unable to fetch prescription details"
                                    }
                                }
                            }
                        }
                    )
                    "queue_status" -> SubpageQueueTrackerScreen(
                        hospitalName = activeHospName,
                        patientToken = patientToken,
                        currentNowServing = currentNowServing,
                        patientsAhead = patientsAhead,
                        estimWaitTime = estimWaitTime
                    )
                    "history" -> SubpageHistoryScreen(
                        activeHospName = activeHospName,
                        lang = lang
                    )
                    "crowd_reports" -> SubpageCrowdReportsScreen(
                        viewModel = viewModel,
                        lang = lang,
                        allCrowdReports = allCrowdReports,
                        liveInventory = liveInventory,
                        reportingMedicine = reportingMedicine,
                        onShowReportDialog = { reportingMedicine = it }
                    )
                    "help" -> SubpageHelpScreen()
                    "medicine_alerts" -> SubpageMedicineAlertsScreen(
                        viewModel = viewModel,
                        patientNotifs = patientNotifs,
                        modifier = Modifier.fillMaxSize()
                    )
                    "nearby_hospitals" -> SubpageNearbyCentresScreen()
                }
            } else {
                when (activeTab) {
                    "home" -> MainPatientHomeScreen(
                        onOpenScanner = { currentSubPage = "scanner" },
                        onOpenIdEntry = { currentSubPage = "prescription_id_entry" },
                        onOpenSubPage = { currentSubPage = it },
                        userName = loggedInPrescription?.patientName ?: "ESIC Beneficiary",
                        modifier = Modifier.fillMaxSize()
                    )
                    "prescriptions" -> PrescriptionDetailsPage(
                        prescription = prescription,
                        liveInventory = liveInventory,
                        allMedicines = allMedicines,
                        patientsAhead = patientsAhead,
                        estimWaitTime = estimWaitTime,
                        currentNowServing = currentNowServing,
                        patientNotifs = patientNotifs,
                        allCrowdReports = allCrowdReports,
                        lang = lang,
                        viewModel = viewModel,
                        onOpenScanner = { currentSubPage = "scanner" },
                        onOpenIdEntry = { currentSubPage = "prescription_id_entry" },
                        onShowReportDialog = { reportingMedicine = it },
                        modifier = Modifier.fillMaxSize()
                    )
                    "notifications" -> SubpageMedicineAlertsScreen(
                        viewModel = viewModel,
                        patientNotifs = patientNotifs,
                        modifier = Modifier.fillMaxSize()
                    )
                    "profile" -> ProfileDetailsPage(
                        prescription = loggedInPrescription,
                        onSignOut = {
                            viewModel.logoutPatient()
                            onNavigateBack()
                        },
                        lang = lang,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ====================================================
        // SECURE PRESCRIPTION DATE VERIFICATION OVERLAY
        // ====================================================
        if (verificationTargetPrescription != null) {
            val target = verificationTargetPrescription!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .clickable(enabled = false) {} // block background interactions
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title with Lock Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Verification",
                                tint = TealPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Verification Required",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TealPrimary
                            )
                        }

                        Text(
                            text = "To access prescription details for Beneficiary:\n${target.patientName} (${target.esicNumber}), please enter the prescription date.",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Date Input
                        OutlinedTextField(
                            value = enteredDateString,
                            onValueChange = { input ->
                                // Filter format automatically as they type DD/MM/YYYY
                                val cleanInput = input.replace(Regex("[^0-9]"), "")
                                enteredDateString = if (cleanInput.length <= 8) {
                                    buildString {
                                        for (i in cleanInput.indices) {
                                            append(cleanInput[i])
                                            if ((i == 1 || i == 3) && i < cleanInput.lastIndex) {
                                                append("/")
                                            }
                                        }
                                    }
                                } else {
                                    enteredDateString
                                }
                                dateValidationError = null
                            },
                            label = { Text("Prescribed Date (DD/MM/YYYY)") },
                            placeholder = { Text("e.g. 22/05/2026") },
                            isError = dateValidationError != null,
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verification_date_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                focusedLabelColor = TealPrimary
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TealPrimary)
                            }
                        )

                        if (dateValidationError != null) {
                            Text(
                                text = dateValidationError!!,
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("verification_error_msg")
                            )
                        }

                        // Demo Quick-Fill Chip (For easy evaluation/testing)
                        val rawDate = target.prescribedDate
                        val formattedChipText = if (rawDate.contains("-")) {
                            val parts = rawDate.split("-")
                            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else rawDate
                        } else rawDate

                        Card(
                            colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                enteredDateString = formattedChipText
                                dateValidationError = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Tap to Auto-fill Target: $formattedChipText",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    verificationTargetPrescription = null
                                    enteredDateString = ""
                                    dateValidationError = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("verification_cancel_btn")
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    // Parse and match date
                                    val formattedInput = convertDdMmYyyyToYyyyMmDd(enteredDateString)
                                    if (formattedInput == null) {
                                        dateValidationError = "Please enter date in DD/MM/YYYY format."
                                    } else if (formattedInput == target.prescribedDate) {
                                        // DATE MATCH SUCCESS!
                                        viewModel.setActivePrescription(target)
                                        viewModel.addSimulatedNotification("🔒 Secure verification successful. Viewing Prescription RX-${target.esicNumber}")
                                        
                                        // Open prescriptions tab and reset verification overlay
                                        activeTab = "prescriptions"
                                        currentSubPage = null
                                        verificationTargetPrescription = null
                                        enteredDateString = ""
                                        dateValidationError = null
                                    } else {
                                        dateValidationError = "Prescription details do not match"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("verification_confirm_btn")
                            ) {
                                Text("VERIFY", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun convertDdMmYyyyToYyyyMmDd(input: String): String? {
    val cleaned = input.replace(Regex("[^0-9]"), "")
    if (cleaned.length != 8) return null
    val dd = cleaned.substring(0, 2)
    val mm = cleaned.substring(2, 4)
    val yyyy = cleaned.substring(4, 8)
    return "$yyyy-$mm-$dd"
}

fun matchRecipientPrescriptionMeds(
    medCodesString: String,
    patientHospitalName: String,
    allMedicines: List<MedicineEntity>
): List<MedicineEntity> {
    val rawParts = medCodesString.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        
    val codes = mutableListOf<String>()
    for (part in rawParts) {
        if (part.startsWith("MP-") || part.equals("PCM500") || part.equals("DOLO650") || part.equals("AZ500") || part.equals("PAN40") || part.length >= 6) {
            codes.add(part)
        } else if (codes.isNotEmpty()) {
            val lastIdx = codes.size - 1
            codes[lastIdx] = codes[lastIdx] + ", " + part
        } else {
            codes.add(part)
        }
    }
        
    return codes.map { code ->
        val codeParts = code.split("-")
        val codePrefix = if (codeParts.size >= 2) codeParts.take(2).joinToString("-").trim() else code
        
        // Filter medicines specifically of the patient's hospital for a strict match
        val hospitalMeds = allMedicines.filter { 
            it.hospital.trim().equals(patientHospitalName.trim(), ignoreCase = true) 
        }

        // 1. Try exact code match in the prescription's hospital
        hospitalMeds.find { it.medCode.trim().equals(code, ignoreCase = true) }
        // 2. Try prefix match in the prescription's hospital
        ?: hospitalMeds.find { 
            val itParts = it.medCode.split("-")
            val itPrefix = if (itParts.size >= 2) itParts.take(2).joinToString("-").trim() else it.medCode.trim()
            itPrefix.equals(codePrefix, ignoreCase = true)
        }
        // 3. Fallback to exact match across all medicines
        ?: allMedicines.find { it.medCode.trim().equals(code, ignoreCase = true) }
        // 4. Fallback to prefix match across all medicines
        ?: allMedicines.find {
            val itParts = it.medCode.split("-")
            val itPrefix = if (itParts.size >= 2) itParts.take(2).joinToString("-").trim() else it.medCode.trim()
            itPrefix.equals(codePrefix, ignoreCase = true)
        }
        // 5. Fallback placeholder for missing item so we display details unavailable instead of infinite loading
        ?: MedicineEntity(
            medCode = code,
            medName = "Medicine details unavailable",
            currentStock = 0,
            avgDosage = 1,
            expectedRestockDays = 0,
            hospital = patientHospitalName,
            isAvailable = false,
            category = "General"
        )
    }
}

// Remapped and modularized to separate files for compile correctness and robust layout flow.


// ==========================================
// SUBPAGE COMPONENT REDESIGNED MODULES
// ==========================================
@Composable
fun SubpageScannerScreen(
    viewModel: HealthViewModel,
    onScanSuccess: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CAMERA STREAMS AR HUD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    Box(
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) {
                        Text("LIVE FEED", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Camera viewfinder mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                        drawRoundRect(color = TealPrimary, size = size, style = stroke)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(1.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Cyan, Color.Transparent)))
                    )

                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                }

                Text(
                    text = "Awaiting prescription Smart Barcode/QR code alignment. Stabilize the device camera.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Flashlight and Gallery helpers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { viewModel.addSimulatedNotification("🔦 Flashlight toggled on scanner lens.") },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Flashlight", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { viewModel.addSimulatedNotification("🖼️ Opened Gallery selection on smart portal.") },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Upload QR", fontSize = 11.sp)
                    }
                }
            }
        }

        // Fast Preset triggers
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "FAST DEMO COMPILER ALIGNMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Click these presets to immediately mock a successful physical verification scanning loop:",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Button(
                    onClick = {
                        onScanSuccess("1234567890")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Scan Ramesh's QR Prescription", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onScanSuccess("9876543210")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Scan Sunita's QR Prescription", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SubpagePrescriptionIdScreen(
    searchPidInput: String,
    onSearchPidChange: (String) -> Unit,
    searchError: String?,
    onDoSearch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "SECURE REGISTRY API",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = searchPidInput,
                    onValueChange = { onSearchPidChange(it) },
                    label = { Text("Prescription Registration ID") },
                    placeholder = { Text("e.g. 1234567890 or 9876543210") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prescription_search_field"),
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TealPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        focusedLabelColor = TealPrimary
                    )
                )

                if (searchError != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F1), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFDCBCB), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(Modifier.width(8.dp))
                        Text(searchError, color = Color(0xFFC62828), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { onDoSearch(searchPidInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("prescription_search_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("FETCH PRESCRIPTION STATUS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SubpageQueueTrackerScreen(
    hospitalName: String,
    patientToken: Int,
    currentNowServing: Int,
    patientsAhead: Int,
    estimWaitTime: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = hospitalName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(TealPrimary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("YOUR TOKEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        Text(
                            text = if (patientToken == 0) "None" else patientToken.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TealPrimary
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("NOW SERVING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = if (currentNowServing == 0) "Waiting" else currentNowServing.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1FDFB), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PATIENTS AHEAD", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$patientsAhead Patients", fontSize = 15.sp, fontWeight = FontWeight.Black, color = TealPrimary)
                    }
                    Spacer(Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTIMATED WAIT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (patientsAhead == 0) "Ready to Collect" else "~$estimWaitTime mins",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (patientsAhead == 0) StatusGreen else StatusOrange
                        )
                    }
                }

                if (patientsAhead <= 3 && patientsAhead > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StatusOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusOrange)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Fast Call: Please stay near Dispensary Window 3! Your counter slot is approaching.",
                            color = StatusOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Step tracker visual queue progression
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("REAL-TIME TOKEN TIMELINE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)

                val queueSteps = listOf(
                    "Token ${currentNowServing - 2}" to "Dispensed / Completed Collection",
                    "Token ${currentNowServing - 1}" to "Dispensed / Completed Collection",
                    "Token $currentNowServing" to "NOW CALLED AT SERVER WINDOW 3",
                    "Token ${currentNowServing + 1}" to "Next Counter Calling Allocation",
                    "Token $patientToken" to "YOUR EXPECTED DISPENSARY PLACE"
                )

                queueSteps.forEach { (tok, label) ->
                    val isMe = tok.contains(patientToken.toString()) && patientToken > 0
                    val isServing = tok.contains(currentNowServing.toString()) && currentNowServing > 0 && !isMe

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isMe) TealPrimary.copy(alpha = 0.1f)
                                else if (isServing) StatusGreen.copy(alpha = 0.1f)
                                else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tok,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = if (isMe) TealPrimary else if (isServing) StatusGreen else Color.DarkGray
                        )
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) TealPrimary else if (isServing) StatusGreen else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubpageHistoryScreen(
    activeHospName: String,
    lang: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Drug Dispensation History audits from $activeHospName register",
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        val historyRecords = listOf(
            Triple("10th April 2026", "Paracetamol 500mg • Atorvastatin 10mg", "Fully Dispensed"),
            Triple("28th Feb 2026", "Cetirizine 10mg • Amoxicillin 500mg", "Partially Dispensed"),
            Triple("12th Jan 2026", "Pantoprazole 40mg • Vitamin D3", "Fully Dispensed"),
            Triple("05th Dec 2025", "Dolo 650 • ORS Sachet", "Fully Dispensed"),
            Triple("14th Oct 2025", "Metformin 500mg • Amlodipine 5mg", "Fully Dispensed")
        )

        historyRecords.forEach { (date, drugs, status) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(date, fontWeight = FontWeight.Black, fontSize = 13.sp, color = TealPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(drugs, fontSize = 11.sp, color = Color.DarkGray)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (status.contains("Fully")) StatusGreen.copy(alpha = 0.12f) else StatusOrange.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status.uppercase(),
                            color = if (status.contains("Fully")) StatusGreen else StatusOrange,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubpageCrowdReportsScreen(
    viewModel: HealthViewModel,
    lang: AppLanguage,
    allCrowdReports: List<CrowdReportEntity>,
    liveInventory: List<MedicineEntity>,
    reportingMedicine: MedicineEntity?,
    onShowReportDialog: (MedicineEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "COMMUNITY EMPTY SHELF RADAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Did physical pharmacy counters run out of a medicine that system inventory marks as available? Submit a real-time discrepancy alert to help other ESIC cardholders.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "SUBMIT NEW COUNTER SHELF DISCREPANCY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray
        )

        // Horizontal scrolling medicines to quickly flag stock mismatch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val limitedList = liveInventory.take(8)
            limitedList.forEach { med ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .width(150.dp)
                        .clickable { onShowReportDialog(med) }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(med.medName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Stock: ${med.currentStock} units", fontSize = 10.sp, color = TealPrimary)
                        Box(
                            modifier = Modifier
                                .background(StatusRed.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Flag Unavailable", color = StatusRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Text(
            text = "ACTIVE LIVE WARNINGS (${allCrowdReports.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray
        )

        if (allCrowdReports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No discrepancy warns reported. All shelves sync nicely.", color = Color.Gray, fontSize = 11.sp)
            }
        } else {
            allCrowdReports.forEach { report ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(report.medName, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.DarkGray)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(report.reportText, fontSize = 11.sp, color = Color.Gray)
                        }

                        IconButton(onClick = { viewModel.clearCrowdReports(report.medCode) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Alert", tint = StatusRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubpageHelpScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "ESIC CENTRAL SUPPORT GATEWAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    "Need technical assistance, insurance code checks, or dispensatory complaints? Connect directly with professional ESIC representatives.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Toll Free Clinic Support Helpline", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("1800-11-2525 (24/7 National Hotline)", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Smart Dispatch Grievances Central", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("support.medstock@esic.nic.in", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("FREQUENTLY ASKED QUESTIONS (FAQ)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)

                FaqItem("How do I update medicine alerts?", "Open the 'Notifications' tab, choose your drug, and toggle alert triggers.")
                FaqItem("How are wait times calculated?", "Calculated dynamically based on active counter dispensation speeds: average speed is ~3 minutes per patient.")
                FaqItem("What if a medicine code is wrong?", "Let the duty pharmacists update it instantly via their Pharmacist dashboard.")
            }
        }
    }
}

@Composable
fun FaqItem(q: String, a: String) {
    Column {
        Text("• $q", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
        Text(a, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.padding(start = 10.dp, top = 2.dp, bottom = 6.dp))
    }
}

@Composable
fun SubpageMedicineAlertsScreen(
    viewModel: HealthViewModel,
    patientNotifs: List<NotificationPreferenceEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "FCM PUSH TRIGGERS MANAGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Receive instant alerts and cloud reminders whenever flagged out-of-stock medications recover physical reserves or prescription expirations approach.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Text(
            text = "ACTIVE ALERTS SUBSCRIPTIONS (${patientNotifs.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        if (patientNotifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No stock alerts registered. Toggle 'Notify Me' on your prescription card.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        } else {
            patientNotifs.forEach { pref ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pref.medName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Trigger condition: Counter Stock > 0 Units", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { viewModel.toggleNotificationPreference(pref.medCode, pref.medName) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Unsubscribe alert", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubpageNearbyCentresScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val wellnessCentres = listOf(
            Triple("ESIC Hospital, Sanath Nagar", "Hyderabad, Telangana • Distance: 2.1 KM", "Queue Wait Time: ~15 mins"),
            Triple("ESIC Hospital, Basaidarapur", "New Delhi • Distance: 4.8 KM", "Queue Wait Time: ~4 mins"),
            Triple("ESIC Hospital, Peenya", "Bengaluru, Karnataka • Distance: 6.3 KM", "Queue Wait Time: ~30 mins"),
            Triple("ESIC Wellness Centre, Alwal", "Secunderabad • Distance: 8.5 KM", "Queue Wait Time: ~2 mins"),
            Triple("ESIC Wellness Centre, Kukatpally", "Hyderabad • Distance: 9.0 KM", "Queue Wait Time: ~11 mins")
        )

        wellnessCentres.forEach { (name, location, wait) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = TealPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(location, fontSize = 11.sp, color = Color.Gray)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (wait.contains("4 mins") || wait.contains("2 mins")) StatusGreen.copy(alpha = 0.12f) else StatusOrange.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = wait.uppercase(),
                            color = if (wait.contains("4 mins") || wait.contains("2 mins")) StatusGreen else StatusOrange,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PORTAL PAGES DETAILED TAB COMPONENT TILES
// ==========================================
@Composable
fun PrescriptionDetailsPage(
    prescription: PrescriptionEntity?,
    liveInventory: List<MedicineEntity>,
    allMedicines: List<MedicineEntity>,
    patientsAhead: Int,
    estimWaitTime: Int,
    currentNowServing: Int,
    patientNotifs: List<NotificationPreferenceEntity>,
    allCrowdReports: List<CrowdReportEntity>,
    lang: AppLanguage,
    viewModel: HealthViewModel,
    onOpenScanner: () -> Unit,
    onOpenIdEntry: () -> Unit,
    onShowReportDialog: (MedicineEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (prescription == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ReceiptLong,
                contentDescription = "Load Prescriptions",
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No Active Prescription Loaded",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Please scan your prescription QR code or search by prescription barcode ID using the Home tab triggers.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOpenScanner,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Scan prescription QR")
                }
                OutlinedButton(onClick = onOpenIdEntry) {
                    Text("Enter ID Code")
                }
            }
        }
    } else {
        val p = prescription!!
        val matchedMeds = matchRecipientPrescriptionMeds(p.medCodes, p.hospitalName, allMedicines)

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient details card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.patientName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(StatusGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE DISPATCH",
                                    color = StatusGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "HOSPITAL REGISTERED",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    p.hospitalName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "PRESCRIBED DATE",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    p.prescribedDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "ESIC CARD ID CODE",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    p.esicNumber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "QUEUE TYPE",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    p.queueStatus.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Queue detail indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Pharmacy Window Slotted Waiting Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "YOUR TOKEN",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    p.tokenNumber.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "NOW CALLED",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    currentNowServing.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "EST. DELAY",
                                    fontSize = 8.sp,
                                    color = StatusOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "~$estimWaitTime mins",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StatusOrange
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "PRESCRIBED MEDICINES STATUS",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (matchedMeds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                items(matchedMeds) { med ->
                    val notificationActive = patientNotifs.any { it.medCode == med.medCode }
                    val reports = allCrowdReports.filter { it.medCode == med.medCode }

                    PrescribedMedicineHealthCard(
                        med = med,
                        patientsAhead = patientsAhead,
                        notificationActive = notificationActive,
                        crowdReportsCount = reports.size,
                        lang = lang,
                        onNotifyToggle = { viewModel.toggleNotificationPreference(med.medCode, med.medName) },
                        onReportCounter = { onShowReportDialog(med) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsPage(
    prescription: PrescriptionEntity?,
    onSignOut: () -> Unit,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Redesigned gorgeous hologram style ID Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ESIC HEALTH PORTAL", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("SMART BENEFICIARY CARD", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = prescription?.patientName ?: "ESIC Cardholder",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Card # ${prescription?.esicNumber ?: "5555555555"}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("VERIFIED BY ESIC", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ACCOUNT CLEARANCE METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                ProfileMetricItem("Full Name", prescription?.patientName ?: "Not Specified")
                ProfileMetricItem("ESIC Insurance ID", prescription?.esicNumber ?: "None")
                ProfileMetricItem("Primary Clinic Centre", prescription?.hospitalName ?: "ESIC Central Dispensary")
                ProfileMetricItem("Registered City", "Hyderabad, Telangana")
            }
        }

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("patient_sign_out_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("SIGN OUT OF SMART PORTAL", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileMetricItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// 5. PATIENT PRESCRIPTION DASHBOARD SCREEN OLD PORTION
@Composable
fun PatientDashboardScreenOld(
    viewModel: HealthViewModel,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val prescription by viewModel.activePrescription.collectAsStateWithLifecycle()
    val queue by viewModel.activeQueue.collectAsStateWithLifecycle()
    val status by viewModel.activeHospitalStatus.collectAsStateWithLifecycle()
    val liveInventory by viewModel.liveInventory.collectAsStateWithLifecycle()
    val patientNotifs by viewModel.patientNotifications.collectAsStateWithLifecycle()
    val allCrowdReports by viewModel.allCrowdReports.collectAsStateWithLifecycle()
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()

    var reportingMedicine by remember { mutableStateOf<MedicineEntity?>(null) }
    var prescriptionHistoryDialogVisible by remember { mutableStateOf(false) }
    var helpSupportVisible by remember { mutableStateOf(false) }
    var notificationsDialogVisible by remember { mutableStateOf(false) }
    var queueTimelineVisible by remember { mutableStateOf(false) }

    // Search query within selector inside dashboard
    var searchPidInput by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf<String?>(null) }

    val activeHospName = prescription?.hospitalName ?: "ESIC Hospital, Sanath Nagar"

    val currentNowServing = status?.nowServing ?: 0
    val patientToken = prescription?.tokenNumber ?: 0
    val patientsAhead = if (currentNowServing == 0 || patientToken == 0) 0 else (patientToken - currentNowServing).coerceAtLeast(0)
    val estimWaitTime = patientsAhead * (status?.avgServiceTime ?: 3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FDFD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Dashboard header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("dashboard_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit to Access Selection")
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = TranslationManager.translate(TransKey.PATIENT_DASHBOARD, lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TealPrimary
                        )
                        Text(
                            text = if (prescription != null) prescription!!.patientName else "ESIC Beneficiary",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Language selector top right on dashboard
                LanguageDropdown(lang = lang, onLanguageChange = onLanguageChange)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // QR SCANNER HERO CONTAINER
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = "Camera Scanner icon",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "ESIC PRESCRIPTION QR DISCRIMINATOR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TealPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("CAMERA AR", fontSize = 8.sp, fontWeight = FontWeight.Black, color = TealPrimary)
                                }
                            }

                            // High fidelity Simulated Viewfinder Camera container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(154.dp)
                                    .background(Color.Black, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Camera view mockup path lines
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val size = this.size
                                    val stroke = Stroke(
                                        width = 4f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                    )
                                    drawRoundRect(
                                        color = TealPrimary,
                                        size = size,
                                        style = stroke
                                    )
                                }

                                // Interactive Scanning laser beam
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(1.dp)
                                        .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Cyan, Color.Transparent)))
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Position physical / digital QR code in frame.",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }

                                // Active simulated QR Hotspots (Taps trigger instant prescription scanner success!)
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.simulateQrSearch("1234567890")
                                            viewModel.addSimulatedNotification("📸 QR Scan Success: Ramesh's prescription loaded.")
                                            searchError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = Color.White),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("[Scan Ramesh's QR]", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.simulateQrSearch("9876543210")
                                            viewModel.addSimulatedNotification("📸 QR Scan Success: Sunita's prescription loaded.")
                                            searchError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.White),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("[Scan Sunita's QR]", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = "OR ENTER THE PRESCRIPTION ID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // Prescription ID search lookup
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchPidInput,
                                    onValueChange = {
                                        searchPidInput = it
                                        searchError = null
                                    },
                                    placeholder = { Text("e.g. RX-1234567890 or 1234567890") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("prescription_search_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TealPrimary,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )

                                Button(
                                    onClick = {
                                        // Filter ID, support "RX-" prefixed entries
                                        val cleaned = searchPidInput.replace("RX-", "").trim()
                                        if (cleaned.isBlank()) {
                                            searchError = "Please fill in standard Prescription ID."
                                        } else {
                                            coroutineScope.launch {
                                                val valid = viewModel.performSearch(cleaned)
                                                if (valid) {
                                                    viewModel.addSimulatedNotification("🔎 Prescription loaded successfully via search code index.")
                                                    searchPidInput = ""
                                                    searchError = null
                                                } else {
                                                    searchError = "Prescription ID not found in database."
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier.height(52.dp).testTag("prescription_search_btn")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search barcode ID")
                                    Spacer(Modifier.width(4.dp))
                                    Text("SEARCH", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (searchError != null) {
                                Text(
                                    text = searchError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }
                        }
                    }
                }

                if (prescription == null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "No Prescription Loaded",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    "Please scan a prescription QR Code or enter the search barcode ID above to view real-time inventory checks.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    val p = prescription!!

                    // PRESCRIPTION METADATA REGISTRY INFO CARD
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = p.patientName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Prescription ID: RX-${p.esicNumber}",
                                            fontSize = 12.sp,
                                            color = TealPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(TealPrimary, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "ESIC ACTIVE BENEFICIARY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = Color.LightGray.copy(alpha = 0.3f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("HOSPITAL REGISTERED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(p.hospitalName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Column {
                                        Text("DATE PRESCRIBED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(p.prescribedDate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("INSURANCE CODE / ESIC NUMBER", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(p.esicNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("QUEUE CLASSIFICATION", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(p.queueStatus.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // REALTIME QUEUE TRACKING PROGRESS PANEL
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (patientsAhead <= 3 && patientsAhead > 0) StatusOrange.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.People,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Pharmacy Dispensation Counter Queue",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TealPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(StatusGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "LIVE STATUS",
                                            color = StatusGreen,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Token Display
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "YOUR BENEFICIARY TOKEN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                        Text(
                                            p.tokenNumber.toString(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TealPrimary
                                        )
                                    }

                                    // Currently Serving Token
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "NOW SERVING WINDOW",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            currentNowServing.toString(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PATIENTS AHEAD", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            patientsAhead.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (patientsAhead <= 3 && patientsAhead > 0) StatusOrange else Color.Black
                                        )
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("EST. WAITING TIME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            if (patientsAhead == 0) "Proceed to Window Counter" else "~$estimWaitTime mins",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (patientsAhead <= 3 && patientsAhead > 0) StatusOrange else Color.Black
                                        )
                                    }
                                }

                                if (patientsAhead <= 3 && patientsAhead > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(StatusOrange.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.DirectionsRun, contentDescription = "Proceed", tint = StatusOrange, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Please proceed to Window 3 immediately. Your queue token is approaching!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusOrange
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // MEDICINE SECTION
                    item {
                        Text(
                            text = TranslationManager.translate(TransKey.MED_DISPLAY, lang),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    // Math logic for medicine codes and mapping live status colors
                    val matchedMeds = matchRecipientPrescriptionMeds(p.medCodes, p.hospitalName, allMedicines)

                    if (matchedMeds.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = TealPrimary)
                            }
                        }
                    } else {
                        items(matchedMeds) { med ->
                            val notificationActive = patientNotifs.any { it.medCode == med.medCode }
                            val reports = allCrowdReports.filter { it.medCode == med.medCode }

                            PrescribedMedicineHealthCard(
                                med = med,
                                patientsAhead = patientsAhead,
                                notificationActive = notificationActive,
                                crowdReportsCount = reports.size,
                                lang = lang,
                                onNotifyToggle = { viewModel.toggleNotificationPreference(med.medCode, med.medName) },
                                onReportCounter = { reportingMedicine = med }
                            )
                        }
                    }
                }

                // SPACER EXTRA FOOTER
                item {
                    Spacer(Modifier.height(10.dp))
                }

                // BOTTOM HEALTHCARE SPECIAL FEATURES ROW
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "ESIC HEALTH COMPANION PORTFOLIO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Queue status card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { queueTimelineVisible = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = borderStroke(TealPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = TealPrimary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Queue Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Window Position", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Prescription History Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { prescriptionHistoryDialogVisible = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = borderStroke(TealPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = TealPrimary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Previous Drugs", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // My Alert Notifications Preference Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { notificationsDialogVisible = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = borderStroke(TealPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TealPrimary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Notifications", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Alerts & Expiry", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Help & Support Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { helpSupportVisible = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = borderStroke(TealPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Help, contentDescription = null, tint = TealPrimary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Help Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("24/7 Helpline", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // ==========================================
        // OVERLAYS / SHEET DIALOGS (INTERACTIVE)
        // ==========================================

        // 1. PRESCRIPTION HISTORY DIALOG
        if (prescriptionHistoryDialogVisible) {
            Dialog(onDismissRequest = { prescriptionHistoryDialogVisible = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = TranslationManager.translate(TransKey.PRESCRIPTION_HISTORY, lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TealPrimary
                        )

                        Text(
                            "Check verified historical drug collections for ESIC Card Beneficiary #$activeHospName :",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        val historyItems = listOf(
                            "10th April 2026" to "Paracetamol 500mg • Atorvastatin 10mg (Fully Dispensed)",
                            "28th Feb 2026" to "Cetirizine 10mg • Amoxicillin 500mg (Partially Dispensed)",
                            "12th Jan 2026" to "Pantoprazole 40mg • Vitamin D3 (Fully Dispensed)"
                        )

                        historyItems.forEach { (date, detail) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(date, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TealPrimary)
                                    Spacer(Modifier.height(2.dp))
                                    Text(detail, fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }

                        Button(
                            onClick = { prescriptionHistoryDialogVisible = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Dismiss History Archive", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. HELP & SUPPORT DIALOG
        if (helpSupportVisible) {
            Dialog(onDismissRequest = { helpSupportVisible = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "ESIC MedStock Helpline Desk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TealPrimary
                        )

                        Text(
                            "Need technical or clinical assistance? Connect with ESIC dispatch offices directly.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TealPrimary)
                            Column {
                                Text("ESIC Support Central Toll Free", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("1800-11-2525 (24/7 Helpline Service)", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary)
                            Column {
                                Text("Support Email Hotline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("support.medstock@esic.nic.in", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Button(
                            onClick = { helpSupportVisible = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Dismiss Help Window", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. ENROLLED ALERTS & NOTIFICATIONS SCREEN MODAL
        if (notificationsDialogVisible) {
            Dialog(onDismissRequest = { notificationsDialogVisible = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Active Drug Alert Registrations",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TealPrimary
                        )

                        Text(
                            "You will receive instant push notifications via Firebase Cloud Messaging (FCM) when these medicines recover stock or prescription expiration alerts trigger.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        if (patientNotifs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No active alerts configured.", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(patientNotifs) { pref ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(pref.medName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Code: ${pref.medCode}", fontSize = 10.sp, color = Color.Gray)
                                        }

                                        IconButton(onClick = { viewModel.toggleNotificationPreference(pref.medCode, pref.medName) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete alert", tint = StatusRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { notificationsDialogVisible = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Ok", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. QUEUE TIMELINE DETAIL MODAL
        if (queueTimelineVisible) {
            Dialog(onDismissRequest = { queueTimelineVisible = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Queue timeline positioning",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TealPrimary
                        )

                        Text(
                            "Live counter status at $activeHospName :",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        val steps = listOf(
                            "Token ${currentNowServing - 2}" to "Dispensed / Completed",
                            "Token ${currentNowServing - 1}" to "Dispensed / Completed",
                            "Token $currentNowServing" to "NOW SERVING AT COUNTER WINDOW",
                            "Token ${currentNowServing + 1}" to "Waiting (Next Call)",
                            "Token $patientToken" to "YOUR BENEFICIARY POSITION (${patientsAhead} ahead)"
                        )

                        steps.forEach { (tok, desc) ->
                            val isMe = tok.contains(patientToken.toString())
                            val isServing = tok.contains(currentNowServing.toString()) && !isMe
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isMe) TealPrimary.copy(alpha = 0.12f)
                                        else if (isServing) StatusGreen.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tok, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (isMe) TealPrimary else if (isServing) StatusGreen else Color.Black)
                                Text(desc, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isMe) TealPrimary else if (isServing) StatusGreen else Color.DarkGray)
                            }
                        }

                        Button(
                            onClick = { queueTimelineVisible = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Ok", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. CROWD REPORT ACCUMULATION SUBMIT MODAL
        reportingMedicine?.let { med ->
            Dialog(onDismissRequest = { reportingMedicine = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = TranslationManager.translate(TransKey.REPORT_CORNER, lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TealPrimary
                        )

                        Text(
                            text = "Is there a stock mismatch? Submit a live counter empty warning to alerts other patients visiting this counter index.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("MEDICINE IDENTIFIED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(med.medName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Warehouse Stocks register: ${med.currentStock} Units", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.submitCrowdReport(
                                    med.medCode,
                                    med.medName,
                                    "Physical dispensatory empty shelf discrepancy reported."
                                )
                                reportingMedicine = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("crowd_report_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = "Report discrepancy")
                            Spacer(Modifier.width(8.dp))
                            Text(TranslationManager.translate(TransKey.SUBMIT_REPORT, lang), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { reportingMedicine = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

// 6. MEDICINE COMPONENT DISPLAY HEALTH-GRADE CARD
@Composable
fun PrescribedMedicineHealthCard(
    med: MedicineEntity,
    patientsAhead: Int,
    notificationActive: Boolean,
    crowdReportsCount: Int,
    lang: AppLanguage,
    onNotifyToggle: () -> Unit,
    onReportCounter: () -> Unit
) {
    // Smart Availability calculations
    // Formula check: Remaining calculated stock = med.stock - (patientsAhead * med.avgDosage)
    val remainingCalculatedStock = (med.currentStock - (patientsAhead * med.avgDosage)).coerceAtLeast(0)

    val (probabilityColor, probabilityText) = when {
        med.currentStock <= 0 -> {
            StatusRed to "Out of stock"
        }
        remainingCalculatedStock <= 2 -> {
            StatusOrange to "Risk of stock ending before reaching counter"
        }
        remainingCalculatedStock < 15 -> {
            StatusYellow to "Limited availability"
        }
        else -> {
            StatusGreen to "Highly likely available"
        }
    }

    // Secondary UI status badge colors
    val (statusColor, statusText) = when {
        med.currentStock == 0 -> {
            if (med.expectedRestockDays > 0) {
                StatusGray to "Expected in ${med.expectedRestockDays} Days"
            } else {
                StatusRed to TranslationManager.translate(TransKey.OUT_OF_STOCK, lang)
            }
        }
        med.currentStock < 10 -> {
            StatusOrange to TranslationManager.translate(TransKey.LIMITED_STOCK, lang)
        }
        med.currentStock < 30 -> {
            StatusYellow to TranslationManager.translate(TransKey.LOW_STOCK, lang)
        }
        else -> {
            StatusGreen to TranslationManager.translate(TransKey.AVAILABLE, lang)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (crowdReportsCount >= 2) StatusDarkRed.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = med.medName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = med.category,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Medicine Code: ${med.medCode}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Dynamic Status Badge
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusText.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Crowd Report mismatch community warning
            if (crowdReportsCount >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusDarkRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, StatusDarkRed, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusDarkRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = TranslationManager.translate(TransKey.COMMUNITY_WARNING, lang),
                                color = StatusDarkRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "$crowdReportsCount patients reported this item unavailable at counter index.",
                                color = StatusDarkRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Information details panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE WAREHOUSE",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${med.currentStock} Units",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (med.currentStock == 0) StatusRed else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "DOSAGE DEMAND",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${med.avgDosage} units/patient",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "TOTAL DEMAND",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${patientsAhead * med.avgDosage} units needed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Smart Prediction analysis Block
            Card(
                colors = CardDefaults.cardColors(containerColor = probabilityColor.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SMART PREDICTION ENGINE ANALYSIS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color = probabilityColor)
                        }
                        Text(
                            text = probabilityText.uppercase(),
                            color = probabilityColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "Calculated Stock Remaining on Arrival: ~$remainingCalculatedStock Units",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // If drug is in out-of-stock (Expected Restock DAYS and Notify ME)
            if (med.currentStock == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusOrange.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "expected supply".uppercase(),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (med.expectedRestockDays > 0) "Restocking forecast: ${med.expectedRestockDays} Days" else "No restock forecast recorded",
                            color = StatusOrange,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onNotifyToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (notificationActive) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary,
                            contentColor = if (notificationActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("notify_toggle_${med.medCode}")
                    ) {
                        Icon(
                            if (notificationActive) Icons.Default.Check else Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (notificationActive) "Enrolled 🔔" else "Notify Me",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Report Mismatch action line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Counter shelf empty or mismatch?",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "Report Medicine Out Only",
                    color = StatusRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable { onReportCounter() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("report_mismatch_${med.medCode}")
                )
            }
        }
    }
}

// 7. ADMIN / PHARMACIST CONTROL PANEL
@Composable
fun AdminPanelScreen(
    viewModel: HealthViewModel,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit
) {
    val selectedHosp by viewModel.selectedHospital.collectAsStateWithLifecycle()
    val allMeds by viewModel.allMedicines.collectAsStateWithLifecycle()
    val allCrowdReports by viewModel.allCrowdReports.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    
    // Auth profile fields from HealthViewModel persist session
    val adminEmpId by viewModel.adminEmployeeId.collectAsStateWithLifecycle()
    val adminName by viewModel.adminFullName.collectAsStateWithLifecycle()
    val adminRoleState by viewModel.adminRole.collectAsStateWithLifecycle()
    val adminHospState by viewModel.adminHospital.collectAsStateWithLifecycle()
    val adminPhoneState by viewModel.adminPhoneNumber.collectAsStateWithLifecycle()

    // Real-time Firestore notifications list
    val realtimeNotifs by viewModel.realtimeNotifications.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    var currentAdminTab by remember { mutableStateOf("dashboard") }

    var editingCabinetMedicine by remember { mutableStateOf<MedicineEntity?>(null) }
    var addingMedicineVisible by remember { mutableStateOf(false) }
    var managingPrescriptionsVisible by remember { mutableStateOf(false) }
    var viewingQrCodePrescription by remember { mutableStateOf<PrescriptionEntity?>(null) }

    val activeHospitalMeds = allMeds.filter { it.hospital == selectedHosp }

    // Queue status (Read-only for Admin / Pharmacist)
    val status by viewModel.activeHospitalStatus.collectAsStateWithLifecycle()
    val currentNowServing = status?.nowServing ?: 0

    val hospitals = listOf(
        "ESIC Hospital, Sanath Nagar",
        "ESIC Hospital, Basaidarapur",
        "ESIC Hospital, Peenya"
    )

    // Form inputs for Adding Medicine inside "add_medicine" tab
    var formName by remember { mutableStateOf("") }
    var formCodeSuffix by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("") }
    var formAvgDosage by remember { mutableStateOf("1") }
    var formStock by remember { mutableStateOf("100") }
    var formHospAssign by remember { mutableStateOf(selectedHosp) }
    var formIsAvailable by remember { mutableStateOf(true) }
    var formExpectedDays by remember { mutableStateOf("0") }
    var formErrorMsg by remember { mutableStateOf<String?>(null) }

    // Search & Filter controls for "update_stock" and "view_stock" tabs
    var stockSearchQuery by remember { mutableStateOf("") }
    var viewSearchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf("All") }
    var activeStatusFilter by remember { mutableStateOf("All") }

    // Custom notification broadcast simulations controls
    var targetEsicInput by remember { mutableStateOf("") }
    var customNotifMsgInput by remember { mutableStateOf("") }
    var broadcastStatusMsg by remember { mutableStateOf<String?>(null) }

    val formattedEmpId = adminEmpId ?: "ESICADM001"
    val formattedName = adminName ?: "Dr. Ramesh Kumar"
    val formattedRole = adminRoleState ?: "Pharmacist"
    val formattedHosp = adminHospState ?: "ESIC Hospital, Sanath Nagar"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- ADMIN HEADER BANNER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentAdminTab != "dashboard") {
                    IconButton(onClick = { currentAdminTab = "dashboard" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return to Dashboard", tint = TealPrimary)
                    }
                } else {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                    }
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        text = when (currentAdminTab) {
                            "dashboard" -> "ESIC Admin Desk"
                            "add_medicine" -> "Register Medicine"
                            "update_stock" -> "Stock Control Center"
                            "view_stock" -> "Realtime Inventory Feed"
                            "low_stock" -> "Low Stock Monitor"
                            "recent_updates" -> "Timeline & Activities"
                            "notifications" -> "FCM Dispatch History"
                            "analytics" -> "Stock Metrics Analytics"
                            "profile" -> "Corporate Profile"
                            else -> "ESIC Portal"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TealPrimary
                    )
                    Text(
                        text = "Authorized Session • $formattedEmpId",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LanguageDropdown(lang = lang, onLanguageChange = onLanguageChange)
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        viewModel.logoutAdmin()
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("pharmacist_sign_out_btn")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Deregister Session", tint = StatusRed)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- HOSPITAL CENTER SECTOR ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ACTIVE CONTROL CENTER STATION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = TealPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hospitals.forEach { hosp ->
                        val selected = selectedHosp == hosp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) TealPrimary else Color.White)
                                .border(1.dp, if (selected) Color.Transparent else Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSelectedHospital(hosp) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (hosp) {
                                    "ESIC Hospital, Sanath Nagar" -> "Sanath Nagar"
                                    "ESIC Hospital, Basaidarapur" -> "Basaidarapur"
                                    else -> "Peenya"
                                },
                                color = if (selected) Color.White else Color.DarkGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // --- SUB-PAGE ROUTING PANEL ---
        Box(modifier = Modifier.weight(1f)) {
            when (currentAdminTab) {
                "dashboard" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Profile greeting banner (PART 1 & 2)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Welcome, $formattedName",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "$formattedRole • $formattedHosp",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "🔒 Persistent Authorized Session Mode",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }

                        // Patient Queue window feed (PART 11 (strict read-only))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = borderStroke(Color.LightGray),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Patient Dispensation Query Token",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        "Realtime Synchronized Feed",
                                        fontSize = 10.sp,
                                        color = TealPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = "TOKEN #$currentNowServing",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = TealPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "PRIMARY OPERATIONS DESK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // 8-Card Layout Desk
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DashboardFeatureCard(
                                    title = "Add New Medicine",
                                    desc = "Seed new drugs",
                                    icon = Icons.Default.Add,
                                    badgeColor = TealPrimary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "add_medicine" }
                                )
                                DashboardFeatureCard(
                                    title = "Update Stock",
                                    desc = "Adjust physical stock",
                                    icon = Icons.Default.Edit,
                                    badgeColor = StatusOrange,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "update_stock" }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DashboardFeatureCard(
                                    title = "Current Stock",
                                    desc = "Inventory feeds",
                                    icon = Icons.Default.Inventory,
                                    badgeColor = StatusGreen,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "view_stock" }
                                )
                                DashboardFeatureCard(
                                    title = "Low Stock Alerts",
                                    desc = "${allMeds.count { it.currentStock < 20 }} warnings",
                                    icon = Icons.Default.Warning,
                                    badgeColor = StatusRed,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "low_stock" }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DashboardFeatureCard(
                                    title = "Recently Updated",
                                    desc = "Check activities timeline",
                                    icon = Icons.Default.History,
                                    badgeColor = Color.Blue,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "recent_updates" }
                                )
                                DashboardFeatureCard(
                                    title = "FCM History",
                                    desc = "${realtimeNotifs.size} records",
                                    icon = Icons.Default.Notifications,
                                    badgeColor = TealPrimary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "notifications" }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DashboardFeatureCard(
                                    title = "Analytics",
                                    desc = "Realtime visual metrics",
                                    icon = Icons.Default.BarChart,
                                    badgeColor = TealPrimary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "analytics" }
                                )
                                DashboardFeatureCard(
                                    title = "Profile Section",
                                    desc = "Assigned center ID",
                                    icon = Icons.Default.Person,
                                    badgeColor = Color.Gray,
                                    modifier = Modifier.weight(1f),
                                    onClick = { currentAdminTab = "profile" }
                                )
                            }
                        }
                    }
                }

                "add_medicine" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderStroke(Color.LightGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("REGISTER NEW DRUG SECURELY", fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 12.sp)

                                OutlinedTextField(
                                    value = formName,
                                    onValueChange = { formName = it },
                                    label = { Text("Medicine Trade / Chemical Name") },
                                    placeholder = { Text("e.g. Paracetamol 500mg") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = formCodeSuffix,
                                    onValueChange = { formCodeSuffix = it },
                                    label = { Text("Medicine Unique Suffix ID Key") },
                                    placeholder = { Text("e.g. PCM-500") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = formCategory,
                                    onValueChange = { formCategory = it },
                                    label = { Text("Classification Category") },
                                    placeholder = { Text("e.g. Analgesic, Cardiovascular") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = formStock,
                                        onValueChange = { formStock = it },
                                        label = { Text("Initial Stock") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = formAvgDosage,
                                        onValueChange = { formAvgDosage = it },
                                        label = { Text("Avg. Dosage Indx") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                OutlinedTextField(
                                    value = formExpectedDays,
                                    onValueChange = { formExpectedDays = it },
                                    label = { Text("Expected Restock forecast (Days)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Immediate Available in Store checklist", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Switch(checked = formIsAvailable, onCheckedChange = { formIsAvailable = it })
                                }

                                if (formErrorMsg != null) {
                                    Text(formErrorMsg!!, color = StatusRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val stock = formStock.toIntOrNull()
                                        val dosage = formAvgDosage.toIntOrNull()
                                        val days = formExpectedDays.toIntOrNull()
                                        
                                        if (formName.isBlank() || formCodeSuffix.isBlank() || formCategory.isBlank() || stock == null || dosage == null || days == null) {
                                            formErrorMsg = "Please verify all numerical/text inputs are completed."
                                        } else {
                                            val fullCode = "${formCodeSuffix.trim().uppercase()}-${formHospAssign.take(15).replace(" ", "")}"
                                            val duplicate = allMeds.any { it.medCode == fullCode }
                                            if (duplicate) {
                                                formErrorMsg = "duplicate Code! A drug with identifier $fullCode already exists."
                                            } else {
                                                val entity = MedicineEntity(
                                                    medCode = fullCode,
                                                    medName = formName,
                                                    currentStock = stock,
                                                    avgDosage = dosage,
                                                    expectedRestockDays = days,
                                                    hospital = formHospAssign,
                                                    isAvailable = formIsAvailable,
                                                    category = formCategory
                                                )
                                                viewModel.addNewMedicine(entity)
                                                viewModel.addSimulatedNotification("📦 REGISTERED: successfully synchronized new medication $formName ($fullCode).")
                                                
                                                // Clear entries
                                                formName = ""
                                                formCodeSuffix = ""
                                                formCategory = ""
                                                formStock = "100"
                                                formAvgDosage = "1"
                                                formExpectedDays = "0"
                                                formErrorMsg = null
                                                currentAdminTab = "dashboard"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("Commit & Synchronize Cloud Inventory", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "update_stock" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stockSearchQuery,
                            onValueChange = { stockSearchQuery = it },
                            label = { Text("Search medicine by name or code to update stock") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val filteredUpdateMeds = activeHospitalMeds.filter {
                            it.medName.contains(stockSearchQuery, ignoreCase = true) ||
                            it.medCode.contains(stockSearchQuery, ignoreCase = true)
                        }

                        if (filteredUpdateMeds.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No drugs found matching query.", color = Color.Gray, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredUpdateMeds) { med ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { editingCabinetMedicine = med },
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = borderStroke(Color.LightGray)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(med.medName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("Code: ${med.medCode}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = "${med.currentStock} Units",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 12.sp,
                                                    color = if (med.currentStock == 0) StatusRed else TealPrimary
                                                )
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Stock", tint = TealPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "view_stock" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = viewSearchQuery,
                            onValueChange = { viewSearchQuery = it },
                            placeholder = { Text("Filter items dynamically...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Chips
                        val categories = listOf("All", "Analgesic", "Cardiovascular", "Antibiotic", "Inhaler")
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val active = cat == activeCategoryFilter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(if (active) TealPrimary else Color.LightGray.copy(alpha = 0.5f))
                                        .clickable { activeCategoryFilter = cat }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(cat, color = if (active) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Stock Condition chips
                        val conditions = listOf("All Stock", "Low Stock (< 20)", "Out of Stock (0)")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            conditions.forEach { cond ->
                                val active = cond == activeStatusFilter
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(if (active) TealPrimary else Color.LightGray.copy(alpha = 0.3f))
                                        .clickable { activeStatusFilter = cond }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cond, color = if (active) Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        val viewFilteredMeds = activeHospitalMeds.filter {
                            (it.medName.contains(viewSearchQuery, ignoreCase = true) || it.medCode.contains(viewSearchQuery, ignoreCase = true)) &&
                            (activeCategoryFilter == "All" || it.category.contains(activeCategoryFilter, ignoreCase = true)) &&
                            (when (activeStatusFilter) {
                                "Low Stock (< 20)" -> it.currentStock in 1..19
                                "Out of Stock (0)" -> it.currentStock == 0
                                else -> true
                            })
                        }

                        if (viewFilteredMeds.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No medicines correspond to selected filters.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(viewFilteredMeds) { med ->
                                    val isLow = med.currentStock in 1..19
                                    val isOut = med.currentStock == 0
                                    
                                    val borderColor = when {
                                        isOut -> StatusRed
                                        isLow -> StatusOrange
                                        else -> Color.LightGray
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(med.medName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Spacer(Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                when {
                                                                    isOut -> StatusRed.copy(alpha = 0.15f)
                                                                    isLow -> StatusOrange.copy(alpha = 0.15f)
                                                                    else -> StatusGreen.copy(alpha = 0.15f)
                                                                },
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = when {
                                                                isOut -> "OUT OF STOCK ❌"
                                                                isLow -> "LOW STOCK ⚠️"
                                                                else -> "HEALTHY STOCK ✅"
                                                            },
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when {
                                                                isOut -> StatusRed
                                                                isLow -> StatusOrange
                                                                else -> StatusGreen
                                                            }
                                                        )
                                                    }
                                                }
                                                Text("Unique ID: ${med.medCode} • Category: ${med.category}", fontSize = 10.sp, color = Color.Gray)
                                                if (isOut) {
                                                    Text("Expected Restock forecast: ${med.expectedRestockDays} days", fontSize = 10.sp, color = StatusOrange, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Text(
                                                text = "${med.currentStock} Units",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = when {
                                                    isOut -> StatusRed
                                                    isLow -> StatusOrange
                                                    else -> StatusGreen
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "low_stock" -> {
                    val lowMedsList = activeHospitalMeds.filter { it.currentStock < 20 }
                    
                    if (lowMedsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("All registered stocks are within healthy thresholds! Good work.", color = StatusGreen, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(lowMedsList) { med ->
                                val reportsForMed = allCrowdReports.filter { it.medCode == med.medCode }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.05f)),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, StatusRed)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(med.medName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.DarkGray)
                                                Text("Code: ${med.medCode}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Text(
                                                "${med.currentStock} Units",
                                                color = StatusRed,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            )
                                        }

                                        if (reportsForMed.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "⚠️ Patients triggered empty counter alerts: ${reportsForMed.size} times!",
                                                color = StatusRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(Modifier.height(8.dp))
                                        
                                        // Replenish action button
                                        Button(
                                            onClick = {
                                                viewModel.updateMedicineStock(med.medCode, med.currentStock + 50, true, 0)
                                                viewModel.addSimulatedNotification("⚡ Replenish Quick Task: Added 50 units physical stocks of ${med.medName}.")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Quick REPLENISH (+50 Units)", fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "recent_updates" -> {
                    // Modern Circle Time Tracker timeline layout
                    val recentlySeededTimeline = allMeds.sortedBy { it.currentStock }.take(10)
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentlySeededTimeline) { med ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Bullets
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(TealPrimary, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(40.dp)
                                            .background(Color.LightGray)
                                    )
                                }
                                Column {
                                    Text(
                                        text = med.medName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text("Status update on ${med.hospital}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Synchronized count: ${med.currentStock} Units available", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "notifications" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Simulated notifications broadcast card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderStroke(Color.LightGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("BROADCAST MANUAL REALTIME NOTIFICATION", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TealPrimary)
                                
                                OutlinedTextField(
                                    value = targetEsicInput,
                                    onValueChange = { targetEsicInput = it },
                                    label = { Text("Target Patient ESIC Number") },
                                    placeholder = { Text("e.g. 1234567890") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = customNotifMsgInput,
                                    onValueChange = { customNotifMsgInput = it },
                                    label = { Text("Broadcast Alert Message") },
                                    placeholder = { Text("Your medicine is available at the counter.") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (targetEsicInput.isBlank() || customNotifMsgInput.isBlank()) {
                                            broadcastStatusMsg = "Please populate all fields."
                                        } else {
                                            coroutineScope.launch {
                                                val fs = viewModel.firebaseSyncManager.firestore
                                                if (fs != null) {
                                                    try {
                                                        val nData = mapOf(
                                                            "esic_number" to targetEsicInput.trim(),
                                                            "medicine_code" to "MANUAL",
                                                            "medicine_name" to "Broadcast Msg",
                                                            "hospital" to selectedHosp,
                                                            "message" to customNotifMsgInput,
                                                            "timestamp" to System.currentTimeMillis(),
                                                            "read_status" to false
                                                        )
                                                        fs.collection("notifications").add(nData)
                                                        viewModel.addSimulatedNotification("📢 Manual Notification Dispatched to ESIC #$targetEsicInput")
                                                        broadcastStatusMsg = "Dispatched and synchronized successfully!"
                                                        targetEsicInput = ""
                                                        customNotifMsgInput = ""
                                                    } catch (e: Exception) {
                                                        broadcastStatusMsg = "Failed to dispatch: ${e.message}"
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Broadcast Alert Now")
                                }

                                if (broadcastStatusMsg != null) {
                                    Text(broadcastStatusMsg!!, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                                }
                            }
                        }

                        // Notifications Dispatch history logs (PART 7)
                        Text("REALTIME DISPATCHED LOGS HISTORY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)

                        if (realtimeNotifs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No recent notifications recorded in live database yet.", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(realtimeNotifs) { notif ->
                                    val msg = notif["message"] as? String ?: ""
                                    val targetEsicNum = notif["esic_number"] as? String ?: ""
                                    val timestamp = notif["timestamp"] as? Long ?: System.currentTimeMillis()
                                    
                                    val formattedTime = try {
                                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                        sdf.format(java.util.Date(timestamp))
                                    } catch (e: Exception) {
                                        "Just now"
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = borderStroke(Color.LightGray)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("TO ESIC: $targetEsicNum", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
                                                Text(formattedTime, fontSize = 9.sp, color = Color.Gray)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(msg, fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "analytics" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val totalMeds = allMeds.size
                        val lowMeds = allMeds.count { it.currentStock in 1..19 }
                        val outMeds = allMeds.count { it.currentStock == 0 }
                        val healthyMeds = totalMeds - lowMeds - outMeds

                        // Visual grid metrics
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AnalyticsMetricCard(title = "Total Medicines", count = "$totalMeds Items", color = TealPrimary, modifier = Modifier.weight(1f))
                                AnalyticsMetricCard(title = "Healthy Reserve", count = "$healthyMeds Items", color = StatusGreen, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AnalyticsMetricCard(title = "Low Reserves", count = "$lowMeds Items", color = StatusOrange, modifier = Modifier.weight(1f))
                                AnalyticsMetricCard(title = "Out of Stock", count = "$outMeds Items", color = StatusRed, modifier = Modifier.weight(1f))
                            }
                        }

                        // Custom Drawn Stock distribution ProgressBar Chart
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderStroke(Color.LightGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("ESTIMATED INVENTORY DISTRIBUTION INDEX", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TealPrimary)

                                if (totalMeds > 0) {
                                    val healthyPct = healthyMeds.toFloat() / totalMeds
                                    val lowPct = lowMeds.toFloat() / totalMeds
                                    val outPct = outMeds.toFloat() / totalMeds

                                    // Custom visual distribution row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        if (healthyMeds > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(healthyPct)
                                                    .fillMaxHeight()
                                                    .background(StatusGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${(healthyPct * 100).toInt()}%", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (lowMeds > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(lowPct)
                                                    .fillMaxHeight()
                                                    .background(StatusOrange),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${(lowPct * 100).toInt()}%", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (outMeds > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(outPct)
                                                    .fillMaxHeight()
                                                    .background(StatusRed),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${(outPct * 100).toInt()}%", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Indicators list legend
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LegendLabel(label = "Healthy", color = StatusGreen)
                                        LegendLabel(label = "Low Stock", color = StatusOrange)
                                        LegendLabel(label = "Out of Stock", color = StatusRed)
                                    }
                                }
                            }
                        }

                        // Most Requested Medicines Compiled List
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderStroke(Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("MOST CRITICAL COMPLAINT INDEX REDS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusRed)
                                
                                val complaintsList = allMeds.map { med ->
                                    val reports = allCrowdReports.count { it.medCode == med.medCode }
                                    med to reports
                                }.filter { it.second > 0 }.sortedByDescending { it.second }

                                if (complaintsList.isEmpty()) {
                                    Text("Excellent: No patient empty counter alerts are active in system.", fontSize = 11.sp, color = Color.Gray)
                                } else {
                                    complaintsList.take(5).forEach { (med, count) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(StatusRed, CircleShape)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(med.medName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            }
                                            Text("$count Complaints", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "profile" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderStroke(Color.LightGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("OFFICIAL ADMINISTRATIVE DELEGATION DETAIL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TealPrimary)
                                
                                ProfileMetaRow(label = "AUTHORIZED EMPLOYEE ID", value = formattedEmpId)
                                ProfileMetaRow(label = "FULL CLINICAL NAME", value = formattedName)
                                ProfileMetaRow(label = "DELEGATION ROLE", value = formattedRole)
                                ProfileMetaRow(label = "ASSIGNED PRIMARY HOSPITAL", value = formattedHosp)
                                ProfileMetaRow(label = "DEPT. EMERGENCY CONTACT", value = if(adminPhoneState.isNullOrBlank()) "9876501234" else adminPhoneState!!)
                                ProfileMetaRow(label = "REGULATORY PRIVILEGES", value = "CLOUD INVENTORY READ/WRITE ACTIVE")
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.logoutAdmin()
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("DEREGISTER SECURITY SESSION", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    // =====================================
    // PORTED MODALS / DIALOGS RETAINED & POLISHED
    // =====================================

    // 1. PHARMACIST MEDICINE DETAILS EDIT DRAWER Dialog
    editingCabinetMedicine?.let { med ->
        var stockInput by remember { mutableStateOf(med.currentStock.toString()) }
        var expectedDaysInput by remember { mutableStateOf(med.expectedRestockDays.toString()) }
        var isAvailableInput by remember { mutableStateOf(med.isAvailable) }

        Dialog(onDismissRequest = { editingCabinetMedicine = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Warehouse Stock Adjuster",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TealPrimary
                    )

                    Text(med.medName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TealPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick step decrease
                        Button(
                            onClick = {
                                val current = stockInput.toIntOrNull() ?: 0
                                stockInput = (current - 10).coerceAtLeast(0).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) { Text("-10") }

                        OutlinedTextField(
                            value = stockInput,
                            onValueChange = { stockInput = it },
                            label = { Text("Available Units") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Quick step increase
                        Button(
                            onClick = {
                                val current = stockInput.toIntOrNull() ?: 0
                                stockInput = (current + 10).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) { Text("+10") }
                    }

                    OutlinedTextField(
                        value = expectedDaysInput,
                        onValueChange = { expectedDaysInput = it },
                        label = { Text("Expected restock forecast count (days)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Store Visibility Status", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isAvailableInput,
                            onCheckedChange = { isAvailableInput = it }
                        )
                    }

                    // Clear warnings in single tap
                    Button(
                        onClick = {
                            viewModel.clearCrowdReports(med.medCode)
                            viewModel.addSimulatedNotification("🧹 Pharmacist replenished medical shelf of ${med.medName}. Cleared alerts.")
                            editingCabinetMedicine = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Replenish shelf / Clear warnings")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingCabinetMedicine = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }

                        Button(
                            onClick = {
                                val stock = stockInput.toIntOrNull() ?: 0
                                val days = expectedDaysInput.toIntOrNull() ?: 0
                                viewModel.updateMedicineStock(
                                    med.medCode,
                                    stock,
                                    isAvailableInput && stock > 0,
                                    days
                                )
                                viewModel.addSimulatedNotification("Saved adjusted ${med.medName} stock to $stock units.")
                                editingCabinetMedicine = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SAVE")
                        }
                    }
                }
            }
        }
    }
}

// Dialog helper elements
@Composable
fun DashboardFeatureCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke(Color.LightGray.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(badgeColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }

            Column {
                Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.DarkGray)
                Text(desc, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

@Composable
fun AnalyticsMetricCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke(Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(count, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun LegendLabel(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileMetaRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 0.5.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

// Utility: simple border helper
@Composable
fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
