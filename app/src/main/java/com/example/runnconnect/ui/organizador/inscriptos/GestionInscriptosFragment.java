package com.example.runnconnect.ui.organizador.inscriptos;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
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

    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }

    //recuperamos el estado cuando enviamos el bundle, si el estado es finalizado se ocultara el bnt "dar de baja"
    estadoEvento= getArguments().getString("estadoEvento", "");

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
      mostrarDialogoValidacion(viewModel.getDatosValidacion().getValue());
    });

    viewModel.getMostrarDetalleSignal().observe(getViewLifecycleOwner(), signal -> {
      mostrarDetalleRunner(viewModel.getDatosDetalle().getValue());
    });

    viewModel.getMostrarConfirmacionBajaSignal().observe(getViewLifecycleOwner(), signal -> {
      mostrarDialogoConfirmacionBaja(viewModel.getDatosConfirmacionBaja().getValue());
    });
  }

  private void setupRecyclerView() {
    adapter = new InscriptosAdapter(item -> viewModel.onInscriptoSeleccionado(item));
    binding.recyclerInscriptos.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerInscriptos.setAdapter(adapter);
  }

  // --- DIALOGOS ---
  private void mostrarDetalleRunner(InscriptoEventoResponse item) {
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_detalle_runner);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Bindings
    TextView tvNombre = dialog.findViewById(R.id.tvNombreCompleto);
    TextView tvDniSexo = dialog.findViewById(R.id.tvDniSexoEdad);
    TextView tvLocalidad = dialog.findViewById(R.id.tvLocalidad);
    TextView tvEmail = dialog.findViewById(R.id.tvEmail);
    TextView tvTel = dialog.findViewById(R.id.tvTelefono);
    TextView tvEmergencia = dialog.findViewById(R.id.tvContactoEmergencia);
    TextView tvTelEmergencia = dialog.findViewById(R.id.tvTelEmergencia);
    TextView tvCatTalle = dialog.findViewById(R.id.tvCategoriaTalle);
    Button btnCerrar = dialog.findViewById(R.id.btnCerrarDetalle);
    Button btnDarDeBaja = dialog.findViewById(R.id.btnDarDeBaja);

    InscriptoEventoResponse.RunnerInscriptoInfo r = item.getRunner();

    tvNombre.setText(r.getNombreCompleto());
    tvDniSexo.setText(r.getDniSexoFormateado());
    tvLocalidad.setText(r.getLocalidad());
    tvEmail.setText(r.getEmail());
    tvTel.setText(r.getTelefono());
    tvEmergencia.setText(r.getContactoEmergenciaFormateado());
    tvTelEmergencia.setText(r.getTelEmergenciaFormateado());
    tvCatTalle.setText(item.getCategoriaTalleFormateado());

    // Visibilidad basada puramente en el estado del ViewModel
    Boolean pb = viewModel.getPermitirBajas().getValue();
    btnDarDeBaja.setVisibility(pb != null && pb ? View.VISIBLE : View.GONE);

    btnDarDeBaja.setOnClickListener(v -> {
      viewModel.intentarDarDeBajaRunner(item);
      dialog.dismiss();
    });

    btnCerrar.setOnClickListener(v -> dialog.dismiss());
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
    dialog.setContentView(R.layout.dialog_validar_pago);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    ImageView imgComprobante = dialog.findViewById(R.id.imgComprobante);
    Button btnAceptar = dialog.findViewById(R.id.btnAceptarPago);
    Button btnRechazar = dialog.findViewById(R.id.btnRechazarPago);
    TextView tvNombre = dialog.findViewById(R.id.tvNombreRunner);
    TextView tvDni = dialog.findViewById(R.id.tvDniRunner);

    InscriptoEventoResponse.RunnerInscriptoInfo r = item.getRunner();
    tvNombre.setText(r.getNombreCompleto());
    tvDni.setText(r.getDniFormateado());

    Glide.with(this)
      .load(item.getComprobantePagoURL())
      .placeholder(R.drawable.ic_launcher_background)
      .fallback(R.drawable.ic_launcher_foreground)
      .error(R.drawable.ic_launcher_foreground)
      .into(imgComprobante);

    btnAceptar.setOnClickListener(v -> {
      viewModel.aprobarPago(item.getIdInscripcion());
      dialog.dismiss();
    });

    btnRechazar.setOnClickListener(v -> {
      viewModel.rechazarPago(item.getIdInscripcion());
      dialog.dismiss();
    });

    dialog.show();
  }
}