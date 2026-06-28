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
import android.content.Intent
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable
import androidx.activity.OnBackPressedCallback
import android.os.Build
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.core.utils.DashedRoundedBorderDrawable
import com.practicum.playlistmaker.mediateka.ui.view.CreatePlaylistViewModel
import org.koin.android.ext.android.inject
import java.io.File

class CreatePlaylistFragment : Fragment() {
    private val viewModel: CreatePlaylistViewModel by inject()
    private lateinit var createButton: Button
    private lateinit var nameField: TextInputLayout
    private lateinit var descriptionField: TextInputLayout
    private lateinit var coverImage: ImageView
    private lateinit var backButton: TextView
    private lateinit var borderImage: ImageView
    private lateinit var playlistCenterIcon: ImageView

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
        savedInstanceState?.let { bundle ->
            val playlistName = bundle.getString("playlistName", "")
            val playlistDescription = bundle.getString("playlistDescription", "")
            val coverUri = bundle.getParcelable("selectedCoverUri", Uri::class.java)
            val nameFieldHasError = bundle.getBoolean("nameFieldHasError", false)
            if (playlistName.isNotBlank()) {
                nameField.editText?.setText(playlistName)
                nameField.editText?.setSelection(playlistName.length)
            }
            if (playlistDescription.isNotBlank()) {
                descriptionField.editText?.setText(playlistDescription)
                descriptionField.editText?.setSelection(playlistDescription.length)
            }
            if (coverUri != null) {
                viewModel.updateSelectedCoverUri(coverUri)
                updateCoverImage(coverUri)
            }
            if (nameFieldHasError) {
                nameField.post {
                    nameField.error = getString(R.string.playlist_name_required)
                }
            }
        }
        setupTextWatchers()
        setupErrorClearingOnInput()

