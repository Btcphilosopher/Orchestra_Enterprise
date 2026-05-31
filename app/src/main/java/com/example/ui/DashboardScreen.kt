package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.PlatformScreen
import com.example.viewmodel.PlatformViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DashboardScreen(
    viewModel: PlatformViewModel,
    modifier: Modifier = Modifier
) {
    // Collect UI state reactively from Room DB tables via the viewmodel
    val currentEmp by viewModel.currentEmployee.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val approvals by viewModel.approvals.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val apiLogs by viewModel.apiLogs.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val searchQuery by viewModel.enterpriseSearchQuery.collectAsState()

    var isRoleSelectorExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CorporateSlateDarkest,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Corporate Title with Pulse Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonTeal)
                            )
                            Text(
                                text = "ORCHESTRA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GATEWAY-API v1",
                                color = NeutralGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .border(1.dp, CorporateSlateAccent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        // Adaptive Persona/Role System Switcher
                        Box {
                            Card(
                                onClick = { isRoleSelectorExpanded = !isRoleSelectorExpanded },
                                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                                border = BorderStroke(1.dp, NeonTeal),
                                modifier = Modifier.testTag("persona_switcher")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (currentEmp?.role) {
                                                    "ADMINISTRATOR" -> StatusError
                                                    "DIRECTOR" -> StatusOrange
                                                    "MANAGER" -> NeonTeal
                                                    else -> NeonTeal.copy(alpha = 0.5f)
                                                }
                                            )
                                    )
                                    Text(
                                        text = currentEmp?.name?.substringBefore(" ") ?: "System",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Switch Role",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isRoleSelectorExpanded,
                                onDismissRequest = { isRoleSelectorExpanded = false },
                                modifier = Modifier.background(CorporateSlateDark)
                            ) {
                                Text(
                                    text = "SECURE IDENTITY PORTAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralGray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(emp.name, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("${emp.role} • ${departments.find { it.id == emp.departmentId }?.name ?: "Corp"}", fontSize = 11.sp, color = NeutralGray)
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectEmployeeRole(emp.id)
                                            isRoleSelectorExpanded = false
                                        }
                                    );
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Elegant navigation bar targeting exact platform requirements
            NavigationBar(
                containerColor = CorporateSlateDarkest,
                tonalElevation = 8.dp
            ) {
                val screens = listOf(
                    Triple(PlatformScreen.WORKSPACE, "Workspace", Icons.Default.AccountBox),
                    Triple(PlatformScreen.MANAGER, "Team Hub", Icons.Default.Build),
                    Triple(PlatformScreen.APPROVALS, "Approvals", Icons.Default.Check),
                    Triple(PlatformScreen.EXECUTIVE, "Intelligence", Icons.Default.Star),
                    Triple(PlatformScreen.API_GATEWAY, "API Gateway", Icons.Default.Lock)
                )

                screens.forEach { (screen, label, icon) ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.changeScreen(screen) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) NeonTeal else NeutralGray
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) NeonTeal else NeutralGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CorporateSlateAccent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(CorporateSlateDarkest)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Enterprise Central Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CorporateSlateDarkest,
                    tonalElevation = 2.dp
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Enterprise search: query tasks, projects, employees...", color = NeutralGray, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeutralGray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = NeutralGray)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CorporateSlateDark,
                            unfocusedContainerColor = CorporateSlateDark,
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = CorporateSlateAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("enterprise_search_bar")
                    )
                }

                // If enterprise search has a non-empty query, show Search Results Screen.
                // Otherwise support normal Tab/Screen Navigation.
                if (searchQuery.isNotBlank()) {
                    SearchResultsView(
                        query = searchQuery,
                        tasks = tasks,
                        projects = projects,
                        employees = employees,
                        messages = messages,
                        documents = documents,
                        depts = departments,
                        viewModel = viewModel
                    )
                } else {
                    when (currentScreen) {
                        PlatformScreen.WORKSPACE -> WorkspaceView(viewModel, currentEmp, tasks, projects, documents, messages)
                        PlatformScreen.MANAGER -> TeamHubView(viewModel, currentEmp, tasks, projects, employees, departments)
                        PlatformScreen.APPROVALS -> ApprovalsView(viewModel, currentEmp, approvals)
                        PlatformScreen.EXECUTIVE -> ExecutiveDashboardView(viewModel, employees, tasks, departments, projects, apiLogs)
                        PlatformScreen.API_GATEWAY -> ApiGatewayView(viewModel, apiLogs)
                    }
                }
            }
        }
    }
}

