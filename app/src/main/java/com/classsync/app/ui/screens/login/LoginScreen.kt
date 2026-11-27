package com.classsync.app.ui.screens.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.ui.theme.*

/**
 * Screen 0 — Google Sign-In / Welcome
 * Uses real Credential Manager sign-in via LoginViewModel.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Navigate on success
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            // Branding badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(listOf(SageGreen.copy(alpha = .6f), ObsidianGray))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.School,
                    contentDescription = null,
                    tint               = PureWhite,
                    modifier           = Modifier.size(48.dp)
                )
            }

            Text("ClassSync", style = MaterialTheme.typography.displayMedium, color = PureWhite)

            Text(
                "Attendance · Timetable · Assignments\nAll synced with your Google Sheet",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MutedSilver,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Error banner
            if (state is LoginUiState.Error) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralRose.copy(alpha = .15f)
                ) {
                    Text(
                        (state as LoginUiState.Error).message,
                        modifier  = Modifier.padding(12.dp),
                        style     = MaterialTheme.typography.labelSmall,
                        color     = CoralRose,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Google Sign-In button
            Button(
                onClick  = { viewModel.signIn(context as Activity) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor   = PitchBlack
                ),
                enabled  = state !is LoginUiState.Loading
            ) {
                if (state is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(20.dp),
                        color     = PitchBlack,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign in with Google", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                "By continuing you agree to ClassSync's Terms of Use.",
                style     = MaterialTheme.typography.labelSmall,
                color     = MutedSilver,
                textAlign = TextAlign.Center
            )
        }
    }
}
