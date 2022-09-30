package com.androidandrew.sunscreen.ui.main

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.databinding.FragmentMainBinding
import com.github.mikephil.charting.data.LineData
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val mainViewModel: MainViewModel by viewModel()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        mainViewModel.chartData.observe(viewLifecycleOwner) { lineDataSet ->
            UvChartFormatter.formatDataSet(lineDataSet)
            binding.uvChart.apply {
                UvChartFormatter.formatChart(
                    lineChart = this,
                    use24HourTime = DateFormat.is24HourFormat(requireContext()))
                data = LineData(lineDataSet)
                invalidate()
            }
        }

        return binding.root
    }
}