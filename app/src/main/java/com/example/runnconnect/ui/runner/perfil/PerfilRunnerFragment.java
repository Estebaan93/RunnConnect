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
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import android.app.DatePickerDialog;
import java.util.Calendar;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentPerfilRunnerBinding;

public class PerfilRunnerFragment extends Fragment {

  private FragmentPerfilRunnerBinding binding;
  private PerfilRunnerViewModel mv;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaImagen;
  private final String [] opcGenero={"F", "M", "X"};

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPerfilRunnerBinding.inflate(inflater, container, false);
    mv = new ViewModelProvider(this).get(PerfilRunnerViewModel.class);

    //spinner de genero
    ArrayAdapter<String> adapter=new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_dropdown_item, opcGenero);
    binding.spGenero.setAdapter(adapter);
    //deshabilitado por defecto
    binding.spGenero.setEnabled(false);


    // inicializar selector de imagen
    mediaImagen = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      //if (uri != null) {
        mv.onImagenSeleccionada(uri);
      //}
    });

    setupObservers();
    setupListeners();
    mv.cargarPerfil(); //cargar datos iniciales

    return binding.getRoot();
  }

  private void setupListeners() {
    // la logica de "si es editar o guardar" la decide el VM, nosotros solo mandamos datos
    binding.btnAccion.setOnClickListener(v -> recolectarYEnviar());

    binding.btnEditAvatar.setOnClickListener(v -> mv.onEditAvatarClicked());
    binding.ivAvatar.setOnClickListener(v -> mv.onAvatarImageClicked());

    binding.etFechaNac.setOnClickListener(v->{
      //solo abrimos el calendario si esta habilitado para editar el perfil
      /*if(binding.etFechaNac.isEnabled()){
        mostrarCalendario();
      }*/
      mv.onFechaNacClick(binding.etFechaNac.getText().toString());
    });
  }

  private void mostrarCalendario(String fechaActual) {
    //pedimos al VM los datos listos para el calendario
    //el Fragment no parsea nada.
    Calendar cal = mv.obtenerFechaCalendario(fechaActual);

    int anio = cal.get(Calendar.YEAR);
    int mes = cal.get(Calendar.MONTH);
    int dia = cal.get(Calendar.DAY_OF_MONTH);

    //mostramos UI (DatePicker es un componente de UI)
    DatePickerDialog datePicker = new DatePickerDialog(
      requireContext(),
      (view, year, month, dayOfMonth) -> {
        // pedimos al VM que formatee el resultado
        //el Fragment no formatea Strings.
        String fechaTexto = mv.procesarFechaSeleccionada(year, month, dayOfMonth);
        //actuali la vista
        binding.etFechaNac.setText(fechaTexto);
        //limpiamos error si existia
        binding.etFechaNac.setError(null);
      },
      anio, mes, dia
    );

    datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
    datePicker.show();
  }

  private void setupObservers() {
    //observadores de DATOS
    mv.getPerfilData().observe(getViewLifecycleOwner(), p -> {
      //el fragment solo asigna, no formatea
      binding.etEmail.setText(p.getEmail());
      binding.etNombre.setText(p.getNombre());
      binding.etApellido.setText(p.getApellido());
      binding.etTelefono.setText(p.getTelefono());
      binding.etDni.setText(p.getDni() != null ? String.valueOf(p.getDni()) : "");

      binding.etFechaNac.setText(p.getFechaNacimiento()); // fecha ya viene limpia del VM

      binding.spGenero.setSelection(mv.obtenerIndiceGenero(p.getGenero(), opcGenero));

      binding.etLocalidad.setText(p.getLocalidad());
      binding.etAgrupacion.setText(p.getAgrupacion());
      binding.etNombreContacto.setText(p.getNombreContactoEmergencia());
      binding.etTelContacto.setText(p.getTelefonoEmergencia());
    });
    mv.getEventShowDatePicker().observe(getViewLifecycleOwner(), fechaBase -> {
      if (fechaBase != null) {
        mostrarCalendario(fechaBase); //abre el dialog
        mv.onDatePickerShown(); // Reseteamos el evento
      }
    });

    mv.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
      Glide.with(this)
              .load(url)
              .placeholder(android.R.drawable.ic_menu_camera)
              .error(android.R.drawable.ic_menu_camera)
              .circleCrop()
              .into(binding.ivAvatar);
    });

    //habilitar/deshabilitar campos
    mv.getIsEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etNombre.setEnabled(enabled);
      binding.etApellido.setEnabled(enabled);
      binding.etTelefono.setEnabled(enabled);
      binding.etDni.setEnabled(enabled);
      binding.etFechaNac.setEnabled(enabled);
      binding.spGenero.setEnabled(enabled);

      binding.spGenero.setAlpha(enabled ? 1.0f : 0.7f);

      binding.etLocalidad.setEnabled(enabled);
      binding.etAgrupacion.setEnabled(enabled);
      binding.etNombreContacto.setEnabled(enabled);
      binding.etTelContacto.setEnabled(enabled);
      binding.etEmail.setEnabled(false); // email nunca editable

      if (!enabled) { // limpiar errores al salir de edicion
        binding.etNombre.setError(null);
        binding.etApellido.setError(null);
        binding.etDni.setError(null);
        binding.etTelefono.setError(null);
        binding.etFechaNac.setError(null);
        binding.etLocalidad.setError(null);
        binding.etAgrupacion.setError(null);
        binding.etNombreContacto.setError(null);
        binding.etTelContacto.setError(null);
      }
    });

    mv.getBtnText().observe(getViewLifecycleOwner(), binding.btnAccion::setText);

    mv.getIsLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    //mensajes globales
    mv.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });
    mv.getEsMensajeError().observe(getViewLifecycleOwner(), isError -> {
      binding.tvMensajeGlobal.setTextColor(isError ? Color.RED : Color.parseColor("#008000")); // Rojo o Verde oscuro
    });

    //errores en campos
    mv.getErrorNombre().observe(getViewLifecycleOwner(), e -> binding.etNombre.setError(e));
    mv.getErrorApellido().observe(getViewLifecycleOwner(), e -> binding.etApellido.setError(e));
    mv.getErrorDni().observe(getViewLifecycleOwner(), e -> binding.etDni.setError(e));
    mv.getErrorTelefono().observe(getViewLifecycleOwner(), e -> binding.etTelefono.setError(e));
    mv.getErrorFechaNac().observe(getViewLifecycleOwner(), e -> binding.etFechaNac.setError(e));
    mv.getErrorLocalidad().observe(getViewLifecycleOwner(), e -> binding.etLocalidad.setError(e));
    mv.getErrorAgrupacion().observe(getViewLifecycleOwner(), e -> binding.etAgrupacion.setError(e));
    mv.getErrorNombreContacto().observe(getViewLifecycleOwner(), e -> binding.etNombreContacto.setError(e));
    mv.getErrorTelContacto().observe(getViewLifecycleOwner(), e -> binding.etTelContacto.setError(e));


    //eventos
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

  //metodos ui
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
    //solo extraemos datos y los empaquetamos
    String generoSeleccionado= binding.spGenero.getSelectedItem().toString();
    PerfilRunnerViewModel.RunnerInput input = new PerfilRunnerViewModel.RunnerInput(
            binding.etNombre.getText().toString(),
            binding.etApellido.getText().toString(),
            binding.etTelefono.getText().toString(),
            binding.etDni.getText().toString(),
            binding.etFechaNac.getText().toString(),
            generoSeleccionado,
            binding.etLocalidad.getText().toString(),
            binding.etAgrupacion.getText().toString(),
            binding.etNombreContacto.getText().toString(),
            binding.etTelContacto.getText().toString()
    );
    //enviamos al VM
    mv.onBotonPrincipalClick(input);
  }
}