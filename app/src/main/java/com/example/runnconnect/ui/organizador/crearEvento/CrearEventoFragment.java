package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentCrearEventoBinding;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class CrearEventoFragment extends Fragment {

  private FragmentCrearEventoBinding binding;
  private CrearEventoViewModel viewModel;
  private CategoriasTemporalAdapter categoriasAdapter;

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
    String[] tiposEvento = {"Calle", "Trail", "Cross", "Aventura", "Obstáculos", "Correcaminata", "Kids", "Triatlón"};
    ArrayAdapter<String> adapterMod = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, tiposEvento);
    binding.spTipoEventoGlobal.setAdapter(adapterMod);

    String[] generosVisual = {"Mixto / General", "Femenino", "Masculino"};
    ArrayAdapter<String> adapterGen = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, generosVisual);
    binding.spGeneroCat.setAdapter(adapterGen);

    //recycler categorias
    categoriasAdapter = new CategoriasTemporalAdapter(pos -> viewModel.eliminarCategoriaLocal(pos));
    binding.rvCategoriasAgregadas.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvCategoriasAgregadas.setAdapter(categoriasAdapter);

  }

  private void setupObservers() {
    // 1. Lista de categorias (RecyclerView)
    viewModel.getCategoriasLive().observe(getViewLifecycleOwner(), lista -> {
      categoriasAdapter.setLista(lista);
      binding.rvCategoriasAgregadas.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
    });

    // 2. Visibilidad del formulario de categorias (Se oculta al Editar)
    viewModel.getUiVisibilidadCamposExtra().observe(getViewLifecycleOwner(), visibility -> {
      binding.containerFormCategoria.setVisibility(visibility);
      binding.tvTituloSeccionCat.setVisibility(visibility);
    });

    // 3. NUEVO: Bloqueo de campos en modo Edicion
    // Esto es lo que faltaba para cumplir el punto 1
    viewModel.getUiCamposHabilitados().observe(getViewLifecycleOwner(), habilitado -> {
      // Campos que NO se pueden editar si el evento ya existe
      binding.etTitulo.setEnabled(habilitado);
      binding.etUbicacion.setEnabled(habilitado);
      binding.etCupo.setEnabled(habilitado);

      // Efecto visual (grisaceo) para indicar que estan bloqueados
      float alpha = habilitado ? 1.0f : 0.5f;
      binding.etTitulo.setAlpha(alpha);
      binding.etUbicacion.setAlpha(alpha);
      binding.etCupo.setAlpha(alpha);

      // Campos que SIEMPRE se pueden editar
      binding.etDescripcion.setEnabled(true);
      binding.etDatosPago.setEnabled(true);

      // Observer para pre-seleccionar el Spinner en modo Edicion
      viewModel.getTipoEventoGlobal().observe(getViewLifecycleOwner(), tipo -> {
        setSpinnerSelection(binding.spTipoEventoGlobal, tipo);
      });

      // Fecha y Hora siempre habilitadas (manejan su propio click listener)
      binding.etFecha.setEnabled(true);
      binding.etHora.setEnabled(true);
      binding.etFecha.setClickable(true);
      binding.etHora.setClickable(true);
    });

    // 4. Textos estaticos
    viewModel.getUiTituloPagina().observe(getViewLifecycleOwner(), binding.tvTituloPagina::setText);
    viewModel.getUiTextoBoton().observe(getViewLifecycleOwner(), binding.btnContinuarMapa::setText);
    viewModel.getUiTextoAviso().observe(getViewLifecycleOwner(), binding.tvAvisoMapa::setText);

    // 5. Data Binding (Llenado de campos desde el ViewModel)
    viewModel.getTitulo().observe(getViewLifecycleOwner(), s -> binding.etTitulo.setText(s));
    viewModel.getDescripcion().observe(getViewLifecycleOwner(), s -> binding.etDescripcion.setText(s));
    viewModel.getUbicacion().observe(getViewLifecycleOwner(), s -> binding.etUbicacion.setText(s));
    viewModel.getFechaDisplay().observe(getViewLifecycleOwner(), s -> binding.etFecha.setText(s));
    viewModel.getHoraDisplay().observe(getViewLifecycleOwner(), s -> binding.etHora.setText(s));
    viewModel.getDatosPago().observe(getViewLifecycleOwner(), s -> binding.etDatosPago.setText(s));
    viewModel.getCupo().observe(getViewLifecycleOwner(), s -> binding.etCupo.setText(s));

    // Formulario de Categoria
    viewModel.getDistancia().observe(getViewLifecycleOwner(), s -> binding.etDistanciaValor.setText(s));
    viewModel.getPrecio().observe(getViewLifecycleOwner(), s -> binding.etCatPrecio.setText(s));

    // 6. Manejo de Errores en Inputs
    viewModel.getErrorTitulo().observe(getViewLifecycleOwner(), e -> {
      if(e!=null) { binding.etTitulo.setError(e); binding.etTitulo.requestFocus(); }
    });
    viewModel.getErrorUbicacion().observe(getViewLifecycleOwner(), e -> {
      if(e!=null) { binding.etUbicacion.setError(e); binding.etUbicacion.requestFocus(); }
    });
    viewModel.getErrorDistancia().observe(getViewLifecycleOwner(), e -> {
      if(e!=null) { binding.etDistanciaValor.setError(e); binding.etDistanciaValor.requestFocus(); }
    });
    viewModel.getErrorPrecio().observe(getViewLifecycleOwner(), e -> {
      if(e!=null) { binding.etCatPrecio.setError(e); binding.etCatPrecio.requestFocus(); }
    });

    // 7. Mensajes Globales y Loading
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null && !msg.isEmpty()) {
        binding.tvMensajeGlobal.setText(msg);
        binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
        if (msg.contains("!") || msg.toLowerCase().contains("exito") || msg.toLowerCase().contains("mapa")) {
          binding.tvMensajeGlobal.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
          binding.tvMensajeGlobal.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
      } else {
        binding.tvMensajeGlobal.setVisibility(View.GONE);
      }
    });

    // Lista de categorias
    viewModel.getCategoriasLive().observe(getViewLifecycleOwner(), lista -> {
      categoriasAdapter.setLista(lista);
      binding.rvCategoriasAgregadas.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
    });

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnContinuarMapa.setEnabled(!loading);
    });

    // 8. Navegacion tras exito
    viewModel.getNavegacionExito().observe(getViewLifecycleOwner(), code -> {
      if (code == 0) return;
      if (code == 2) { // Edicion exitosa -> Volver atras
        Navigation.findNavController(requireView()).popBackStack();
      } else { // Creacion exitosa -> Ir a mapa (code es el ID del nuevo evento)
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
    // BOTON AGREGAR CATEGORIA A LA LISTA
    binding.btnAgregarCategoria.setOnClickListener(v -> {
      boolean exito = viewModel.agregarCategoriaLocal(
        binding.etDistanciaValor.getText().toString(),
        binding.spGeneroCat.getSelectedItem().toString(),
        binding.etEdadMin.getText().toString(),
        binding.etEdadMax.getText().toString(),
        binding.etCatPrecio.getText().toString(),
        binding.etCupo.getText().toString()
      );

      if (exito) {
        // Limpiar campos para cargar otra
        binding.etDistanciaValor.setText("");
        binding.etCatPrecio.setText("");
        binding.chipGroupDistancias.clearCheck();
        Toast.makeText(getContext(), "Categoria agregada", Toast.LENGTH_SHORT).show();
      }
    });

    // --- BOTON FINAL (GUARDAR TODeO) ---
    binding.btnContinuarMapa.setOnClickListener(v -> {
      // Obtenemos el valor del Spinner GLOBAL
      String tipoSeleccionado = binding.spTipoEventoGlobal.getSelectedItem().toString();

      viewModel.guardarEvento(
        binding.etTitulo.getText().toString(),
        binding.etDescripcion.getText().toString(),
        binding.etUbicacion.getText().toString(),
        binding.etDatosPago.getText().toString(),
        binding.etCupo.getText().toString(),
        tipoSeleccionado
      );
    });

    // --- CHIPS Y FECHAS ---
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