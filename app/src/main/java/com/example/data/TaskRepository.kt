package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allCompletionLogs: Flow<List<CompletionLog>> = taskDao.getAllCompletionLogs()

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTaskById(taskId)
        taskDao.deleteLogsByTaskId(taskId)
    }

    suspend fun completeTask(taskId: Int, dateString: String) {
        val log = CompletionLog(taskId = taskId, dateString = dateString)
        taskDao.insertCompletionLog(log)
    }

    suspend fun uncompleteTask(taskId: Int, dateString: String) {
        taskDao.deleteCompletionLog(taskId, dateString)
    }
}
