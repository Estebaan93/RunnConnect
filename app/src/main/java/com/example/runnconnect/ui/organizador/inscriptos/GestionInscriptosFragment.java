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
  private InscriptosAdapter adapter; // Asumo que ya tienes o crearás este adapter
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

    // Carga inicial (el VM usa filtro "procesando" por defecto)
    viewModel.cargarInscriptos(idEvento);

    return binding.getRoot();
  }

  private void setupListeners() {
    // Listener de Chips para filtrar estados
    binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (checkedIds.isEmpty()) return;
      int id = checkedIds.get(0);

      if (id == R.id.chipProcesando) viewModel.cambiarFiltro("procesando");
      else if (id == R.id.chipPagado) viewModel.cambiarFiltro("pagado");
      else if (id == R.id.chipPendiente) viewModel.cambiarFiltro("pendiente");
      else viewModel.cambiarFiltro(null); // Todos
    });
  }

  private void setupObservers() {
    // 1. Loading
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    // 2. Mensajes (Toast)
    viewModel.getMensajeToast().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        viewModel.limpiarMensaje();
      }
    });

    // 3. Lista de Inscriptos
    viewModel.getListaInscriptos().observe(getViewLifecycleOwner(), lista -> {
      if (lista != null) {
        adapter.setLista(lista);
      }
    });

    // 4. Estado vacío (si no hay resultados)
    viewModel.getEsListaVacia().observe(getViewLifecycleOwner(), vacio -> {
      binding.tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
      binding.recyclerInscriptos.setVisibility(vacio ? View.GONE : View.VISIBLE);
    });
  }

  private void setupRecyclerView() {
    adapter = new InscriptosAdapter(item -> {
      // Lógica de click
      if ("procesando".equalsIgnoreCase(item.getEstadoPago())) {
        mostrarDialogoValidacion(item); // Tu diálogo existente para aprobar pagos
      } else {
        mostrarDetalleRunner(item); // NUEVO: Diálogo informativo
      }
    });

    binding.recyclerInscriptos.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerInscriptos.setAdapter(adapter);
  }
  // --- NUEVO MÉTODO: Muestra ficha del corredor ---
  private void mostrarDetalleRunner(InscriptoEventoResponse item) {
    final Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_detalle_runner);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Referencias
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

      // Formateo de datos (Manejo de nulos)
      String dni = r.getDni();
      String genero = r.getGenero() != null ? r.getGenero() : "-";
      // Nota: Podrías calcular la edad aquí si tienes la fecha de nacimiento
      tvDniSexo.setText(String.format("DNI: %s | Sexo: %s", dni, genero));

      tvLocalidad.setText(r.getLocalidad() != null ? r.getLocalidad() : "Localidad no especificada");
      tvEmail.setText(r.getEmail());
      tvTel.setText(r.getTelefono() != null ? r.getTelefono() : "-");

      tvEmergencia.setText("Contacto: " + (r.getNombreContactoEmergencia() != null ? r.getNombreContactoEmergencia() : "No informado"));
      tvTelEmergencia.setText("Tel: " + (r.getTelefonoEmergencia() != null ? r.getTelefonoEmergencia() : "-"));
    } else {
      tvNombre.setText("Usuario eliminado o no disponible");
    }

    String talle = item.getTalleRemera() != null ? item.getTalleRemera() : "-";
    tvCatTalle.setText("Categoría: " + item.getNombreCategoria() + " | Talle: " + talle);

    btnCerrar.setOnClickListener(v -> dialog.dismiss());

    dialog.show();
  }

  // --- DIÁLOGO DE VALIDACIÓN DE PAGO ---
  private void mostrarDialogoValidacion(InscriptoEventoResponse item) {
    final Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.dialog_validar_pago);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Referencias a las vistas del diálogo
    ImageView imgComprobante = dialog.findViewById(R.id.imgComprobante);
    Button btnAceptar = dialog.findViewById(R.id.btnAceptarPago);
    Button btnRechazar = dialog.findViewById(R.id.btnRechazarPago);
    TextView tvNombre = dialog.findViewById(R.id.tvNombreRunner);
    TextView tvDni = dialog.findViewById(R.id.tvDniRunner);

    // Llenar datos usando el objeto Runner interno
    if (item.getRunner() != null) {
      tvNombre.setText(item.getRunner().getNombreCompleto());
      tvDni.setText("DNI: " + item.getRunner().getDni());
    }

    // Cargar imagen con Glide
    if (item.getComprobantePagoURL() != null && !item.getComprobantePagoURL().isEmpty()) {
      Glide.with(this)
        .load(item.getComprobantePagoURL())
        .placeholder(R.drawable.ic_launcher_background) // Usa un placeholder real
        .error(R.drawable.ic_launcher_foreground) // Imagen de error
        .into(imgComprobante);
    } else {
      // Manejo si no hay URL (aunque no debería pasar en estado "procesando")
      imgComprobante.setImageResource(R.drawable.ic_launcher_foreground);
    }

    // Botones de acción
    btnAceptar.setOnClickListener(v -> {
      viewModel.validarPago(item.getIdInscripcion(), true, "Pago confirmado por organizador");
      dialog.dismiss();
    });

    btnRechazar.setOnClickListener(v -> {
      viewModel.validarPago(item.getIdInscripcion(), false, "Comprobante inválido o ilegible");
      dialog.dismiss();
    });

    dialog.show();
  }



}
