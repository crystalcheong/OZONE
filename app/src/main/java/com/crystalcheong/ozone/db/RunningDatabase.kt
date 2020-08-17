package com.crystalcheong.ozone.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crystalcheong.ozone.db.Converters
import com.crystalcheong.ozone.db.Run
import com.crystalcheong.ozone.db.RunDAO

//TODO: Initialize Run database with Room
@Database(
    entities = [Run::class],
    version = 1
)

/**TODO INFO: Singleton declaration of the Run database
 ** that can be accessed with the DAO
 ** through Dagger-Hilt dependency injection
 **/
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDAO
}