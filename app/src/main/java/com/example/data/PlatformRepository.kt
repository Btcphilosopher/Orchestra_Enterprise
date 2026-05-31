package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class PlatformRepository(private val context: Context) {

    private val db: PlatformDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            PlatformDatabase::class.java,
            "orchestra_enterprise_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val dao: PlatformDao by lazy { db.platformDao() }

    // Read flows
    val departments: Flow<List<DepartmentEntity>> = dao.getAllDepartmentsFlow()
    val employees: Flow<List<EmployeeEntity>> = dao.getAllEmployeesFlow()
    val projects: Flow<List<ProjectEntity>> = dao.getAllProjectsFlow()
    val tasks: Flow<List<TaskEntity>> = dao.getAllTasksFlow()
    val approvals: Flow<List<ApprovalWorkflowEntity>> = dao.getAllApprovalsFlow()
    val messages: Flow<List<MessageEntity>> = dao.getAllMessagesFlow()
    val documents: Flow<List<DocumentEntity>> = dao.getAllDocumentsFlow()
    val attendance: Flow<List<TimeAttendanceEntity>> = dao.getAllTimeAttendanceFlow()
    val apiLogs: Flow<List<ApiGatewayLogEntity>> = dao.getApiGatewayLogsFlow()

    // Simulated JWT generator helper
    fun generateSimulatedJwt(name: String, role: String, deptId: Int): String {
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" // standard header
        val payloadBody = "{\"name\":\"$name\",\"role\":\"$role\",\"deptId\":$deptId,\"iss\":\"orchestra-gateway\",\"exp\":${System.currentTimeMillis() + 86400000}}"
        val payloadEncoded = android.util.Base64.encodeToString(payloadBody.toByteArray(), android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING or android.util.Base64.URL_SAFE)
        val signature = "sI46hZ3yK_simulatedSig_L8s0Lsw91x4m_EnterpriseSaaS"
        return "$header.$payloadEncoded.$signature"
    }

    private suspend fun logApiGateway(
        method: String,
        url: String,
        requestBody: String?,
        responseStatus: Int,
        responseBody: String,
        empName: String = "System",
        empRole: String = "ADMINISTRATOR",
        deptId: Int = 1
    ) {
        val jwt = generateSimulatedJwt(empName, empRole, deptId)
        dao.insertApiLog(
            ApiGatewayLogEntity(
                method = method,
                url = url,
                requestBody = requestBody,
                responseStatus = responseStatus,
                responseBody = responseBody,
                timestamp = System.currentTimeMillis(),
                jwt = jwt
            )
        )
    }

    // --- API Gate Operations ---

    // 1. Task Management
    suspend fun createTask(
        title: String,
        description: String,
        projectId: Int,
        assignedEmployeeId: Int,
        creatorId: Int,
        priority: String,
        dueDate: String,
        dependsOnTaskId: Int? = null,
        actor: EmployeeEntity
    ): Int {
        val task = TaskEntity(
            title = title,
            description = description,
            projectId = projectId,
            assignedEmployeeId = assignedEmployeeId,
            creatorEmployeeId = creatorId,
            status = "Assigned",
            priority = priority,
            dueDate = dueDate,
            dependsOnTaskId = dependsOnTaskId
        )
        val id = dao.insertTask(task).toInt()
        val createdTask = task.copy(id = id)

        val reqJson = """
            {
               "title": "$title",
               "description": "$description",
               "projectId": $projectId,
               "assignedEmployeeId": $assignedEmployeeId,
               "creatorEmployeeId": $creatorId,
               "priority": "$priority",
               "dueDate": "$dueDate",
               "dependsOnTaskId": ${dependsOnTaskId ?: "null"}
            }
        """.trimIndent()

        val resJson = """
            {
               "status": "success",
               "statusCode": 201,
               "message": "Task created and assigned successfully",
               "data": {
                  "taskId": $id,
                  "title": "$title",
                  "status": "Assigned",
                  "priority": "$priority",
                  "assignedEmployeeId": $assignedEmployeeId,
                  "dependsOn": ${dependsOnTaskId ?: "null"}
               }
            }
        """.trimIndent()

        logApiGateway("POST", "/api/v1/tasks/create", reqJson, 201, resJson, actor.name, actor.role, actor.departmentId)
        return id
    }

    suspend fun assignTask(
        taskId: Int,
        employeeId: Int,
        actor: EmployeeEntity
    ) {
        val task = dao.getTaskById(taskId)
        if (task != null) {
            val updated = task.copy(assignedEmployeeId = employeeId, status = "Assigned")
            dao.updateTask(updated)

            val reqJson = """{"taskId": $taskId, "employeeId": $employeeId}"""
            val resJson = """
                {
                   "status": "success",
                   "statusCode": 200,
                   "message": "Task reassigned successfully",
                   "data": {
                      "taskId": $taskId,
                      "newAssigneeId": $employeeId,
                      "status": "Assigned"
                   }
                }
            """.trimIndent()
            logApiGateway("PUT", "/api/v1/tasks/assign", reqJson, 200, resJson, actor.name, actor.role, actor.departmentId)
        }
    }

    suspend fun updateTaskStatus(
        taskId: Int,
        status: String, // In Progress, Awaiting Review, Completed, Cancelled
        actor: EmployeeEntity
    ) {
        val task = dao.getTaskById(taskId)
        if (task != null) {
            val completedDate = if (status == "Completed") {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else null
            val updated = task.copy(status = status, completedDate = completedDate)
            dao.updateTask(updated)

            // Auto trigger approvals if task went to Awaiting Review
            if (status == "Awaiting Review") {
                createApprovalWorkflow(
                    requesterId = actor.id,
                    creatorName = actor.name,
                    type = "Document Approval",
                    title = "Task completion review: ${task.title}",
                    description = "Requesting approval for task completion. Task Code #TSK-$taskId.",
                    actor = actor
                )
            }

            val reqJson = """{"status": "$status"}"""
            val resJson = """
                {
                   "status": "success",
                   "statusCode": 200,
                   "message": "Task status updated to $status",
                   "data": {
                      "taskId": $taskId,
                      "newStatus": "$status",
                      "completedDate": ${if (completedDate != null) "\"$completedDate\"" else "null"}
                   }
                }
            """.trimIndent()
            logApiGateway("POST", "/api/v1/tasks/$taskId/complete", reqJson, 200, resJson, actor.name, actor.role, actor.departmentId)
        }
    }

    // 2. Project Operations
    suspend fun createProject(
        name: String,
        description: String,
        departmentId: Int,
        managerId: Int,
        deadline: String,
        actor: EmployeeEntity
    ): Int {
        val proj = ProjectEntity(
            name = name,
            description = description,
            departmentId = departmentId,
            managerId = managerId,
            deadline = deadline
        )
        val id = dao.insertProject(proj).toInt()

        val reqJson = """
            {
               "name": "$name",
               "description": "$description",
               "departmentId": $departmentId,
               "managerId": $managerId,
               "deadline": "$deadline"
            }
        """.trimIndent()

        val resJson = """
            {
               "status": "success",
               "statusCode": 201,
               "message": "Project registered in system chart",
               "data": {
                  "projectId": $id,
                  "name": "$name",
                  "deadline": "$deadline",
                  "allocatedStaff": []
               }
            }
        """.trimIndent()
        logApiGateway("POST", "/api/v1/projects/create", reqJson, 201, resJson, actor.name, actor.role, actor.departmentId)
        return id
    }

    // 3. Document Operations
    suspend fun uploadDocument(
        name: String,
        fileType: String,
        urlContent: String,
        uploadedBy: Int,
        sizeBytes: Long,
        actor: EmployeeEntity
    ): Int {
        val doc = DocumentEntity(
            name = name,
            fileType = fileType,
            urlContent = urlContent,
            uploadedByEmployeeId = uploadedBy,
            sizeBytes = sizeBytes
        )
        val id = dao.insertDocument(doc).toInt()

        val reqJson = """
            {
               "name": "$name",
               "fileType": "$fileType",
               "uploaderId": $uploadedBy,
               "sizeBytes": $sizeBytes
            }
        """.trimIndent()

        val resJson = """
            {
               "status": "success",
               "statusCode": 201,
               "documentId": $id,
               "resourceUri": "/api/v1/documents/downloads/$id",
               "meta": {
                  "uploadedAt": ${System.currentTimeMillis()},
                  "checksumSha256": "4a71b...simulated"
               }
            }
        """.trimIndent()
        logApiGateway("POST", "/api/v1/documents/upload", reqJson, 201, resJson, actor.name, actor.role, actor.departmentId)
        return id
    }

    // 4. Time & Attendance tracking
    suspend fun clockIn(employeeId: Int, actor: EmployeeEntity) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val record = TimeAttendanceEntity(
            employeeId = employeeId,
            clockIn = System.currentTimeMillis(),
            clockOut = null,
            breakMinutes = 0,
            workDayDate = todayStr
        )
        dao.insertTimeAttendance(record)

        val updatedEmp = actor.copy(clockInTime = record.clockIn, currentStatus = "Active")
        dao.updateEmployee(updatedEmp)

        val reqJson = """{"employeeId": $employeeId, "action": "CLOCK_IN"}"""
        val resJson = """
            {
               "status": "success",
               "message": "Punch recorded successfully",
               "clockInTime": ${record.clockIn},
               "status": "Active"
            }
        """.trimIndent()
        logApiGateway("POST", "/api/v1/attendance/punch", reqJson, 200, resJson, actor.name, actor.role, actor.departmentId)
    }

    suspend fun clockOut(employeeId: Int, actor: EmployeeEntity) {
        val activeRecord = dao.getActiveTimeAttendance(employeeId)
        if (activeRecord != null) {
            val updatedRecord = activeRecord.copy(clockOut = System.currentTimeMillis())
            dao.updateTimeAttendance(updatedRecord)
        }
        val updatedEmp = actor.copy(clockInTime = null, currentStatus = "Offline")
        dao.updateEmployee(updatedEmp)

        val reqJson = """{"employeeId": $employeeId, "action": "CLOCK_OUT"}"""
        val resJson = """
            {
               "status": "success",
               "message": "Punch out recorded. Session closed.",
               "clockOutTime": ${System.currentTimeMillis()},
               "status": "Offline"
            }
        """.trimIndent()
        logApiGateway("POST", "/api/v1/attendance/punch", reqJson, 200, resJson, actor.name, actor.role, actor.departmentId)
    }

    // 5. Workflow/Approvals
    suspend fun createApprovalWorkflow(
        requesterId: Int,
        creatorName: String,
        type: String,
        title: String,
        description: String,
        actor: EmployeeEntity
    ): Int {
        val approval = ApprovalWorkflowEntity(
            requesterId = requesterId,
            creatorName = creatorName,
            type = type,
            title = title,
            description = description,
            currentApproverRole = "MANAGER",
            status = "Pending",
            stage = 1
        )
        val id = dao.insertApproval(approval).toInt()

        val reqJson = """
            {
               "requesterId": $requesterId,
               "type": "$type",
               "title": "$title",
               "description": "$description"
            }
        """.trimIndent()

        val resJson = """
            {
               "status": "success",
               "statusCode": 201,
               "approvalId": $id,
               "workflowState": "Awaiting Line Manager approval",
               "stage": 1
            }
        """.trimIndent()
        logApiGateway("POST", "/api/v1/workflows/create", reqJson, 201, resJson, actor.name, actor.role, actor.departmentId)
        return id
    }

    suspend fun processApproval(
        approvalId: Int,
        approved: Boolean,
        actor: EmployeeEntity
    ) {
        // Fetch all approvals flow values in memory to find the approval to update
        val list = dao.getAllApprovalsFlow().first()
        val approval = list.find { it.id == approvalId }
        if (approval != null) {
            val status = if (approved) {
                if (approval.stage == 1 && approval.currentApproverRole == "MANAGER") {
                    "Pending" // advance to next stage
                } else {
                    "Approved" // completed
                }
            } else {
                "Rejected"
            }

            val nextStage = if (approved && approval.stage == 1) 2 else approval.stage
            val nextRole = if (approved && approval.stage == 1) "DIRECTOR" else approval.currentApproverRole

            val updated = approval.copy(
                status = if (approved && nextStage == 1) "Approved" else status,
                stage = nextStage,
                currentApproverRole = nextRole
            )

            // Actually, if we hit Stage 2 and approve, status becomes "Approved"
            val finalStatus = if (approved) {
                if (approval.stage == 1) {
                    // Let's do instant completion or advance
                    "Approved" // simple 1-step or 2-step. Let's make complete approved
                } else "Approved"
            } else "Rejected"

            val finalApproval = updated.copy(status = finalStatus)
            dao.updateApproval(finalApproval)

            val actionName = if (approved) "APPROVE" else "REJECT"
            val reqJson = """{"approvalId": $approvalId, "action": "$actionName"}"""
            val resJson = """
                {
                   "status": "success",
                   "statusCode": 200,
                   "message": "Approval choice processed.",
                   "data": {
                      "approvalId": $approvalId,
                      "finalStatus": "$finalStatus",
                      "processedBy": "${actor.name}",
                      "processedRole": "${actor.role}"
                   }
                }
            """.trimIndent()
            logApiGateway("POST", "/api/v1/workflows/$approvalId/process", reqJson, 200, resJson, actor.name, actor.role, actor.departmentId)
        }
    }

    // 6. Messaging & Chat
    suspend fun sendMessage(
        senderId: Int,
        senderName: String,
        targetType: String,
        targetId: Int,
        text: String,
        actor: EmployeeEntity
    ) {
        val msg = MessageEntity(
            senderId = senderId,
            senderName = senderName,
            targetType = targetType,
            targetId = targetId,
            messageText = text
        )
        dao.insertMessage(msg)

        val reqJson = """
            {
               "senderId": $senderId,
               "targetType": "$targetType",
               "targetId": $targetId,
               "messageText": "$text"
            }
        """.trimIndent()

        val resJson = """
            {
               "status": "delivered",
               "statusCode": 200,
               "messageId": ${System.currentTimeMillis() % 100000},
               "receiptInfo": {
                  "deliveredCount": 12,
                  "websocketBroadcast": true
               }
            }
        """.trimIndent()
        logApiGateway("POST", "/api/v1/messages/send", reqJson, 200, resJson, actor.name, actor.role, actor.departmentId)
    }

    // Clear logs
    suspend fun clearLogs() {
        dao.clearApiLogs()
    }

    // Seeding logic
    suspend fun seedInitialDataIfNecessary() {
        // Query database flows to see if employees contains anyone
        val currentEmps = dao.getAllEmployeesFlow().first()
        if (currentEmps.isNotEmpty()) return

        // 1. Seed Departments
        val devDept = DepartmentEntity(id = 1, name = "Engineering", managerName = "Sarah", code = "ENG")
        val salesDept = DepartmentEntity(id = 2, name = "Sales", managerName = "Emily", code = "SALES")
        val opsDept = DepartmentEntity(id = 3, name = "Operations", managerName = "Robert", code = "OPS")
        val finDept = DepartmentEntity(id = 4, name = "Finance", managerName = "Andrew", code = "FIN")

        dao.insertDepartment(devDept)
        dao.insertDepartment(salesDept)
        dao.insertDepartment(opsDept)
        dao.insertDepartment(finDept)

        // 2. Seed Employees
        // Current active user: tom@ahyx.org. Let's make sure employee id 1 is Tom
        val tomState = EmployeeEntity(
            id = 1,
            name = "Tom Developer",
            email = "tom@ahyx.org",
            role = "EMPLOYEE",
            departmentId = 1,
            clockInTime = null,
            currentStatus = "Offline"
        )
        val sarahState = EmployeeEntity(
            id = 2,
            name = "Sarah Engineering Manager",
            email = "sarah.m@company.org",
            role = "MANAGER",
            departmentId = 1,
            clockInTime = null,
            currentStatus = "Offline"
        )
        val robertState = EmployeeEntity(
            id = 3,
            name = "Robert Operations Director",
            email = "robert.director@company.org",
            role = "DIRECTOR",
            departmentId = 3,
            clockInTime = null,
            currentStatus = "Offline"
        )
        val aliceState = EmployeeEntity(
            id = 4,
            name = "Alice Platform Administrator",
            email = "alice.admin@company.org",
            role = "ADMINISTRATOR",
            departmentId = 4,
            clockInTime = null,
            currentStatus = "Offline"
        )
        val jackState = EmployeeEntity(
            id = 5,
            name = "Jack Tech Lead",
            email = "jack.tl@company.org",
            role = "TEAM_LEAD",
            departmentId = 1,
            clockInTime = null,
            currentStatus = "Offline"
        )

        dao.insertEmployee(tomState)
        dao.insertEmployee(sarahState)
        dao.insertEmployee(robertState)
        dao.insertEmployee(aliceState)
        dao.insertEmployee(jackState)

        // 3. Seed Projects
        val proj1 = ProjectEntity(
            id = 1,
            name = "Orion Core API v2",
            description = "High-efficiency microservices orchestration gateway supporting active gRPC and REST sync-pipelines.",
            departmentId = 1,
            managerId = 2,
            progress = 45,
            deadline = "2026-08-15"
        )
        val proj2 = ProjectEntity(
            id = 2,
            name = "OmniChannel CRM Integrator",
            description = "Centralized customer feedback pipe connected directly into Salesforce, Slack, and internal messaging.",
            departmentId = 2,
            managerId = 3,
            progress = 20,
            deadline = "2026-09-30"
        )
        dao.insertProject(proj1)
        dao.insertProject(proj2)

        // 4. Seed Tasks
        val task1 = TaskEntity(
            id = 1,
            title = "Optimize Redis Cache Invalidation Pipelines",
            description = "Optimize active redis publisher performance during sudden high queue depth states under 10k operations/second.",
            projectId = 1,
            assignedEmployeeId = 1, // Tom
            creatorEmployeeId = 2, // Sarah
            status = "In Progress",
            priority = "Critical",
            dueDate = "2026-06-12",
            dependsOnTaskId = null
        )
        val task2 = TaskEntity(
            id = 2,
            title = "Implement Secure JWT Token Validation Interceptor",
            description = "Rewrite standard authentication filters to safely validate signature claims and verify active session stores.",
            projectId = 1,
            assignedEmployeeId = 5, // Jack
            creatorEmployeeId = 2, // Sarah
            status = "Assigned",
            priority = "High",
            dueDate = "2026-06-25",
            dependsOnTaskId = null
        )
        val task3 = TaskEntity(
            id = 3,
            title = "Formulate Q3 Logistics Budget Analysis",
            description = "Consolidate regional site hardware and hosting expenses across five worldwide offices to balance departmental quotas.",
            projectId = 2,
            assignedEmployeeId = 3, // Robert
            creatorEmployeeId = 4, // Alice
            status = "Awaiting Review",
            priority = "Medium",
            dueDate = "2026-06-15",
            dependsOnTaskId = null
        )
        val task4 = TaskEntity(
            id = 4,
            title = "Finalize Postgres Clustering Deployment Runbook",
            description = "Draft full deployment strategies including passive standby replication sequences for secondary operational sites.",
            projectId = 1,
            assignedEmployeeId = 1, // Tom
            creatorEmployeeId = 2, // Sarah
            status = "Completed",
            priority = "High",
            dueDate = "2026-05-20",
            completedDate = "2026-05-18",
            dependsOnTaskId = 2 // Depends on JWT Token Task
        )

        dao.insertTask(task1)
        dao.insertTask(task2)
        dao.insertTask(task3)
        dao.insertTask(task4)

        // 5. Seed Approvals
        val app1 = ApprovalWorkflowEntity(
            id = 1,
            requesterId = 5,
            creatorName = "Jack Tech Lead",
            type = "Annual Leave",
            title = "Jack leave request: Summer holiday",
            description = "Requesting annual leave from Dec 15 to Dec 24 (7 working days). Tasks reassigned to Tom.",
            currentApproverRole = "MANAGER",
            status = "Pending",
            stage = 1
        )
        val app2 = ApprovalWorkflowEntity(
            id = 2,
            requesterId = 1,
            creatorName = "Tom Developer",
            type = "Expense Reimbursement",
            title = "Enterprise Kafka Training Kursus",
            description = "Reimbursement request for professional Kafka Developer certification course ($350.00). Approved by HR.",
            currentApproverRole = "MANAGER",
            status = "Pending",
            stage = 1
        )
        dao.insertApproval(app1)
        dao.insertApproval(app2)

        // 6. Seed Documents
        val doc1 = DocumentEntity(
            id = 1,
            name = "Orion_Architecture_Whitepaper_v2.1.pdf",
            fileType = "PDF",
            urlContent = "https://orchestra.enterprise.org/docs/arch_spec_v2_1.pdf",
            uploadedByEmployeeId = 2,
            sizeBytes = 4194304
        )
        val doc2 = DocumentEntity(
            id = 2,
            name = "Engineering_Q3_Workforce_Budget.xlsx",
            fileType = "Excel",
            urlContent = "https://orchestra.enterprise.org/docs/q3_budget.xlsx",
            uploadedByEmployeeId = 4,
            sizeBytes = 1887436
        )
        dao.insertDocument(doc1)
        dao.insertDocument(doc2)

        // 7. Seed Messages
        val m1 = MessageEntity(
            id = 1,
            senderId = 2,
            senderName = "Sarah Engineering Manager",
            targetType = "CHANNEL",
            targetId = 1, // ENG
            messageText = "Hello team! Welcome to the Orchestra task orchestration workspace. All development sprints are updated. Let's make sure our Redis optimizations are ready by due date!"
        )
        val m2 = MessageEntity(
            id = 2,
            senderId = 1,
            senderName = "Tom Developer",
            targetType = "CHANNEL",
            targetId = 1, // ENG
            messageText = "Thanks Sarah! I've started on task #1. Local Redis stress testing has begun. The profile metrics look excellent so far."
        )
        val m3 = MessageEntity(
            id = 3,
            senderId = 4,
            senderName = "Alice Platform Administrator",
            targetType = "BROADCAST",
            targetId = 0,
            messageText = "ANNOUNCEMENT: System infrastructure maintenance scheduled for tomorrow morning at 02:00 - 03:00 UTC. The API gateway will remain available with cached read-pipelines."
        )

        dao.insertMessage(m1)
        dao.insertMessage(m2)
        dao.insertMessage(m3)

        // 8. Seeding API gateway logs to demonstrate the history right away!
        val initLogBody = """
            {
               "status": "online",
               "service": "Orchestra Task Orchestration Engine",
               "apiVersion": "v1.0.0",
               "host": "k8s-pod-gateway-5f89",
               "integrations": ["RoomDatabase", "PostgreSQL-Sim", "Redis-Sim", "JWT-Manager"]
            }
        """.trimIndent()
        logApiGateway("GET", "/api/v1/health", null, 200, initLogBody, "Alice", "ADMINISTRATOR", 4)
    }
}


