package com.crystalcheong.ozone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.crystalcheong.ozone.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.other.Constants.ACTION_SHOW_ACTIVITY_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

//TODO INFO: Mark for dependency injection in the class
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //INFO: Enable full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main)

        //TODO: Use the current intent regardless of lifecycle state (Navigation banner action routing)
        navigateToTrackingFragmentIfNeeded(intent)

        //TODO: Utilized specified custom toolbar/appbar
        setSupportActionBar(toolbar)

        //TODO: Link the nav_graph with the respective fragments
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */ }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ -> //TODO INFO: ['controller'] & ['arguments'] are '_" because they are not needed
                when(destination.id) {
                    //TODO: Only display nav buttons for the specified fragments
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment, R.id.activityFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
                when(destination.id) {
                    //TODO: Append toolbar to the mapped fragment destination
                    R.id.settingsFragment -> tvToolbarSubtitle.text = "Profile"
                    R.id.runFragment -> tvToolbarSubtitle.text = "Routes"
                    R.id.statisticsFragment -> tvToolbarSubtitle.text = "Report"
                    R.id.trackingFragment -> tvToolbarSubtitle.text = "Running"
                    else -> tvToolbarSubtitle.text = ""
                }
            }
    }

    //TODO INFO: When lifecycle state is onCreate()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    //TODO: Listen if attached action of an intent is ['ACTION_SHOW_TRACKING_FRAGMENT']
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        intent?.let{
            when(it.action){
                ACTION_SHOW_TRACKING_FRAGMENT -> {
                    Timber.d("action : TRACKING")
                    navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
                }
                ACTION_SHOW_ACTIVITY_FRAGMENT -> {
                    Timber.d("action : SENSOR")
                    navHostFragment.findNavController().navigate(R.id.action_global_activityFragment)
                }
            }
        }
    }
}
