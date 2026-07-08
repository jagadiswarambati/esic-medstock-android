package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ESICBottomNavigation(
    activeTab: String,
    onActiveTabChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Triple("home", Icons.Default.Home, "Home"),
        Triple("prescriptions", Icons.Default.ReceiptLong, "Prescriptions"),
        Triple("notifications", Icons.Default.Notifications, "Notifications"),
        Triple("profile", Icons.Default.Person, "Profile")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp), // Reduced height
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            navItems.forEach { (tabId, icon, label) ->

                val isSelected = activeTab == tabId

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onActiveTabChange(tabId) },
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected)
                                FontWeight.Bold
                            else
                                FontWeight.Medium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}