package com.crystalcheong.ozone.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.other.Constants
import com.crystalcheong.ozone.other.Constants.ACTION_PAUSE_SERVICE
import com.crystalcheong.ozone.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.crystalcheong.ozone.other.Constants.ACTION_STOP_SERVICE
import com.crystalcheong.ozone.other.Constants.FASTEST_LOCATION_INTERVAL
import com.crystalcheong.ozone.other.Constants.LOCATION_UPDATE_INTERVAL
import com.crystalcheong.ozone.other.Constants.NOTIFICATION_CHANNEL_ID
import com.crystalcheong.ozone.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.crystalcheong.ozone.other.Constants.NOTIFICATION_ID
import com.crystalcheong.ozone.other.Constants.TIMER_UPDATE_INTERVAL
import com.crystalcheong.ozone.other.Polylines
import com.crystalcheong.ozone.other.TrackingUtility
import com.crystalcheong.ozone.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
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
//INFO: Observer for LiveData objects to notify the current lifecycle state
class TrackingService : LifecycleService() {

    //INFO: Internal tracking of key lifecycle stages
    var isFirstRun = true
    var serviceKilled = false

    //TODO: Diluted measurement for notification banner (so it doesn't spam changes)
    private val timeRunInSeconds = MutableLiveData<Long>()

    //INFO: Use Dagger-Hilt to inject objects defined in Service Module
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    //INFO: Notification with class specific tweaks
    lateinit var curNotificationBuilder: NotificationCompat.Builder

    //INFO: Class observables
    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()     //TODO: Attach an observer to track status
        val pathPoints = MutableLiveData<Polylines>()   //TODO: MutableLiveData< [MutableList< [MutableList<LatLng>] -> A single polyline >] -> A list of polylines >()
    }

    //INFO: Assign initial values for observables
    private fun postInitialValues() {
        isTracking.postValue(false)

        //TODO : Add a new empty List before drawing new polylines
        pathPoints.postValue(mutableListOf())

        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Creating tracking service . . .")

        //INFO: Initialize notification with the injected boilerplate
        curNotificationBuilder = baseNotificationBuilder

        //INFO: Initialize the observables
        postInitialValues()

        //TODO: Initialize API for location tracking
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        //TODO: Initialize observer for the LiveData
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    //TODO: React to the intent's current lifecycle state
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        //TODO: Start displaying notification banner
                        startForegroundService()
                        isFirstRun = false
                        Timber.d("Running tracking service...")
                    } else {
                        //TODO: Resume stopwatch timer
                        startTimer()
                        Timber.d("Resuming tracking service...")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Paused tracking service")
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                    Timber.d("Stopped tracking service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //INFO: Internal timer tracking
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        //TODO: Begin drawing polyline
        addEmptyPolyline()

        //TODO: Update tracking observable
        isTracking.postValue(true)

        //TODO: Starts timer
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        //INFO: Executes the task (ASYNC) w/o blocking the current thread
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                //INFO: Calculate the time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                //TODO: Update the observahles with the new lapTime recorded
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    //TODO: Extract the last WHOLE second from millis
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                //INFO: Spamming listener updates might crash the app
                delay(TIMER_UPDATE_INTERVAL)
            }

            //TODO: Collate the total run duration
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()

        //TODO: Reset LiveData values
        postInitialValues()

        stopForeground(true)

        //INFO: LifecycleService state == onDestroy()
        stopSelf()
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        //TODO: Toggle the notification banner according to the intent action
        val notificationActionText = if(isTracking) "Pause" else "Resume"

        //TODO: Check the intent received from the Main Activity and react to it's corresponding action
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT) //TODO INFO: Update existing pending intent on launch
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        //TODO: Remove all actions before updating it
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        //INFO: Update the tracking notification when the service is running
        if(!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)   //TODO: Customize the notification banner
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates( //TODO: Explicitly request location updates
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            //INFO: Asserts that an expression is non-null
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        //TODO: Call function to add to last index of the polyline list
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            //TODO: Retrieve current location coordinates
            val pos = LatLng(location.latitude, location.longitude)

            pathPoints.value?.apply {
                //TODO: Append coordinates at the index of existing list
                last().add(pos)

                //TODO: Update observer with the latest value
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        //TODO: Add a new empty List before drawing new polyline
        add(mutableListOf())

        //TODO: Notify observers of changes
        pathPoints.postValue(this)

    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))   //INFO: If null, then just add a new 'empty' polyline to an new empty list


    private fun startForegroundService() {
        //TODO: Start timer to be displayed in the notification banner
        startTimer()

        isTracking.postValue(true)

        //INFO: Android Oreo or later, all notifications must be assigned to a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        //TODO: Start service in the foreground
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        //TODO: Update the notification with observable data
        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled) {
                //TODO: Update the notification banner with run timing (ms -> s)
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))  //TODO: Update the notification banner with the stopwatch timing
                    .setContentIntent(getMainActivityPendingIntent())
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            //TODO : Assign intent action routing in MainActivity & nav_graph.xml
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        //INFO: Update existing activity instead of relaunch
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW  //TODO INFO: Anything higher, it will ring for each notification
        )
        notificationManager.createNotificationChannel(channel)
    }
}














