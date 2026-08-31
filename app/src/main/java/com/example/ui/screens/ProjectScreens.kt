package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.util.CurrencyUtils

@Composable
fun ProjectManagementScreen(
    viewModel: SmartQuoteViewModel,
    onSelectProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: Completed

    val activeProjects = projects.filter { !it.isCompleted }
    val completedProjects = projects.filter { it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active Projects (${activeProjects.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_active_projects")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Completed (${completedProjects.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_completed_projects")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentList = if (selectedTab == 0) activeProjects else completedProjects

        if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No projects in this section", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Projects are automatically created when quotations are accepted", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(currentList) { project ->
                    ProjectCardItem(project = project, onClick = { onSelectProject(project.id) })
                }
            }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: ProjectEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.projectName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Client: ${project.customerName} • ${project.projectType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = project.stage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Project Progress:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${project.progressPercent}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Budget: ${CurrencyUtils.formatInr(project.totalBudget)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Started: ${CurrencyUtils.formatDate(project.startDate)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    viewModel: SmartQuoteViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val project = projects.find { it.id == projectId }

    var features by remember { mutableStateOf<List<ProjectFeatureEntity>>(emptyList()) }

    LaunchedEffect(projectId) {
        viewModel.repository.getProjectFeatures(projectId).collect {
            features = it
        }
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val stages = listOf(
        "Requirement Received" to 10,
        "UI/UX Design" to 25,
        "UI Approved" to 40,
        "Development Started" to 60,
        "Backend & Database" to 75,
        "Testing & QA" to 85,
        "Client Review" to 95,
        "Completed" to 100
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.projectName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // Project Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(project.projectName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            StatusBadge(status = project.stage)
                        }
                        Text("Client: ${project.customerName} • Type: ${project.projectType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Budget: ${CurrencyUtils.formatInr(project.totalBudget)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${project.progressPercent}% Completed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Interactive Project Pipeline Stage Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Update Project Lifecycle Stage", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(stages) { (stageName, progress) ->
                                val isCurrent = project.stage == stageName
                                FilterChip(
                                    selected = isCurrent,
                                    onClick = {
                                        viewModel.updateProjectStage(
                                            id = project.id,
                                            stage = stageName,
                                            progress = progress,
                                            isCompleted = stageName == "Completed"
                                        )
                                    },
                                    label = { Text(stageName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Feature Checklist Breakdown
            item {
                Text(
                    text = "Requirement & Feature Tracker (${features.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(features) { feat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(feat.featureName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Category: ${feat.category} • Est: ${feat.estimatedDays} days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Feature status cycle button
                        Surface(
                            color = when (feat.status) {
                                "Completed" -> Color(0xFFDCFCE7)
                                "In Progress" -> Color(0xFFDBEAFE)
                                else -> Color(0xFFF1F5F9)
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable {
                                val nextStatus = when (feat.status) {
                                    "Not Started" -> "In Progress"
                                    "In Progress" -> "Completed"
                                    else -> "Not Started"
                                }
                                viewModel.updateProjectFeatureStatus(feat.id, nextStatus)
                            }
                        ) {
                            Text(
                                text = feat.status,
                                color = when (feat.status) {
                                    "Completed" -> Color(0xFF15803D)
                                    "In Progress" -> Color(0xFF1E40AF)
                                    else -> Color(0xFF475569)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
