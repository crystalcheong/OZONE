package com.crystalcheong.ozone.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crystalcheong.ozone.R
import com.crystalcheong.ozone.adapters.RunAdapter
import com.crystalcheong.ozone.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.crystalcheong.ozone.other.SortType
import com.crystalcheong.ozone.other.TrackingUtility
import com.crystalcheong.ozone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

//TODO INFO: Mark for dependency injection in the class
@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    //TODO INFO: Fetch data from ViewModels to be displayed/utilized
    private val viewModel: MainViewModel by viewModels()

    //INFO: RecyclerView adapter to display dynamic content
    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        //TODO INFO: Array declared in strings.xml
        when(viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        //TODO: Refresh the data send to the RecyclerView adapter based on filter menu
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            //INFO: Populate RecyclerView with universal ViewModel
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }

        //INFO: Invoke RecyclerView adapter's defined AsyncListDiffer object to only asynchronously update changes based on ViewModel's LiveData object
        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        fab.setOnClickListener {
            //TODO: Redirect to RunFragment with globally defined navigation action
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    //TODO: Utilize RecyclerView() from RunAdpater
    private fun setupRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

}






