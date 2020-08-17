package com.crystalcheong.ozone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crystalcheong.ozone.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint  //TODO INFO: Mark for dependency injection in the class
class SplashScreenActivity : AppCompatActivity() {

    lateinit var handler: Handler

    //INFO: @set Explicitly specifies that the @Inject annotation should be applied to the setter that will be generated in Java.
    @set:Inject
    var isFirstAppOpen = true

    @set:Inject
    var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //TODO INFO: Only display the Snackbar greeting after initial setup
        if(!isFirstAppOpen) {
            Snackbar.make(
                findViewById(R.id.clSplashScreen),
                "Welcome back, ${name.toUpperCase()}",
                Snackbar.LENGTH_LONG
            ).show()
        }


        //TODO: Display splash screen for 3 secs
        handler = Handler()
        handler.postDelayed({
            val intent = if(!isFirstAppOpen){
                Intent(this, MainActivity::class.java)
            } else{
                Intent(this, TutorialActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}