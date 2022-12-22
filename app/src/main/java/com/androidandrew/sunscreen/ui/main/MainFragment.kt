package com.androidandrew.sunscreen.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.databinding.FragmentMainBinding
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModel()
    private lateinit var binding: FragmentMainBinding
    private val chartFormatter: UvChartFormatter by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate<FragmentMainBinding>(
            inflater,
            R.layout.fragment_main,
            container,
            false
        ).apply {
            composeView.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MaterialTheme {
                        MainScreen(mainViewModel)
                    }
                }
            }
        }

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        mainViewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(binding.main, message, Snackbar.LENGTH_LONG).show()
        }

        mainViewModel.closeKeyboard.observe(viewLifecycleOwner) {
            closeKeyboard()
        }

        return binding.root
    }

    private fun closeKeyboard() {
        activity?.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}