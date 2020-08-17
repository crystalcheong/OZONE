package com.crystalcheong.ozone.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.db.Run
import com.crystalcheong.ozone.other.Constants
import com.crystalcheong.ozone.other.Constants.ACTION_PAUSE_SERVICE
import com.crystalcheong.ozone.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.crystalcheong.ozone.other.Constants.ACTION_STOP_SERVICE
import com.crystalcheong.ozone.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.crystalcheong.ozone.other.Constants.MAP_ZOOM
import com.crystalcheong.ozone.other.Constants.POLYLINE_COLOR
import com.crystalcheong.ozone.other.Constants.POLYLINE_WIDTH
import com.crystalcheong.ozone.other.Polyline
import com.crystalcheong.ozone.other.TrackingUtility
import com.crystalcheong.ozone.services.TrackingService
import com.crystalcheong.ozone.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

//INFO: Mark for dependency injection in the class
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    //INFO: Use Dagger-Hilt to inject objects defined in Service Module
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    //INFO: Fetch data from ViewModels to be displayed/utilized
    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    //INFO: @set Explicitly specifies that the @Inject annotation should be applied to the setter that will be generated in Java.
    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //TODO: Toggle app bar actions visibility
        setHasOptionsMenu(true)
        //TODO: Toggle app bar navigation icon
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_pink)
        requireActivity().toolbar.setNavigationOnClickListener {
            requireActivity().toolbar.navigationIcon = null
            requireActivity().onBackPressed()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Utilizes custom dialog fragment to survive screen rotation
        if(savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        Timber.d("RECORDED weight : $weight kg")

        //INFO: Start the MapView lifecycle
        mapView.onCreate(savedInstanceState)

        //TODO: Assign click event to button to toggle location tracking state
        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        //TODO: Assign click event to button to save run into DB
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        //TODO: Initialize GoogleMap object in MapView
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        //TODO: Invoke function to retrieve sensor data from various observables
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        //TODO: Update service state with listener's observable object(s)
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        //TODO: Draw polyline using the coordinates provided by the listener's observable object(s)
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        //INFO: Display run data recorded by listener's observable object
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime

            if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
                var distanceInMeters = 0
                for(polyline in pathPoints) {
                    //TODO: Loop through polylines to sum up total distance
                    distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
                }
                val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f

                tvDistance.text = "%.2f".format((distanceInMeters / 1000F))
                tvPace.text = "%.2f".format(avgSpeed)
            }
        })
    }

    private fun toggleRun() {
        //TODO: Toggle intent action to notify observers of lifecycle state
        if(isTracking) {
            //TODO: Display action icon when run has started
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        //INFO: Only if run has already started
        if(curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //INFO: Map each menu option to an action via loop
        when(item.itemId) {
            R.id.miCancelTracking -> {
                //TODO: Display confirmation prompt (alert dialog)
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //TODO: Invoke custom dialog fragment
    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            //INFO: Assign functionality to positive button
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        requireActivity().toolbar.navigationIcon = null
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    //TODO: Toggle UI buttons based on tracking state
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking && curTimeInMillis > 0L) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking){
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    //TODO INFO: Zoom to latest user location
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    //TODO: Zoom out to take screenshot
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                //TODO: Loop through polylines to sum up total distance
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            //TODO INFO: /1000 to secs, /60, to min, /60 to hrs, round(*10) to remove decimal place, /10 to get only 1 d.p
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        //TODO: Loop through list of LatLng to redraw polylines
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            //TODO INFO: Reference the last PAIR of coordinates in list
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            //TODO INFO: Reference the last coordinates in list
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    //TODO: Utilize the TrackingService to toggle the tracking state
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            //TODO INFO: Delivers the intent to the service to be executed
            requireContext().startService(it)
        }

    //TODO INFO: Declare all of the MapView's lifecycle stages
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}











