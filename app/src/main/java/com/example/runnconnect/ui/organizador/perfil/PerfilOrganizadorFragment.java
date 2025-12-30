package com.example.runnconnect.ui.organizador.perfil;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentPerfilOrganizadorBinding;

public class PerfilOrganizadorFragment extends Fragment {
  private FragmentPerfilOrganizadorBinding binding;
  private PerfilOrganizadorViewModel mv;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaImagen;

  // Referencias Dialogo Password
  private AlertDialog dialogPassword;
  private EditText etPassActualRef, etPassNuevaRef, etPassConfirmRef;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPerfilOrganizadorBinding.inflate(inflater, container, false);
    mv = new ViewModelProvider(this).get(PerfilOrganizadorViewModel.class);

    // Habilitar menu (engranaje)
    setHasOptionsMenu(true);

    //inicializar selector de imagen
    mediaImagen = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      //if (uri != null)
        mv.onImagenSeleccionada(uri);
    });

    setupObservers();
    setupListeners();
    mv.cargarPerfil();

    return binding.getRoot();

  }

  private void setupListeners() {
    binding.btnAccion.setOnClickListener(v -> recolectarYEnviar());
    binding.btnEditAvatar.setOnClickListener(v -> mv.onEditAvatarClicked());
    binding.ivAvatar.setOnClickListener(v -> mv.onAvatarImageClicked());
  }

  //MENU Y DIALOGO PASSWORD
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_perfil_opciones, menu); // Reutilizamos el XML del Runner
    super.onCreateOptionsMenu(menu, inflater);
  }
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_cambiar_pass) {
      mostrarDialogoCambiarPassword();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
  private void mostrarDialogoCambiarPassword() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_password, null);

    etPassActualRef = view.findViewById(R.id.etPassActual);
    etPassNuevaRef = view.findViewById(R.id.etPassNueva);
    etPassConfirmRef = view.findViewById(R.id.etPassConfirm);

    builder.setView(view)
      .setPositiveButton("Cambiar", null)
      .setNegativeButton("Cancelar", (d, w) -> limpiarReferenciasDialogo());

    dialogPassword = builder.create();
    dialogPassword.show();

    dialogPassword.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      etPassActualRef.setError(null);
      etPassNuevaRef.setError(null);
      etPassConfirmRef.setError(null);

      String actual = etPassActualRef.getText().toString();
      String nueva = etPassNuevaRef.getText().toString();
      String confirm = etPassConfirmRef.getText().toString();

      mv.cambiarPassword(actual, nueva, confirm);
    });

    dialogPassword.setOnDismissListener(d -> limpiarReferenciasDialogo());
  }

  private void limpiarReferenciasDialogo() {
    etPassActualRef = null; etPassNuevaRef = null; etPassConfirmRef = null; dialogPassword = null;
  }

  private void setupObservers() {
    // Datos del Perfil
    mv.getPerfilData().observe(getViewLifecycleOwner(), p -> {
      binding.etEmail.setText(p.getEmail());
      // Mapeo especifico Organizador
      binding.etNombreComercial.setText(p.getNombreComercial());
      binding.etRazonSocial.setText(p.getRazonSocial());
      binding.etCuit.setText(p.getCuit());
      binding.etNombreContacto.setText(p.getNombre()); // Nombre del usuario es el contacto
      binding.etTelefono.setText(p.getTelefono());
      binding.etDireccionLegal.setText(p.getDireccionLegal());
    });

    mv.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
      Glide.with(this)
        .load(url)
        .placeholder(android.R.drawable.ic_menu_camera)
        .error(android.R.drawable.ic_menu_camera)
        .circleCrop()
        .into(binding.ivAvatar);
    });

    // Estado Edicion
    mv.getIsEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etNombreComercial.setEnabled(enabled);
      binding.etRazonSocial.setEnabled(enabled);
      binding.etCuit.setEnabled(enabled);
      binding.etNombreContacto.setEnabled(enabled);
      binding.etTelefono.setEnabled(enabled);
      binding.etDireccionLegal.setEnabled(enabled);
      binding.etEmail.setEnabled(false); // Nunca editable

      if (!enabled) { // Limpiar errores visuales
        binding.etNombreComercial.setError(null);
        binding.etRazonSocial.setError(null);
        binding.etCuit.setError(null);
        binding.etNombreContacto.setError(null);
        binding.etTelefono.setError(null);
        binding.etDireccionLegal.setError(null);
      }
    });

    mv.getBtnText().observe(getViewLifecycleOwner(), binding.btnAccion::setText);

    mv.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    // Mensajes y Errores
    mv.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });
    mv.getEsMensajeError().observe(getViewLifecycleOwner(), isError ->
      binding.tvMensajeGlobal.setTextColor(isError ? Color.RED : Color.parseColor("#008000")));

    mv.getErrorNombreComercial().observe(getViewLifecycleOwner(), e -> binding.etNombreComercial.setError(e));
    mv.getErrorRazonSocial().observe(getViewLifecycleOwner(), e -> binding.etRazonSocial.setError(e));
    mv.getErrorCuit().observe(getViewLifecycleOwner(), e -> binding.etCuit.setError(e));
    mv.getErrorNombreContacto().observe(getViewLifecycleOwner(), e -> binding.etNombreContacto.setError(e));
    mv.getErrorTelefono().observe(getViewLifecycleOwner(), e -> binding.etTelefono.setError(e));
    mv.getErrorDireccion().observe(getViewLifecycleOwner(), e -> binding.etDireccionLegal.setError(e));

    // OBSERVER PASSWORD (logica visual errores en dialog)
    mv.getMensajePassword().observe(getViewLifecycleOwner(), msg -> {
      if (msg == null) return;

      if (msg.startsWith("Éxito") || msg.startsWith("Exito")) {
        if (dialogPassword != null && dialogPassword.isShowing()) dialogPassword.dismiss();
        binding.tvMensajeGlobal.setText(msg);
        binding.tvMensajeGlobal.setTextColor(Color.parseColor("#008000"));
        binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
      }
      else if (msg.startsWith("Error")) {
        if (dialogPassword != null && dialogPassword.isShowing()) {
          if (msg.contains("campos son obligatorios")) {
            if(etPassActualRef.getText().toString().isEmpty()) etPassActualRef.setError("Requerido");
            if(etPassNuevaRef.getText().toString().isEmpty()) etPassNuevaRef.setError("Requerido");
            if(etPassConfirmRef.getText().toString().isEmpty()) etPassConfirmRef.setError("Requerido");
          } else if (msg.contains("no coinciden")) {
            etPassConfirmRef.setError("No coinciden"); etPassConfirmRef.requestFocus();
          } else if (msg.contains("6 caracteres")) {
            etPassNuevaRef.setError("Mínimo 6"); etPassNuevaRef.requestFocus();
          } else {
            etPassActualRef.setError(msg.replace("Error: ", "")); etPassActualRef.requestFocus();
          }
        } else {
          binding.tvMensajeGlobal.setText(msg);
          binding.tvMensajeGlobal.setTextColor(Color.RED);
          binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
        }
      }
      mv.limpiarMensajePassword();
    });


    // Eventos de Dialogs
    mv.getEventShowAvatarOptions().observe(getViewLifecycleOwner(), show -> {
      if (Boolean.TRUE.equals(show)) {
        mostrarDialogoOpciones();
        mv.onAvatarOptionsConsumed();
      }
    });
    mv.getEventShowDeleteConfirmation().observe(getViewLifecycleOwner(), show -> {
      if (Boolean.TRUE.equals(show)) { mostrarDialogoConfirmacion(); mv.onDeleteConfirmationConsumed(); }
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

  private void recolectarYEnviar() {
    PerfilOrganizadorViewModel.OrganizadorInput input = new PerfilOrganizadorViewModel.OrganizadorInput(
      binding.etNombreComercial.getText().toString(),
      binding.etRazonSocial.getText().toString(),
      binding.etCuit.getText().toString(),
      binding.etNombreContacto.getText().toString(),
      binding.etTelefono.getText().toString(),
      binding.etDireccionLegal.getText().toString()
    );
    mv.onBotonPrincipalClick(input);
  }

  //metodos UI Auxiliares (Dialogs)
  private void mostrarDialogoOpciones() {
    String[] opciones = {"Cambiar Foto", "Eliminar Foto", "Cancelar"};
    new AlertDialog.Builder(getContext())
      .setTitle("Foto de Perfil")
      .setItems(opciones, (dialog, which) -> {
        if (which == 0) mv.onChangePhotoOptionSelected();
        else if (which == 1) mv.onDeletePhotoOptionSelected();
      }).show();
  }

  private void mostrarDialogoConfirmacion() {
    new AlertDialog.Builder(getContext())
      .setTitle("Eliminar foto")
      .setMessage("¿Volver a la imagen por defecto?")
      .setPositiveButton("Sí", (d, w) -> mv.onDeleteConfirmed())
      .setNegativeButton("No", null).show();
  }

  private void mostrarDialogoZoom(String url) {
    Dialog dialog = new Dialog(getContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_ver_imagen);
    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    ImageView ivZoom = dialog.findViewById(R.id.ivZoom);
    Glide.with(this).load(url).into(ivZoom);
    dialog.show();
  }
}