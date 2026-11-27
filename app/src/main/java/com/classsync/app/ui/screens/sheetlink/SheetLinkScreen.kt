package com.classsync.app.ui.screens.sheetlink

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.ui.theme.*

/**
 * Sheet Link & Student Import Screen.
 * Guides the user through pasting a Google Sheet URL → validating access → importing roster.
 */
@Composable
fun SheetLinkScreen(
    onDone: () -> Unit,
    onSkip: () -> Unit,
    viewModel: SheetLinkViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.step) {
        if (state.step == SheetLinkStep.DONE) {
            kotlinx.coroutines.delay(1500)
            onDone()
        }
    }

    Box(
        modifier         = Modifier.fillMaxSize().background(PitchBlack).statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier                = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment     = Alignment.CenterHorizontally,
            verticalArrangement     = Arrangement.spacedBy(20.dp)
        ) {
            // Icon
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(SageGreen.copy(alpha = .2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, tint = SageGreen, modifier = Modifier.size(36.dp))
            }

            Text("Link Your Google Sheet", style = MaterialTheme.typography.displayMedium, color = PureWhite, textAlign = TextAlign.Center)
            Text(
                "Paste the URL of your attendance Google Sheet.\nClassSync will read the student roster from the \"Roster\" tab.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MutedSilver,
                textAlign = TextAlign.Center
            )

            // URL input
            AnimatedVisibility(state.step == SheetLinkStep.INPUT || state.step == SheetLinkStep.ERROR) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value         = state.spreadsheetUrl,
                        onValueChange = viewModel::onUrlChange,
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Google Sheets URL", color = MutedSilver) },
                        placeholder   = { Text("https://docs.google.com/spreadsheets/d/...", color = Graphite) },
                        isError       = state.step == SheetLinkStep.ERROR,
                        supportingText = state.error?.let { { Text(it, color = CoralRose) } },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = SageGreen,
                            unfocusedBorderColor = Graphite,
                            focusedTextColor     = PureWhite,
                            unfocusedTextColor   = PureWhite,
                            cursorColor          = SageGreen
                        ),
                        minLines      = 2,
                        maxLines      = 3
                    )
                    Button(
                        onClick  = viewModel::linkAndImport,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = SageGreen, contentColor = PureWhite)
                    ) {
                        Text("Link & Import Roster")
                    }
                    TextButton(onClick = onSkip) {
                        Text("Skip for now", color = MutedSilver)
                    }
                }
            }

            // Validating / Importing state
            AnimatedVisibility(
                state.step == SheetLinkStep.VALIDATING || state.step == SheetLinkStep.IMPORTING
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = SageGreen, strokeWidth = 3.dp)
                    Text(
                        if (state.step == SheetLinkStep.VALIDATING) "Validating access…"
                        else "Importing student roster…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSilver
                    )
                    if (state.sheetTitle.isNotBlank()) {
                        Text("📄 \"${state.sheetTitle}\"", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                    }
                }
            }

            // Success state
            AnimatedVisibility(state.step == SheetLinkStep.DONE) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(72.dp).clip(CircleShape).background(SageGreen.copy(alpha = .2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreen, modifier = Modifier.size(36.dp))
                    }
                    Text("Linked! 🎉", style = MaterialTheme.typography.displayMedium, color = PureWhite)
                    Text(
                        "\"${state.sheetTitle}\" connected.\n${state.studentCount} students imported.",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MutedSilver,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
