package com.androidandrew.sunscreen.ui.init

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
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
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FragmentInitBinding>(
            inflater,
            R.layout.fragment_init,
            container,
            false
        ).apply {
            composeView.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MaterialTheme {
                        InitScreen(initViewModel)
                    }
                }
            }
        }

        binding.viewModel = initViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                initViewModel.location.collect { destinationId ->
                    if (destinationId > 0) {
                        findNavController().navigate(destinationId)
                    }
                }
            }
        }

        return binding.root
    }
}