package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("SELECT * FROM completion_logs ORDER BY timestamp DESC")
    fun getAllCompletionLogs(): Flow<List<CompletionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionLog(log: CompletionLog)

    @Query("DELETE FROM completion_logs WHERE taskId = :taskId AND dateString = :dateString")
    suspend fun deleteCompletionLog(taskId: Int, dateString: String)

    @Query("DELETE FROM completion_logs WHERE taskId = :taskId")
    suspend fun deleteLogsByTaskId(taskId: Int)
}
