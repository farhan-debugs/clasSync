package com.classsync.app.ui.screens.timetable

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.data.local.entity.TimetableEntity
import com.classsync.app.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Screen 3 — Dynamic Timetable
 * Full CRUD: add, edit, delete class slots with inline swipe-to-delete.
 */
@Composable
fun TimetableScreen(viewModel: TimetableViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val now   = LocalTime.now()

    var showAddDialog  by remember { mutableStateOf(false) }
    var editingSlot    by remember { mutableStateOf<TimetableEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .statusBarsPadding()
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Timetable", style = MaterialTheme.typography.displayMedium, color = PureWhite)
            IconButton(
                onClick  = { showAddDialog = true },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SageGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class", tint = PureWhite)
            }
        }

        // ── Day Strip ────────────────────────────────────────────────────────
        DayStrip(selectedDay = state.selectedDay, onSelect = viewModel::selectDay)

        Spacer(Modifier.height(16.dp))

        // ── Slot List ────────────────────────────────────────────────────────
        if (state.slots.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No classes today 🎉", style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("+ Add a class", color = SageGreen)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.slots, key = { it.id }) { slot ->
                    val isLive = isLiveClass(slot, now)
                    TimetableCard(
                        slot    = slot,
                        isLive  = isLive,
                        onEdit  = { editingSlot = slot },
                        onDelete = { viewModel.deleteSlot(slot) }
                    )
                }
            }
        }
    }

    // ── Add/Edit Dialog ──────────────────────────────────────────────────────
    if (showAddDialog) {
        SlotDialog(
            title     = "Add Class",
            initial   = null,
            selectedDay = state.selectedDay,
            onDismiss = { showAddDialog = false },
            onConfirm = { slot ->
                viewModel.addSlot(slot)
                showAddDialog = false
            }
        )
    }

    editingSlot?.let { slot ->
        SlotDialog(
            title     = "Edit Class",
            initial   = slot,
            selectedDay = slot.dayOfWeek,
            onDismiss = { editingSlot = null },
            onConfirm = { updated ->
                viewModel.updateSlot(updated)
                editingSlot = null
            }
        )
    }
}

// ── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun DayStrip(selectedDay: Int, onSelect: (Int) -> Unit) {
    val days = listOf("M", "T", "W", "T", "F", "S")
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(days.size) { idx ->
            val dayNum   = idx + 1
            val isActive = dayNum == selectedDay
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isActive) PureWhite else Graphite)
                    .clickable { onSelect(dayNum) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    days[idx],
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) PitchBlack else MutedSilver
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableCard(
    slot: TimetableEntity,
    isLive: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
            }
            false // Don't auto-dismiss — confirm first
        }
    )

    SwipeToDismissBox(
        state            = dismissState,
        backgroundContent = {
            // Red delete background revealed on swipe
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CoralRose.copy(alpha = .25f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint     = CoralRose,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isLive) SageGreen else Graphite, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = ObsidianGray
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Live indicator dot
                if (isLive) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SageGreen)
                            .align(Alignment.Top)
                    )
                    Spacer(Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${slot.startTime} – ${slot.endTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSilver
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(slot.subject, style = MaterialTheme.typography.titleLarge, color = PureWhite)
                    Text(
                        "${slot.room} • ${slot.professor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSilver
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isLive) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SageGreen.copy(alpha = .2f)
                        ) {
                            Text(
                                "Take Attendance →",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = SageGreen
                            )
                        }
                    } else {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MutedSilver, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = ObsidianGray,
            title   = { Text("Delete Class?", color = PureWhite) },
            text    = { Text("Remove ${slot.subject} from the timetable?", color = MutedSilver) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = CoralRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }
}

// ── Add/Edit Slot Dialog ────────────────────────────────────────────────────

@Composable
private fun SlotDialog(
    title: String,
    initial: TimetableEntity?,
    selectedDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (TimetableEntity) -> Unit
) {
    val subjects = listOf("Data Structures", "Operating Systems", "Algorithms", "DBMS", "Mathematics", "English")

    var subject   by remember { mutableStateOf(initial?.subject   ?: subjects.first()) }
    var startTime by remember { mutableStateOf(initial?.startTime ?: "09:00") }
    var endTime   by remember { mutableStateOf(initial?.endTime   ?: "10:00") }
    var room      by remember { mutableStateOf(initial?.room      ?: "") }
    var professor by remember { mutableStateOf(initial?.professor ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = ObsidianGray,
        title   = { Text(title, color = PureWhite) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Subject picker
                Text("Subject", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick  = { subject = s },
                            label    = { Text(s.take(5), style = MaterialTheme.typography.labelSmall) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreen.copy(alpha = .3f),
                                selectedLabelColor     = SageGreen
                            )
                        )
                    }
                }

                // Time fields
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = startTime,
                        onValueChange = { startTime = it },
                        label         = { Text("Start", color = MutedSilver) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        colors        = timetableFieldColors()
                    )
                    OutlinedTextField(
                        value         = endTime,
                        onValueChange = { endTime = it },
                        label         = { Text("End", color = MutedSilver) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        colors        = timetableFieldColors()
                    )
                }
                OutlinedTextField(
                    value         = room,
                    onValueChange = { room = it },
                    label         = { Text("Room", color = MutedSilver) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    colors        = timetableFieldColors()
                )
                OutlinedTextField(
                    value         = professor,
                    onValueChange = { professor = it },
                    label         = { Text("Professor", color = MutedSilver) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    colors        = timetableFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        (initial ?: TimetableEntity(dayOfWeek = selectedDay, startTime = "", endTime = "", subject = "", room = "", professor = ""))
                            .copy(
                                subject   = subject,
                                startTime = startTime,
                                endTime   = endTime,
                                room      = room,
                                professor = professor,
                                dayOfWeek = selectedDay
                            )
                    )
                }
            ) { Text("Save", color = SageGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedSilver) }
        }
    )
}

@Composable
private fun timetableFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = SageGreen,
    unfocusedBorderColor = Graphite,
    focusedTextColor     = PureWhite,
    unfocusedTextColor   = PureWhite
)

private fun isLiveClass(slot: TimetableEntity, now: LocalTime): Boolean {
    return try {
        val fmt   = DateTimeFormatter.ofPattern("HH:mm")
        val start = LocalTime.parse(slot.startTime, fmt)
        val end   = LocalTime.parse(slot.endTime,   fmt)
        !now.isBefore(start) && now.isBefore(end)
    } catch (_: Exception) { false }
}
