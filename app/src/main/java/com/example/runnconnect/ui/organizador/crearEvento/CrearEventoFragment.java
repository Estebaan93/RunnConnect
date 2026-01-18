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

    if (getArguments() != null) {
      viewModel.verificarModoEdicion(getArguments().getInt("idEvento", 0));
    }

    return binding.getRoot();
  }

  private void setupUI() {
    String[] modalidades = {"Calle", "Trail", "Cross", "Aventura", "Obstáculos", "Correcaminata", "Kids", "Triatlón"};
    ArrayAdapter<String> adapterMod = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modalidades);
    binding.spModalidad.setAdapter(adapterMod);

    String[] generosVisual = {"Mixto / General", "Femenino", "Masculino"};
    ArrayAdapter<String> adapterGen = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, generosVisual);
    binding.spGeneroCat.setAdapter(adapterGen);
  }

  private void setupObservers() {
    // --- TEXTOS ESTATICOS ---
    viewModel.getUiTituloPagina().observe(getViewLifecycleOwner(), binding.tvTituloPagina::setText);
    viewModel.getUiTextoBoton().observe(getViewLifecycleOwner(), binding.btnContinuarMapa::setText);
    viewModel.getUiTextoAviso().observe(getViewLifecycleOwner(), binding.tvAvisoMapa::setText);

    // --- CAMPOS DE TEXTO (Binding) ---
    viewModel.getTitulo().observe(getViewLifecycleOwner(), s -> binding.etTitulo.setText(s));
    viewModel.getDescripcion().observe(getViewLifecycleOwner(), s -> binding.etDescripcion.setText(s));
    viewModel.getUbicacion().observe(getViewLifecycleOwner(), s -> binding.etUbicacion.setText(s));
    viewModel.getFechaDisplay().observe(getViewLifecycleOwner(), s -> binding.etFecha.setText(s));
    viewModel.getHoraDisplay().observe(getViewLifecycleOwner(), s -> binding.etHora.setText(s));
    viewModel.getDatosPago().observe(getViewLifecycleOwner(), s -> binding.etDatosPago.setText(s));
    viewModel.getCupo().observe(getViewLifecycleOwner(), s -> binding.etCupo.setText(s));

    viewModel.getDistancia().observe(getViewLifecycleOwner(), s -> binding.etDistanciaValor.setText(s));
    viewModel.getPrecio().observe(getViewLifecycleOwner(), s -> binding.etCatPrecio.setText(s));
    viewModel.getEdadMin().observe(getViewLifecycleOwner(), s -> binding.etEdadMin.setText(s));
    viewModel.getEdadMax().observe(getViewLifecycleOwner(), s -> binding.etEdadMax.setText(s));

    viewModel.getSeleccionModalidad().observe(getViewLifecycleOwner(), val -> setSpinnerSelection(binding.spModalidad, val));
    viewModel.getSeleccionGenero().observe(getViewLifecycleOwner(), val -> setSpinnerSelection(binding.spGeneroCat, val));

    // --- VISIBILIDAD ---
    viewModel.getUiVisibilidadChips().observe(getViewLifecycleOwner(), binding.scrollChips::setVisibility);
    viewModel.getUiVisibilidadCamposExtra().observe(getViewLifecycleOwner(), visibility -> {
      binding.tvTituloCat.setVisibility(visibility);
      binding.lblDistancia.setVisibility(visibility);
      binding.etDistanciaValor.setVisibility(visibility);
      binding.lblModalidad.setVisibility(visibility);
      binding.spModalidad.setVisibility(visibility);
      binding.lblGenero.setVisibility(visibility);
      binding.spGeneroCat.setVisibility(visibility);
      binding.lblEdad.setVisibility(visibility);
      binding.etEdadMin.setVisibility(visibility);
      binding.etEdadMax.setVisibility(visibility);
      binding.lblCatPrecio.setVisibility(visibility);
      binding.etCatPrecio.setVisibility(visibility);
    });

    // --- HABILITACION ---
    viewModel.getUiCamposHabilitados().observe(getViewLifecycleOwner(), habilitado -> {
      binding.etTitulo.setEnabled(habilitado);
      binding.etUbicacion.setEnabled(habilitado);
      // Estos siempre deben coincidir
      binding.etDistanciaValor.setEnabled(habilitado);
      binding.spModalidad.setEnabled(habilitado);
      binding.spGeneroCat.setEnabled(habilitado);
      binding.etCatPrecio.setEnabled(habilitado);
      binding.etEdadMin.setEnabled(habilitado);
      binding.etEdadMax.setEnabled(habilitado);
    });

    // --- MANEJO DE ERRORES VISUALES-

    // 1. Errores en Inputs (setError + RequestFocus)
    viewModel.getErrorTitulo().observe(getViewLifecycleOwner(), error -> {
      if (error != null) { binding.etTitulo.setError(error); binding.etTitulo.requestFocus(); }
    });

    viewModel.getErrorUbicacion().observe(getViewLifecycleOwner(), error -> {
      if (error != null) { binding.etUbicacion.setError(error); binding.etUbicacion.requestFocus(); }
    });

    viewModel.getErrorDistancia().observe(getViewLifecycleOwner(), error -> {
      if (error != null) { binding.etDistanciaValor.setError(error); binding.etDistanciaValor.requestFocus(); }
    });

    viewModel.getErrorPrecio().observe(getViewLifecycleOwner(), error -> {
      if (error != null) { binding.etCatPrecio.setError(error); binding.etCatPrecio.requestFocus(); }
    });

    viewModel.getErrorCupo().observe(getViewLifecycleOwner(), error -> {
      if (error != null) { binding.etCupo.setError(error); binding.etCupo.requestFocus(); }
    });

    // 2. Mensajes Globales (Exito, Fallo API, Fecha invalida)
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null && !msg.isEmpty()) {
        binding.tvMensajeGlobal.setText(msg);
        binding.tvMensajeGlobal.setVisibility(View.VISIBLE);

        // Colorizacion
        if (msg.contains("!") || msg.toLowerCase().contains("exito") || msg.toLowerCase().contains("mapa")) {
          binding.tvMensajeGlobal.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
          binding.tvMensajeGlobal.setBackgroundColor(getResources().getColor(R.color.white)); // O un verde
        } else {
          binding.tvMensajeGlobal.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
          binding.tvMensajeGlobal.setBackgroundColor(getResources().getColor(R.color.white)); // O un rojo
        }
      } else {
        binding.tvMensajeGlobal.setVisibility(View.GONE);
      }
    });

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnContinuarMapa.setEnabled(!loading);
    });

    // --- NAVEGACION ---
    viewModel.getNavegacionExito().observe(getViewLifecycleOwner(), code -> {
      if (code == 0) return;
      if (code == 2) {
        Navigation.findNavController(requireView()).popBackStack();
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

  private void setSpinnerSelection(Spinner spinner, String value) {
    if (value == null) return;
    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
    int pos = adapter.getPosition(value);
    if (pos >= 0) spinner.setSelection(pos);
  }
}