package com.crystalcheong.ozone.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import com.crystalcheong.ozone.other.Constants
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class) //TODO INFO: Inject dependencies during the lifetime of the Service required
object ServiceModule {

    @ServiceScoped  //TODO: Only one instance in the lifetime of the Service ( == Singleton)
    @Provides       //TODO: Tell Dagger that the method has a return type bounded to its return values
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideSensorManager(
        @ApplicationContext app: Context
    ) = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext app: Context
    ) = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context
    ): NotificationCompat.Builder = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_ozone_logo)
        .setContentTitle("OZONE Activity")

}














