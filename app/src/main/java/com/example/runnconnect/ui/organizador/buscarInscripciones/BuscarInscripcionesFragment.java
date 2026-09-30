package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.DialogDetalleRunnerBinding;
import com.example.runnconnect.databinding.DialogVerImagenBinding;
import com.example.runnconnect.databinding.FragmentBuscarInscripcionesBinding;

import java.util.ArrayList;

public class BuscarInscripcionesFragment extends Fragment {

  private BuscarInscripcionesViewModel mViewModel;
  private FragmentBuscarInscripcionesBinding binding;
  private BuscarAdapter adapter;

  private Dialog dialogDetalleActual;
  private DialogDetalleRunnerBinding dialogDetalleBinding;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentBuscarInscripcionesBinding.inflate(inflater, container, false);
    mViewModel = new ViewModelProvider(this).get(BuscarInscripcionesViewModel.class);

    initDialog();
    setupRecyclerView();
    setupObservers();
    setupSearchView();

    return binding.getRoot();
  }

  @Override
  public void onResume() {
    super.onResume();
    mViewModel.limpiarBusqueda();
    binding.searchViewBusqueda.setQuery("", false);
    binding.searchViewBusqueda.clearFocus();
  }

  private void setupRecyclerView() {
    adapter = new BuscarAdapter(new ArrayList<>(), item -> {
      mViewModel.seleccionarItem(item);
    });

    binding.recyclerViewBusqueda.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerViewBusqueda.setAdapter(adapter);
  }

  private void setupObservers() {
    mViewModel.getResultadosUi().observe(getViewLifecycleOwner(), resultados -> {
      adapter.setResultados(resultados);
    });

    mViewModel.getEstadoBusquedaMensaje().observe(getViewLifecycleOwner(), binding.tvEstadoBusqueda::setText);
    mViewModel.getEstadoBusquedaVisibilidad().observe(getViewLifecycleOwner(), binding.tvEstadoBusqueda::setVisibility);

    mViewModel.getOcultarDialog().observe(getViewLifecycleOwner(), signal -> dialogDetalleActual.dismiss());

    mViewModel.getDetalleUiState().observe(getViewLifecycleOwner(), state -> {
      if (state == null) return;
      
      dialogDetalleBinding.tvNombreCompleto.setText(state.nombre);
      dialogDetalleBinding.tvDniSexoEdad.setText(state.dniSexo);
      dialogDetalleBinding.tvLocalidad.setText(state.localidad);
      dialogDetalleBinding.tvCategoriaTalle.setText(state.eventoCatTalle);
      dialogDetalleBinding.tvEmail.setText(state.email);
      dialogDetalleBinding.tvTelefono.setText(state.telefono);
      dialogDetalleBinding.tvContactoEmergencia.setText(state.emergencia);
      dialogDetalleBinding.tvTelEmergencia.setText(state.telEmergencia);
      
      dialogDetalleBinding.btnDarDeBaja.setVisibility(state.btnBajaVisibilidad);
      dialogDetalleBinding.btnVerComprobante.setVisibility(state.btnVerComprobanteVisibilidad);
      
      dialogDetalleActual.show();
    });

    mViewModel.getUiMostrarComprobanteUrl().observe(getViewLifecycleOwner(), this::mostrarDialogoComprobante);

    mViewModel.getFeedbackDialogMensaje().observe(getViewLifecycleOwner(), dialogDetalleBinding.tvMensajeBaja::setText);
    mViewModel.getFeedbackDialogVisibilidad().observe(getViewLifecycleOwner(), dialogDetalleBinding.tvMensajeBaja::setVisibility);
    mViewModel.getFeedbackDialogColor().observe(getViewLifecycleOwner(), dialogDetalleBinding.tvMensajeBaja::setTextColor);
    mViewModel.getBtnBajaHabilitado().observe(getViewLifecycleOwner(), dialogDetalleBinding.btnDarDeBaja::setEnabled);
  }

  private void setupSearchView() {
    binding.searchViewBusqueda.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        mViewModel.buscar(query);
        binding.searchViewBusqueda.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        mViewModel.onTextoBuscadorCambiado(newText);
        return false;
      }
    });

    binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (!checkedIds.isEmpty()) {
        int checkedId = checkedIds.get(0);
        com.google.android.material.chip.Chip chip = binding.getRoot().findViewById(checkedId);
        if (chip != null) {
          mViewModel.setFiltroEstado(chip.getText().toString());
        }
      }
    });
  }

  private void initDialog() {
    dialogDetalleActual = new Dialog(requireContext());
    dialogDetalleActual.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialogDetalleBinding = DialogDetalleRunnerBinding.inflate(getLayoutInflater());
    dialogDetalleActual.setContentView(dialogDetalleBinding.getRoot());
    if (dialogDetalleActual.getWindow() != null) {
      dialogDetalleActual.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    dialogDetalleBinding.btnCerrarDetalle.setOnClickListener(v -> dialogDetalleActual.dismiss());
    dialogDetalleBinding.btnVerComprobante.setOnClickListener(v -> mViewModel.onVerComprobanteClicked());

    dialogDetalleActual.setOnDismissListener(dialog -> mViewModel.limpiarDetalle());

    dialogDetalleBinding.btnDarDeBaja.setOnClickListener(v -> {
      BuscarInscripcionesViewModel.DetalleUiState state = mViewModel.getDetalleUiState().getValue();
      if (state != null) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
          .setTitle("Confirmar baja")
          .setMessage("¿Estás seguro de eliminar a " + state.nombre + "?")
          .setPositiveButton("Sí, eliminar", (d, w) -> {
            String textoBusqueda = binding.searchViewBusqueda.getQuery().toString();
            mViewModel.confirmarBaja(state.idInscripcion, state.nombre, textoBusqueda);
          })
          .setNegativeButton("Cancelar", null)
          .show();
      }
    });
  }

  private void mostrarDialogoComprobante(String url) {
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    DialogVerImagenBinding zoomBinding = DialogVerImagenBinding.inflate(getLayoutInflater());
    dialog.setContentView(zoomBinding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    Glide.with(this)
        .load(url)
        .into(zoomBinding.ivZoom);

    zoomBinding.btnCerrarImagen.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    dialogDetalleBinding = null;
    dialogDetalleActual = null;
    binding = null;
  }
}