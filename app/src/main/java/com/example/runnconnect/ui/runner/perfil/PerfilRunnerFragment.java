package com.example.runnconnect.ui.runner.perfil;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentPerfilRunnerBinding;

public class PerfilRunnerFragment extends Fragment {

  private FragmentPerfilRunnerBinding binding;
  private PerfilRunnerViewModel mv;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaImagen;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPerfilRunnerBinding.inflate(inflater, container, false);
    mv = new ViewModelProvider(this).get(PerfilRunnerViewModel.class);

    // Inicializar selector de imagen
    mediaImagen = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) {
        mv.onImagenSeleccionada(uri);
      }
    });

    setupObservers();
    setupListeners();
    mv.cargarPerfil();

    return binding.getRoot();
  }

  private void setupListeners() {
    // La lógica de "si es editar o guardar" la decide el VM, nosotros solo mandamos datos
    binding.btnAccion.setOnClickListener(v -> recolectarYEnviar());

    binding.btnEditAvatar.setOnClickListener(v -> mv.onEditAvatarClicked());
    binding.ivAvatar.setOnClickListener(v -> mv.onAvatarImageClicked());
  }

  private void setupObservers() {
    // --- Observadores de DATOS ---
    mv.getPerfilData().observe(getViewLifecycleOwner(), p -> {
      // El fragment solo asigna, no formatea
      binding.etEmail.setText(p.getEmail());
      binding.etNombre.setText(p.getNombre());
      binding.etApellido.setText(p.getApellido());
      binding.etTelefono.setText(p.getTelefono());
      binding.etDni.setText(p.getDni() != null ? String.valueOf(p.getDni()) : "");
      binding.etFechaNac.setText(p.getFechaNacimiento()); // Fecha ya viene limpia del VM
      binding.etGenero.setText(p.getGenero());
      binding.etLocalidad.setText(p.getLocalidad());
      binding.etAgrupacion.setText(p.getAgrupacion());
      binding.etNombreContacto.setText(p.getNombreContactoEmergencia());
      binding.etTelContacto.setText(p.getTelefonoEmergencia());
    });

    mv.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
      Glide.with(this)
              .load(url)
              .placeholder(android.R.drawable.ic_menu_camera)
              .error(android.R.drawable.ic_menu_camera)
              .circleCrop()
              .into(binding.ivAvatar);
    });

    // Habilitar/Deshabilitar campos
    mv.getIsEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etNombre.setEnabled(enabled);
      binding.etApellido.setEnabled(enabled);
      binding.etTelefono.setEnabled(enabled);
      binding.etDni.setEnabled(enabled);
      binding.etFechaNac.setEnabled(enabled);
      binding.etGenero.setEnabled(enabled);
      binding.etLocalidad.setEnabled(enabled);
      binding.etAgrupacion.setEnabled(enabled);
      binding.etNombreContacto.setEnabled(enabled);
      binding.etTelContacto.setEnabled(enabled);
      binding.etEmail.setEnabled(false); // Email nunca editable
    });

    mv.getBtnText().observe(getViewLifecycleOwner(), binding.btnAccion::setText);

    mv.getIsLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    // Toast con consumo de evento (Fix para que no se repita)
    mv.getMensajeToast().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        mv.onToastConsumed(); // IMPORTANTE: Avisar que ya se mostró
      }
    });


    // --- Observadores de DIÁLOGOS (Eventos) ---

    mv.getEventShowAvatarOptions().observe(getViewLifecycleOwner(), show -> {
      if (Boolean.TRUE.equals(show)) {
        mostrarDialogoOpciones();
        mv.onAvatarOptionsConsumed();
      }
    });

    mv.getEventShowDeleteConfirmation().observe(getViewLifecycleOwner(), show -> {
      if (Boolean.TRUE.equals(show)) {
        mostrarDialogoConfirmacion();
        mv.onDeleteConfirmationConsumed();
      }
    });

    mv.getEventOpenGallery().observe(getViewLifecycleOwner(), open -> {
      if (Boolean.TRUE.equals(open)) {
        mediaImagen.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        mv.onGalleryOpenConsumed();
      }
    });

    mv.getEventShowZoomImage().observe(getViewLifecycleOwner(), url -> {
      if (url != null) {
        mostrarDialogoZoom(url);
        mv.onZoomImageConsumed();
      }
    });
  }

  // --- MÉTODOS UI ---

  private void mostrarDialogoOpciones() {
    String[] opciones = {"Cambiar Foto", "Eliminar Foto", "Cancelar"};
    new AlertDialog.Builder(getContext())
            .setTitle("Foto de Perfil")
            .setItems(opciones, (dialog, which) -> {
              if (which == 0) mv.onChangePhotoOptionSelected();
              else if (which == 1) mv.onDeletePhotoOptionSelected();
            })
            .show();
  }

  private void mostrarDialogoConfirmacion() {
    new AlertDialog.Builder(getContext())
            .setTitle("Eliminar foto")
            .setMessage("¿Estás seguro de que quieres volver a la imagen por defecto?")
            .setPositiveButton("Sí", (d, w) -> mv.onDeleteConfirmed())
            .setNegativeButton("No", null)
            .show();
  }

  private void mostrarDialogoZoom(String url) {
    Dialog dialog = new Dialog(getContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_ver_imagen);

    if (dialog.getWindow() != null) {
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    ImageView ivZoom = dialog.findViewById(com.example.runnconnect.R.id.ivZoom);
    Glide.with(this).load(url).into(ivZoom);

    dialog.show();
  }

  private void recolectarYEnviar() {
    // Solo extraemos datos (UI Logic) y los empaquetamos
    PerfilRunnerViewModel.RunnerInput input = new PerfilRunnerViewModel.RunnerInput(
            binding.etNombre.getText().toString(),
            binding.etApellido.getText().toString(),
            binding.etTelefono.getText().toString(),
            binding.etDni.getText().toString(),
            binding.etFechaNac.getText().toString(),
            binding.etGenero.getText().toString(),
            binding.etLocalidad.getText().toString(),
            binding.etAgrupacion.getText().toString(),
            binding.etNombreContacto.getText().toString(),
            binding.etTelContacto.getText().toString()
    );
    // Enviamos al VM
    mv.onBotonPrincipalClick(input);
  }
}