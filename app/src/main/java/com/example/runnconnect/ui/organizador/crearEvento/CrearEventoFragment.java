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
    //recycler categorias
    categoriasAdapter = new CategoriasTemporalAdapter(pos -> viewModel.eliminarCategoriaLocal(pos));
    binding.rvCategoriasAgregadas.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvCategoriasAgregadas.setAdapter(categoriasAdapter);

  }

  private void setupObservers() {
    // 1. Lista de categorias (RecyclerView)
    viewModel.getCategoriasLive().observe(getViewLifecycleOwner(), categoriasAdapter::setLista);
    viewModel.getCategoriasVisibilidad().observe(getViewLifecycleOwner(), binding.rvCategoriasAgregadas::setVisibility);

    // 1.5. Listas para Spinners
    viewModel.getListaTiposEvento().observe(getViewLifecycleOwner(), lista -> {
      ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, lista);
      binding.spTipoEventoGlobal.setAdapter(adapter);
    });

    viewModel.getListaGeneros().observe(getViewLifecycleOwner(), lista -> {
      ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, lista);
      binding.spGeneroCat.setAdapter(adapter);
    });

    // 2. Visibilidad del formulario de categorias (Se oculta al Editar)
    viewModel.getUiVisibilidadCamposExtra().observe(getViewLifecycleOwner(), visibility -> {
      binding.containerFormCategoria.setVisibility(visibility);
      binding.tvTituloSeccionCat.setVisibility(visibility);
    });

    // 3. NUEVO: Bloqueo de campos en modo Edicion
    viewModel.getUiCamposHabilitados().observe(getViewLifecycleOwner(), habilitado -> {
      // Campos que NO se pueden editar si el evento ya existe
      binding.etTitulo.setEnabled(habilitado);
      binding.etUbicacion.setEnabled(habilitado);
      binding.etCupo.setEnabled(habilitado);

      // Campos que SIEMPRE se pueden editar
      binding.etDescripcion.setEnabled(true);
      binding.etDatosPago.setEnabled(true);
    });

    viewModel.getUiCamposAlpha().observe(getViewLifecycleOwner(), alpha -> {
      binding.etTitulo.setAlpha(alpha);
      binding.etUbicacion.setAlpha(alpha);
      binding.etCupo.setAlpha(alpha);
    });
    
    // Observer para pre-seleccionar el Spinner en modo Edicion
    viewModel.getTipoEventoGlobalPosicion().observe(getViewLifecycleOwner(), binding.spTipoEventoGlobal::setSelection);

    // Fecha y Hora siempre habilitadas (manejan su propio click listener)
    binding.etFecha.setEnabled(true);
    binding.etHora.setEnabled(true);
    binding.etFecha.setClickable(true);
    binding.etHora.setClickable(true);

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
    viewModel.getErrorTitulo().observe(getViewLifecycleOwner(), binding.etTitulo::setError);
    viewModel.getErrorUbicacion().observe(getViewLifecycleOwner(), binding.etUbicacion::setError);
    viewModel.getErrorDistancia().observe(getViewLifecycleOwner(), binding.etDistanciaValor::setError);
    viewModel.getErrorPrecio().observe(getViewLifecycleOwner(), binding.etCatPrecio::setError);
    viewModel.getErrorEdad().observe(getViewLifecycleOwner(), error -> {
      binding.etEdadMin.setError(error);
      binding.etEdadMax.setError(error);
    });

    // 7. Mensajes Globales y Loading
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setText);
    
    viewModel.getMensajeGlobalColor().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setTextColor);
    
    viewModel.getMensajeGlobalVisibilidad().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setVisibility);

    viewModel.getChipSeleccionadoId().observe(getViewLifecycleOwner(), binding.chipGroupDistancias::check);

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnContinuarMapa.setEnabled(!loading);
    });

    viewModel.getNavegarAtrasSignal().observe(getViewLifecycleOwner(), navegar -> {
      if (navegar != null && navegar) {
        Navigation.findNavController(requireView()).popBackStack();
        viewModel.resetNavegacion();
      }
    });

    viewModel.getNavegarMapaSignal().observe(getViewLifecycleOwner(), idEvento -> {
      if (idEvento != null) {
        Bundle args = new Bundle();
        args.putInt("idEvento", idEvento);
        try {
          Navigation.findNavController(requireView()).navigate(R.id.action_crear_a_mapaEditor, args);
        } catch (Exception ignored) {}
        viewModel.resetNavegacion();
      }
    });
  }

  private void setupListeners() {
    // BOTON AGREGAR CATEGORIA A LA LISTA
    binding.btnAgregarCategoria.setOnClickListener(v -> {
      viewModel.agregarCategoriaLocal(
        binding.etDistanciaValor.getText().toString(),
        binding.spGeneroCat.getSelectedItem().toString(),
        binding.etEdadMin.getText().toString(),
        binding.etEdadMax.getText().toString(),
        binding.etCatPrecio.getText().toString(),
        binding.etCupo.getText().toString()
      );
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
    binding.chipGroupDistancias.setOnCheckedChangeListener((group, checkedId) -> {
      Chip chip = group.findViewById(checkedId);
      viewModel.setChipSeleccionado(checkedId, chip != null ? chip.getText().toString() : null);
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
}