package com.practicum.playlistmaker.mediateka.ui.fragment

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.view.FragmentPlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.Manifest
import com.google.android.material.snackbar.Snackbar

class CreatePlaylistFragment : Fragment() {

    private val viewModel: FragmentPlaylistsViewModel by viewModel()
    private var selectedCoverUri: Uri? = null
    private var isDataChanged = false
    private var showDiscardDialog = true

    private lateinit var createButton: Button
    private lateinit var nameField: TextInputLayout
    private lateinit var descriptionField: TextInputLayout
    private lateinit var coverImage: ImageView
    private lateinit var backButton: TextView

    private var nameTextWatcher: TextWatcher? = null
    private var descriptionTextWatcher: TextWatcher? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            selectedCoverUri = it
            coverImage.setImageURI(it)
            isDataChanged = hasUnsavedChanges()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_create_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createButton = view.findViewById(R.id.createPlaylistButton)
        nameField = view.findViewById(R.id.playlistNameField)
        descriptionField = view.findViewById(R.id.playlistDescriptionField)
        coverImage = view.findViewById(R.id.playlistCoverImage)
        backButton = view.findViewById(R.id.back)

        nameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createButton.isEnabled = s.toString().trim().isNotBlank()
                isDataChanged = hasUnsavedChanges()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        nameField.editText?.addTextChangedListener(nameTextWatcher)

        descriptionTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isDataChanged = hasUnsavedChanges()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        descriptionField.editText?.addTextChangedListener(descriptionTextWatcher)

        coverImage.setOnClickListener {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                else -> {
                    requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
        backButton.setOnClickListener {
            handleBackNavigation()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })

        createButton.setOnClickListener {
            val playlistName = nameField.editText?.text.toString().trim()
            val playlistDescription = descriptionField.editText?.text.toString().trim() ?: ""

            if (playlistName.isNotBlank()) {
                viewModel.createNewPlaylist(playlistName, playlistDescription, selectedCoverUri, requireContext())
                Toast.makeText(
                    requireContext(),
                    getString(R.string.playlist_created, playlistName),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } else {
                nameField.error = getString(R.string.playlist_name_required)
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(
                    requireView(),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        nameField.editText?.removeTextChangedListener(nameTextWatcher)
        descriptionField.editText?.removeTextChangedListener(descriptionTextWatcher)
        nameTextWatcher = null
        descriptionTextWatcher = null
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Доступ к медиафайлам")
            .setMessage("Для выбора обложки плейлиста приложению нужен доступ к вашим фотографиям. Разрешить?")
            .setPositiveButton("Разрешить") { _, _ ->
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun hasUnsavedChanges(): Boolean {
        val name = nameField.editText?.text.toString().trim()
        val description = descriptionField.editText?.text.toString().trim()
        return name.isNotBlank() || description.isNotBlank() || selectedCoverUri != null
    }

    private fun handleBackNavigation() {
        if (isDataChanged && showDiscardDialog) {
            showDiscardChangesDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showDiscardChangesDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохранённые данные будут потеряны")
            .setPositiveButton("Завершить") { _, _ ->
                showDiscardDialog = false
                findNavController().popBackStack()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { }
            .create()

        dialog.show()
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            showPermissionRationale()
        }
    }

    companion object {
        fun newInstance(): Fragment {
            return CreatePlaylistFragment()
        }
    }
}
