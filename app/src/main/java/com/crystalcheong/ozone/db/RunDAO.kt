package com.crystalcheong.ozone.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.crystalcheong.ozone.db.Run

//TODO: Create Data Access Object (DAO) to perform CRUD operations in the Run database
@Dao
interface RunDAO {
    //TODO: Insert new Run record with coroutines (w/ delay)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    //TODO: Delete Run record with coroutines (w/ delay)
    @Delete
    suspend fun deleteRun(run: Run)

    /**
     * TODO INFO: LiveData characteristics :
     *  - is a wrapper that can be used with any data
     *  - is lifecycle-aware
     *  - only updates observers that are in an active lifecycle state (STARTED, RESUMED)
     *  - bascially, ASYNC
    **/

    //TODO: Query database columns
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    //TODO: Query database calculations
    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>
}










