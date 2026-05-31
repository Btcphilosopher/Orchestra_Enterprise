package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PlatformScreen {
    WORKSPACE,
    MANAGER,
    APPROVALS,
    EXECUTIVE,
    API_GATEWAY
}

class PlatformViewModel(application: Application) : AndroidViewModel(application) {

    val repository = PlatformRepository(application)

    // Current State Managers
    private val _currentEmployee = MutableStateFlow<EmployeeEntity?>(null)
    val currentEmployee: StateFlow<EmployeeEntity?> = _currentEmployee.asStateFlow()

    private val _currentScreen = MutableStateFlow(PlatformScreen.WORKSPACE)
    val currentScreen: StateFlow<PlatformScreen> = _currentScreen.asStateFlow()

    private val _enterpriseSearchQuery = MutableStateFlow("")
    val enterpriseSearchQuery: StateFlow<String> = _enterpriseSearchQuery.asStateFlow()

    // Observable Flows from DB
    val departments: StateFlow<List<DepartmentEntity>> = repository.departments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvals: StateFlow<List<ApprovalWorkflowEntity>> = repository.approvals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = repository.documents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<TimeAttendanceEntity>> = repository.attendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apiLogs: StateFlow<List<ApiGatewayLogEntity>> = repository.apiLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // 1. Seed default corporate structures
            repository.seedInitialDataIfNecessary()

