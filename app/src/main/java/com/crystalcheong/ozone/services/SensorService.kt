package com.crystalcheong.ozone.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.crystalcheong.ozone.BuildConfig
import com.crystalcheong.ozone.other.Constants.ACTION_PAUSE_SERVICE
import com.crystalcheong.ozone.other.Constants.ACTION_SHOW_ACTIVITY_FRAGMENT
import com.crystalcheong.ozone.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.crystalcheong.ozone.other.Constants.ACTION_STOP_SERVICE
import com.crystalcheong.ozone.other.Constants.NOTIFICATION_CHANNEL_ID
import com.crystalcheong.ozone.other.Constants.PNOTIFICATION_CHANNEL_NAME
import com.crystalcheong.ozone.other.Constants.PNOTIFICATION_ID
import com.crystalcheong.ozone.other.Constants.TIMER_UPDATE_INTERVAL
import com.crystalcheong.ozone.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

//INFO: Mark for dependency injection in the class
@AndroidEntryPoint
//INFO: Observer for LiveData objects to notify the current lifecycle state
class SensorService : LifecycleService(), SensorEventListener {

    //INFO: Internal tracking of key lifecycle stages
    private var serviceKilled = false

    //INFO: Use Dagger-Hilt to inject objects defined in Service Module
    @Inject
    lateinit var sensorManager: SensorManager

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    //INFO: Notification with class specific tweaks
    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    //INFO: Class observables
    companion object {
        var steps = MutableLiveData<Int>()
        val isTracking = MutableLiveData<Boolean>()
    }

    //INFO: Assign initial values for observables
    private fun postInitialValues() {
        isTracking.postValue(false)
        steps.postValue(0)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Creating sensor service . . .")

        //INFO: Initialize notification with the injected boilerplate
        curNotificationBuilder = baseNotificationBuilder

        //INFO: Initialize the observables
        postInitialValues()

        //TODO: Initialize observer for the LiveData
        isTracking.observe(this, Observer {
            updateNotificationTrackingState(it)
        })
    }

    //TODO: React to the intent's current lifecycle state
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    //TODO: Start displaying notification banner
                    startForegroundService()
                    Timber.d("Running sensor service...")
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Paused sensor service")
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                    Timber.d("Stopped sensor service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    private fun killService() {
        serviceKilled = true
        pauseService()

        //TODO: Reset LiveData values
        postInitialValues()

        stopForeground(true)

        //INFO: LifecycleService state == onDestroy()
        stopSelf()
    }

    //INFO: Internal step count tracking
    private var totalSteps = 0
    private var stepsTaken = 0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.values[0] > Int.MAX_VALUE) {
            if (BuildConfig.DEBUG)
                Timber.d("Probably not a real value: %s", event.values[0])
            return
        } else {
            //INFO: Executes the task (ASYNC) w/o blocking the current thread
            CoroutineScope(Dispatchers.Main).launch {
                while (isTracking.value!!) {
                    stepsTaken = event.values[0].toInt()

                    //TODO: Update the observables with the latest value
                    steps.postValue(stepsTaken)

                    //INFO: Spamming listener updates might crash the app
                    delay(TIMER_UPDATE_INTERVAL)
                }
                totalSteps += stepsTaken
            }
        }
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        //TODO: Remove all notification actions before updating it
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        //INFO: Update the step notification when the service is running
        if(!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
            notificationManager.notify(PNOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    private fun registerSensor(){
        //TODO: Check for availability of step sensor before registering
        if (sensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER).size < 1) {
            Timber.d("Step sensor is NOT available on this device")
            return
        } else{
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_FASTEST)// emulator
            Timber.d("Step sensor is SUCCESSFULLY registered")
        }
    }

    private fun startForegroundService() {
        //TODO: Register sensor to retrieve sensor data
        registerSensor()

        isTracking.postValue(true)

        //INFO: Android Oreo or later, all notifications must be assigned to a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        //TODO: Start service in the foreground
        startForeground(PNOTIFICATION_ID, baseNotificationBuilder.build())

        //TODO: Update the notification with observable data
        steps.observe(this, Observer {
            if(!serviceKilled) {
                //TODO: Update the notification banner with sensor data
                val notification = curNotificationBuilder
                    .setContentText("Sensor service steps taken: $it")
                    .setContentIntent(getMainActivityPendingIntent())
                notificationManager.notify(PNOTIFICATION_ID, notification.build())
            }
        })
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            //TODO : Assign intent action routing in MainActivity & nav_graph.xml
            it.action = ACTION_SHOW_ACTIVITY_FRAGMENT
        },
        //INFO: Updates existing activity instead of relaunch
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            PNOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW  //INFO: Anything higher, it will ring for each notification
        )
        notificationManager.createNotificationChannel(channel)
    }
}














