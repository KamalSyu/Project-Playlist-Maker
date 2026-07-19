package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.view.EditPlaylistViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    override val viewModel: EditPlaylistViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->

            if (nameField.editText?.text.toString() != state.playlistName) {
                nameField.editText?.setText(state.playlistName)
            }

            if (descriptionField.editText?.text.toString() != state.playlistDescription) {
                descriptionField.editText?.setText(state.playlistDescription)
            }

            updateCoverImage(state.selectedCoverUri)

            createButton.text = getString(R.string.save)
            backButton.text = getString(R.string.edit_playlist)
        }
    }

    override protected fun setupOnBackPressedCallback() {
        val callback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this.remove()
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}
