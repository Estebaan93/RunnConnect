package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.databinding.FragmentCrearEventoBinding;

import java.util.Calendar;
import java.util.Locale;

public class CrearEventoFragment extends Fragment {

  private FragmentCrearEventoBinding binding;
  private CrearEventoViewModel viewModel;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrearEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(CrearEventoViewModel.class);

    setupPickers();
    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupPickers() {
    // SELECTOR DE FECHA
    binding.etFecha.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();

      DatePickerDialog datePicker = new DatePickerDialog(
        requireContext(),
        (view, year, month, day) -> {
          String textoMostrar = viewModel.procesarFecha(year, month, day);
          binding.etFecha.setText(textoMostrar);
          // Limpiamos error visual si existía
          viewModel.validarFechaFutura(year, month, day);
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
      );

      // IMPORTANTE: Bloquear fechas pasadas
      // Restamos 1 segundo para asegurar que el "ahora" sea seleccionable si se desea
      datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

      datePicker.show();
    });

    // TIME PICKER
    binding.etHora.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();
      new TimePickerDialog(requireContext(), (view, hour, minute) -> {
        // Delegamos formato al VM y mostramos el resultado
        String textoMostrar = viewModel.procesarHora(hour, minute);
        binding.etHora.setText(textoMostrar);
      }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    });
  }

  private void setupListeners() {
    binding.btnPublicar.setOnClickListener(v -> {
      viewModel.publicarEvento(
        binding.etTitulo.getText().toString().trim(),
        binding.etDescripcion.getText().toString().trim(),
        binding.etUbicacion.getText().toString().trim(),
        binding.etDatosPago.getText().toString().trim(), // Nuevo: Datos Pago
        binding.etCatNombre.getText().toString().trim(), // Nuevo: Nombre Cat
        binding.etCatPrecio.getText().toString().trim(), // Nuevo: Precio Cat
        binding.etCupo.getText().toString().trim()
      );
    });
  }

  private void setupObservers() {
    // 1. Loading
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnPublicar.setEnabled(!loading);
    });

    // 2. Mensajes Globales (Sin Toast)
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });

    // 3. Color del mensaje (Rojo error / Verde éxito)
    viewModel.getEsError().observe(getViewLifecycleOwner(), isError -> {
      binding.tvMensajeGlobal.setTextColor(isError ? Color.RED : Color.parseColor("#008000"));
    });

    // 4. Navegación (Cerrar al terminar)
    viewModel.getNavegarAtras().observe(getViewLifecycleOwner(), navegar -> {
      if (navegar) requireActivity().onBackPressed();
    });
  }


}
