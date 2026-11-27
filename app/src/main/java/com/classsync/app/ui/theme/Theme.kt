package com.classsync.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background        = PitchBlack,
    surface           = ObsidianGray,
    surfaceVariant    = Graphite,
    primary           = SageGreen,
    secondary         = WarmTaupe,
    error             = CoralRose,
    onBackground      = PureWhite,
    onSurface         = PureWhite,
    onSurfaceVariant  = MutedSilver,
    onPrimary         = PureWhite,
    onSecondary       = PureWhite,
    onError           = PureWhite,
    outline           = Graphite,
)

@Composable
fun ClassSyncTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
