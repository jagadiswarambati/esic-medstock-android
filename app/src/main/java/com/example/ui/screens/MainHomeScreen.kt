package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainPatientHomeScreen(
    onOpenScanner: () -> Unit,
    onOpenIdEntry: () -> Unit,
    onOpenSubPage: (String) -> Unit,
    userName: String,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp), // Unified 16dp spacing under the header
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent 16dp grid-based section spacing
    ) {
        // Compact premium welcoming card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp), // Reduced height slightly, increased outer edges
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp)) // Improved icon-to-text spacing
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Namaste, $userName 👋",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(4.dp)) // Improved text-to-text spacing inside the card
                    Text(
                        text = "Welcome to ESIC Smart Pharmacy Care Portal.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // HERO ZONE - ONLY TWO PRIMARY ACTIONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Perfect 8dp sub-spacing
        ) {
            Text(
                text = "LOAD ACTIVE RX PRESCRIPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            PrescriptionActionCards(
                onOpenScanner = onOpenScanner,
                onOpenIdEntry = onOpenIdEntry,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // BOTTOM FEATURE SECTION: Adaptive Wellness Suite containing 6 static cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Perfect 8dp sub-spacing
        ) {
            Text(
                text = "ESIC WELLNESS SUITE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            if (isTablet) {
                // Expanded layouts: perfect 1 row of 6 columns side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        Triple("Queue Tracker", Icons.Default.FormatListNumbered, "Check counter position"),
                        Triple("Drug Notifications", Icons.Default.NotificationsActive, "Configure restock alerts"),
                        Triple("Prescription History", Icons.Default.History, "View historical issues"),
                        Triple("Crowd Reports", Icons.Default.ReportProblem, "Mismatches empty counters"),
                        Triple("Clinical Help Desk", Icons.Default.SupportAgent, "Helpline & 24/7 support"),
                        Triple("Wellness Centres", Icons.Default.LocalHospital, "Nearest ESIC clinics")
                    )
                    items.forEach { (title, icon, subtitle) ->
                        val route = when (title) {
                            "Queue Tracker" -> "queue_status"
                            "Drug Notifications" -> "medicine_alerts"
                            "Prescription History" -> "history"
                            "Crowd Reports" -> "crowd_reports"
                            "Clinical Help Desk" -> "help"
                            else -> "nearby_hospitals"
                        }
                        FeatureCardItem(
                            title = title,
                            icon = icon,
                            subtitle = subtitle,
                            onClick = { onOpenSubPage(route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Adaptive Mobile Layout: 3 Rows of 2 columns each. Perfect visibility, equal sizes, zero clipping.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCardItem("Queue Tracker", Icons.Default.FormatListNumbered, "Check counter position", onClick = { onOpenSubPage("queue_status") }, modifier = Modifier.weight(1f))
                        FeatureCardItem("Drug Notifications", Icons.Default.NotificationsActive, "Configure restock alerts", onClick = { onOpenSubPage("medicine_alerts") }, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCardItem("Prescription History", Icons.Default.History, "View historical issues", onClick = { onOpenSubPage("history") }, modifier = Modifier.weight(1f))
                        FeatureCardItem("Crowd Reports", Icons.Default.ReportProblem, "Mismatches empty counters", onClick = { onOpenSubPage("crowd_reports") }, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCardItem("Clinical Help Desk", Icons.Default.SupportAgent, "Helpline & 24/7 support", onClick = { onOpenSubPage("help") }, modifier = Modifier.weight(1f))
                        FeatureCardItem("Wellness Centres", Icons.Default.LocalHospital, "Nearest ESIC clinics", onClick = { onOpenSubPage("nearby_hospitals") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCardItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp), // Matches Action Card shape exactly (16.dp)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), // Matches Action Card border style
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Consistent shadows
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp) // Unified equal padding
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp, // Reduced slightly to avoid truncation
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
