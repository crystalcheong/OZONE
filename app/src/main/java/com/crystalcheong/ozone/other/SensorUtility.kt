package com.crystalcheong.ozone.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object SensorUtility {    //TODO INFO: The purpose is to request for the permissions based on the Android build version

    //TODO INFO: Below Android Q, the app don't need to explicitly request for permission because it can track the location by default
    fun hasActivityPermissions(context: Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        }
}
















