package com.androidandrew.sunscreen.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.databinding.FragmentMainBinding
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

        mainViewModel.sunUnitsToday.observe(viewLifecycleOwner) { sunUnits ->
            Toast.makeText(context, "$sunUnits units", Toast.LENGTH_LONG).show()
        }

        mainViewModel.vitaminDUnitsToday.observe(viewLifecycleOwner) { iu ->
            Toast.makeText(context, "Vitamin D $iu IU", Toast.LENGTH_LONG).show()
        }

        mainViewModel.networkResponse.observe(viewLifecycleOwner) { response ->
            android.util.Log.e("Sunscreen", response)
            Toast.makeText(context, "Response: $response", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }
}