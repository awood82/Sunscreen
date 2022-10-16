package com.androidandrew.sunscreen.ui.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.databinding.FragmentLocationBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocationFragment : Fragment() {

    private val locationViewModel: LocationViewModel by viewModel()
    private lateinit var binding: FragmentLocationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_location,
            container,
            false
        )

        binding.viewModel = locationViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.navigate.collect { destination ->
                    if (destination > 0) {
                        findNavController().navigate(destination)
                        locationViewModel.onNavigationComplete()
                    }
                }
            }
        }

        return binding.root
    }
}