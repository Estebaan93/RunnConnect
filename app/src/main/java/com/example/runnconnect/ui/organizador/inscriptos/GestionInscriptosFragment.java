package com.example.runnconnect.ui.organizador.inscriptos;

import android.app.Dialog;
import android.os.Bundle;
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

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentGestionInscriptosBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(GestionInscriptosViewModel.class);

    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }

    setupRecyclerView();
    setupListeners();
    setupObservers();

    viewModel.cargarInscriptos(idEvento);

    return binding.getRoot();
  }

  private void setupListeners() {
    binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (checkedIds.isEmpty()) return;
      int id = checkedIds.get(0);

      if (id == R.id.chipProcesando) viewModel.cambiarFiltro("procesando");
      else if (id == R.id.chipPagado) viewModel.cambiarFiltro("pagado");
      else if (id == R.id.chipPendiente) viewModel.cambiarFiltro("pendiente");
      else viewModel.cambiarFiltro(null);
    });
  }

  private void setupObservers() {
    // UI States
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    viewModel.getMensajeToast().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        viewModel.limpiarMensaje();
      }
    });

    viewModel.getListaInscriptos().observe(getViewLifecycleOwner(), lista -> {
      if (lista != null) adapter.setLista(lista);
    });

    viewModel.getEsListaVacia().observe(getViewLifecycleOwner(), vacio -> {
      binding.tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
      binding.recyclerInscriptos.setVisibility(vacio ? View.GONE : View.VISIBLE);
    });

    // ordenes de dialogos
    viewModel.getOrdenMostrarValidacion().observe(getViewLifecycleOwner(), item -> {
      if (item != null) {
        mostrarDialogoValidacion(item);
        viewModel.limpiarOrdenesDialogo();
      }
    });

    viewModel.getOrdenMostrarDetalle().observe(getViewLifecycleOwner(), item -> {
      if (item != null) {
        mostrarDetalleRunner(item);
        viewModel.limpiarOrdenesDialogo();
      }
    });
  }

  private void setupRecyclerView() {

    adapter = new InscriptosAdapter(item -> viewModel.onInscriptoSeleccionado(item));

    binding.recyclerInscriptos.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerInscriptos.setAdapter(adapter);
  }

  // --- VISUALIZACION (Pura UI: Inflar Layouts y Pintar Datos) ---

  private void mostrarDetalleRunner(InscriptoEventoResponse item) {
    final Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_detalle_runner);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Bindings manuales
    TextView tvNombre = dialog.findViewById(R.id.tvNombreCompleto);
    TextView tvDniSexo = dialog.findViewById(R.id.tvDniSexoEdad);
    TextView tvLocalidad = dialog.findViewById(R.id.tvLocalidad);
    TextView tvCatTalle = dialog.findViewById(R.id.tvCategoriaTalle);
    TextView tvEmail = dialog.findViewById(R.id.tvEmail);
    TextView tvTel = dialog.findViewById(R.id.tvTelefono);
    TextView tvEmergencia = dialog.findViewById(R.id.tvContactoEmergencia);
    TextView tvTelEmergencia = dialog.findViewById(R.id.tvTelEmergencia);
    Button btnCerrar = dialog.findViewById(R.id.btnCerrarDetalle);

    InscriptoEventoResponse.RunnerInscriptoInfo r = item.getRunner();

    if (r != null) {
      tvNombre.setText(r.getNombreCompleto());
      String dni = r.getDni();
      String genero = r.getGenero() != null ? r.getGenero() : "-";
      tvDniSexo.setText(String.format("DNI: %s | Sexo: %s", dni, genero));
      tvLocalidad.setText(r.getLocalidad() != null ? r.getLocalidad() : "Localidad no especificada");
      tvEmail.setText(r.getEmail());
      tvTel.setText(r.getTelefono() != null ? r.getTelefono() : "-");
      tvEmergencia.setText("Contacto: " + (r.getNombreContactoEmergencia() != null ? r.getNombreContactoEmergencia() : "No informado"));
      tvTelEmergencia.setText("Tel: " + (r.getTelefonoEmergencia() != null ? r.getTelefonoEmergencia() : "-"));
    } else {
      tvNombre.setText("Usuario no disponible");
    }

    String talle = item.getTalleRemera() != null ? item.getTalleRemera() : "-";
    tvCatTalle.setText("Categoría: " + item.getNombreCategoria() + " | Talle: " + talle);

    btnCerrar.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
  }

  private void mostrarDialogoValidacion(InscriptoEventoResponse item) {
    final Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_validar_pago);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    ImageView imgComprobante = dialog.findViewById(R.id.imgComprobante);
    Button btnAceptar = dialog.findViewById(R.id.btnAceptarPago);
    Button btnRechazar = dialog.findViewById(R.id.btnRechazarPago);
    TextView tvNombre = dialog.findViewById(R.id.tvNombreRunner);
    TextView tvDni = dialog.findViewById(R.id.tvDniRunner);

    if (item.getRunner() != null) {
      tvNombre.setText(item.getRunner().getNombreCompleto());
      tvDni.setText("DNI: " + item.getRunner().getDni());
    }

    if (item.getComprobantePagoURL() != null && !item.getComprobantePagoURL().isEmpty()) {
      Glide.with(this).load(item.getComprobantePagoURL())
        .placeholder(R.drawable.ic_launcher_background)
        .error(R.drawable.ic_launcher_foreground).into(imgComprobante);
    } else {
      imgComprobante.setImageResource(R.drawable.ic_launcher_foreground);
    }

    // CORRECCION: Delegamos la acción al VM sin pasar strings de negocio
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