package com.crystalcheong.ozone.repositories

import com.crystalcheong.ozone.db.Run
import com.crystalcheong.ozone.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor( //TODO INFO: The job of a repository is to collect all the data from the respective data sources to be utilized by the ViewModels
    val runDao: RunDAO  //TODO: Provides the functions of listed in DAO
) {
    //TODO: Call the functions of the DAO inside ViewModels

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
}