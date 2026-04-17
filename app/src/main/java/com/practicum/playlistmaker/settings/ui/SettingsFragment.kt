package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.settings.ui.view.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()
    private var isRecreating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBar.top)
            insets
        }
        setupViews(view)
        observeUiState()
    }

    private fun setupViews(view: View) {
        val switchButton = view.findViewById<SwitchMaterial>(R.id.switch_button)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitch(isChecked)
        }

        view.findViewById<View>(R.id.btnShare).setOnClickListener {
            viewModel.onShareRequested()
        }

        view.findViewById<View>(R.id.supportButton).setOnClickListener {
            viewModel.onEmailRequested()
        }

        view.findViewById<View>(R.id.userAgreementButton).setOnClickListener {
            openUserAgreement()
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsUiState.Loading -> {
                }
                is SettingsUiState.Loaded -> {
                    view?.findViewById<SwitchMaterial>(R.id.switch_button)?.isChecked = state.isDarkTheme
                    applyThemeDynamically(state.isDarkTheme)
                }
            }
        }
        viewModel.shareApp.observe(viewLifecycleOwner) { intent ->
            startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
        }
        viewModel.sendEmail.observe(viewLifecycleOwner) { intent ->
            startActivity(intent)
        }
    }

    private fun applyThemeDynamically(isDarkMode: Boolean) {
        val mode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
