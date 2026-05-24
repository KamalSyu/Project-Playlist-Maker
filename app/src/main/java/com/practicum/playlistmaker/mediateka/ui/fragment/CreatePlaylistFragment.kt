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
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable
import androidx.activity.OnBackPressedCallback
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.mediateka.ui.CreatePlaylistUiState
import com.practicum.playlistmaker.mediateka.ui.view.CreatePlaylistViewModel
import org.koin.android.ext.android.inject

class CreatePlaylistFragment : Fragment() {

    private val viewModel: CreatePlaylistViewModel by inject()
    private lateinit var createButton: Button
    private lateinit var nameField: TextInputLayout
    private lateinit var descriptionField: TextInputLayout
    private lateinit var coverImage: ImageView
    private lateinit var backButton: TextView

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.updateSelectedCoverUri(uri)
        } else {
            viewModel.updateSelectedCoverUri(null)
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
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        val view = inflater.inflate(R.layout.fragment_create_playlist, container, false)
        setupViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let { bundle ->
            val playlistName = bundle.getString("playlistName", "")
            val playlistDescription = bundle.getString("playlistDescription", "")
            val coverUri = bundle.getParcelable<Uri>("selectedCoverUri")

            viewModel.updatePlaylistName(playlistName)
            viewModel.updatePlaylistDescription(playlistDescription)
            if (coverUri != null) {
                viewModel.updateSelectedCoverUri(coverUri)
            }
        }

        setupTextWatchers()
        setupClickListeners()
        setupObservers()
        updateCreateButtonState()

        if (savedInstanceState == null) {
            nameField.editText?.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameField.editText, InputMethodManager.SHOW_IMPLICIT)
        }

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

        // Инициализация изображения обложки — берём данные из ViewModel
        val currentState = viewModel.uiState.value
        updateCoverImage(currentState?.selectedCoverUri)
    }

    private fun updateCoverImage(uri: Uri?) {
        uri?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .into(coverImage)
        } ?: run {
            coverImage.setImageResource(R.drawable.ic_placeholder_312)
        }
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
        val state = viewModel.uiState.value
        val playlistName = state?.playlistName?.trim().orEmpty()

        createButton.isEnabled = playlistName.isNotBlank()
        if (playlistName.isNotBlank() && nameField.error != null) {
            nameField.error = null
        }
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
        nameField.error = null
        val state = viewModel.uiState.value ?: return
        val playlistName = state.playlistName.trim()

        if (playlistName.isNotBlank()) {
            createButton.isEnabled = false
            createButton.text = getString(R.string.creating_playlist)
            viewModel.createPlaylist(requireContext())
        } else {
            nameField.error = getString(R.string.playlist_name_required)
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state: CreatePlaylistUiState ->
            // Обновляем изображение обложки
            updateCoverImage(state.selectedCoverUri)

            // Обновляем текстовые поля (если они не в фокусе)
            if (!nameField.hasFocus()) {
                nameField.editText?.setText(state.playlistName)
            }
            if (!descriptionField.hasFocus()) {
                descriptionField.editText?.setText(state.playlistDescription)
            }

            // Показываем сообщение об успехе
            state.successMessage?.let { successMessage ->
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                (requireParentFragment() as? FragmentPlaylists)?.refreshPlaylists()
                findNavController().popBackStack()
                viewModel.clearSuccess()
            }

            // Показываем сообщение об ошибке
            state.error?.let { errorMessage ->
                createButton.isEnabled = true
                createButton.text = getString(R.string.create_playlist)
                when {
                    errorMessage.contains("Не удалось скопировать обложку") -> {
                        Toast.makeText(
                            requireContext(),
                            "Не удалось сохранить обложку. Проверьте доступ к хранилищу.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.clearError()
            }
            updateCreateButtonState()
        }
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
        val state = viewModel.uiState.value ?: return false
        val name = state.playlistName.trim()
        val description = state.playlistDescription.trim()
        return name.isNotBlank() || description.isNotBlank() || state.selectedCoverUri != null
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.discard_changes_title)) // «Завершить создание плейлиста?»
            .setMessage(getString(R.string.discard_changes_message)) // «Все несохранённые данные будут потеряны»
            .setPositiveButton(getString(R.string.discard)) { _, _ ->
                viewModel.clearSuccess()  // Сброс состояния успеха
                viewModel.clearError()   // Сброс состояния ошибки
                clearFormState()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss() // Закрытие диалога, пользователь остаётся на экране
            }
            .setOnCancelListener { }
            .create()
            .show()
    }

    private fun clearFormState() {
        viewModel.clearForm()
        nameField.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("nameFieldHasError", nameField.error != null)

        if (nameField.hasFocus()) {
            outState.putString("playlistName", nameField.editText?.text.toString())
        }

        if (descriptionField.hasFocus()) {
            outState.putString("playlistDescription", descriptionField.editText?.text.toString())
        }

        val currentState = viewModel.uiState.value
        currentState?.selectedCoverUri?.let { uri ->
            outState.putParcelable("selectedCoverUri", uri)
        }
    }

    companion object {
        private const val REQUEST_READ_STORAGE = 1001
    }
}
