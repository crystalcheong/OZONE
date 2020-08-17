package com.crystalcheong.ozone.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.ui.viewmodels.StatisticsViewModel
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.other.Constants
import com.crystalcheong.ozone.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.crystalcheong.ozone.other.Constants.KEY_NAME
import com.crystalcheong.ozone.other.Constants.KEY_TARGET
import com.crystalcheong.ozone.other.Constants.KEY_WEIGHT
import com.crystalcheong.ozone.other.SensorUtility
import com.crystalcheong.ozone.other.TrackingUtility
import com.crystalcheong.ozone.services.SensorService
import com.crystalcheong.ozone.services.TrackingService
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_activity.*
import kotlinx.android.synthetic.main.fragment_activity.tvTotalCalories
import kotlinx.android.synthetic.main.fragment_activity.tvTotalDistance
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

//INFO: Mark for dependency injection in the class
@AndroidEntryPoint
class ActivityFragment  : Fragment(R.layout.fragment_activity), EasyPermissions.PermissionCallbacks {

    //NOTE: Fetch data from ViewModels to be displayed/utilized
    private val viewModel: StatisticsViewModel by viewModels()

    //INFO: Use Dagger-Hilt to inject objects defined in Service Module
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    //INFO: @set Explicitly specifies that the @Inject annotation should be applied to the setter that will be generated in Java.
    @set:Inject
    var weight = 80f

    private var totalRunDistance = 0F
    private var totalRunCalories = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("Created Activity fragment . . .")

        //TODO: Check & request for user permission
        requestPermissions()

        //TODO: Populate with stored data
        loadFieldsFromSharedPref()

        //TODO: Initiate sensor service
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)

        //TODO: Retrieve data updates from listener(s)
        subscribeToObservers()

        btnStartRun.setOnClickListener{
            //TODO: Redirect to TrackingFragment with globally defined navigation action
            findNavController().navigate(R.id.action_activityFragment_to_trackingFragment)
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val target = sharedPreferences.getInt(KEY_TARGET, 2500)

        //INFO: Append the TextViews with information
        tvHeaderTitle.text = SimpleDateFormat("E, d MMMM").format(Date())
        tvStepsTarget.text = "/ $target"
    }

    private fun subscribeToObservers() {
        //TODO: Display latest sensor data from listener's observable object(s)
        SensorService.steps.observe(viewLifecycleOwner, Observer {
            val currentStepsTaken = it
            Timber.d("Current steps : $currentStepsTaken")

            //INFO: Update TextView and ProgressBar
            tvSteps.text = currentStepsTaken.toString()

            //TODO: Display real-time data
            val target = sharedPreferences.getInt(KEY_TARGET, 2500).toFloat()
            val curProgress = (currentStepsTaken.toFloat() / target) * 100
            cpbSteps.apply {
                setProgressWithAnimation(curProgress)

                progressBarColorStart = ContextCompat.getColor(requireContext(), R.color.brand_pink)
                progressBarColorEnd = ContextCompat.getColor(requireContext(), R.color.brand_pink_light)
                progressBarColorDirection = CircularProgressBar.GradientDirection.LEFT_TO_RIGHT
            }
            Timber.d("Progress : $curProgress")

            val totalDistanceInKM = ((currentStepsTaken * 78).toFloat() / 100000F)

            val totalDistance = totalRunDistance + (Math.round(totalDistanceInKM * 10F) / 10F)
            tvTotalDistance.text = totalDistance.toString()
            Timber.d("Total distance : $totalDistance")

            val caloriesBurned = (totalDistanceInKM * weight).toInt()
            val totalCalories = totalRunCalories + caloriesBurned
            tvTotalCalories.text = totalCalories.toString()
        })

        //TODO: Display cumulative distance from view model's observable object(s)
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000F
                totalRunDistance = Math.round(km * 10F) / 10F
            }
        })

        //TODO: Display cumulative calories expenditure from view model's observable object(s)
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                totalRunCalories = it
            }
        })
    }
    //TODO: Only hide toolbar for this fragment
    override fun onResume() {
        super.onResume()
        requireActivity().toolbar.isVisible = false
    }

    //TODO: Display toolbar on exit for the rest of the fragments
    override fun onStop() {
        super.onStop()
        requireActivity().toolbar.isVisible = true
    }

    //TODO: Invoke step sensor service (ALWAYS RUNNING)
    private fun sendCommandToService(action: String){
        Intent(requireContext(), SensorService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
        Timber.d("Sent command to listener class . . . ")
    }

    //TODO: Request for all required permissions (ACTIVITY, LOCATION)
    private fun requestPermissions(){
        if(TrackingUtility.hasLocationPermissions(requireContext()) && SensorUtility.hasActivityPermissions(requireContext())) {  //TODO: Utilize method listed in others/TrackingUtility
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location & activity permissions to use this app.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            //INFO: Prompt Settings dialog if any permission has been permanently denied
            AppSettingsDialog.Builder(this)
                .build().show()
        } else{
            //INFO: Re-prompt permission request if it is not already granted
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //TODO: Allow the EasyPermissions to be notified of the permission request result
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}