// ==================== WORKSPACE SCREEN MODULE ====================

@Composable
fun WorkspaceView(
    viewModel: PlatformViewModel,
    employee: EmployeeEntity?,
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    documents: List<DocumentEntity>,
    messages: List<MessageEntity>
) {
    if (employee == null) return

    val myTasks = tasks.filter { it.assignedEmployeeId == employee.id }
    val myPendingTasks = myTasks.filter { it.status != "Completed" && it.status != "Cancelled" }
    val myCompletedTasks = myTasks.filter { it.status == "Completed" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Time & Attendance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TIME & ATTENDANCE RECORD",
                            color = NeutralGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (employee.clockInTime != null) "WORK SESSION: ACTIVE" else "WORK STATUS: OFFLINE",
                            color = if (employee.clockInTime != null) StatusSuccess else StatusError,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        if (employee.clockInTime != null) {
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(employee.clockInTime))
                            Text("Punched In Today at $timeStr", color = Color.White, fontSize = 12.sp)
                        } else {
                            Text("Not clocked in. Start session to log hours.", color = NeutralGray, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleClockIn() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (employee.clockInTime != null) StatusError else StatusSuccess
                        ),
                        modifier = Modifier.testTag("clock_in_out_btn")
                    ) {
                        Icon(
                            imageVector = if (employee.clockInTime != null) Icons.Default.Clear else Icons.Default.Check,
                            contentDescription = "Punch",
                            modifier = Modifier.padding(end = 4.dp).size(16.dp)
                        )
                        Text(
                            text = if (employee.clockInTime != null) "Clock Out" else "Clock In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Active Employee Stat Badges Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ACTIVE BLOCKS", color = NeutralGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${myPendingTasks.size}", color = NeonTeal, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("COMPLETED TASKS", color = NeutralGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${myCompletedTasks.size}", color = StatusSuccess, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // 3. AI Smart Prioritised List (Advanced Feature)
        item {
            val smartList = viewModel.getSmartPrioritizedTasks(myTasks)
            if (smartList.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI",
                            tint = NeonOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI CRITICAL SPRINT SORT (RANKED BY RISK CLAUSE)",
                            color = NeonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CorporateSlateDark.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, NeonOrange.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            smartList.take(2).forEach { (task, score) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(0.7f)) {
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text("Due: ${task.dueDate} • Priority: ${task.priority}", color = NeutralGray, fontSize = 11.sp)
                                    }
                                    Text(
                                        text = "Score $score",
                                        color = NeonOrange,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(CorporateSlateAccent, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Employee's Assigned Task Inbox
        item {
            Text(
                text = "MY ASSIGNED WORKSPACE INBOX",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (myPendingTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = StatusSuccess, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active assignments! Check with manager Sarah.", color = NeutralGray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(myPendingTasks) { task ->
                TaskRowInboxItem(task, projects, viewModel)
            }
        }

        // 5. Employee Collaboration Hub / Team chat integration
        item {
            Text(
                text = "DEPARTMENT CHAT & BROADCASTS",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#Engineering-Feed Channel",
                            color = NeonTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(CorporateSlateAccent, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Live WebSocket Sync", color = NeutralWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val deptChannelMsgs = messages.filter {
                        it.targetType == "BROADCAST" || (it.targetType == "CHANNEL" && it.targetId == employee.departmentId)
                    }.takeLast(3)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        deptChannelMsgs.forEach { msg ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CorporateSlateDarkest, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(msg.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal)
                                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                                    Text(time, fontSize = 10.sp, color = NeutralGray)
                                }
                                Text(msg.messageText, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var sendText by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sendText,
                            onValueChange = { sendText = it },
                            placeholder = { Text("Post message to channel...", color = NeutralGray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal,
                                unfocusedBorderColor = CorporateSlateAccent
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp)
                        )
                        IconButton(
                            onClick = {
                                if (sendText.isNotBlank()) {
                                    viewModel.sendTeamChannelMessage(employee.departmentId, sendText)
                                    sendText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = NeonTeal)
                        }
                    }
                }
            }
        }

        // 6. Corporate Document Vault Box (Files Integration)
        item {
            Text(
                text = "CORPORATE DOCUMENT VAULT",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active PDF Specifications", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("${documents.size} elements", color = NeutralGray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    documents.forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(CorporateSlateDarkest, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "File",
                                    tint = if (doc.fileType == "PDF") StatusError else StatusOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(doc.name, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.sp)
                                    val sizeKb = DecimalFormat("#,###").format(doc.sizeBytes / 1024)
                                    Text("Type: ${doc.fileType} • Size: ${sizeKb} KB", color = NeutralGray, fontSize = 10.sp)
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowBack, // simple download representation
                                contentDescription = "Download File",
                                tint = NeonTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated quick upload mock file
                    Button(
                        onClick = {
                            viewModel.uploadDocument(
                                "Orion_Telemetry_Log_${System.currentTimeMillis() % 1000}.xlsx",
                                "Excel",
                                "https://orchestra.enterprise.org/docs/telemetry.xlsx",
                                sizeBytes = 859345
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateSlateAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate PDF Spec Upload via API", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRowInboxItem(
    task: TaskEntity,
    projects: List<ProjectEntity>,
    viewModel: PlatformViewModel
) {
    var expandedDetail by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandedDetail = !expandedDetail },
        colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
        border = BorderStroke(1.dp, CorporateSlateAccent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.7f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (task.priority.uppercase()) {
                                    "CRITICAL" -> StatusError
                                    "HIGH" -> StatusOrange
                                    else -> NeonTeal
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (task.status) {
                                "In Progress" -> NeonTeal.copy(alpha = 0.2f)
                                "Awaiting Review" -> StatusOrange.copy(alpha = 0.2f)
                                else -> CorporateSlateAccent
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (task.status) {
                            "In Progress" -> NeonTeal
                            "Awaiting Review" -> StatusOrange
                            else -> Color.White
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Project: ${projects.find { it.id == task.projectId }?.name ?: "Operations"}",
                    color = NeutralGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "Deadline: ${task.dueDate}",
                    color = NeutralGray,
                    fontSize = 11.sp
                )
            }

            // Expanded view options to process current tasks (Mark In Progress, Complete, review dependencies)
            AnimatedVisibility(
                visible = expandedDetail,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = CorporateSlateAccent, modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "TASK DESCRIPTION CLAUSE:",
                        color = NeutralGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    if (task.dependsOnTaskId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DEPENDENCY REQUIREMENT: Blocking Task ID #${task.dependsOnTaskId}",
                            color = StatusOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (task.status == "Assigned") {
                            Button(
                                onClick = { viewModel.updateTaskStatus(task.id, "In Progress") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Set In Progress", fontSize = 11.sp)
                            }
                        }

                        if (task.status == "In Progress") {
                            Button(
                                onClick = { viewModel.updateTaskStatus(task.id, "Awaiting Review") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusOrange),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Submit for Review", fontSize = 11.sp)
                            }
                        }

                        if (task.status == "Awaiting Review") {
                            Button(
                                onClick = { viewModel.updateTaskStatus(task.id, "Completed") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Completed", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== MANAGER TEAM HUB MODULE ====================

@Composable
fun TeamHubView(
    viewModel: PlatformViewModel,
    currentUser: EmployeeEntity?,
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    employees: List<EmployeeEntity>,
    departments: List<DepartmentEntity>
) {
    if (currentUser == null) return

    // Role Guard: Require lead positions
    val hasManagerPrivileges = currentUser.role in listOf("MANAGER", "DIRECTOR", "ADMINISTRATOR")

    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var selectedProjId by remember { mutableStateOf(projects.firstOrNull()?.id ?: 1) }
    var selectedAssigneeId by remember { mutableStateOf(employees.firstOrNull()?.id ?: 1) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var newTaskDueDate by remember { mutableStateOf("2026-06-30") }
    var dependsOnTaskId by remember { mutableStateOf<Int?>(null) }

    if (!hasManagerPrivileges) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = StatusError, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Access Denied: High Security Clause",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Team Hub management coordinates resource allocations. Current identity (${currentUser.name}) is restricted. Switch to Sarah Manager role to execute orchestration tasks.",
                color = NeutralGray,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Interactive Action Controls (Assign task)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, NeonTeal)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ORCHESTRATE SPRINT TASK",
                        color = NeonTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Configure workflows, set rigid deadlines, link blocked processes, and assign staff.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showCreateTaskDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_task_trigger_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create and Assign Service Ticket", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Active Projects Gantt Timeline Visualization
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.List, contentDescription = "Projects", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("GANTT SYSTEM & PROJECTS MILESTONES (ENG)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
        }

        items(projects) { proj ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(proj.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text("API CODEPROJ-${proj.id}", fontSize = 10.sp, color = NeutralGray, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(proj.description, fontSize = 11.sp, color = NeutralGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Gantt Canvas Drawing Style Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PROGRESS:", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CorporateSlateDarkest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(proj.progress / 100f)
                                    .background(NeonTeal)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${proj.progress}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("DEADLINE: ${proj.deadline}", fontSize = 11.sp, color = Color.White)
                        val projTasksCount = tasks.filter { it.projectId == proj.id }.size
                        Text("Sprint Tasks: $projTasksCount", fontSize = 11.sp, color = NeonTeal)
                    }
                }
            }
        }

        // 3. Complete Department Workloads Overviews
        item {
            Text("STAFF ALLOCATIONS & TEAM WORKLOADS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        items(employees) { emp ->
            val empTasks = tasks.filter { it.assignedEmployeeId == emp.id && it.status != "Completed" }
            val dept = departments.find { it.id == emp.departmentId }?.name ?: "Head Office"

            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(emp.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("$dept • ${emp.role}", color = NeutralGray, fontSize = 11.sp)
                        Text("Working Hours: ${if (emp.clockInTime != null) "PUNCHED ACTIVE" else "OFFLINE"}", color = if (emp.clockInTime != null) StatusSuccess else NeutralGray, fontSize = 10.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text(
                            text = "${empTasks.size} load units",
                            color = if (empTasks.size > 2) StatusError else StatusSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(if (emp.currentStatus == "Active") StatusSuccess.copy(alpha = 0.2f) else CorporateSlateAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(emp.currentStatus, fontSize = 9.sp, color = if (emp.currentStatus == "Active") StatusSuccess else Color.White)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Task Creation
    if (showCreateTaskDialog) {
        Dialog(onDismissRequest = { showCreateTaskDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, NeonTeal),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "ORCHESTRATE SPRINT TASK TICKET",
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = CorporateSlateAccent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_task_title")
                    )

                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Operational Description Claims") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = CorporateSlateAccent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_task_desc")
                    )

                    // Project selection
                    Text("SELECT SYSTEM CORE PROJECT:", fontSize = 11.sp, color = NeutralGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        projects.forEach { proj ->
                            val isSelected = proj.id == selectedProjId
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) NeonTeal else CorporateSlateAccent, RoundedCornerShape(6.dp))
                                    .clickable { selectedProjId = proj.id }
                                    .padding(8.dp)
                            ) {
                                Text(proj.name.take(15), color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Assignee selection
                    Text("ALLOCATE SPRINT WORKER:", fontSize = 11.sp, color = NeutralGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        employees.take(3).forEach { emp ->
                            val isSelected = emp.id == selectedAssigneeId
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) NeonTeal else CorporateSlateAccent, RoundedCornerShape(6.dp))
                                    .clickable { selectedAssigneeId = emp.id }
                                    .padding(8.dp)
                            ) {
                                Text(emp.name.substringBefore(" "), color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Priority selection
                    Text("PRIORITY LEVEL STATE:", fontSize = 11.sp, color = NeutralGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Low", "Medium", "High", "Critical").forEach { opt ->
                            val isSelected = opt == selectedPriority
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) {
                                            when (opt) {
                                                "Critical" -> StatusError
                                                "High" -> StatusOrange
                                                else -> NeonTeal
                                            }
                                        } else CorporateSlateAccent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedPriority = opt }
                                    .padding(8.dp)
                            ) {
                                Text(opt, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Due Date entry
                    OutlinedTextField(
                        value = newTaskDueDate,
                        onValueChange = { newTaskDueDate = it },
                        label = { Text("Due Date Deadline (yyyy-mm-dd)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = CorporateSlateAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showCreateTaskDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateSlateAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newTaskTitle.isNotEmpty()) {
                                    viewModel.createTask(
                                        title = newTaskTitle,
                                        description = newTaskDesc,
                                        projectId = selectedProjId,
                                        assignedEmployeeId = selectedAssigneeId,
                                        priority = selectedPriority,
                                        dueDate = newTaskDueDate,
                                        dependsOnTaskId = dependsOnTaskId
                                    )
                                    showCreateTaskDialog = false
                                    // Reset fields
                                    newTaskTitle = ""
                                    newTaskDesc = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            modifier = Modifier.weight(1f).testTag("save_task_btn")
                        ) {
                            Text("Commit")
                        }
                    }
                }
            }
        }
    }
}

// ==================== APPROVALS SCREEN MODULE ====================

@Composable
fun ApprovalsView(
    viewModel: PlatformViewModel,
    currentUser: EmployeeEntity?,
    approvals: List<ApprovalWorkflowEntity>
) {
    if (currentUser == null) return

    val hasApproverStatus = currentUser.role in listOf("MANAGER", "DIRECTOR", "ADMINISTRATOR")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Leave / Expense request simulator triggers
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ORGANISATIONAL APPROVALS ENGINE",
                        color = NeonTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Trigger secure approval pipelines. These claims bypass standard lines for audit verification.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.requestLeave(
                                    "Leave request: Tom holiday expansion",
                                    "Requesting 5 days annual leave for personal reasons. Sprint duties handed to Jack Tech Lead."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateSlateAccent),
                            modifier = Modifier.weight(1f).testTag("req_leave_btn")
                        ) {
                            Text("Request Leave", fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                viewModel.requestExpense(
                                    "Expense claim: AWS Cluster Overdrive",
                                    "Reimburse temporary AWS EC2 server billing during stress benchmark runs ($285.40)."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateSlateAccent),
                            modifier = Modifier.weight(1f).testTag("req_expense_btn")
                        ) {
                            Text("Record Expense", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            Text("ACTIVE APPROVAL QUEUE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        val eligibleApprovals = approvals.filter { app ->
            // If manager, show approvals requiring MANAGER status. If director, show DIRECTOR approvals. Or show all if admin.
            when (currentUser.role) {
                "ADMINISTRATOR" -> true
                "DIRECTOR" -> app.status == "Pending"
                "MANAGER" -> app.status == "Pending" && app.currentApproverRole == "MANAGER"
                else -> app.requesterId == currentUser.id // employees can only monitor their own
            }
        }

        if (eligibleApprovals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Clear", tint = StatusSuccess, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All approvals pipelines are fully processed.", color = NeutralGray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(eligibleApprovals) { app ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(app.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Pipeline: ${app.type} • Requested by: ${app.creatorName}", fontSize = 11.sp, color = NeutralGray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (app.status) {
                                            "Approved" -> StatusSuccess.copy(alpha = 0.2f)
                                            "Rejected" -> StatusError.copy(alpha = 0.2f)
                                            else -> StatusOrange.copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    app.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (app.status) {
                                        "Approved" -> StatusSuccess
                                        "Rejected" -> StatusError
                                        else -> StatusOrange
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(app.description, fontSize = 12.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Stage: #${app.stage} • Target Signature: ${app.currentApproverRole}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonTeal
                            )

                            // Action buttons if the current user qualifies as reviewer
                            val userIsAllowedToSign = hasApproverStatus && (app.status == "Pending")
                            if (userIsAllowedToSign) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.processApproval(app.id, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.processApproval(app.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("approve_btn_${app.id}")
                                    ) {
                                        Text("Approve", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== EXECUTIVE INTELLIGENCE MODULE ====================

@Composable
fun ExecutiveDashboardView(
    viewModel: PlatformViewModel,
    employees: List<EmployeeEntity>,
    tasks: List<TaskEntity>,
    departments: List<DepartmentEntity>,
    projects: List<ProjectEntity>,
    apiLogs: List<ApiGatewayLogEntity>
) {
    val forecast = viewModel.getWorkforcePlanningForecast(employees, tasks)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive KPIs canvas drawings
        item {
            Text(
                "SYSTEM METRICS & OPERATIONAL EFFICIENCY",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, CorporateSlateAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ORGANISATIONAL SPRINT COMPLETIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Core Canvas graphics representation
                    val completedTasks = tasks.filter { it.status == "Completed" }.size
                    val totalTasks = tasks.size.coerceAtLeast(1)
                    val completionRate = completedTasks.toFloat() / totalTasks

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = CorporateSlateAccent,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = NeonTeal,
                                    startAngle = -90f,
                                    sweepAngle = 360f * completionRate,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "${(completionRate * 100).toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Text("Total Registered System Tasks: $totalTasks", color = Color.White, fontSize = 13.sp)
                            Text("Closed / Completed Tasks: $completedTasks", color = StatusSuccess, fontSize = 13.sp)
                            Text("Active Sprints Blocking Loads: ${tasks.filter { it.status != "Completed" }.size}", color = StatusOrange, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 2. Workforce Bottlenecks predictive warnings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Risk Warning", tint = OrangeAlert, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WORKFORCE PLANNING BOTTLENECK MODEL", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
        }

        items(forecast) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                border = BorderStroke(1.dp, if (item.riskStatus == "HIGH RISK") StatusError else CorporateSlateAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.departmentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (item.riskStatus == "HIGH RISK") StatusError.copy(alpha = 0.2f)
                                    else if (item.riskStatus == "MEDIUM RISK") StatusOrange.copy(alpha = 0.2f)
                                    else StatusSuccess.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                item.riskStatus,
                                fontWeight = FontWeight.Bold,
                                color = if (item.riskStatus == "HIGH RISK") StatusError
                                else if (item.riskStatus == "MEDIUM RISK") StatusOrange
                                else StatusSuccess,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Staff Allocation: ${item.staffCount} • Unresolved Active Backlogs: ${item.activeTaskCount}", fontSize = 12.sp, color = NeutralGray)
                    Text("Average Employee Task Burden Ratio: ${DecimalFormat("0.0").format(item.averageTaskLoad)} tasks/FTE", fontSize = 12.sp, color = Color.White)

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = CorporateSlateAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PLANNING DECISION ADVICE:",
                        color = NeonTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(item.actionAdvice, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

val OrangeAlert = Color(0xFFF97316)

// ==================== API GATEWAY DEVELOPER CONSOLE ====================

@Composable
fun ApiGatewayView(
    viewModel: PlatformViewModel,
    logs: List<ApiGatewayLogEntity>
) {
    var selectedLog by remember { mutableStateOf<ApiGatewayLogEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Platform REST health status metrics
        Card(
            colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
            border = BorderStroke(1.dp, NeonTeal)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "REST / WEBSOCKET SYSTEM GATEWAY",
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Box(modifier = Modifier.background(StatusSuccess.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("ACTIVE HTTP PORT: 8080", color = StatusSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Observe live audit REST requests triggered by Android applications, web interfaces, work portals and background webhook events.",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.clearApiLogs() },
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateSlateAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Server logs", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.triggerSimulatedExternalApiScript() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                        modifier = Modifier.weight(1.5f).testTag("api_script_trigger")
                    ) {
                        Text("Simulate External cURL Client", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        Text(
            "LIVE ENDPOINT COMMUNICATIONS MONITOR",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Text(
                        "No operational endpoint logs. Make additions inside workspace to seed communications trace.",
                        color = NeutralGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(logs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CorporateSlateDark, RoundedCornerShape(8.dp))
                            .border(1.dp, if (selectedLog?.id == log.id) NeonTeal else CorporateSlateAccent, RoundedCornerShape(8.dp))
                            .clickable { selectedLog = if (selectedLog?.id == log.id) null else log }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.method,
                                fontWeight = FontWeight.Bold,
                                color = when (log.method) {
                                    "POST" -> NeonTeal
                                    "PUT" -> StatusOrange
                                    else -> StatusSuccess
                                },
                                modifier = Modifier
                                    .width(55.dp)
                                    .testTag("log_method_${log.id}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                log.url,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )
                        }

                        Text(
                            text = log.responseStatus.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (log.responseStatus in 200..299) StatusSuccess else StatusError,
                            fontSize = 12.sp
                        )
                    }

                    // Expanded JSON inspector logic
                    if (selectedLog?.id == log.id) {
                        Surface(
                            color = CorporateSlateDarkest,
                            border = BorderStroke(1.dp, CorporateSlateAccent),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("JWT TOKEN:", color = StatusOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    log.jwt,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (log.requestBody != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("REQUEST BODY PAYLOAD:", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        log.requestBody,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("RESPONSE BODY PAYLOAD:", color = StatusSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    log.responseBody ?: "{}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== ENTERPRISE SEARCH FILTER VIEW ====================

@Composable
fun SearchResultsView(
    query: String,
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    employees: List<EmployeeEntity>,
    messages: List<MessageEntity>,
    documents: List<DocumentEntity>,
    depts: List<DepartmentEntity>,
    viewModel: PlatformViewModel
) {
    val q = query.lowercase(Locale.getDefault())

    val filteredTasks = tasks.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
    val filteredProjects = projects.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
    val filteredEmployees = employees.filter { it.name.lowercase().contains(q) || it.email.lowercase().contains(q) || it.role.lowercase().contains(q) }
    val filteredDocs = documents.filter { it.name.lowercase().contains(q) || it.fileType.lowercase().contains(q) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SEARCH RESULTS FOR \"${query.uppercase()}\"",
                color = NeonOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Section Tasks
        if (filteredTasks.isNotEmpty()) {
            item {
                Text("TASKS MATCHES (${filteredTasks.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
            items(filteredTasks) { task ->
                TaskRowInboxItem(task, projects, viewModel)
            }
        }

        // Section Projects
        if (filteredProjects.isNotEmpty()) {
            item {
                Text("PROJECTS MATCHES (${filteredProjects.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
            items(filteredProjects) { proj ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(proj.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(proj.description, fontSize = 11.sp, color = NeutralGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Deadline: ${proj.deadline} • Milestone units Completed: ${proj.progress}%", fontSize = 11.sp, color = NeonTeal)
                    }
                }
            }
        }

        // Section Employees
        if (filteredEmployees.isNotEmpty()) {
            item {
                Text("EMPLOYEES & STATIONS (${filteredEmployees.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
            items(filteredEmployees) { emp ->
                val dept = depts.find { it.id == emp.departmentId }?.name ?: "Corporate"
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(emp.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(emp.email, fontSize = 11.sp, color = NeutralGray)
                        }
                        Text("$dept • ${emp.role}", fontSize = 11.sp, color = NeonTeal)
                    }
                }
            }
        }

        // Section Documents
        if (filteredDocs.isNotEmpty()) {
            item {
                Text("DOCUMENTS (${filteredDocs.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
            items(filteredDocs) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorporateSlateDark),
                    border = BorderStroke(1.dp, CorporateSlateAccent)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(doc.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("Type: ${doc.fileType} • Size: ${doc.sizeBytes} bytes", fontSize = 11.sp, color = NeutralGray)
                    }
                }
            }
        }

        if (filteredTasks.isEmpty() && filteredProjects.isEmpty() && filteredEmployees.isEmpty() && filteredDocs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No enterprise communications found matching query.", color = NeutralGray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
