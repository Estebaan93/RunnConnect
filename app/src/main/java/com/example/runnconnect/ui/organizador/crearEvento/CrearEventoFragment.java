package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentCrearEventoBinding;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class CrearEventoFragment extends Fragment {

  private FragmentCrearEventoBinding binding;
  private CrearEventoViewModel viewModel;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrearEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(CrearEventoViewModel.class);

    setupUI();
    setupListeners();
    setupObservers();

    // Solo le pasamos el ID al ViewModel. El Fragment no decide nada.
    if (getArguments() != null) {
      viewModel.verificarModoEdicion(getArguments().getInt("idEvento", 0));
    }

    return binding.getRoot();
  }

  private void setupUI() {
    // Configuración inicial de los adapters (pura UI)
    String[] modalidades = {"Calle", "Trail", "Cross", "Aventura", "Obstáculos", "Correcaminata", "Kids", "Triatlón"};
    ArrayAdapter<String> adapterMod = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modalidades);
    binding.spModalidad.setAdapter(adapterMod);

    String[] generosVisual = {"Mixto / General", "Femenino", "Masculino"};
    ArrayAdapter<String> adapterGen = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, generosVisual);
    binding.spGeneroCat.setAdapter(adapterGen);
  }

  private void setupObservers() {
    // 1. Estados Globales
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnContinuarMapa.setEnabled(!loading);
    });

    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });

    // 2. Data Binding (VM -> View)
    viewModel.getTitulo().observe(getViewLifecycleOwner(), s -> binding.etTitulo.setText(s));
    viewModel.getDescripcion().observe(getViewLifecycleOwner(), s -> binding.etDescripcion.setText(s));
    viewModel.getUbicacion().observe(getViewLifecycleOwner(), s -> binding.etUbicacion.setText(s));
    viewModel.getFechaDisplay().observe(getViewLifecycleOwner(), s -> binding.etFecha.setText(s));
    viewModel.getHoraDisplay().observe(getViewLifecycleOwner(), s -> binding.etHora.setText(s));
    viewModel.getDatosPago().observe(getViewLifecycleOwner(), s -> binding.etDatosPago.setText(s));
    viewModel.getCupo().observe(getViewLifecycleOwner(), s -> binding.etCupo.setText(s));

    // Campos de categoría (Visualización)
    viewModel.getDistancia().observe(getViewLifecycleOwner(), s -> binding.etDistanciaValor.setText(s));
    viewModel.getPrecio().observe(getViewLifecycleOwner(), s -> binding.etCatPrecio.setText(s));
    viewModel.getEdadMin().observe(getViewLifecycleOwner(), s -> binding.etEdadMin.setText(s));
    viewModel.getEdadMax().observe(getViewLifecycleOwner(), s -> binding.etEdadMax.setText(s));

    // Selección automática de Spinners (El VM dice "Calle", la vista lo busca)
    viewModel.getSeleccionModalidad().observe(getViewLifecycleOwner(), val -> setSpinnerSelection(binding.spModalidad, val));
    viewModel.getSeleccionGenero().observe(getViewLifecycleOwner(), val -> setSpinnerSelection(binding.spGeneroCat, val));

    // 3. Cambios de Visibilidad/Estado (Modo Edición)
    viewModel.getEsModoEdicionUI().observe(getViewLifecycleOwner(), isEdit -> {
      if (isEdit) {
        binding.tvTituloPagina.setText("Editar Evento");
        binding.btnContinuarMapa.setText("Guardar Cambios");
        binding.tvTituloCat.setVisibility(View.VISIBLE);
        binding.lblDistancia.setVisibility(View.VISIBLE);
        binding.etDistanciaValor.setVisibility(View.VISIBLE);
        binding.lblModalidad.setVisibility(View.VISIBLE);
        binding.spModalidad.setVisibility(View.VISIBLE);
        binding.lblGenero.setVisibility(View.VISIBLE);
        binding.spGeneroCat.setVisibility(View.VISIBLE);
        binding.lblEdad.setVisibility(View.VISIBLE);
        binding.etEdadMin.setVisibility(View.VISIBLE);
        binding.etEdadMax.setVisibility(View.VISIBLE);
        binding.lblCatPrecio.setVisibility(View.VISIBLE);
        binding.etCatPrecio.setVisibility(View.VISIBLE);
        binding.scrollChips.setVisibility(View.GONE);
        binding.tvAvisoMapa.setText("Nota: Precio, Distancia y Lugar no se editan.");
      }
    });

    viewModel.getCamposBloqueados().observe(getViewLifecycleOwner(), bloqueados -> {
      if (bloqueados) {
        binding.etTitulo.setEnabled(false);
        binding.etUbicacion.setEnabled(false);
        binding.etDistanciaValor.setEnabled(false);
        binding.spModalidad.setEnabled(false);
        binding.spGeneroCat.setEnabled(false);
        binding.etCatPrecio.setEnabled(false);
        binding.etEdadMin.setEnabled(false);
        binding.etEdadMax.setEnabled(false);
      }
    });

    // 4. Navegación
    viewModel.getNavegacionExito().observe(getViewLifecycleOwner(), code -> {
      if (code == 0) return;
      if (code == 2) {
        Navigation.findNavController(requireView()).popBackStack(); // Volver
      } else {
        Bundle args = new Bundle();
        args.putInt("idEvento", code);
        try {
          Navigation.findNavController(requireView()).navigate(R.id.action_crear_a_mapaEditor, args);
        } catch (Exception e) {}
      }
      viewModel.resetearNavegacion();
    });
  }

  private void setupListeners() {
    binding.btnContinuarMapa.setOnClickListener(v -> {
      // El View solo recolecta y pasa al ViewModel
      viewModel.guardarEvento(
        binding.etTitulo.getText().toString(),
        binding.etDescripcion.getText().toString(),
        binding.etUbicacion.getText().toString(),
        binding.etDatosPago.getText().toString(),
        binding.etDistanciaValor.getText().toString(),
        binding.spModalidad.getSelectedItem().toString(),
        binding.spGeneroCat.getSelectedItem().toString(),
        binding.etEdadMin.getText().toString(),
        binding.etEdadMax.getText().toString(),
        binding.etCatPrecio.getText().toString(),
        binding.etCupo.getText().toString()
      );
    });

    binding.chipGroupDistancias.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (!checkedIds.isEmpty()) {
        Chip chip = group.findViewById(checkedIds.get(0));
        if (chip != null) viewModel.onChipDistanciaSelected(chip.getText().toString());
      }
    });

    binding.etFecha.setOnClickListener(v -> {
      Calendar c = Calendar.getInstance();
      DatePickerDialog d = new DatePickerDialog(requireContext(),
        (view, y, m, d1) -> viewModel.onFechaSelected(y, m, d1),
        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
      d.show();
    });

    binding.etHora.setOnClickListener(v -> {
      Calendar c = Calendar.getInstance();
      new TimePickerDialog(requireContext(),
        (view, h, m) -> viewModel.onHoraSelected(h, m),
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    });
  }

  // Helper UI: Busca el índice del texto en el Spinner (Esto es aceptable en el View)
  private void setSpinnerSelection(Spinner spinner, String value) {
    if (value == null) return;
    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
    int pos = adapter.getPosition(value);
    if (pos >= 0) spinner.setSelection(pos);
  }
}