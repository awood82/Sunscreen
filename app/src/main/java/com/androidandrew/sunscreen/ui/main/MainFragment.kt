package com.androidandrew.sunscreen.ui.main

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.databinding.FragmentMainBinding
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Integer.min

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
            binding.uvChart.apply {
                data = LineData(lineDataSet)
                invalidate()
            }
        }

        return binding.root
    }
}