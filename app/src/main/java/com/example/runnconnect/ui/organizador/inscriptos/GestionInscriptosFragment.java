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

  // Referencias a los dialogos activos para el Observer
  private Dialog dialogValidacionActual;
  private Dialog dialogDetalleActual;

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
    // UI States: Loading
    viewModel.getIsLoading().observe(getViewLifecycleOwner(),
      loading -> binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    // UI States: Mensajes (Toast o Feedback en Diálogo)
    viewModel.getMensajeToast().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) {
        boolean mensajeManejado = false;

        // 1. Validar si hay diálogo de PAGO abierto
        if (dialogValidacionActual != null && dialogValidacionActual.isShowing()) {
          TextView tvFeedback = dialogValidacionActual.findViewById(R.id.tvMensajePago);
          if (tvFeedback != null) {
            tvFeedback.setText(msg);
            tvFeedback.setVisibility(View.VISIBLE);

            // Color segun el mensaje
            if (msg.toLowerCase().contains("rechazado") || msg.toLowerCase().contains("error")) {
              tvFeedback.setTextColor(Color.RED);
            } else {
              tvFeedback.setTextColor(Color.parseColor("#388E3C")); // Verde
            }

            mensajeManejado = true;
            cerrarDialogoConDelay(dialogValidacionActual);
          }
        }

        // 2. Validar si hay dialogo de DETALLE abierto (Baja)
        if (!mensajeManejado && dialogDetalleActual != null && dialogDetalleActual.isShowing()) {
          TextView tvFeedback = dialogDetalleActual.findViewById(R.id.tvMensajeBaja);
          if (tvFeedback != null) {
            tvFeedback.setText(msg);
            tvFeedback.setVisibility(View.VISIBLE);

            if (msg.toLowerCase().contains("error")) {
              tvFeedback.setTextColor(Color.RED);
            } else {
              tvFeedback.setTextColor(Color.parseColor("#388E3C")); // Verde
            }

            mensajeManejado = true;
            cerrarDialogoConDelay(dialogDetalleActual);
          }
        }

        // 3. Fallback: Toast si no hay dialogos
        if (!mensajeManejado) {
          Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }

        viewModel.limpiarMensaje();
      }
    });

    // Lista de datos
    viewModel.getListaInscriptos().observe(getViewLifecycleOwner(), lista -> {
      if (lista != null) adapter.setLista(lista);
    });

    // Estado Vacío
    viewModel.getEsListaVacia().observe(getViewLifecycleOwner(), vacio -> {
      binding.tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
      binding.recyclerInscriptos.setVisibility(vacio ? View.GONE : View.VISIBLE);
    });

    // Órdenes de navegación (Abrir diálogos)
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

  private void cerrarDialogoConDelay(Dialog dialog) {
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (dialog != null && dialog.isShowing()) {
        dialog.dismiss();
      }
    }, 1500); // 1.5 seg de espera para leer
  }

  private void setupRecyclerView() {
    adapter = new InscriptosAdapter(item -> viewModel.onInscriptoSeleccionado(item));
    binding.recyclerInscriptos.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerInscriptos.setAdapter(adapter);
  }

  // --- DIALOGOS ---
  private void mostrarDetalleRunner(InscriptoEventoResponse item) {
    // CORRECCION: Asignar a la variable global 'dialogDetalleActual'
    dialogDetalleActual = new Dialog(requireContext());
    dialogDetalleActual.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialogDetalleActual.setContentView(R.layout.dialog_detalle_runner);
    dialogDetalleActual.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Limpiar referencia al cerrar manualmente
    dialogDetalleActual.setOnDismissListener(d -> dialogDetalleActual = null);

    // Bindings
    TextView tvNombre = dialogDetalleActual.findViewById(R.id.tvNombreCompleto);
    TextView tvDniSexo = dialogDetalleActual.findViewById(R.id.tvDniSexoEdad);
    TextView tvLocalidad = dialogDetalleActual.findViewById(R.id.tvLocalidad);
    TextView tvCatTalle = dialogDetalleActual.findViewById(R.id.tvCategoriaTalle);
    TextView tvEmail = dialogDetalleActual.findViewById(R.id.tvEmail);
    TextView tvTel = dialogDetalleActual.findViewById(R.id.tvTelefono);
    TextView tvEmergencia = dialogDetalleActual.findViewById(R.id.tvContactoEmergencia);
    TextView tvTelEmergencia = dialogDetalleActual.findViewById(R.id.tvTelEmergencia);
    Button btnCerrar = dialogDetalleActual.findViewById(R.id.btnCerrarDetalle);
    Button btnDarDeBaja = dialogDetalleActual.findViewById(R.id.btnDarDeBaja);

    // Inicializar el TextView de mensaje oculto
    TextView tvFeedback = dialogDetalleActual.findViewById(R.id.tvMensajeBaja);
    tvFeedback.setVisibility(View.GONE);

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

    // Listener Dar de Baja
    btnDarDeBaja.setOnClickListener(v -> {
      new androidx.appcompat.app.AlertDialog.Builder(requireContext())
        .setTitle("Confirmar baja")
        .setMessage("¿Estás seguro de que deseas eliminar la inscripción de este corredor?")
        .setPositiveButton("Sí", (d, w) -> {
          // Llamamos al ViewModel. NO cerramos el diálogo aquí.
          // Esperamos a que el Observer reciba el mensaje de éxito/error.
          viewModel.darDeBajaRunner(item.getIdInscripcion());
        })
        .setNegativeButton("No", null)
        .show();
    });
    boolean eventoCerrado = "finalizado".equalsIgnoreCase(estadoEvento) || "cancelado".equalsIgnoreCase(estadoEvento);

    // Si el evento termino O el runner ya esta dado de baja (opcional esta segunda parte)
    if (eventoCerrado) {
      btnDarDeBaja.setVisibility(View.GONE);
    } else {
      btnDarDeBaja.setVisibility(View.VISIBLE);

      // Movemos el listener AQUI ADENTRO para que solo funcione si el boton es visible
      btnDarDeBaja.setOnClickListener(v -> {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
          .setTitle("Confirmar baja")
          .setMessage("¿Estás seguro de que deseas eliminar la inscripción de este corredor?")
          .setPositiveButton("Sí", (d, w) -> {
            viewModel.darDeBajaRunner(item.getIdInscripcion());
          })
          .setNegativeButton("No", null)
          .show();
      });
    }

    btnCerrar.setOnClickListener(v -> dialogDetalleActual.dismiss());
    dialogDetalleActual.show();
  }

  private void mostrarDialogoValidacion(InscriptoEventoResponse item) {
    dialogValidacionActual = new Dialog(requireContext());
    dialogValidacionActual.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialogValidacionActual.setContentView(R.layout.dialog_validar_pago);
    dialogValidacionActual.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Limpiar referencia al cerrar
    dialogValidacionActual.setOnDismissListener(d -> dialogValidacionActual = null);

    ImageView imgComprobante = dialogValidacionActual.findViewById(R.id.imgComprobante);
    Button btnAceptar = dialogValidacionActual.findViewById(R.id.btnAceptarPago);
    Button btnRechazar = dialogValidacionActual.findViewById(R.id.btnRechazarPago);
    TextView tvNombre = dialogValidacionActual.findViewById(R.id.tvNombreRunner);
    TextView tvDni = dialogValidacionActual.findViewById(R.id.tvDniRunner);

    // Inicializar feedback oculto
    TextView tvFeedback = dialogValidacionActual.findViewById(R.id.tvMensajePago);
    tvFeedback.setVisibility(View.GONE);

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

    btnAceptar.setOnClickListener(v -> {
      viewModel.aprobarPago(item.getIdInscripcion());
      // Deshabilitar botones para evitar doble click
      btnAceptar.setEnabled(false);
      btnRechazar.setEnabled(false);
      // NO cerramos aquí, el observer lo hará
    });

    btnRechazar.setOnClickListener(v -> {
      viewModel.rechazarPago(item.getIdInscripcion());
      btnAceptar.setEnabled(false);
      btnRechazar.setEnabled(false);
    });

    dialogValidacionActual.show();
  }
}