package com.practicum.playlistmaker.mediateka.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import android.Manifest
import com.practicum.playlistmaker.R
import android.content.pm.PackageManager
import android.app.AlertDialog
import androidx.core.app.ActivityCompat
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable
import androidx.activity.OnBackPressedCallback
import android.os.Build
import androidx.activity.result.PickVisualMediaRequest
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.practicum.playlistmaker.mediateka.ui.view.CreatePlaylistViewModel

class CreatePlaylistFragment : Fragment() {

    private lateinit var viewModel: CreatePlaylistViewModel
    private var selectedCoverUri: Uri? = null
    private lateinit var createButton: Button
    private lateinit var nameField: TextInputLayout
    private lateinit var descriptionField: TextInputLayout
    private lateinit var coverImage: ImageView
    private lateinit var backButton: TextView

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            selectedCoverUri = it
            coverImage.setImageURI(it)
            updateCreateButtonState() // Обновляем состояние кнопки при выборе обложки
        }
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Скрыть нижнюю панель навигации на экране создания плейлиста
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_create_playlist, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CreatePlaylistViewModel::class.java]
        setupViews(view)
        setupTextWatchers()
        setupClickListeners()
        setupObservers()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showDiscardChangesDialog()
                } else {
                    this.remove()
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
    private fun setupViews(view: View) {
        createButton = view.findViewById(R.id.createPlaylistButton)
        nameField = view.findViewById(R.id.playlistNameField)
        descriptionField = view.findViewById(R.id.playlistDescriptionField)
        coverImage = view.findViewById(R.id.playlistCoverImage)
        backButton = view.findViewById(R.id.back)
    }
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreateButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
        nameField.editText?.addTextChangedListener(textWatcher)
        descriptionField.editText?.addTextChangedListener(textWatcher)
    }
    private fun updateCreateButtonState() {
        val name = nameField.editText?.text.toString().trim()
        createButton.isEnabled = name.isNotBlank()
    }
    private fun setupClickListeners() {
        coverImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                requestReadStoragePermission()
            }
        }
        backButton.setOnClickListener {
            if (hasUnsavedChanges()) {
                showDiscardChangesDialog()
            } else {
                findNavController().popBackStack()
            }
        }
        createButton.setOnClickListener {
            createPlaylist()
        }
    }
    private fun createPlaylist() {
        val playlistName = nameField.editText?.text.toString().trim()
        val playlistDescription = descriptionField.editText?.text.toString().trim() ?: ""
        if (playlistName.isNotBlank()) {
            createButton.isEnabled = false
            createButton.text = getString(R.string.creating_playlist)
            viewModel.createPlaylist(playlistName, playlistDescription, selectedCoverUri, requireContext())
        } else {
            nameField.error = getString(R.string.playlist_name_required)
        }
    }
    private fun setupObservers() {
        viewModel.success.observe(viewLifecycleOwner, Observer { successMessage ->
            successMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                (requireParentFragment() as? FragmentPlaylists)?.refreshPlaylists()
                findNavController().popBackStack()
            }
        })
        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                createButton.isEnabled = true
                createButton.text = getString(R.string.create_playlist)
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun requestReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.permission_rationale_title))
            .setMessage(getString(R.string.permission_rationale_message))
            .setPositiveButton(getString(R.string.allow)) { _, _ ->
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton(getString(R.string.deny)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun hasUnsavedChanges(): Boolean {
        val name = nameField.editText?.text.toString().trim()
        val description = descriptionField.editText?.text.toString().trim()
        return name.isNotBlank() || description.isNotBlank() || selectedCoverUri != null
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.discard_changes_title)) // Заголовок диалога
            .setMessage(getString(R.string.discard_changes_message)) // Сообщение диалога
            .setPositiveButton(getString(R.string.discard)) { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { }
            .create()
            .show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    companion object {
        private const val REQUEST_READ_STORAGE = 1001
    }
}
