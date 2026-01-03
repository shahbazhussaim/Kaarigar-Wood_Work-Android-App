package com.kaarigar.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaarigar.data.local.entity.RequestEntity

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests WHERE workerId IS NULL AND status = 'PENDING'")
    fun getAvailableRequests(): LiveData<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE workerId = :workerId")
    fun getWorkerJobs(workerId: String): LiveData<List<RequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<RequestEntity>)
    
    @Query("UPDATE requests SET status = :status WHERE id = :requestId")
    suspend fun updateStatus(requestId: String, status: String)

    @Query("DELETE FROM requests")
    suspend fun clearRequests()
}
