package com.crystalcheong.ozone.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.crystalcheong.ozone.db.RunningDatabase
import com.crystalcheong.ozone.other.Constants
import com.crystalcheong.ozone.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.crystalcheong.ozone.other.Constants.KEY_NAME
import com.crystalcheong.ozone.other.Constants.KEY_WEIGHT
import com.crystalcheong.ozone.other.Constants.RUNNING_DATABASE_NAME
import com.crystalcheong.ozone.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

//TODO: Tell Dagger how to create dependency objects
@Module
//TODO INFO: Inject dependencies during the lifetime of the Application
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton  //TODO: Singleton declarations for universal instance of database instead of having multiple DB instances each time it is instantiated
    @Provides   //TODO: Tell Dagger that the method has a return type bounded to its return values
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    //TODO: Create an instance of the DAO to access the specified CRUD operations
    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    //TODO: Create an instance of the Shared Preferences
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context): SharedPreferences =
        //TODO INFO: [MODE_PRIVATE] only the app is allowed to read from this shared pref
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}








