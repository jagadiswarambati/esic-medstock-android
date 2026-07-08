package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.translation.AppLanguage

@Composable
fun ESICHeaderSection(
    activeTab: String,
    beneficiaryName: String?,
    lang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onBackToHome: () -> Unit,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp) // Reduced to 52dp to achieve perfect vertical compact space
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LEFT: Back Button or Official ESIC Logo
            if (activeTab != "home") {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("dashboard_back_to_home")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp) // Refined to 36dp for accurate baseline alignment
                        .shadow(elevation = 1.dp, shape = CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.esic_logo),
                        contentDescription = "Official ESIC Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // CENTER: Professional spacing for the Title Block
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ESIC MedStock",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp, // Slightly more compact
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = if (beneficiaryName != null) "Beneficiary: $beneficiaryName" else "ESIC Smart Card Access",
                    fontSize = 10.sp, // Compact and readable
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // RIGHT: Theme Toggle & Compact Language Selector in a Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ThemeToggle(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange
                )
                
                LanguageDropdown(
                    lang = lang,
                    onLanguageChange = onLanguageChange,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}

@Composable
fun ThemeToggle(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = when (themeMode) {
                    "light" -> Icons.Default.LightMode
                    "dark" -> Icons.Default.DarkMode
                    else -> Icons.Default.Settings
                },
                contentDescription = "Select Theme",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Light Theme", fontSize = 13.sp) },
                onClick = {
                    onThemeModeChange("light")
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Light Mode",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Dark Theme", fontSize = 13.sp) },
                onClick = {
                    onThemeModeChange("dark")
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Dark Mode",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("System Default", fontSize = 13.sp) },
                onClick = {
                    onThemeModeChange("system")
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System Default",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
