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
import androidx.navigation.Navigation; // Usaremos esto para navegar

import com.example.runnconnect.R; // Asegúrate de importar R
import com.example.runnconnect.databinding.FragmentCrearEventoBinding;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class CrearEventoFragment extends Fragment {

  private FragmentCrearEventoBinding binding;
  private CrearEventoViewModel viewModel;

  // Variables de estado UI (Selección actual de Spinners)
  private String modalidadSel = "Calle";
  private String generoSel = "X";

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrearEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(CrearEventoViewModel.class);

    setupPickers();
    setupChips();
    setupSpinners();
    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupChips() {
    // Comportamiento puramente de UI: Tocar chip -> Llenar caja de texto
    binding.chipGroupDistancias.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (!checkedIds.isEmpty()) {
        Chip chip = group.findViewById(checkedIds.get(0));
        if (chip != null) {
          // Quitamos la "K" visualmente solo para el EditText
          String texto = chip.getText().toString().replace("K", "").trim();
          binding.etDistanciaValor.setText(texto);
        }
      }
    });
  }

  private void setupSpinners() {
    // 1. Modalidades
    String[] modalidades = {"Calle", "Trail", "Cross", "Aventura", "Obstáculos", "Caminata", "Kids", "MTB", "Triatlón"};
    ArrayAdapter<String> adapterMod = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modalidades);
    binding.spModalidad.setAdapter(adapterMod);

    binding.spModalidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { modalidadSel = modalidades[pos]; }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });

    // 2. Género
    String[] generosVisual = {"Mixto / General", "Femenino", "Masculino"};
    final String[] generosValor = {"X", "F", "M"};

    ArrayAdapter<String> adapterGen = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, generosVisual);
    binding.spGeneroCat.setAdapter(adapterGen);

    binding.spGeneroCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { generoSel = generosValor[pos]; }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });
  }

  private void setupListeners() {
    binding.btnContinuarMapa.setOnClickListener(v -> {
      // MVVM PURO: El Fragment no valida nada. Pasa todo crudo al VM.
      viewModel.procesarYContinuar(
        binding.etTitulo.getText().toString(),
        binding.etDescripcion.getText().toString(),
        binding.etUbicacion.getText().toString(),
        binding.etDatosPago.getText().toString(),
        binding.etDistanciaValor.getText().toString(), // Ej: "10" o vacío
        modalidadSel, // Ej: "Trail"
        generoSel,    // Ej: "X"
        binding.etEdadMin.getText().toString(),
        binding.etEdadMax.getText().toString(),
        binding.etCatPrecio.getText().toString(),
        binding.etCupo.getText().toString()
      );
    });
  }

  private void setupObservers() {
    // 1. Loading
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnContinuarMapa.setEnabled(!loading);
    });

    // 2. Mensajes de Error/Exito
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });

    viewModel.getEsError().observe(getViewLifecycleOwner(), isError -> {
      binding.tvMensajeGlobal.setTextColor(isError ? android.graphics.Color.RED : android.graphics.Color.parseColor("#008000"));
    });

    // 3. NAVEGACIÓN AL MAPA (Paso 2)
    viewModel.getIrAlMapa().observe(getViewLifecycleOwner(), idEvento -> {
      if (idEvento != null) {
        // A. Preparamos el paquete de datos
        Bundle args = new Bundle();
        args.putInt("idEvento", idEvento);

        // B. EJECUTAMOS LA NAVEGACIÓN REAL
        // Asegúrate que "action_crear_a_mapaEditor" coincida con tu mobile_navigation.xml
        try {
          Navigation.findNavController(requireView())
            .navigate(R.id.action_crear_a_mapaEditor, args);
        } catch (Exception e) {
          Toast.makeText(getContext(), "Error nav: Verifique mobile_navigation.xml", Toast.LENGTH_LONG).show();
          e.printStackTrace();
        }

        // C. Reseteamos para que no vuelva a navegar si rotas la pantalla
        viewModel.resetearNav(); // Navigation.findNavController(requireView()).navigate(R.id.action_crear_a_mapa, args);
      }
    });
  }

  // Pickers visuales (Fecha/Hora)
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
}