        setupClickListeners()
        setupObservers()
        if (savedInstanceState == null) {
            nameField.editText?.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameField.editText, InputMethodManager.SHOW_IMPLICIT)
        }
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showDiscardChangesDialog()
                } else {
                    this.remove()
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        borderImage.setImageDrawable(DashedRoundedBorderDrawable())
    }

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
            if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showGoToSettingsDialog()
            } else {
                showPermissionRationale()
            }
        }
    }

    private fun setupViews(view: View) {
        createButton = view.findViewById(R.id.createPlaylistButton)
        nameField = view.findViewById(R.id.playlistNameField)
        descriptionField = view.findViewById(R.id.playlistDescriptionField)
        coverImage = view.findViewById(R.id.playlistCoverImage)
        backButton = view.findViewById(R.id.back)
        borderImage = view.findViewById(R.id.playlistBorderImage)
        playlistCenterIcon = view.findViewById(R.id.playlistCenterIcon)

        val currentState = viewModel.uiState.value
        updateCoverImage(currentState?.selectedCoverUri)
    }

    private fun updateCoverImage(uri: Uri?) {
        val state = viewModel.uiState.value
        if (state?.isCreated == true && uri == null) {
            coverImage.setImageResource(R.drawable.ic_placeholder_312)
            playlistCenterIcon.visibility = View.VISIBLE
        } else {
            uri?.let { sourceUri ->
                val file = try {
                    when (sourceUri.scheme) {
                        "file" -> File(sourceUri.path ?: "")
                        "content" -> {
                            val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
                            val cursor = requireContext().contentResolver.query(sourceUri, projection, null, null, null)
                            if (cursor != null) {
                                try {
                                    if (cursor.moveToFirst()) {
                                        val index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
                                        File(cursor.getString(index))
                                    } else {
                                        null
                                    }
                                } finally {
                                    cursor.close()
                                }
                            } else {
                                null
                            }
                        }
                        else -> File(sourceUri.path ?: "")
                    }
                } catch (e: Exception) {
                    Log.w("CoverImage", "Не удалось превратить URI в File: ${e.message}")
                    null
                }

                if (file != null && file.exists()) {
                    Glide.with(this)
                        .load(file)
                        .placeholder(R.drawable.ic_placeholder_312)
                        .error(R.drawable.ic_placeholder_312)
                        .into(coverImage)
                    playlistCenterIcon.visibility = View.GONE
                } else {
                    Glide.with(this)
                        .load(sourceUri)
                        .placeholder(R.drawable.ic_placeholder_312)
                        .error(R.drawable.ic_placeholder_312)
                        .into(coverImage)
                    playlistCenterIcon.visibility = View.GONE
                }
            } ?: run {
                coverImage.setImageDrawable(null)
                playlistCenterIcon.visibility = View.VISIBLE
            }
        }
    }

    private fun setupTextWatchers() {
        val nameTextWatcher = object : TextWatcher {
            private var isUpdating = false
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                val text = s.toString()
                viewModel.updatePlaylistName(text)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
        nameField.editText?.addTextChangedListener(nameTextWatcher)

        val descriptionTextWatcher = object : TextWatcher {
            private var isUpdating = false
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                val text = s.toString()
                viewModel.updatePlaylistDescription(text)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
        descriptionField.editText?.addTextChangedListener(descriptionTextWatcher)
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
        if (state.isCreateButtonEnabled) {
            viewModel.createPlaylist(requireContext())
        } else {
            nameField.postDelayed({
                nameField.error = getString(R.string.playlist_name_required)
            }, 100)
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateCoverImage(state.selectedCoverUri)
            createButton.isEnabled = state.isCreateButtonEnabled

            state.successMessage?.let { message ->
                val inflater = LayoutInflater.from(requireContext())
                val view = inflater.inflate(R.layout.toast_playlist_created, null)
                val textView = view.findViewById<TextView>(R.id.toast_text)
                textView.text = message

                val metrics = requireContext().resources.displayMetrics
                val density = metrics.density
                val screenWidthPx = metrics.widthPixels
                val marginPx = ((7f + 8f) * density).toInt() // 15 dp в пикселях
                val toastWidthPx = screenWidthPx - marginPx

                view.layoutParams = ViewGroup.LayoutParams(toastWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)

                val toast = Toast(requireContext()).apply {
                    setView(view)
                    setGravity(
                        android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM,
                        0,
                        16
                    )
                    duration = Toast.LENGTH_SHORT
                }
                toast.show()

                findNavController().popBackStack()
                viewModel.clearSuccess()
            }

            state.error?.let { errorMessage ->
                nameField.error = errorMessage
                viewModel.clearError()
            }
        }
    }

    private fun requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionRationale()
                } else {
                    requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        } else {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.permission_rationale_title))
            .setMessage(getString(R.string.permission_denied_permanently_message))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
        return name.isNotBlank()
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.discard_changes_title))
            .setMessage(getString(R.string.discard_changes_message))
            .setPositiveButton(getString(R.string.discard)) { _, _ ->
                viewModel.clearSuccess()
                viewModel.clearError()
                clearFormState()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { }
            .create()
            .show()
    }

    private fun clearFormState() {
        viewModel.clearForm()
        nameField.error = null
        nameField.editText?.setText("")
        descriptionField.editText?.setText("")
        updateCoverImage(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val state = viewModel.uiState.value ?: return
        outState.putString("playlistName", state.playlistName)
        outState.putString("playlistDescription", state.playlistDescription)
        state.selectedCoverUri?.let { uri ->
            outState.putParcelable("selectedCoverUri", uri)
        }
        outState.putBoolean("nameFieldHasError", nameField.error != null)
        outState.putBoolean("isCreateButtonEnabled", state.isCreateButtonEnabled)
    }

    private fun setupErrorClearingOnInput() {
        val nameTextWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (nameField.error != null && !s.isNullOrEmpty()) {
                    nameField.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
        nameField.editText?.addTextChangedListener(nameTextWatcher)
    }
}
