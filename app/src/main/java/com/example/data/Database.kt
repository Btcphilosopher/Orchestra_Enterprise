package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatformDao {
    // Departments
    @Query("SELECT * FROM departments")
    fun getAllDepartmentsFlow(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(dept: DepartmentEntity)

    // Employees
    @Query("SELECT * FROM employees")
    fun getAllEmployeesFlow(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Int): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE email = :email")
    suspend fun getEmployeeByEmail(email: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(emp: EmployeeEntity)

    @Update
    suspend fun updateEmployee(emp: EmployeeEntity)

    // Projects
    @Query("SELECT * FROM projects")
    fun getAllProjectsFlow(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(proj: ProjectEntity): Long

    @Update
    suspend fun updateProject(proj: ProjectEntity)

    // Tasks
    @Query("SELECT * FROM tasks")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Approvals
    @Query("SELECT * FROM approvals ORDER BY timestamp DESC")
    fun getAllApprovalsFlow(): Flow<List<ApprovalWorkflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalWorkflowEntity): Long

    @Update
    suspend fun updateApproval(approval: ApprovalWorkflowEntity)

    // Messages
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    // Documents
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocumentsFlow(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity): Long

    // Time & Attendance
    @Query("SELECT * FROM time_attendance ORDER BY clockIn DESC")
    fun getAllTimeAttendanceFlow(): Flow<List<TimeAttendanceEntity>>

    @Query("SELECT * FROM time_attendance WHERE employeeId = :employeeId AND clockOut IS NULL LIMIT 1")
    suspend fun getActiveTimeAttendance(employeeId: Int): TimeAttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeAttendance(record: TimeAttendanceEntity): Long

    @Update
    suspend fun updateTimeAttendance(record: TimeAttendanceEntity)

    // API Gateway Logs
    @Query("SELECT * FROM api_gateway_logs ORDER BY timestamp DESC LIMIT 200")
    fun getApiGatewayLogsFlow(): Flow<List<ApiGatewayLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiLog(log: ApiGatewayLogEntity)

    @Query("DELETE FROM api_gateway_logs")
    suspend fun clearApiLogs()
}

@Database(
    entities = [
        DepartmentEntity::class,
        EmployeeEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        ApprovalWorkflowEntity::class,
        MessageEntity::class,
        DocumentEntity::class,
        TimeAttendanceEntity::class,
        ApiGatewayLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PlatformDatabase : RoomDatabase() {
    abstract fun platformDao(): PlatformDao
}
