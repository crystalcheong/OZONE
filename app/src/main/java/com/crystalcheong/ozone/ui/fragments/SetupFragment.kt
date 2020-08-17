package com.crystalcheong.ozone.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.crystalcheong.ozone.other.Constants.KEY_HEIGHT
import com.crystalcheong.ozone.other.Constants.KEY_NAME
import com.crystalcheong.ozone.other.Constants.KEY_TARGET
import com.crystalcheong.ozone.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import timber.log.Timber
import javax.inject.Inject

//INFO: Mark for dependency injection in the class
@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    //TODO: Initialize the common shared preferences
    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    //INFO: @set Explicitly specifies that the @Inject annotation should be applied to the setter that will be generated in Java.
    @set:Inject
    var name = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //INFO: Only display the fragment on the first launch else, redirect to the RunFragment
        if(!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_activityFragment,
                savedInstanceState,
                navOptions
            )
        }

        var validWeight = false
        //NOTE: Arguments for [start, before, count] are not needed
        tilWeight.editText?.doOnTextChanged{ weight, _, _, _ ->
            if(weight!!.isNotEmpty()){
                val weightInput = weight.toString().trim().toFloat()
                if(weightInput <= 30F || weightInput > 700F){
                    validWeight = false
                    tilWeight.helperText = "INVALID WEIGHT"
                    Timber.d("WEIGHT must be more than 30kg")
                } else{
                    validWeight = true
                    tilWeight.isHelperTextEnabled = false
                    Timber.d("WEIGHT input : $weight")
                }
            }
        }

        var validHeight = false
        //NOTE: Arguments for [start, before, count] are not needed
        tilHeight.editText?.doOnTextChanged{ height, _, _, _ ->
            if(height!!.isNotEmpty()){
                val heightInput = height.toString().trim().toFloat()
                if(heightInput <= 100F || heightInput > 260F){
                    validHeight = false
                    tilHeight.helperText = "INVALID HEIGHT"
                    Timber.d("HEIGHT must be more than 100cm")
                } else{
                    validHeight = true
                    tilHeight.isHelperTextEnabled = false
                    Timber.d("HEIGHT input : $height")
                }
            }
        }

        var validTarget = false
        //NOTE: Arguments for [start, before, count] are not needed
        tilTarget.editText?.doOnTextChanged{ target, _, _, _ ->
            // Respond to input text change
            if(target!!.isNotEmpty()){
                val targetSteps : Float = target.toString().trim().toFloat()
                if(targetSteps <= 0F){
                    validTarget = false
                    tilTarget.helperText = "CANNOT BE ZERO"
                    Timber.d("TARGET cannot be lesser than zero")
                } else{
                    validTarget = true
                    tilTarget.isHelperTextEnabled = false
                    Timber.d("TARGET input : $target")
                }
            }
        }


        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()   //TODO: Save user input into the shared preferences
            if(success) {
                findNavController().navigate(R.id.action_setupFragment_to_activityFragment)
            } else {
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT).show()
            }
        }
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

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = etName.text.toString().trim()
        val weight = etWeight.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val target = etTarget.text.toString().trim()
        if(name.isEmpty() || weight.isEmpty() ||  height.isEmpty() || target.isEmpty()) {
            return false    //TODO: Only save when there is data
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat()) //TODO: Enforce decimal in EditText
            .putFloat(KEY_HEIGHT, height.toFloat()) //TODO: Enforce decimal in EditText
            .putInt(KEY_TARGET, target.toInt()) //TODO: Enforce int in EditText
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }

}