            // 2. Load Active User based on metadata default email or ID=1
            val emps = repository.employees.first()
            val tom = emps.find { it.email == "tom@ahyx.org" } ?: emps.firstOrNull()
            _currentEmployee.value = tom
        }
    }

    // --- Actions ---

    fun changeScreen(screen: PlatformScreen) {
        _currentScreen.value = screen
    }

    fun selectEmployeeRole(employeeId: Int) {
        viewModelScope.launch {
            val emps = repository.employees.first()
            val chosen = emps.find { it.id == employeeId }
            if (chosen != null) {
                _currentEmployee.value = chosen
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _enterpriseSearchQuery.value = query
    }

    // Task Functions
    fun createTask(
        title: String,
        description: String,
        projectId: Int,
        assignedEmployeeId: Int,
        priority: String,
        dueDate: String,
        dependsOnTaskId: Int? = null
    ) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.createTask(
                title = title,
                description = description,
                projectId = projectId,
                assignedEmployeeId = assignedEmployeeId,
                creatorId = actor.id,
                priority = priority,
                dueDate = dueDate,
                dependsOnTaskId = dependsOnTaskId,
                actor = actor
            )
        }
    }

    fun reassignTask(taskId: Int, employeeId: Int) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.assignTask(taskId, employeeId, actor)
        }
    }

    fun updateTaskStatus(taskId: Int, status: String) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.updateTaskStatus(taskId, status, actor)
        }
    }

    // Time punch Clock-In
    fun toggleClockIn() {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            if (actor.clockInTime == null) {
                repository.clockIn(actor.id, actor)
            } else {
                repository.clockOut(actor.id, actor)
            }
            // reload employee details
            val updated = repository.dao.getEmployeeById(actor.id)
            if (updated != null) {
                _currentEmployee.value = updated
            }
        }
    }

    // Approval queues
    fun requestLeave(title: String, desc: String) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.createApprovalWorkflow(
                requesterId = actor.id,
                creatorName = actor.name,
                type = "Annual Leave",
                title = title,
                description = desc,
                actor = actor
            )
        }
    }

    fun requestExpense(title: String, desc: String) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.createApprovalWorkflow(
                requesterId = actor.id,
                creatorName = actor.name,
                type = "Expense Reimbursement",
                title = title,
                description = desc,
                actor = actor
            )
        }
    }

    fun processApproval(approvalId: Int, approved: Boolean) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.processApproval(approvalId, approved, actor)
        }
    }

    // Chat discussions with team
    fun sendBroadcastAnnouncement(text: String) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.sendMessage(
                senderId = actor.id,
                senderName = actor.name,
                targetType = "BROADCAST",
                targetId = 0,
                text = text,
                actor = actor
            )
        }
    }

    fun sendTeamChannelMessage(deptId: Int, text: String) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.sendMessage(
                senderId = actor.id,
                senderName = actor.name,
                targetType = "CHANNEL",
                targetId = deptId,
                text = text,
                actor = actor
            )
        }
    }

    // Create project
    fun createProject(name: String, description: String, deptId: Int, deadline: String) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.createProject(
                name = name,
                description = description,
                departmentId = deptId,
                managerId = actor.id,
                deadline = deadline,
                actor = actor
            )
        }
    }

    // Document uploading
    fun uploadDocument(name: String, fileType: String, url: String, sizeBytes: Long) {
        viewModelScope.launch {
            val actor = _currentEmployee.value ?: return@launch
            repository.uploadDocument(
                name = name,
                fileType = fileType,
                urlContent = url,
                uploadedBy = actor.id,
                sizeBytes = sizeBytes,
                actor = actor
            )
        }
    }

    // Clear REST logs
    fun clearApiLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Simulated Web Portal Integration Script
    fun triggerSimulatedExternalApiScript() {
        viewModelScope.launch {
            val emps = repository.employees.first()
            val depts = repository.departments.first()
            val adminUser = emps.find { it.role == "ADMINISTRATOR" } ?: emps.first()

            // Simulation of External Corporate System creating a task and updating details via API
            // 1. Log a GET request for health check
            repository.dao.insertApiLog(
                ApiGatewayLogEntity(
                    method = "GET",
                    url = "/api/v1/departments",
                    requestBody = null,
                    responseStatus = 200,
                    responseBody = """
                        [
                           {"id":1, "name":"Engineering", "code":"ENG"},
                           {"id":2, "name":"Sales", "code":"SALES"},
                           {"id":3, "name":"Operations", "code":"OPS"}
                        ]
                    """.trimIndent(),
                    jwt = repository.generateSimulatedJwt("Webhook-Gate", "ADMINISTRATOR", 4)
                )
            )

            // 2. Perform a simulated Task Create and insert via DAO
            val newTask = TaskEntity(
                id = 100 + (System.currentTimeMillis() % 100).toInt(),
                title = "Automated SecOps Firewall Patch",
                description = "Triggered by API Gateway intrusion policy. Ensure subnets are updated.",
                projectId = 1,
                assignedEmployeeId = 1, // Tom
                creatorEmployeeId = 4, // Alice Admin
                status = "Assigned",
                priority = "Critical",
                dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 86400000))
            )
            repository.dao.insertTask(newTask)

            // Log corresponding POST call
            val reqPayload = """
                {
                   "title": "${newTask.title}",
                   "projectId": 1,
                   "assigneeId": 1,
                   "triggerSource": "CloudArmorSentinel"
                }
            """.trimIndent()
            val resPayload = """
                {
                   "status": "success",
                   "statusCode": 201,
                   "message": "Automated priority task triggered",
                   "taskId": ${newTask.id}
                }
            """.trimIndent()

            repository.dao.insertApiLog(
                ApiGatewayLogEntity(
                    method = "POST",
                    url = "/api/v1/tasks/create",
                    requestBody = reqPayload,
                    responseStatus = 201,
                    responseBody = resPayload,
                    jwt = repository.generateSimulatedJwt("CloudArmorSentinel", "SYSTEM", 4)
                )
            )
        }
    }

    // --- HEURISTIC INTELLIGENT TASK PRIORITISATION ENGINE ---
    // Automatically ranks tasks based on urgency, remaining days, criticality score, and dependencies.
    fun getSmartPrioritizedTasks(taskList: List<TaskEntity>): List<Pair<TaskEntity, Int>> {
        val today = Date()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return taskList.map { task ->
            var score = 0

            // 1. Base score by Priority
            when (task.priority.uppercase()) {
                "CRITICAL" -> score += 100
                "HIGH" -> score += 60
                "MEDIUM" -> score += 30
                "LOW" -> score += 10
            }

            // 2. Status score modifier
            when (task.status) {
                "In Progress" -> score += 20
                "Awaiting Review" -> score += 40
                "Completed" -> score -= 1000 // push completed tasks to bottom
                "Cancelled" -> score -= 1000
            }

            // 3. Due Date score
            try {
                val dueDateObj = format.parse(task.dueDate)
                if (dueDateObj != null) {
                    val diffTime = dueDateObj.time - today.time
                    val diffDays = (diffTime / (1000 * 60 * 60 * 24)).toInt()

                    if (diffDays < 0) {
                        score += 80 // Overdue penalty
                    } else if (diffDays <= 1) {
                        score += 60 // Due tomorrow
                    } else if (diffDays <= 3) {
                        score += 40 // Due in 3 days
                    } else if (diffDays <= 7) {
                        score += 20 // Due in a week
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }

            // 4. Dependencies modifier (if this task blocking any other tasks)
            val hasDependencies = taskList.any { it.dependsOnTaskId == task.id }
            if (hasDependencies) {
                score += 35 // blocking another task gives high importance!
            }

            // 5. If this task itself depends on another task which is not completed
            if (task.dependsOnTaskId != null) {
                val parentTask = taskList.find { it.id == task.dependsOnTaskId }
                if (parentTask != null && parentTask.status != "Completed") {
                    score -= 50 // Reduce priority score since it is current BLOCKED!
                }
            }

            task to score
        }.sortedByDescending { it.second }
    }

    // --- STAFFING BOTTLENECK PREDICTION ANALYSIS MODEL ---
    // Returns prediction warnings for each Department
    fun getWorkforcePlanningForecast(
        employees: List<EmployeeEntity>,
        tasks: List<TaskEntity>
    ): List<DepartmentPlanningForecast> {
        val deptsMap = mapOf(1 to "Engineering", 2 to "Sales", 3 to "Operations", 4 to "Finance")

        return deptsMap.map { (deptId, deptName) ->
            val deptEmps = employees.filter { it.departmentId == deptId }
            val deptTasks = tasks.filter { task ->
                val assignedEmp = employees.find { it.id == task.assignedEmployeeId }
                assignedEmp?.departmentId == deptId && task.status != "Completed" && task.status != "Cancelled"
            }

            val totalActiveTasks = deptTasks.size
            val staffCount = deptEmps.size.coerceAtLeast(1)
            val averageTaskLoad = totalActiveTasks.toDouble() / staffCount

            // Assess Bottleneck risk
            val riskLevel = when {
                averageTaskLoad > 3.0 -> "HIGH RISK"
                averageTaskLoad > 1.5 -> "MEDIUM RISK"
                else -> "STABLE"
            }

            val advice = when (riskLevel) {
                "HIGH RISK" -> "Critical Bottleneck. Hire +${(averageTaskLoad / 1.5).toInt()} staff or delegate to secondary department."
                "MEDIUM RISK" -> "At capacity limits. Monitor milestones closely."
                else -> "Healthy allocation. Workforce is fully balanced."
            }

            DepartmentPlanningForecast(
                departmentId = deptId,
                departmentName = deptName,
                activeTaskCount = totalActiveTasks,
                staffCount = staffCount,
                averageTaskLoad = averageTaskLoad,
                riskStatus = riskLevel,
                actionAdvice = advice
            )
        }
    }
}

data class DepartmentPlanningForecast(
    val departmentId: Int,
    val departmentName: String,
    val activeTaskCount: Int,
    val staffCount: Int,
    val averageTaskLoad: Double,
    val riskStatus: String, // STABLE, MEDIUM RISK, HIGH RISK
    val actionAdvice: String
)
