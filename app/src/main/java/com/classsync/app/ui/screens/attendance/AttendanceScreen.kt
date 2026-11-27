package com.classsync.app.ui.screens.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.ui.theme.*

/**
 * Screen 1 — Class Attendance & Student Spotlight
 * Full roll-call UI with real ViewModel data, Mark All Present, and sync badge.
 */
@Composable
fun AttendanceScreen(
    onOpenAnalytics: (Long) -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state       by viewModel.uiState.collectAsState()
    val percentages by viewModel.percentages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .statusBarsPadding()
    ) {
        // ── Top Header ──────────────────────────────────────────────────────
        TopBar(
            className    = state.className,
            studentCount = state.students.size,
            syncState    = state.syncState,
            onMarkAll    = viewModel::markAllPresent
        )

        // ── Hero Student Spotlight ───────────────────────────────────────────
        if (state.students.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SageGreen)
                    Spacer(Modifier.height(8.dp))
                    Text("Loading students…", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                }
            }
        } else {
            state.currentStudent?.let { student ->
                HeroSpotlightCard(
                    student       = student,
                    attendancePct = percentages[student.id] ?: 88f,
                    onExpand      = { onOpenAnalytics(student.id) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Subject Filter Pills ─────────────────────────────────────────────
        SubjectFilterRow(
            subjects        = viewModel.subjects,
            selectedSubject = state.selectedSubject,
            onSelect        = viewModel::selectSubject
        )

        Spacer(Modifier.weight(1f))

        // ── Bottom: Student Carousel + Quick-Mark Controls ───────────────────
        if (state.students.isNotEmpty()) {
            StudentCarouselSection(
                students        = state.students,
                currentIndex    = state.currentIndex,
                onSelectStudent = viewModel::selectStudent,
                onPrevious      = viewModel::previousStudent,
                onNext          = viewModel::nextStudent,
                onMark          = viewModel::markAttendance
            )
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    className: String,
    studentCount: Int,
    syncState: SyncState,
    onMarkAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(className, style = MaterialTheme.typography.displayMedium, color = PureWhite)
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("$studentCount Students", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                SyncBadge(syncState)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Mark All Present shortcut
            IconButton(
                onClick  = onMarkAll,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SageGreen.copy(alpha = .2f))
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = "Mark All Present", tint = SageGreen)
            }
            IconButton(
                onClick  = {},
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Graphite)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = PureWhite)
            }
        }
    }
}

@Composable
private fun SyncBadge(syncState: SyncState) {
    val (label, color) = when (syncState) {
        SyncState.SYNCING        -> "Syncing…" to WarmTaupe
        SyncState.SYNCED         -> "Synced 🟢" to SageGreen
        SyncState.OFFLINE_QUEUED -> "Offline Queued" to CoralRose
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = .15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = color
        )
    }
}

@Composable
private fun HeroSpotlightCard(
    student: com.classsync.app.data.local.entity.StudentEntity,
    attendancePct: Float,
    onExpand: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Avatar monogram area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Graphite, PitchBlack))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                student.name.take(2).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = PureWhite
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, PitchBlack),
                            startY = 80f
                        )
                    )
            )
        }

        // Bottom sheet overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(ObsidianGray)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(student.name, style = MaterialTheme.typography.titleLarge, color = PureWhite)
                    Text("Roll No: ${student.rollNumber}", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                }
                IconButton(onClick = onExpand) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "View Analytics", tint = MutedSilver)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val pctColor = when {
                    attendancePct >= 85 -> SageGreen
                    attendancePct >= 75 -> WarmTaupe
                    else                -> CoralRose
                }
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value    = "48",
                    unit     = "Classes",
                    pct      = "${attendancePct.toInt()}%",
                    pctColor = pctColor
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value    = "3",
                    unit     = "Tasks",
                    pct      = "75%",
                    pctColor = CoralRose
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    value: String,
    unit: String,
    pct: String,
    pctColor: Color
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Graphite) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.displayLarge, color = PureWhite)
            Text(unit,  style = MaterialTheme.typography.labelSmall,   color = MutedSilver)
            Text(pct,   style = MaterialTheme.typography.bodyMedium,   color = pctColor)
        }
    }
}

@Composable
private fun SubjectFilterRow(
    subjects: List<String>,
    selectedSubject: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subjects.size) { idx ->
            val subject    = subjects[idx]
            val isSelected = subject == selectedSubject
            Surface(
                onClick  = { onSelect(subject) },
                shape    = RoundedCornerShape(50),
                color    = if (isSelected) SageGreen.copy(alpha = .2f) else Graphite,
                border   = if (isSelected) BorderStroke(1.dp, SageGreen) else BorderStroke(1.dp, Graphite)
            ) {
                Text(
                    subject,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (isSelected) SageGreen else MutedSilver
                )
            }
        }
    }
}

@Composable
private fun StudentCarouselSection(
    students: List<com.classsync.app.data.local.entity.StudentEntity>,
    currentIndex: Int,
    onSelectStudent: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMark: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(ObsidianGray)
            .padding(vertical = 16.dp)
    ) {
        // Progress indicator
        val progress = if (students.isNotEmpty()) (currentIndex + 1).toFloat() / students.size else 0f
        LinearProgressIndicator(
            progress    = { progress },
            modifier    = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(2.dp).clip(RoundedCornerShape(50)),
            color       = SageGreen,
            trackColor  = Graphite
        )
        Spacer(Modifier.height(10.dp))

        // Student counter
        Text(
            "${currentIndex + 1} / ${students.size}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style    = MaterialTheme.typography.labelSmall,
            color    = MutedSilver
        )

        Spacer(Modifier.height(8.dp))

        // Carousel row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = PureWhite)
            }
            LazyRow(
                modifier              = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding        = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(students) { idx, student ->
                    val isActive = idx == currentIndex
                    val scale by animateFloatAsState(
                        targetValue    = if (isActive) 1.1f else 0.85f,
                        animationSpec  = tween(200),
                        label          = "avatar_scale"
                    )
                    val borderColor by animateColorAsState(
                        targetValue   = if (isActive) PureWhite else Graphite,
                        animationSpec = tween(200),
                        label         = "avatar_border"
                    )
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, borderColor, CircleShape)
                            .background(Graphite)
                            .clickable { onSelectStudent(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(student.name.take(1), style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                    }
                }
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = PureWhite)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick-mark P / A / L
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AttendanceMarkButton("P", "Present", SageGreen,  Modifier.weight(1f), onMark)
            AttendanceMarkButton("A", "Absent",  CoralRose,  Modifier.weight(1f), onMark)
            AttendanceMarkButton("L", "Late",    WarmTaupe,  Modifier.weight(1f), onMark)
        }
    }
}

@Composable
private fun AttendanceMarkButton(
    code: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Button(
        onClick = { onClick(code) },
        modifier = modifier.height(48.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = .2f),
            contentColor   = color
        )
    ) {
        Text(
            "$code  $label",
            style    = MaterialTheme.typography.bodyMedium,
            color    = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
