package com.crystalcheong.ozone.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.adapters.TutorialAdapter
import kotlinx.android.synthetic.main.activity_tutorial.*
import timber.log.Timber

class TutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        //INFO: Define ViewPager items
        val images = listOf<Int>(
            0,
            R.drawable.tutorial_activity,
            R.drawable.tutorial_tracking,
            R.drawable.tutorial_statistics
        )
        val titles = listOf<String>(
            "Welcome to OZONE Activity!",
            "MAKE EVERY STEP COUNT",
            "REAL-TIME MAP TRACKER",
            "MANAGE YOUR HEALTH DATA"
        )
        val descriptions = listOf<String>(
            "Get instant insights when you exercise and see real-time stats for your runs, walks and bike-rides.",
            "OZONE uses the in-built motion sensor to read your daily activity to save battery.\nTo do that, it needs access to motion data",
            "In GPS tracking mode, step counter tracks your fitness activity in detail, and records your routes on the map with GPS in real-time",
            "OZONE can show you information about your activities to give you a holistic view of your health"
        )

        //TODO: Set the adapter to an instance of the TutorialAdapter
        val adapter = TutorialAdapter(images, titles, descriptions)
        vpTutorial.adapter = adapter

        //TODO: Link the indicator to the ViewPager
        ciIndicator.setViewPager(vpTutorial)

        vpTutorial.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("Selected_Page  $position")

                if(position == titles.size - 1){
                    tvNext.text = "Setup Profile"

                    tvNext.setOnClickListener {
                        //TODO: Redirect to MainActivity
                        var intent = Intent(this@TutorialActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                } else{
                    tvNext.text = "Next"

                    tvNext.setOnClickListener {
                        vpTutorial.apply {
                            beginFakeDrag()
                            fakeDragBy(-700F)
                            endFakeDrag()
                        }
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }
}