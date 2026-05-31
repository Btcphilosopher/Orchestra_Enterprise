package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val parentId: Int? = null,
    val managerName: String,
    val code: String
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val role: String, // EMPLOYEE, TEAM_LEAD, MANAGER, DIRECTOR, ADMINISTRATOR
    val departmentId: Int,
    val clockInTime: Long? = null,
    val currentStatus: String = "Offline" // "Active", "On Break", "Offline"
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val departmentId: Int,
    val managerId: Int,
    val progress: Int = 0, // 0 to 100
    val deadline: String
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val projectId: Int,
    val assignedEmployeeId: Int,
    val creatorEmployeeId: Int,
    val status: String, // Draft, Assigned, In Progress, Awaiting Review, Approved, Completed, Cancelled
    val priority: String, // Low, Medium, High, Critical
    val dueDate: String,
    val completedDate: String? = null,
    val dependsOnTaskId: Int? = null
)

@Entity(tableName = "approvals")
data class ApprovalWorkflowEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requesterId: Int,
    val creatorName: String,
    val type: String, // Annual Leave, Expense, Procurement, Document Approval
    val title: String,
    val description: String,
    val currentApproverRole: String, // MANAGER, DIRECTOR
    val status: String, // Pending, Approved, Rejected
    val stage: Int = 1, // 1 = Line Manager, 2 = Dept Head (Approved after 2)
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val senderName: String,
    val targetType: String, // CHANNEL, DIRECT, BROADCAST
    val targetId: Int, // e.g. departmentId, specific employeeId, or 0 for broadcast
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fileType: String, // PDF, Word, Excel, Image
    val urlContent: String,
    val uploadedByEmployeeId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long
)

@Entity(tableName = "time_attendance")
data class TimeAttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val clockIn: Long,
    val clockOut: Long? = null,
    val breakMinutes: Int = 0,
    val workDayDate: String
)

@Entity(tableName = "api_gateway_logs")
data class ApiGatewayLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val method: String,
    val url: String,
    val requestBody: String? = null,
    val responseStatus: Int,
    val responseBody: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val jwt: String
)
