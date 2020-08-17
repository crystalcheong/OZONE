package com.crystalcheong.ozone.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class AboutFragment  : Fragment(R.layout.fragment_about){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //TODO: Toggle app bar navigation icon
        requireActivity().toolbar.logo = null
        requireActivity().tvToolbarTitle.text = ""
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_pink)
        requireActivity().toolbar.setNavigationOnClickListener {
            requireActivity().toolbar.navigationIcon = null
            requireActivity().tvToolbarTitle.text = "OZONE"
            requireActivity().toolbar.setLogo(R.drawable.ic_ozone_logo)
            requireActivity().onBackPressed()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("Created About fragment . . .")
    }

}