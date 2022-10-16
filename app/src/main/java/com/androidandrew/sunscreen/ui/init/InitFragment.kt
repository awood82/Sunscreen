package com.androidandrew.sunscreen.ui.init

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
import com.androidandrew.sunscreen.databinding.FragmentInitBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InitFragment : Fragment() {

    private val initViewModel: InitViewModel by viewModel()
    private lateinit var binding: FragmentInitBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_init,
            container,
            false
        )

        binding.viewModel = initViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                initViewModel.navigate.collect { action ->
                    if (action > 0) {
                        findNavController().navigate(action)
                        initViewModel.onNavigationComplete()
                    }
                }
            }
        }

        return binding.root
    }
}