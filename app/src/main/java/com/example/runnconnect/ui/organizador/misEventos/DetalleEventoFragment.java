package com.example.runnconnect.ui.organizador.misEventos;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
import com.example.runnconnect.databinding.FragmentDetalleEventoBinding;

import java.util.ArrayList;
import java.util.List;

public class DetalleEventoFragment extends Fragment {
  private FragmentDetalleEventoBinding binding;
  private DetalleEventoViewModel viewModel;


  private CategoriasInfoAdapter categoriasInfoAdapter;
  private RunnerSimpleAdapter runnersAdapterDialog;

  private int idEvento = 0;

  // LAUNCHER
  private final ActivityResultLauncher<String> selectorArchivo = registerForActivityResult(
    new ActivityResultContracts.GetContent(),
    uri -> viewModel.procesarArchivoSeleccionado(uri)
  );

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentDetalleEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(DetalleEventoViewModel.class);

    Log.d("DEBUG_EVENTO", "getArguments() es: " + getArguments());
    idEvento = (getArguments() != null) ? getArguments().getInt("idEvento") : 0;
    Log.d("DEBUG_EVENTO", "idEvento parseado es: " + idEvento);
    viewModel.cargarDetalle(idEvento);

    setupListeners();
    setupObservers();
    binding.tvMensajeGlobal.bringToFront();

