package com.example.runnconnect.ui.organizador.inscriptos;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
import com.example.runnconnect.databinding.DialogDetalleRunnerBinding;
import com.example.runnconnect.databinding.DialogValidarPagoBinding;
import com.example.runnconnect.databinding.DialogVerImagenBinding;
import com.example.runnconnect.databinding.FragmentGestionInscriptosBinding;

public class GestionInscriptosFragment extends Fragment {

  private FragmentGestionInscriptosBinding binding;
  private GestionInscriptosViewModel viewModel;
  private InscriptosAdapter adapter;
  private int idEvento = 0;
  private String estadoEvento = "";

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentGestionInscriptosBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(GestionInscriptosViewModel.class);

    // se envio bundle desde detalleEventoFragmen binding.btnGestionInscriptos
    idEvento = getArguments().getInt("idEvento", 0);
    //recuperamos el estado cuando enviamos el bundle, si el estado es finalizado se ocultara el bnt "dar de baja"
    estadoEvento = getArguments().getString("estadoEvento", "");

    setupRecyclerView();
    setupListeners();
    setupObservers();

    viewModel.inicializar(idEvento, estadoEvento);

    return binding.getRoot();
  }

  private void setupListeners() {
    binding.chipProcesando.setOnClickListener(v -> viewModel.cambiarFiltro("procesando"));
    binding.chipPagado.setOnClickListener(v -> viewModel.cambiarFiltro("pagado"));
    binding.chipPendiente.setOnClickListener(v -> viewModel.cambiarFiltro("pendiente"));
  }

  private void setupObservers() {
    // UI States: Loading
    viewModel.getIsLoading().observe(getViewLifecycleOwner(),
      loading -> binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    // 1. Observers del Mensaje Global
    viewModel.getUiMensajeGlobal().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setText);
    viewModel.getUiMensajeGlobalColor().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setTextColor);
    viewModel.getUiMensajeGlobalVisibilidad().observe(getViewLifecycleOwner(), binding.tvMensajeGlobal::setVisibility);

    // Lista de datos
    viewModel.getListaInscriptos().observe(getViewLifecycleOwner(), lista -> {
      adapter.setLista(lista);
    });

    // Estado Vacío
    viewModel.getEsListaVacia().observe(getViewLifecycleOwner(), vacio -> {
      binding.tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
      binding.recyclerInscriptos.setVisibility(vacio ? View.GONE : View.VISIBLE);
    });

    // Órdenes de navegación (Abrir diálogos)
    viewModel.getMostrarValidacionSignal().observe(getViewLifecycleOwner(), signal -> {
      if (Boolean.TRUE.equals(signal)) {
        viewModel.resetMostrarValidacionSignal();
        mostrarDialogoValidacion(viewModel.getDatosValidacion().getValue());
      }
    });

    viewModel.getMostrarDetalleSignal().observe(getViewLifecycleOwner(), signal -> {
      if (Boolean.TRUE.equals(signal)) {
        viewModel.resetMostrarDetalleSignal();
        mostrarDetalleRunner(viewModel.getDatosDetalle().getValue());
      }
    });

    viewModel.getMostrarConfirmacionBajaSignal().observe(getViewLifecycleOwner(), signal -> {
      if (Boolean.TRUE.equals(signal)) {
        viewModel.resetMostrarConfirmacionBajaSignal();
        mostrarDialogoConfirmacionBaja(viewModel.getDatosConfirmacionBaja().getValue());
      }
    });

    viewModel.getUiMostrarComprobanteUrl().observe(getViewLifecycleOwner(), this::mostrarDialogoComprobante);
  }

  private void setupRecyclerView() {
    adapter = new InscriptosAdapter(item -> viewModel.onInscriptoSeleccionado(item));
    binding.recyclerInscriptos.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerInscriptos.setAdapter(adapter);

    binding.recyclerInscriptos.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0 && !recyclerView.canScrollVertically(1)) {
          viewModel.cargarSiguientePagina();
        }
      }
    });
  }

  // --- DIALOGOS ---
  private void mostrarDetalleRunner(InscriptoEventoResponse item) {
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    DialogDetalleRunnerBinding detalleBinding = DialogDetalleRunnerBinding.inflate(getLayoutInflater());
    dialog.setContentView(detalleBinding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    InscriptoEventoResponse.RunnerInscriptoInfo r = item.getRunner();

    detalleBinding.tvNombreCompleto.setText(r.getNombreCompleto());
    detalleBinding.tvDniSexoEdad.setText(r.getDniSexoFormateado());
    detalleBinding.tvLocalidad.setText(r.getLocalidad());
    detalleBinding.tvEmail.setText(r.getEmail());
    detalleBinding.tvTelefono.setText(r.getTelefono());
    detalleBinding.tvContactoEmergencia.setText(r.getContactoEmergenciaFormateado());
    detalleBinding.tvTelEmergencia.setText(r.getTelEmergenciaFormateado());
    detalleBinding.tvCategoriaTalle.setText(item.getCategoriaTalleFormateado());

    // Visibilidad y accion del comprobante delegada al ViewModel
    detalleBinding.btnVerComprobante.setVisibility(View.VISIBLE);
    detalleBinding.btnVerComprobante.setOnClickListener(v -> viewModel.onVerComprobanteClicked());

    // Visibilidad de baja basada en el estado del ViewModel
    Boolean pb = viewModel.getPermitirBajas().getValue();
    detalleBinding.btnDarDeBaja.setVisibility(Boolean.TRUE.equals(pb) ? View.VISIBLE : View.GONE);

    detalleBinding.btnDarDeBaja.setOnClickListener(v -> {
      viewModel.intentarDarDeBajaRunner(item);
      dialog.dismiss();
    });

    detalleBinding.btnCerrarDetalle.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
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

  private void mostrarDialogoConfirmacionBaja(InscriptoEventoResponse item) {
    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
      .setTitle("Confirmar baja")
      .setMessage("¿Estás seguro de que deseas eliminar la inscripción de este corredor?")
      .setPositiveButton("Sí", (d, w) -> {
        viewModel.darDeBajaRunner(item.getIdInscripcion());
      })
      .setNegativeButton("No", null)
      .show();
  }

  private void mostrarDialogoValidacion(InscriptoEventoResponse item) {
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    DialogValidarPagoBinding validarBinding = DialogValidarPagoBinding.inflate(getLayoutInflater());
    dialog.setContentView(validarBinding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    InscriptoEventoResponse.RunnerInscriptoInfo r = item.getRunner();
    validarBinding.tvNombreRunner.setText(r.getNombreCompleto());
    validarBinding.tvDniRunner.setText(r.getDniFormateado());

    String comprobanteUrl = item.getComprobantePagoURL();
    if (comprobanteUrl != null && comprobanteUrl.contains("localhost")) {
      comprobanteUrl = comprobanteUrl.replace("localhost", "10.0.2.2");
    }

    Glide.with(this)
      .load(comprobanteUrl)
      .into(validarBinding.imgComprobante);

    validarBinding.btnAceptarPago.setOnClickListener(v -> {
      viewModel.aprobarPago(item.getIdInscripcion());
      dialog.dismiss();
    });

    validarBinding.btnRechazarPago.setOnClickListener(v -> {
      viewModel.rechazarPago(item.getIdInscripcion());
      dialog.dismiss();
    });

    dialog.show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}