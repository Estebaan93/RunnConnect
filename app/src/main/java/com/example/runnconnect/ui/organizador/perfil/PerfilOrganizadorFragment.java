package com.example.runnconnect.ui.organizador.perfil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.DialogCambiarPasswordBinding;
import com.example.runnconnect.databinding.DialogVerImagenBinding;
import com.example.runnconnect.databinding.FragmentPerfilOrganizadorBinding;

public class PerfilOrganizadorFragment extends Fragment {
  private FragmentPerfilOrganizadorBinding binding;
  private PerfilOrganizadorViewModel mv;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaImagen;

  private AlertDialog dialogPassword;
  private DialogCambiarPasswordBinding dialogPasswordBinding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPerfilOrganizadorBinding.inflate(inflater, container, false);
    mv = new ViewModelProvider(this).get(PerfilOrganizadorViewModel.class);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setHasOptionsMenu(true);

    mediaImagen = registerForActivityResult(
      new ActivityResultContracts.PickVisualMedia(),
      uri -> mv.onImagenSeleccionada(uri));

    setupObservers();
    setupListeners();
    mv.cargarPerfil();
  }

  //Listeners
  private void setupListeners() {
    binding.btnAccion.setOnClickListener(v ->
      mv.onBotonPrincipalClick(new PerfilOrganizadorViewModel.OrganizadorInput(
        binding.etNombreComercial.getText().toString(),
        binding.etRazonSocial.getText().toString(),
        binding.etCuit.getText().toString(),
        binding.etNombreContacto.getText().toString(),
        binding.etTelefono.getText().toString(),
        binding.etDireccionLegal.getText().toString()
      )));
    binding.btnEditAvatar.setOnClickListener(v -> mv.onEditAvatarClicked());
    binding.ivAvatar.setOnClickListener(v -> mv.onAvatarImageClicked());
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_perfil_opciones, menu);
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

  //Observers
  private void setupObservers() {

    mv.getPerfilData().observe(getViewLifecycleOwner(), p -> {
      binding.etEmail.setText(p.getEmail());
      binding.etNombreComercial.setText(p.getNombreComercial());
      binding.etRazonSocial.setText(p.getRazonSocial());
      binding.etCuit.setText(p.getCuit());
      binding.etNombreContacto.setText(p.getNombre());
      binding.etTelefono.setText(p.getTelefono());
      binding.etDireccionLegal.setText(p.getDireccionLegal());
    });

    mv.getAvatarUrl().observe(getViewLifecycleOwner(), url ->
      Glide.with(this).load(url)
        .placeholder(android.R.drawable.ic_menu_camera)
        .error(android.R.drawable.ic_menu_camera)
        .circleCrop()
        .into(binding.ivAvatar));

    // El vm limpia los errores en deshabilitarEdicion — el Fragment solo habilita/deshabilita
    mv.getIsEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etNombreComercial.setEnabled(enabled);
      binding.etRazonSocial.setEnabled(enabled);
      binding.etCuit.setEnabled(enabled);
      binding.etNombreContacto.setEnabled(enabled);
      binding.etTelefono.setEnabled(enabled);
      binding.etDireccionLegal.setEnabled(enabled);
      binding.etEmail.setEnabled(false);
    });

    mv.getBtnText().observe(getViewLifecycleOwner(), binding.btnAccion::setText);

    mv.getProgressVisibility().observe(getViewLifecycleOwner(),
      binding.progressBar::setVisibility);

    mv.getMensajeGlobal().observe(getViewLifecycleOwner(),
      binding.tvMensajeGlobal::setText);
    mv.getMensajeVisibility().observe(getViewLifecycleOwner(),
      binding.tvMensajeGlobal::setVisibility);
    mv.getMensajeColor().observe(getViewLifecycleOwner(),
      binding.tvMensajeGlobal::setTextColor);

    // Errores del perfil
    mv.getErrorNombreComercial().observe(getViewLifecycleOwner(), binding.etNombreComercial::setError);
    mv.getErrorRazonSocial().observe(getViewLifecycleOwner(),     binding.etRazonSocial::setError);
    mv.getErrorCuit().observe(getViewLifecycleOwner(),            binding.etCuit::setError);
    mv.getErrorNombreContacto().observe(getViewLifecycleOwner(),  binding.etNombreContacto::setError);
    mv.getErrorTelefono().observe(getViewLifecycleOwner(),        binding.etTelefono::setError);
    mv.getErrorDireccion().observe(getViewLifecycleOwner(),       binding.etDireccionLegal::setError);

    // Errores de password
    mv.getErrorPassActual().observe(getViewLifecycleOwner(), e -> {
      if (dialogPasswordBinding != null) {
        dialogPasswordBinding.etPassActual.setError(e);
        dialogPasswordBinding.etPassActual.requestFocus();
      }
    });
    mv.getErrorPassNuevo().observe(getViewLifecycleOwner(), e -> {
      if (dialogPasswordBinding != null) {
        dialogPasswordBinding.etPassNueva.setError(e);
      }
    });
    mv.getErrorPassConfirm().observe(getViewLifecycleOwner(), e -> {
      if (dialogPasswordBinding != null) {
        dialogPasswordBinding.etPassConfirm.setError(e);
      }
    });

    // Eventos
    mv.getEventCerrarDialogPassword().observe(getViewLifecycleOwner(),
      ignored -> {
        if (dialogPassword != null) {
          dialogPassword.dismiss();
        }
      });

    mv.getEventShowAvatarOptions().observe(getViewLifecycleOwner(),
      ignored -> mostrarDialogoOpciones());

    mv.getEventShowDeleteConfirmation().observe(getViewLifecycleOwner(),
      ignored -> mostrarDialogoConfirmacion());

    mv.getEventOpenGallery().observe(getViewLifecycleOwner(),
      ignored -> mediaImagen.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build()));

    mv.getEventShowZoomImage().observe(getViewLifecycleOwner(),
      url -> mostrarDialogoZoom(url));

    mv.getEventConfirmarBaja().observe(getViewLifecycleOwner(),
      ignored -> mostrarDialogoAdvertenciaBaja());

    mv.getEventNavegarAlLogin().observe(getViewLifecycleOwner(),
      ignored -> cerrarSesionYNavegar());
  }

  //Dialogos
  private void mostrarDialogoCambiarPassword() {
    dialogPasswordBinding = DialogCambiarPasswordBinding.inflate(getLayoutInflater());

    dialogPasswordBinding.btnDarDeBajaUsuario.setOnClickListener(v -> mv.btnDarBaja());

    dialogPassword = new AlertDialog.Builder(requireContext())
      .setView(dialogPasswordBinding.getRoot())
      .setPositiveButton("Cambiar", null)
      .setNegativeButton("Cancelar", (d, w) -> limpiarReferenciasDialogo())
      .create();

    dialogPassword.show();

    dialogPassword.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
      mv.cambiarPassword(
        dialogPasswordBinding.etPassActual.getText() != null ? dialogPasswordBinding.etPassActual.getText().toString() : "",
        dialogPasswordBinding.etPassNueva.getText() != null ? dialogPasswordBinding.etPassNueva.getText().toString() : "",
        dialogPasswordBinding.etPassConfirm.getText() != null ? dialogPasswordBinding.etPassConfirm.getText().toString() : ""
      ));

    dialogPassword.setOnDismissListener(d -> limpiarReferenciasDialogo());
  }

  private void limpiarReferenciasDialogo() {
    dialogPasswordBinding = null;
    dialogPassword = null;
  }

  private void mostrarDialogoOpciones() {
    new AlertDialog.Builder(requireContext())
      .setTitle("Foto de Perfil")
      .setItems(new String[]{"Cambiar Foto", "Eliminar Foto", "Cancelar"},
        (dialog, which) -> mv.onOpcionAvatarSeleccionada(which))
      .show();
  }

  private void mostrarDialogoConfirmacion() {
    new AlertDialog.Builder(getContext())
      .setTitle("Eliminar foto")
      .setMessage("¿Volver a la imagen por defecto?")
      .setPositiveButton("Sí", (d, w) -> mv.onDeleteConfirmed())
      .setNegativeButton("No", null)
      .show();
  }

  private void mostrarDialogoZoom(String url) {
    DialogVerImagenBinding zoomBinding = DialogVerImagenBinding.inflate(getLayoutInflater());
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(zoomBinding.getRoot());
    if (dialog.getWindow() != null)
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    Glide.with(this).load(url).into(zoomBinding.ivZoom);
    dialog.show();
  }

  private void mostrarDialogoAdvertenciaBaja() {
    new AlertDialog.Builder(getContext())
      .setTitle("¡Peligro!")
      .setMessage("¿Estás seguro de que deseas dar de baja tu cuenta? Esta acción deshabilitará tu perfil.")
      .setPositiveButton("Sí, dar de baja", (d, w) -> mv.confirmarDarDeBaja())
      .setNegativeButton("Cancelar", null)
      .show();
  }

  private void cerrarSesionYNavegar() {
    android.content.Intent intent = new android.content.Intent(
      requireActivity(), com.example.runnconnect.ui.login.LoginActivity.class);
    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK |
      android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    requireActivity().finish();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    limpiarReferenciasDialogo();
    binding = null;
  }
}