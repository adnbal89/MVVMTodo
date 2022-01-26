package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    //Flow : async stream of Data, so every async task is in the Flow so we do not
    //define this func as suspend.
    @Query("SELECT * FROM task_table")
    fun getTasks(): Flow<List<Task>>

    //kotlin coroutines function. it is like in an other thread than main thread
    //use a background thread.
    //If record exists, than replace the new one with the old.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}