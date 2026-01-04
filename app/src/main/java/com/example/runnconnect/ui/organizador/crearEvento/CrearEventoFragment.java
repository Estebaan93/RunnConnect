package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.databinding.FragmentCrearEventoBinding;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class CrearEventoFragment extends Fragment {

  private FragmentCrearEventoBinding binding;
  private CrearEventoViewModel viewModel;

  // Estado UI temporal (Selecciones de Spinners)
  private String modalidadSel = "Calle";
  private String generoSel = "X";

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrearEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(CrearEventoViewModel.class);

    setupPickers();
    setupChips();     // UI Helper: Llena el EditText al tocar un Chip
    setupSpinners();
    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupChips() {
    // Esto es comportamiento de VISTA: Actualizar un input al tocar otro componente
    binding.chipGroupDistancias.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (!checkedIds.isEmpty()) {
        Chip chip = group.findViewById(checkedIds.get(0));
        if (chip != null) {
          // Solo UI: Ponemos el número en el EditText
          String texto = chip.getText().toString().replace("K", "").trim();
          binding.etDistanciaValor.setText(texto);
        }
      }
    });
  }

  private void setupSpinners() {
    // Configuración de adaptadores (UI pura)
    String[] modalidades = {"Calle", "Trail", "Cross", "Aventura", "Obstáculos", "Caminata", "Kids", "MTB", "Triatlón"};
    ArrayAdapter<String> adapterMod = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modalidades);
    binding.spModalidad.setAdapter(adapterMod);

    binding.spModalidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
        modalidadSel = modalidades[pos];
      }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });

    String[] generosVisual = {"Mixto / General", "Femenino", "Masculino"};
    final String[] generosValor = {"X", "F", "M"};

    ArrayAdapter<String> adapterGen = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, generosVisual);
    binding.spGeneroCat.setAdapter(adapterGen);

    binding.spGeneroCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
        generoSel = generosValor[pos];
      }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });
  }

  private void setupListeners() {
    binding.btnPublicar.setOnClickListener(v -> {
      // PURE MVVM: Pasamos los datos CRUDOS al ViewModel.
      // No validamos nada aquí.
      viewModel.procesarYPublicar(
        binding.etTitulo.getText().toString(),
        binding.etDescripcion.getText().toString(),
        binding.etUbicacion.getText().toString(),
        binding.etDatosPago.getText().toString(),
        binding.etDistanciaValor.getText().toString(), // El número crudo (ej: "10")
        modalidadSel,
        generoSel,
        binding.etEdadMin.getText().toString(), // String vacío o número
        binding.etEdadMax.getText().toString(), // String vacío o número
        binding.etCatPrecio.getText().toString(),
        binding.etCupo.getText().toString()
      );
    });
  }

  // setupPickers y setupObservers se mantienen igual (solo pintan)
  private void setupPickers() {
    binding.etFecha.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();
      DatePickerDialog d = new DatePickerDialog(requireContext(), (view, y, m, d1) -> {
        binding.etFecha.setText(viewModel.procesarFecha(y, m, d1));
      }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
      d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
      d.show();
    });

    binding.etHora.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();
      new TimePickerDialog(requireContext(), (view, h, m) -> {
        binding.etHora.setText(viewModel.procesarHora(h, m));
      }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    });
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnPublicar.setEnabled(!loading);
    });

    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });

    viewModel.getEsError().observe(getViewLifecycleOwner(), isError -> {
      binding.tvMensajeGlobal.setTextColor(isError ? android.graphics.Color.RED : android.graphics.Color.parseColor("#008000"));
    });

    viewModel.getNavegarAtras().observe(getViewLifecycleOwner(), nav -> {
      if (Boolean.TRUE.equals(nav)) requireActivity().onBackPressed();
    });
  }
}