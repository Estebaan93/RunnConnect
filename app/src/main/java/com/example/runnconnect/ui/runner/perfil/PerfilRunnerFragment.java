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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import android.app.DatePickerDialog;

import java.util.Calendar;
import java.util.List;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentPerfilRunnerBinding;
import com.example.runnconnect.utils.LocalidadesHelper;

public class PerfilRunnerFragment extends Fragment {

  private FragmentPerfilRunnerBinding binding;
  private PerfilRunnerViewModel mv;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaImagen;
  private final String[] opcGenero = {"F", "M", "X"};

  private AlertDialog dialogPassword;
  private EditText etPassActualRef, etPassNuevaRef, etPassConfirmRef;

  private ArrayAdapter<String> adapterProvincia;
  private ArrayAdapter<String> adapterLocalidad;
  private String provinciaSeleccionada = "San Luis";
  private String localidadSeleccionada = "";

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPerfilRunnerBinding.inflate(inflater, container, false);
    mv = new ViewModelProvider(this).get(PerfilRunnerViewModel.class);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
      requireContext(), android.R.layout.simple_spinner_dropdown_item, opcGenero);
    binding.spGenero.setAdapter(adapter);

    //
    binding.spGenero.setEnabled(false);
    binding.spProvincia.setEnabled(false);
    binding.spLocalidad.setEnabled(false);
    binding.etFechaNac.setEnabled(false);

    setupSpinnersUbicacion();

    mediaImagen = registerForActivityResult(
      new ActivityResultContracts.PickVisualMedia(),
      uri -> mv.onImagenSeleccionada(uri));

    setHasOptionsMenu(true);
    setupObservers();
    setupListeners();
    mv.cargarPerfil();

    return binding.getRoot();
  }

  //Spinners de ubicacion
  private void setupSpinnersUbicacion() {
    List<String> provincias = LocalidadesHelper.getProvincias();
    adapterProvincia = new ArrayAdapter<>(
      requireContext(), android.R.layout.simple_spinner_dropdown_item, provincias);
    binding.spProvincia.setAdapter(adapterProvincia);

    binding.spProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        provinciaSeleccionada = provincias.get(position);
        cargarLocalidades(provinciaSeleccionada);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void cargarLocalidades(String provincia) {
    List<String> localidades = LocalidadesHelper.getLocalidades(provincia);
    adapterLocalidad = new ArrayAdapter<>(
      requireContext(), android.R.layout.simple_spinner_dropdown_item, localidades);
    binding.spLocalidad.setAdapter(adapterLocalidad);

    binding.spLocalidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        localidadSeleccionada = localidades.get(position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  //Listeners
  private void setupListeners() {
    binding.btnAccion.setOnClickListener(v -> recolectarYEnviar());
    binding.btnEditAvatar.setOnClickListener(v -> mv.onEditAvatarClicked());
    binding.ivAvatar.setOnClickListener(v -> mv.onAvatarImageClicked());
    binding.etFechaNac.setOnClickListener(v ->
      mv.onFechaNacClick(binding.etFechaNac.getText().toString()));
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
      binding.etNombre.setText(p.getNombre());
      binding.etApellido.setText(p.getApellido());
      binding.etTelefono.setText(p.getTelefono());
      binding.etDni.setText(p.getDni() != null ? String.valueOf(p.getDni()) : "");
      binding.etFechaNac.setText(p.getFechaNacimiento());
      binding.spGenero.setSelection(mv.obtenerIndiceGenero(p.getGenero(), opcGenero));

      if (p.getLocalidad() != null && !p.getLocalidad().isEmpty()) {
        String[] parts = p.getLocalidad().split(", ");
        String ciudadBD = parts[0];
        String provinciaBD = parts.length > 1 ? parts[1] : "San Luis";

        int posProv = adapterProvincia.getPosition(provinciaBD);
        if (posProv >= 0) {
          binding.spProvincia.setSelection(posProv);
          cargarLocalidades(provinciaBD);
          binding.spLocalidad.post(() -> {
            if (adapterLocalidad != null) {
              int posLoc = adapterLocalidad.getPosition(ciudadBD);
              if (posLoc >= 0) binding.spLocalidad.setSelection(posLoc);
            }
          });
        }
      }

      binding.etAgrupacion.setText(p.getAgrupacion());
      binding.etNombreContacto.setText(p.getNombreContactoEmergencia());
      binding.etTelContacto.setText(p.getTelefonoEmergencia());
    });

    mv.getAvatarUrl().observe(getViewLifecycleOwner(), url ->
      Glide.with(this).load(url)
        .placeholder(android.R.drawable.ic_menu_camera)
        .error(android.R.drawable.ic_menu_camera)
        .circleCrop()
        .into(binding.ivAvatar));

    mv.getIsEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etNombre.setEnabled(false); // esto se debe deshabilitar luego de completar el perfil
      binding.etApellido.setEnabled(false); // esto se debe deshabilitar luego de completar el perfil
      binding.etEmail.setEnabled(false);

      binding.etTelefono.setEnabled(enabled);
      binding.etDni.setEnabled(enabled);
      binding.etFechaNac.setEnabled(enabled);
      binding.spGenero.setEnabled(enabled);
      binding.spGenero.setAlpha(enabled ? 1.0f : 0.7f);
      binding.etAgrupacion.setEnabled(enabled);
      binding.etNombreContacto.setEnabled(enabled);
      binding.etTelContacto.setEnabled(enabled);
      binding.spProvincia.setEnabled(enabled); // esto se debe deshabilitar luego de completar el perfil
      binding.spLocalidad.setEnabled(enabled); // esto se debe deshabilitar luego de completar el perfil

    });

    //Habilitacion condicionada por el ViewModel (Una sola carga)
    mv.getIsDniEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etDni.setEnabled(enabled);
    });

    mv.getIsUbicacionEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.spProvincia.setEnabled(enabled);
      binding.spLocalidad.setEnabled(enabled);
      binding.spProvincia.setAlpha(enabled ? 1.0f : 0.7f);
      binding.spLocalidad.setAlpha(enabled ? 1.0f : 0.7f);
    });

    mv.getIsFechaNacEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etFechaNac.setEnabled(enabled);
    });

    mv.getIsGeneroEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.spGenero.setEnabled(enabled);
      binding.spGenero.setAlpha(enabled ? 1.0f : 0.7f);
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
    mv.getErrorNombre().observe(getViewLifecycleOwner(), binding.etNombre::setError);
    mv.getErrorApellido().observe(getViewLifecycleOwner(), binding.etApellido::setError);
    mv.getErrorDni().observe(getViewLifecycleOwner(), binding.etDni::setError);
    mv.getErrorTelefono().observe(getViewLifecycleOwner(), binding.etTelefono::setError);
    mv.getErrorFechaNac().observe(getViewLifecycleOwner(), binding.etFechaNac::setError);
    mv.getErrorAgrupacion().observe(getViewLifecycleOwner(), binding.etAgrupacion::setError);
    mv.getErrorNombreContacto().observe(getViewLifecycleOwner(), binding.etNombreContacto::setError);
    mv.getErrorTelContacto().observe(getViewLifecycleOwner(), binding.etTelContacto::setError);

    // Errores de password
    mv.getErrorPassActual().observe(getViewLifecycleOwner(), e -> {
      etPassActualRef.setError(e);
      etPassActualRef.requestFocus();
    });
    mv.getErrorPassNuevo().observe(getViewLifecycleOwner(), e -> etPassNuevaRef.setError(e));
    mv.getErrorPassConfirm().observe(getViewLifecycleOwner(), e -> etPassConfirmRef.setError(e));

    // Eventos
    mv.getEventShowDatePicker().observe(getViewLifecycleOwner(),
      fechaBase -> mostrarCalendario(fechaBase));

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

    mv.getEventCerrarDialogPassword().observe(getViewLifecycleOwner(),
      ignored -> dialogPassword.dismiss());

    mv.getEventConfirmarBaja().observe(getViewLifecycleOwner(),
      ignored -> mostrarDialogoAdvertenciaBaja());

    mv.getEventNavegarAlLogin().observe(getViewLifecycleOwner(),
      ignored -> cerrarSesionYNavegar());
  }

  //Dialogos
  private void mostrarDialogoCambiarPassword() {
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_password, null);

    etPassActualRef = view.findViewById(R.id.etPassActual);
    etPassNuevaRef = view.findViewById(R.id.etPassNueva);
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
    etPassActualRef = null;
    etPassNuevaRef = null;
    etPassConfirmRef = null;
    dialogPassword = null;
  }

  private void mostrarCalendario(String fechaActual) {
    Calendar cal = mv.obtenerFechaCalendario(fechaActual);

    new DatePickerDialog(
      requireContext(),
      (view, year, month, dayOfMonth) -> {
        binding.etFechaNac.setText(mv.procesarFechaSeleccionada(year, month, dayOfMonth));
        binding.etFechaNac.setError(null);
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH)
    ) {{
      getDatePicker().setMaxDate(System.currentTimeMillis());
    }}.show();
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

  // Recoleccion de datos
  private void recolectarYEnviar() {
    String ubicacionFinal = localidadSeleccionada + ", " + provinciaSeleccionada;
    mv.onBotonPrincipalClick(new PerfilRunnerViewModel.RunnerInput(
      binding.etNombre.getText().toString(),
      binding.etApellido.getText().toString(),
      binding.etTelefono.getText().toString(),
      binding.etDni.getText().toString(),
      binding.etFechaNac.getText().toString(),
      binding.spGenero.getSelectedItem().toString(),
      ubicacionFinal,
      binding.etAgrupacion.getText().toString(),
      binding.etNombreContacto.getText().toString(),
      binding.etTelContacto.getText().toString()
    ));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}