    return binding.getRoot();
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
    );

    // MVVM Puro: La vista no juzga el texto ni controla el tiempo.
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setText);
    viewModel.getMensajeGlobalVisibilidad().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setVisibility);

    viewModel.getMensajeColorTexto().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setTextColor);
    viewModel.getMensajeColorFondo().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setBackgroundColor);

    // MVVM Puro: Reacciona a un booleano en lugar de parsear "EXITO"
    viewModel.getExitoCargaArchivo().observe(getViewLifecycleOwner(), exito -> {
      binding.btnConfirmarCargaArchivo.setText("Subir Archivo");
    });

    /*Revisar*/
    viewModel.getResumenCargaArchivo().observe(getViewLifecycleOwner(), resumen -> {
      if (resumen != null) {
        viewModel.resetResumenCargaArchivo();
        new AlertDialog.Builder(requireContext())
          .setTitle("Resultado de la Carga")
          .setMessage(resumen)
          .setPositiveButton("Cerrar", null)
          .show();
      }
    });

    viewModel.getNombreArchivoSeleccionado().observe(getViewLifecycleOwner(), binding.tvNombreArchivoCarga::setText);

    viewModel.getArchivoEsValido().observe(getViewLifecycleOwner(), binding.btnConfirmarCargaArchivo::setEnabled);

    viewModel.getUiVisibilidadCarga().observe(getViewLifecycleOwner(), binding.cargaResultados::setVisibility);

    viewModel.getOpcionesSpinnerCategoria().observe(getViewLifecycleOwner(), nombres -> {
      ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres);
      binding.spCategoriaResultadosCarga.setAdapter(adapter);
    });

    /*Revisar*/
    viewModel.getOpcionesMenuResultados().observe(getViewLifecycleOwner(), opciones -> {
      if (opciones != null) {
        viewModel.resetOpcionesMenu();
        new AlertDialog.Builder(requireContext())
          .setTitle("Gestión de Resultados")
          .setItems(opciones, (dialog, which) -> viewModel.procesarSeleccionMenuResultados(opciones[which]))
          .show();
      }
    });

    /*Revisar*/
    viewModel.getEventVerResultados().observe(getViewLifecycleOwner(), navegar -> {
      if (Boolean.TRUE.equals(navegar)) {
        viewModel.resetEventVerResultados();
        navegarAVerResultados();
      }
    });

    // Binding UI
    viewModel.getUiTitulo().observe(getViewLifecycleOwner(), binding.tvTituloDetalle::setText);
    viewModel.getUiFecha().observe(getViewLifecycleOwner(), binding.tvFechaDetalle::setText);
    viewModel.getUiLugar().observe(getViewLifecycleOwner(), binding.tvLugarDetalle::setText);
    viewModel.getUiDescripcion().observe(getViewLifecycleOwner(), binding.tvDescripcion::setText);
    viewModel.getUiInscriptos().observe(getViewLifecycleOwner(), binding.tvInscriptosCount::setText);
    viewModel.getUiCupo().observe(getViewLifecycleOwner(), binding.tvCupoTotal::setText);
    viewModel.getUiEstadoTexto().observe(getViewLifecycleOwner(), binding.tvEstadoDetalle::setText);
    viewModel.getUiEstadoColor().observe(getViewLifecycleOwner(), c -> { if (c != null) binding.tvEstadoDetalle.setTextColor(c); });
    viewModel.getUiDistanciaTipo().observe(getViewLifecycleOwner(), binding.tvDistanciaTipo::setText);
    viewModel.getUiGeneroPrecio().observe(getViewLifecycleOwner(), binding.tvGeneroPrecio::setText);

    // MVVM Puro: Recibe Boolean y lo traduce a View.GONE
    viewModel.getUiVisibilidadDatosCategoria().observe(getViewLifecycleOwner(), visible -> {
      int v = Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE;
      binding.tvDistanciaTipo.setVisibility(v);
      binding.tvGeneroPrecio.setVisibility(v);
    });

    viewModel.getVisibilityBtnResultados().observe(getViewLifecycleOwner(), visible ->
      binding.btnResultados.setVisibility(Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE)
    );

    // Listas
    categoriasInfoAdapter = new CategoriasInfoAdapter();
    categoriasInfoAdapter.setOnCategoriaClickListener(categoria -> viewModel.onCategoriaClickNormal(categoria));
    categoriasInfoAdapter.setOnCategoriaLongClickListener(categoria -> viewModel.onCategoriaClickLargo(categoria));

    binding.rvCategoriasDetalle.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvCategoriasDetalle.setAdapter(categoriasInfoAdapter);

    viewModel.getUiVisibilidadRunners().observe(getViewLifecycleOwner(), binding.runnersOverlay::setVisibility);
    
    viewModel.getHabilitarEliminacionRunners().observe(getViewLifecycleOwner(), habilitado ->
        runnersAdapterDialog.setHabilitarEliminacion(Boolean.TRUE.equals(habilitado))
    );
    
    viewModel.getListaCategoriasUI().observe(getViewLifecycleOwner(), categoriasInfoAdapter::setLista);


    viewModel.getListaRunnersDialog().observe(getViewLifecycleOwner(), runnersAdapterDialog::setLista);

    /*Revisar*/
    viewModel.getTextoConfirmacionBaja().observe(getViewLifecycleOwner(), texto -> {
      if (texto != null) {
        new AlertDialog.Builder(requireContext())
          .setTitle("Dar de baja")
          .setMessage(texto)
          .setPositiveButton("Sí", (d, w) -> viewModel.confirmarBajaRunnerPendiente())
          .setNegativeButton("No", (d, w) -> viewModel.limpiarConfirmacionBaja())
          .setOnCancelListener(d -> viewModel.limpiarConfirmacionBaja())
          .show();
      }
    });

    viewModel.getUiVisibilidadEstado().observe(getViewLifecycleOwner(), binding.cambiarEstadoEvento::setVisibility);
    
    viewModel.getPosicionEstadoEvento().observe(getViewLifecycleOwner(), pos -> 
      binding.rgEstadoEvento.check(binding.rgEstadoEvento.getChildAt(pos).getId())
    );
    
    viewModel.getMotivoEventoTexto().observe(getViewLifecycleOwner(), binding.etMotivoEvento::setText);

    viewModel.getDialogErrorTexto().observe(getViewLifecycleOwner(), binding.tvErrorDialogEstado::setText);
    viewModel.getDialogErrorVisibilidad().observe(getViewLifecycleOwner(), binding.tvErrorDialogEstado::setVisibility);

    viewModel.getUiVisibilidadEstadoCategoria().observe(getViewLifecycleOwner(), binding.cambiarEstadoCategoria::setVisibility);

    /*Revisar*/
    viewModel.getErrorMotivoCategoriaTexto().observe(getViewLifecycleOwner(), binding.tvErrorMotivoCategoria::setText);
    viewModel.getErrorMotivoCategoriaVisibilidad().observe(getViewLifecycleOwner(), binding.tvErrorMotivoCategoria::setVisibility);

    viewModel.getEstadosCategoriasValidos().observe(getViewLifecycleOwner(), estados -> {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, estados);
        binding.spEstadoCategoria.setAdapter(adapter);
    });

    viewModel.getPosicionPreseleccionadaCategoria().observe(getViewLifecycleOwner(), pos -> 
        binding.spEstadoCategoria.setSelection(pos)
    );
  }



  private void setupListeners() {
    setupListenersOverlayEvento();
    setupRunners();
    binding.btnCambiarEstado.setOnClickListener(v -> viewModel.prepararDialogoEstado());

    binding.btnGestionInscriptos.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      args.putString("estadoEvento", viewModel.getEstadoActualEvento().getValue());
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_gestionInscriptos, args);
    });

    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      args.putString("estadoEvento", viewModel.getEstadoActualEvento().getValue());
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_mapaEditor, args);
    });

    binding.btnEditarInfo.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_editarEvento, args);
    });

    binding.btnResultados.setOnClickListener(v -> viewModel.solicitarMenuResultados());

    binding.btnSeleccionarArchivoCarga.setOnClickListener(v -> selectorArchivo.launch("*/*"));
    binding.btnCancelarCargaArchivo.setOnClickListener(v -> viewModel.cerrarCarga());
    binding.btnConfirmarCargaArchivo.setOnClickListener(v -> {
        int pos = binding.spCategoriaResultadosCarga.getSelectedItemPosition();
        viewModel.procesarSubidaArchivo(idEvento, pos);
    });
    binding.btnCancelarEstadoCategoria.setOnClickListener(v -> viewModel.cerrarEstadoCategoria());

    /*ocultar teclado*/
    /*Revisar*/
    binding.btnConfirmarEstadoCategoria.setOnClickListener(v -> {
        int pos = binding.spEstadoCategoria.getSelectedItemPosition();
        String motivo = binding.etMotivoCategoria.getText().toString();
        
        // Ocultar teclado de forma directa, Vista 100% tonta
        ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(v.getWindowToken(), 0);
            
        viewModel.guardarNuevoEstadoCategoria(pos, motivo);
    });
  }



  private void navegarAVerResultados() {
    Bundle args = new Bundle();
    args.putInt("idEvento", idEvento);
    Navigation.findNavController(requireView()).navigate(R.id.action_detalle_to_resultados, args);
  }

  private void setupRunners() {
    runnersAdapterDialog = new RunnerSimpleAdapter(runner -> 
      viewModel.solicitarConfirmacionBaja(runner, viewModel.getCategoriaSeleccionada().getIdCategoria())
    );
    binding.rvRunnersDialog.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvRunnersDialog.setAdapter(runnersAdapterDialog);

    binding.btnCerrarRunners.setOnClickListener(v -> viewModel.cerrarRunnersOverlay());
  }

  private void setupListenersOverlayEvento() {
    binding.btnConfirmarEstadoEvento.setOnClickListener(v -> {
      int selectedId = binding.rgEstadoEvento.getCheckedRadioButtonId();
      View selectedView = binding.rgEstadoEvento.findViewById(selectedId);
      int selectedIndex = binding.rgEstadoEvento.indexOfChild(selectedView);

      String motivo = binding.etMotivoEvento.getText().toString();
      ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
          .hideSoftInputFromWindow(binding.etMotivoEvento.getWindowToken(), 0);

      viewModel.procesarCambioEstadoEvento(idEvento, selectedIndex, motivo);
    });

    binding.btnCancelarEstadoEvento.setOnClickListener(v -> viewModel.cerrarDialogoEstado());
  }
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}