package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentBuscarInscripcionesBinding;

import java.util.ArrayList;

public class BuscarInscripcionesFragment extends Fragment {

  private BuscarInscripcionesViewModel mViewModel;
  private FragmentBuscarInscripcionesBinding binding;
  private BuscarAdapter adapter;

  private Dialog dialogDetalleActual;
  private TextView tvMensajeFeedback;
  private Button btnDarDeBajaGlobal;
  private BuscarInscripcionesViewModel.DetalleUiState detalleStateGlobal;
  
  private TextView tvNombreDialog;
  private TextView tvDniSexoDialog;
  private TextView tvLocalidadDialog;
  private TextView tvCatTalleDialog;
  private TextView tvEmailDialog;
  private TextView tvTelDialog;
  private TextView tvEmergenciaDialog;
  private TextView tvTelEmergenciaDialog;

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

    mViewModel.getEstadoBusquedaMensaje().observe(getViewLifecycleOwner(), msg -> {
      binding.tvEstadoBusqueda.setText(msg);
    });

    mViewModel.getEstadoBusquedaVisibilidad().observe(getViewLifecycleOwner(), visibilidad -> {
      binding.tvEstadoBusqueda.setVisibility(visibilidad);
    });

    mViewModel.getOcultarDialog().observe(getViewLifecycleOwner(), signal -> dialogDetalleActual.dismiss());

    mViewModel.getDetalleUiState().observe(getViewLifecycleOwner(), state -> {
      detalleStateGlobal = state;
      
      tvNombreDialog.setText(state.nombre);
      tvDniSexoDialog.setText(state.dniSexo);
      tvLocalidadDialog.setText(state.localidad);
      tvCatTalleDialog.setText(state.eventoCatTalle);
      tvEmailDialog.setText(state.email);
      tvTelDialog.setText(state.telefono);
      tvEmergenciaDialog.setText(state.emergencia);
      tvTelEmergenciaDialog.setText(state.telEmergencia);
      
      btnDarDeBajaGlobal.setVisibility(state.btnBajaVisible ? View.VISIBLE : View.GONE);
      
      dialogDetalleActual.show();
    });

    mViewModel.getFeedbackDialogMensaje().observe(getViewLifecycleOwner(), msg -> {
      tvMensajeFeedback.setText(msg);
    });

    mViewModel.getFeedbackDialogVisibilidad().observe(getViewLifecycleOwner(), vis -> {
      tvMensajeFeedback.setVisibility(vis);
    });

    mViewModel.getFeedbackDialogColor().observe(getViewLifecycleOwner(), color -> {
      tvMensajeFeedback.setTextColor(color);
    });

    mViewModel.getBtnBajaHabilitado().observe(getViewLifecycleOwner(), hab -> {
      btnDarDeBajaGlobal.setEnabled(hab);
    });
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
  }

  private void initDialog() {
    dialogDetalleActual = new Dialog(requireContext());
    dialogDetalleActual.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialogDetalleActual.setContentView(R.layout.dialog_detalle_runner);
    if (dialogDetalleActual.getWindow() != null) {
      dialogDetalleActual.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    Button btnCerrar = dialogDetalleActual.findViewById(R.id.btnCerrarDetalle);
    btnDarDeBajaGlobal = dialogDetalleActual.findViewById(R.id.btnDarDeBaja);
    tvMensajeFeedback = dialogDetalleActual.findViewById(R.id.tvMensajeBaja);
    
    tvNombreDialog = dialogDetalleActual.findViewById(R.id.tvNombreCompleto);
    tvDniSexoDialog = dialogDetalleActual.findViewById(R.id.tvDniSexoEdad);
    tvLocalidadDialog = dialogDetalleActual.findViewById(R.id.tvLocalidad);
    tvCatTalleDialog = dialogDetalleActual.findViewById(R.id.tvCategoriaTalle);
    tvEmailDialog = dialogDetalleActual.findViewById(R.id.tvEmail);
    tvTelDialog = dialogDetalleActual.findViewById(R.id.tvTelefono);
    tvEmergenciaDialog = dialogDetalleActual.findViewById(R.id.tvContactoEmergencia);
    tvTelEmergenciaDialog = dialogDetalleActual.findViewById(R.id.tvTelEmergencia);

    btnCerrar.setOnClickListener(v -> dialogDetalleActual.dismiss());

    btnDarDeBajaGlobal.setOnClickListener(v -> {
      new androidx.appcompat.app.AlertDialog.Builder(requireContext())
        .setTitle("Confirmar baja")
        .setMessage("¿Estás seguro de eliminar a " + detalleStateGlobal.nombre + "?")
        .setPositiveButton("Sí, eliminar", (d, w) -> {
          String textoBusqueda = binding.searchViewBusqueda.getQuery().toString();
          mViewModel.confirmarBaja(detalleStateGlobal.idInscripcion, detalleStateGlobal.nombre, textoBusqueda);
        })
        .setNegativeButton("Cancelar", null)
        .show();
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
    dialogDetalleActual = null;
    tvMensajeFeedback = null;
    btnDarDeBajaGlobal = null;
    tvNombreDialog = null;
    tvDniSexoDialog = null;
    tvLocalidadDialog = null;
    tvCatTalleDialog = null;
    tvEmailDialog = null;
    tvTelDialog = null;
    tvEmergenciaDialog = null;
    tvTelEmergenciaDialog = null;
  }
}