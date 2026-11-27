package com.classsync.app.ui.screens.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Screen 2 — Attendance Analytics & Sheet Sync
 * Driven by AnalyticsViewModel — real Room DB data with graceful demo fallback.
 */
@Composable
fun AnalyticsScreen(
    studentId: Long,
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Graphite)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Attendance Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
                Text(
                    "Semester 5 • Google Sheet Live",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSilver
                )
            }
            IconButton(
                onClick  = {},
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Graphite)
            ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = PureWhite)
            }
        }

        // ── Loading / Content ────────────────────────────────────────────────
        if (state.isLoading) {
            Box(
                modifier            = Modifier.fillMaxWidth().height(300.dp),
                contentAlignment    = Alignment.Center
            ) {
                CircularProgressIndicator(color = SageGreen)
            }
        } else {
            // ── Radial Ring Chart ────────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                RadialTickChart(
                    presentPct = state.presentPct,
                    latePct    = state.latePct,
                    absentPct  = state.absentPct,
                    modifier   = Modifier.size(240.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${state.totalClasses}",
                        style = MaterialTheme.typography.displayLarge,
                        color = PureWhite
                    )
                    Text("Classes", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                    Text(
                        "${state.overallPctInt}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = when {
                            state.overallPctInt >= 85 -> SageGreen
                            state.overallPctInt >= 75 -> WarmTaupe
                            else                       -> CoralRose
                        }
                    )
                }
            }

            // ── Attendance Warning Banner ────────────────────────────────────
            if (state.overallPctInt < 75) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CoralRose.copy(alpha = .15f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRose, modifier = Modifier.size(18.dp))
                        Text(
                            "Attendance below 75% — at risk of detainment!",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralRose
                        )
                    }
                }
            }

            // ── Breakdown Sheet ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(ObsidianGray)
                    .padding(20.dp)
            ) {
                Text(
                    "${state.totalClasses} Total Lectures",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
                Spacer(Modifier.height(16.dp))

                AttendanceRow(
                    emoji    = "🟢",
                    label    = "Present",
                    fraction = "${state.presentCount}/${state.totalClasses}",
                    pct      = "${(state.presentPct * 100).toInt()}%",
                    color    = SageGreen
                )
                HorizontalDivider(color = Graphite, modifier = Modifier.padding(vertical = 8.dp))
                AttendanceRow(
                    emoji    = "⚪",
                    label    = "Late / Excused",
                    fraction = "${state.lateCount}/${state.totalClasses}",
                    pct      = "${(state.latePct * 100).toInt()}%",
                    color    = CreamWhite
                )
                HorizontalDivider(color = Graphite, modifier = Modifier.padding(vertical = 8.dp))
                AttendanceRow(
                    emoji    = "🔴",
                    label    = "Absent",
                    fraction = "${state.absentCount}/${state.totalClasses}",
                    pct      = "${(state.absentPct * 100).toInt()}%",
                    color    = CoralRose
                )

                Spacer(Modifier.height(24.dp))
                Text("Per Subject", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                Spacer(Modifier.height(12.dp))

                state.subjectBreakdowns.forEach { breakdown ->
                    SubjectProgressRow(
                        subject  = breakdown.subject,
                        subtitle = breakdown.subtitle,
                        pct      = breakdown.pct
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────

@Composable
private fun AttendanceRow(
    emoji: String,
    label: String,
    fraction: String,
    pct: String,
    color: Color
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(emoji)
            Column {
                Text(label,    style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                Text(fraction, style = MaterialTheme.typography.labelSmall,  color = MutedSilver)
            }
        }
        Text(pct, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun SubjectProgressRow(subject: String, subtitle: String, pct: Int) {
    val color = when {
        pct >= 85 -> SageGreen
        pct >= 75 -> WarmTaupe
        else      -> CoralRose
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.weight(1f)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Graphite),
                contentAlignment = Alignment.Center
            ) {
                Text(subject.take(2), style = MaterialTheme.typography.labelSmall, color = PureWhite)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    subject,
                    style   = MaterialTheme.typography.bodyMedium,
                    color   = PureWhite,
                    maxLines = 1
                )
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MutedSilver)
            }
        }
        // Mini progress bar + percentage
        Column(horizontalAlignment = Alignment.End) {
            Text("$pct%", style = MaterialTheme.typography.bodyMedium, color = color)
            LinearProgressIndicator(
                progress     = { pct / 100f },
                modifier     = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(50)),
                color        = color,
                trackColor   = Graphite
            )
        }
    }
}

/**
 * Custom Canvas — 60-tick radial gauge colour-coded by attendance ratios.
 */
@Composable
private fun RadialTickChart(
    presentPct: Float,
    latePct: Float,
    absentPct: Float,
    modifier: Modifier = Modifier
) {
    val totalTicks   = 60
    val presentTicks = (presentPct * totalTicks).toInt()
    val lateTicks    = (latePct    * totalTicks).toInt()

    Canvas(modifier = modifier) {
        val radius      = size.minDimension / 2f - 20f
        val cx          = size.width  / 2f
        val cy          = size.height / 2f
        val tickLength  = 18f
        val strokeWidth = 6f

        for (i in 0 until totalTicks) {
            val angleDeg = (i * 6f) - 90f
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            val cosA     = cos(angleRad)
            val sinA     = sin(angleRad)

            val color = when {
                i < presentTicks                     -> SageGreen
                i < presentTicks + lateTicks         -> CreamWhite
                else                                  -> CoralRose
            }

            drawLine(
                color       = color,
                start       = Offset(cx + (radius - tickLength) * cosA, cy + (radius - tickLength) * sinA),
                end         = Offset(cx + radius * cosA,                 cy + radius * sinA),
                strokeWidth = strokeWidth,
                cap         = StrokeCap.Round
            )
        }
    }
}
