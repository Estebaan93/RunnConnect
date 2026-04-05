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
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentPerfilOrganizadorBinding;

public class PerfilOrganizadorFragment extends Fragment {

  private FragmentPerfilOrganizadorBinding binding;
  private PerfilOrganizadorViewModel mv;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaImagen;

  private AlertDialog dialogPassword;
  private EditText etPassActualRef, etPassNuevaRef, etPassConfirmRef;

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

    // El vm ya limpia los errores en deshabilitarEdicion() — el Fragment solo habilita/deshabilita
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

    // Errores de password — las refs existen porque el vm solo emite con el dialogo abierto
    mv.getErrorPassActual().observe(getViewLifecycleOwner(), e -> {
      etPassActualRef.setError(e);
      etPassActualRef.requestFocus();
    });
    mv.getErrorPassNuevo().observe(getViewLifecycleOwner(),   e -> etPassNuevaRef.setError(e));
    mv.getErrorPassConfirm().observe(getViewLifecycleOwner(), e -> etPassConfirmRef.setError(e));

    // Eventos — pulso, el Fragment solo ejecuta
    mv.getEventCerrarDialogPassword().observe(getViewLifecycleOwner(),
      ignored -> dialogPassword.dismiss());

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
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_password, null);

    etPassActualRef  = view.findViewById(R.id.etPassActual);
    etPassNuevaRef   = view.findViewById(R.id.etPassNueva);
    etPassConfirmRef = view.findViewById(R.id.etPassConfirm);

    view.findViewById(R.id.btnDarDeBajaUsuario)
      .setOnClickListener(v -> mv.btnDarBaja());

    dialogPassword = new AlertDialog.Builder(requireContext())
      .setView(view)
      .setPositiveButton("Cambiar", null)
      .setNegativeButton("Cancelar", (d, w) -> limpiarReferenciasDialogo())
      .create();

    dialogPassword.show();

    dialogPassword.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
      mv.cambiarPassword(
        etPassActualRef.getText().toString(),
        etPassNuevaRef.getText().toString(),
        etPassConfirmRef.getText().toString()
      ));

    dialogPassword.setOnDismissListener(d -> limpiarReferenciasDialogo());
  }

  private void limpiarReferenciasDialogo() {
    etPassActualRef  = null;
    etPassNuevaRef   = null;
    etPassConfirmRef = null;
    dialogPassword   = null;
  }

  private void mostrarDialogoOpciones() {
    new AlertDialog.Builder(getContext())
      .setTitle("Foto de Perfil")
      .setItems(new String[]{"Cambiar Foto", "Eliminar Foto", "Cancelar"}, (dialog, which) -> {
        if (which == 0) mv.onChangePhotoOptionSelected();
        else if (which == 1) mv.onDeletePhotoOptionSelected();
      }).show();
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
    Dialog dialog = new Dialog(getContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_ver_imagen);
    if (dialog.getWindow() != null)
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    Glide.with(this).load(url).into((ImageView) dialog.findViewById(R.id.ivZoom));
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
    binding = null;
  }
}