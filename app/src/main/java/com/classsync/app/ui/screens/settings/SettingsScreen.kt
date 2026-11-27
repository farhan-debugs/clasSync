package com.classsync.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.ui.theme.*

/**
 * Settings Screen — class name, sheet link, account, sign-out.
 */
@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    onRelinkSheet: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showClassNameDialog by remember { mutableStateOf(false) }
    var showSignOutDialog   by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            "Settings",
            style    = MaterialTheme.typography.displayMedium,
            color    = PureWhite,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        )

        // ── Account Card ───────────────────────────────────────────────────
        SettingsSectionHeader("Account")
        SettingsCard {
            AccountRow(
                name  = state.userName.ifBlank { "Student" },
                email = state.userEmail.ifBlank { "Not signed in" }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Class Settings ─────────────────────────────────────────────────
        SettingsSectionHeader("Class")
        SettingsCard {
            SettingsRow(
                icon    = Icons.Default.Class,
                label   = "Class Name",
                value   = state.className,
                onClick = { showClassNameDialog = true }
            )
            HorizontalDivider(color = Graphite)
            SettingsRow(
                icon    = Icons.Default.TableChart,
                label   = "Linked Google Sheet",
                value   = if (state.linkedSheetId.isNullOrBlank()) "Not linked" else "✅ Linked",
                onClick = onRelinkSheet,
                tint    = if (state.linkedSheetId.isNullOrBlank()) CoralRose else SageGreen
            )
            if (!state.linkedSheetId.isNullOrBlank()) {
                HorizontalDivider(color = Graphite)
                SettingsRow(
                    icon    = Icons.Default.LinkOff,
                    label   = "Unlink Sheet",
                    value   = "",
                    onClick = viewModel::unlinkSheet,
                    tint    = WarmTaupe
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Notifications ──────────────────────────────────────────────────
        SettingsSectionHeader("Notifications")
        SettingsCard {
            SettingsRow(
                icon  = Icons.Default.Notifications,
                label = "Assignment Reminders",
                value = ""
            ) { /* open notification settings via Intent */ }
            HorizontalDivider(color = Graphite)
            SettingsRow(
                icon  = Icons.Default.WbSunny,
                label = "Daily Morning Digest",
                value = ""
            ) { /* configure daily digest time */ }
        }

        Spacer(Modifier.height(12.dp))

        // ── Danger Zone ────────────────────────────────────────────────────
        SettingsSectionHeader("Account")
        SettingsCard {
            SettingsRow(
                icon    = Icons.Default.Logout,
                label   = "Sign Out",
                value   = "",
                onClick = { showSignOutDialog = true },
                tint    = CoralRose
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    if (showClassNameDialog) {
        ClassNameDialog(
            current   = state.className,
            onDismiss = { showClassNameDialog = false },
            onConfirm = { name ->
                viewModel.updateClassName(name)
                showClassNameDialog = false
            }
        )
    }
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor   = ObsidianGray,
            title   = { Text("Sign Out?", color = PureWhite) },
            text    = { Text("Your local data will be preserved.", color = MutedSilver) },
            confirmButton = {
                TextButton(onClick = { viewModel.signOut(onSignOut) }) {
                    Text("Sign Out", color = CoralRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }
}

// ── Reusable components ────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style    = MaterialTheme.typography.labelSmall,
        color    = MutedSilver,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianGray,
        content = { Column(content = content) }
    )
}

@Composable
private fun AccountRow(name: String, email: String) {
    Row(
        modifier            = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(Graphite),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = PureWhite)
        }
        Column {
            Text(name,  style = MaterialTheme.typography.bodyMedium, color = PureWhite)
            Text(email, style = MaterialTheme.typography.labelSmall,  color = MutedSilver)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit = {},
    tint: androidx.compose.ui.graphics.Color = PureWhite
) {
    Surface(
        onClick = onClick,
        color   = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = PureWhite, modifier = Modifier.weight(1f))
            if (value.isNotBlank()) {
                Text(value, style = MaterialTheme.typography.labelSmall, color = MutedSilver)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ClassNameDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = ObsidianGray,
        title   = { Text("Class Name", color = PureWhite) },
        text    = {
            OutlinedTextField(
                value         = text,
                onValueChange = { text = it },
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SageGreen,
                    unfocusedBorderColor = Graphite,
                    focusedTextColor     = PureWhite,
                    unfocusedTextColor   = PureWhite
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) {
                Text("Save", color = SageGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedSilver) }
        }
    )
}
