package com.crystalcheong.ozone.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.other.Constants.KEY_HEIGHT
import com.crystalcheong.ozone.other.Constants.KEY_NAME
import com.crystalcheong.ozone.other.Constants.KEY_TARGET
import com.crystalcheong.ozone.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_activity.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.etName
import kotlinx.android.synthetic.main.fragment_settings.etTarget
import kotlinx.android.synthetic.main.fragment_settings.etWeight
import kotlinx.android.synthetic.main.fragment_settings.tilHeight
import kotlinx.android.synthetic.main.fragment_settings.tilTarget
import kotlinx.android.synthetic.main.fragment_settings.tilWeight
import timber.log.Timber
import javax.inject.Inject

//INFO: Mark for dependency injection in the class
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    //INFO: Use Dagger-Hilt to inject objects defined in Service Module
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        requireActivity().tvToolbarSubtitle.text = "Profile"

        loadFieldsFromSharedPref()

        var validWeight = false
        tilWeight.editText?.doOnTextChanged{ weight, _, _, _ ->
            // Respond to input text change
            if(weight!!.isNotEmpty()){
                if(weight.toString().trim().toFloat() <= 30F){
                    validWeight = false
                    tilWeight.helperText = "INVALID HEIGHT"
                    Timber.d("WEIGHT must be more than 30kg")
                } else{
                    validWeight = true
                    tilWeight.isHelperTextEnabled = false
                    Timber.d("WEIGHT input : $weight")
                }
            }
        }

        var validHeight = false
        tilHeight.editText?.doOnTextChanged{ height, _, _, _ ->
            // Respond to input text change
            if(height!!.isNotEmpty()){
                if(height.toString().trim().toFloat() <= 100F){
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


        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success) {
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }

        tvAbout.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        val height = sharedPreferences.getFloat(KEY_HEIGHT, 160f)
        val target = sharedPreferences.getInt(KEY_TARGET, 2500)
        etName.setText(name)
        etWeight.setText(weight.toString().trim())
        etHeight.setText(height.toString().trim())
        etTarget.setText(target.toString().trim())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = etName.text.toString().trim()
        val weightText = etWeight.text.toString().trim()
        val heightText = etHeight.text.toString().trim()
        val targetText = etTarget.text.toString().trim()
        if(nameText.isEmpty() || weightText.isEmpty() || heightText.isEmpty() || targetText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .putFloat(KEY_HEIGHT, heightText.toFloat())
            .putInt(KEY_TARGET, targetText.toInt())
            .apply()
        return true
    }
}