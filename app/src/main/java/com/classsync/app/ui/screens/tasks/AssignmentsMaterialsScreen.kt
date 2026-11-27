package com.classsync.app.ui.screens.tasks

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.classsync.app.data.local.entity.AssignmentEntity
import com.classsync.app.data.local.entity.StudyMaterialEntity
import com.classsync.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen 4 — Assignments & Study Materials
 * Segmented tab switcher: Assignments (To-Do) | Study Materials
 */
@Composable
fun AssignmentsMaterialsScreen(viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PitchBlack,
        floatingActionButton = {
            if (state.selectedTab == TaskTab.ASSIGNMENTS) {
                FloatingActionButton(
                    onClick          = { showAddDialog = true },
                    containerColor   = PureWhite,
                    contentColor     = PitchBlack,
                    shape            = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Header
            Text(
                "My Academic Tasks",
                style    = MaterialTheme.typography.displayMedium,
                color    = PureWhite,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Pill tab switcher
            TabSwitcher(
                selected  = state.selectedTab,
                onSelect  = viewModel::selectTab
            )

            Spacer(Modifier.height(12.dp))

            // Subject filter
            SubjectFilter(
                subjects  = viewModel.subjects,
                selected  = state.selectedSubjectFilter,
                onSelect  = viewModel::selectSubject
            )

            Spacer(Modifier.height(12.dp))

            // Content
            when (state.selectedTab) {
                TaskTab.ASSIGNMENTS -> AssignmentList(
                    assignments = state.assignments.filter {
                        state.selectedSubjectFilter == "All" || it.subject == state.selectedSubjectFilter
                    },
                    onToggle = { id, done -> viewModel.toggleComplete(id, done) },
                    onDelete = viewModel::deleteAssignment
                )
                TaskTab.MATERIALS -> MaterialsList(
                    materials = state.materials.filter {
                        state.selectedSubjectFilter == "All" || it.subject == state.selectedSubjectFilter
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddAssignmentDialog(
            subjects  = viewModel.subjects.drop(1),
            onDismiss = { showAddDialog = false },
            onConfirm = { title, subject, dueMillis, priority ->
                viewModel.addAssignment(title, subject, dueMillis, priority)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TabSwitcher(selected: TaskTab, onSelect: (TaskTab) -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = Graphite
    ) {
        Row(Modifier.padding(4.dp)) {
            TaskTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick  = { onSelect(tab) },
                    shape    = RoundedCornerShape(50),
                    color    = if (isSelected) ObsidianGray else Color.Transparent
                ) {
                    Text(
                        text     = if (tab == TaskTab.ASSIGNMENTS) "Assignments" else "Study Materials",
                        modifier = Modifier.padding(vertical = 10.dp).wrapContentWidth(Alignment.CenterHorizontally),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = if (isSelected) PureWhite else MutedSilver
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectFilter(subjects: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding      = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subjects) { subject ->
            val isSelected = subject == selected
            Surface(
                onClick = { onSelect(subject) },
                shape   = RoundedCornerShape(50),
                color   = if (isSelected) SageGreen.copy(alpha = .2f) else Graphite
            ) {
                Text(
                    subject,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = if (isSelected) SageGreen else MutedSilver
                )
            }
        }
    }
}

@Composable
private fun AssignmentList(
    assignments: List<AssignmentEntity>,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (AssignmentEntity) -> Unit
) {
    if (assignments.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No assignments — you're all caught up! 🎉", style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
        }
        return
    }
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(assignments, key = { it.id }) { assignment ->
            AssignmentCard(assignment = assignment, onToggle = onToggle, onDelete = onDelete)
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: AssignmentEntity,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (AssignmentEntity) -> Unit
) {
    val now      = System.currentTimeMillis()
    val hoursLeft = (assignment.dueDateTime - now) / 3_600_000L
    val isUrgent  = hoursLeft in 0..24 && !assignment.isCompleted
    val dueText   = when {
        assignment.isCompleted -> "Completed ✓"
        hoursLeft < 0          -> "Overdue!"
        hoursLeft < 24         -> "Due in ${hoursLeft}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(assignment.dueDateTime))
    }
    val dueColor  = when {
        assignment.isCompleted -> SageGreen
        hoursLeft < 0          -> CoralRose
        isUrgent               -> CoralRose
        else                   -> MutedSilver
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ObsidianGray
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked         = assignment.isCompleted,
                onCheckedChange = { onToggle(assignment.id, it) },
                colors          = CheckboxDefaults.colors(
                    checkedColor   = SageGreen,
                    checkmarkColor = PureWhite
                )
            )
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = SageGreen.copy(alpha = .15f)) {
                        Text(
                            assignment.subject.take(4),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = SageGreen
                        )
                    }
                    PriorityBadge(assignment.priority)
                }
                Spacer(Modifier.height(4.dp))
                Text(assignment.title, style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                Text(dueText, style = MaterialTheme.typography.labelSmall, color = dueColor)
            }
            IconButton(onClick = { onDelete(assignment) }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MutedSilver)
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: String) {
    val color = when (priority) {
        "HIGH"   -> CoralRose
        "MEDIUM" -> WarmTaupe
        else     -> MutedSilver
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = .15f)) {
        Text(
            priority,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = color
        )
    }
}

@Composable
private fun MaterialsList(materials: List<StudyMaterialEntity>) {
    if (materials.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No study materials yet.", style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
        }
        return
    }
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(materials, key = { it.id }) { material ->
            MaterialCard(material)
        }
    }
}

@Composable
private fun MaterialCard(material: StudyMaterialEntity) {
    val sizeText = when {
        material.fileSize > 1_000_000 -> "${"%.1f".format(material.fileSize / 1_000_000.0)} MB"
        material.fileSize > 1_000     -> "${material.fileSize / 1_000} KB"
        else                          -> "${material.fileSize} B"
    }
    Surface(shape = RoundedCornerShape(18.dp), color = ObsidianGray) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Graphite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (material.fileType) {
                        "PDF"   -> Icons.Default.PictureAsPdf
                        "SLIDE" -> Icons.Default.Slideshow
                        "LINK"  -> Icons.Default.Link
                        else    -> Icons.Default.Article
                    },
                    contentDescription = null,
                    tint = PureWhite
                )
            }
            Column(Modifier.weight(1f)) {
                Text(material.title, style = MaterialTheme.typography.bodyMedium, color = PureWhite, maxLines = 2)
                Text("${material.subject} • $sizeText", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
            }
            Icon(
                imageVector = if (material.isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                contentDescription = "Download",
                tint = if (material.isDownloaded) SageGreen else MutedSilver
            )
        }
    }
}

@Composable
private fun AddAssignmentDialog(
    subjects: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, String) -> Unit
) {
    var title    by remember { mutableStateOf("") }
    var subject  by remember { mutableStateOf(subjects.firstOrNull() ?: "") }
    var priority by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = ObsidianGray,
        title   = { Text("Add Assignment", color = PureWhite) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text("Title", color = MutedSilver) },
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = SageGreen,
                        unfocusedBorderColor = Graphite,
                        focusedTextColor     = PureWhite,
                        unfocusedTextColor   = PureWhite
                    )
                )
                Text("Subject:", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick  = { subject = s },
                            label    = { Text(s.take(4)) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreen.copy(alpha = .3f),
                                selectedLabelColor     = SageGreen
                            )
                        )
                    }
                }
                Text("Priority:", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick  = { priority = p },
                            label    = { Text(p) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onConfirm(title, subject, System.currentTimeMillis() + 86_400_000L, priority)
                }
            }) { Text("Add", color = SageGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedSilver) }
        }
    )
}
