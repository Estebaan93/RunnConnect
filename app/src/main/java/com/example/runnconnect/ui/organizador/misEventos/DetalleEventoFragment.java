package com.example.runnconnect.ui.organizador.misEventos;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class DetalleEventoFragment extends Fragment {
  private FragmentDetalleEventoBinding binding;
  private DetalleEventoViewModel viewModel;

  // UI Dialogs
  private AlertDialog dialogCarga;
  private TextView tvNombreEnDialog;
  private Button btnSubirEnDialog;

  private AlertDialog dialogRunners;
  private AlertDialog dialogEstado;

  private CategoriasInfoAdapter categoriasInfoAdapter;
  private RunnerSimpleAdapter runnersAdapterDialog;

  private int idEvento = 0;

  // LAUNCHER
  private final ActivityResultLauncher<String> selectorArchivo = registerForActivityResult(
    new ActivityResultContracts.GetContent(),
    uri -> {
      if (uri != null) {
        viewModel.procesarArchivoSeleccionado(uri);
      }
    }
  );

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentDetalleEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(DetalleEventoViewModel.class);

    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }

    if (idEvento != 0) {
      viewModel.cargarDetalle(idEvento);
    } else {
      binding.tvMensajeGlobal.setText("Error: ID inválido");
      binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
    }

    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupObservers() {
    // 1. Loader General
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    // 2. Mensajes Globales
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      // Solo actuamos si hay mensaje
      if (msg != null && !msg.isEmpty()) {
        mostrarMensajeEnPantalla(msg);
        // Limpiamos en el VM inmediatamente para que no se repita
        viewModel.limpiarMensajeGlobal();
      }

    });

    // 3. Resultado de subida
    viewModel.getMensajeCargaArchivo().observe(getViewLifecycleOwner(), msg -> {
      if (msg == null) return;

      if ("EXITO".equals(msg)) {
        Toast.makeText(getContext(), "¡Resultados cargados!", Toast.LENGTH_SHORT).show();

        // DEMORA DE SEGURIDAD: Le damos 200ms al sistema para que respire
        // antes de destruir la ventana del dialogo, evitando el ANR.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
          if (dialogCarga != null && dialogCarga.isShowing()) {
            dialogCarga.dismiss();
          }
        }, 200);

      } else {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
      }
      viewModel.resetMensajeCarga();
    });

    // 4. Actualizacion del Dialog
    viewModel.getNombreArchivoSeleccionado().observe(getViewLifecycleOwner(), nombre -> {
      if (tvNombreEnDialog != null && dialogCarga != null && dialogCarga.isShowing()) {
        tvNombreEnDialog.setText(nombre);
      }
    });

    viewModel.getArchivoEsValido().observe(getViewLifecycleOwner(), valido -> {
      if (btnSubirEnDialog != null && dialogCarga != null && dialogCarga.isShowing()) {
        btnSubirEnDialog.setEnabled(valido);
      }
    });

    // 5. Menu Dinamico
    viewModel.getOpcionesMenuResultados().observe(getViewLifecycleOwner(), opciones -> {
      if (opciones != null && opciones.length > 0) {
        new AlertDialog.Builder(requireContext())
          .setTitle("Gestión de Resultados")
          .setItems(opciones, (dialog, which) -> {
            viewModel.onOpcionMenuSeleccionada(which);
          })
          .show();
        viewModel.resetOpcionesMenu();
      }
    });

    viewModel.getAccionNavegacionResultados().observe(getViewLifecycleOwner(), accion -> {
      if (accion == null) return;
      if ("CARGAR".equals(accion)) abrirDialogoCarga();
      else if ("VER".equals(accion)) navegarAVerResultados();
      viewModel.resetAccionNavegacion();
    });

    // 6. Binding UI
    viewModel.getUiTitulo().observe(getViewLifecycleOwner(), binding.tvTituloDetalle::setText);
    viewModel.getUiFecha().observe(getViewLifecycleOwner(), binding.tvFechaDetalle::setText);
    viewModel.getUiLugar().observe(getViewLifecycleOwner(), binding.tvLugarDetalle::setText);
    viewModel.getUiDescripcion().observe(getViewLifecycleOwner(), binding.tvDescripcion::setText);
    viewModel.getUiInscriptos().observe(getViewLifecycleOwner(), binding.tvInscriptosCount::setText);
    viewModel.getUiCupo().observe(getViewLifecycleOwner(), binding.tvCupoTotal::setText);
    viewModel.getUiEstadoTexto().observe(getViewLifecycleOwner(), binding.tvEstadoDetalle::setText);
    viewModel.getUiEstadoColor().observe(getViewLifecycleOwner(), c -> binding.tvEstadoDetalle.setTextColor(c));
    viewModel.getUiDistanciaTipo().observe(getViewLifecycleOwner(), binding.tvDistanciaTipo::setText);
    viewModel.getUiGeneroPrecio().observe(getViewLifecycleOwner(), binding.tvGeneroPrecio::setText);
    viewModel.getUiVisibilidadDatosCategoria().observe(getViewLifecycleOwner(), v -> {
      binding.tvDistanciaTipo.setVisibility(v);
      binding.tvGeneroPrecio.setVisibility(v);
    });
    viewModel.getVisibilityBtnResultados().observe(getViewLifecycleOwner(), v -> binding.btnResultados.setVisibility(v));

    // 7. Listas
    categoriasInfoAdapter = new CategoriasInfoAdapter();
    categoriasInfoAdapter.setOnCategoriaClickListener(this::mostrarDialogoRunners);
    binding.rvCategoriasDetalle.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvCategoriasDetalle.setAdapter(categoriasInfoAdapter);

    viewModel.getListaCategorias().observe(getViewLifecycleOwner(), lista -> {
      if (lista != null) categoriasInfoAdapter.setLista(lista);
    });

    viewModel.getListaRunnersDialog().observe(getViewLifecycleOwner(), runners -> {
      if (runnersAdapterDialog != null) runnersAdapterDialog.setLista(runners);
    });

    viewModel.getDialogDismiss().observe(getViewLifecycleOwner(), dismiss -> {
      if (dismiss && dialogEstado != null && dialogEstado.isShowing()) dialogEstado.dismiss();
    });
  }
  private void mostrarMensajeEnPantalla(String msg) {
    if (binding == null || binding.tvMensajeGlobal == null) return;

    // 1. Hacer visible INMEDIATAMENTE
    binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
    binding.tvMensajeGlobal.bringToFront(); // Asegurar que quede encima si hay solapamiento

    // 2. Logica de colores
    if (msg.startsWith("EXITO:")) {
      // Verde
      String textoLimpio = msg.replace("EXITO:", "").trim();
      binding.tvMensajeGlobal.setText(textoLimpio);
      binding.tvMensajeGlobal.setTextColor(Color.parseColor("#1B5E20")); // Verde oscuro
      binding.tvMensajeGlobal.setBackgroundColor(Color.parseColor("#C8E6C9")); // Verde claro
    }
    else if (msg.startsWith("ERROR:")) {
      // Rojo
      String textoLimpio = msg.replace("ERROR:", "").trim();
      binding.tvMensajeGlobal.setText(textoLimpio);
      binding.tvMensajeGlobal.setTextColor(Color.parseColor("#B71C1C")); // Rojo oscuro
      binding.tvMensajeGlobal.setBackgroundColor(Color.parseColor("#FFCDD2")); // Rojo claro
    }
    else {
      // Normal (Gris/Negro)
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setTextColor(Color.BLACK);
      binding.tvMensajeGlobal.setBackgroundColor(Color.parseColor("#F5F5F5"));
    }

    // 3. Auto-ocultar a los 4 segundos
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (binding != null && binding.tvMensajeGlobal != null) {
        binding.tvMensajeGlobal.setVisibility(View.GONE);
      }
    }, 4000);
  }

  private void setupListeners() {
    binding.btnCambiarEstado.setOnClickListener(v -> mostrarDialogoEstado());

    binding.btnGestionInscriptos.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);

      //enviamos el estado tambien
      if (viewModel.getEventoRaw().getValue() != null) {
        args.putString("estadoEvento", viewModel.getEventoRaw().getValue().getEstado());
      }

      Navigation.findNavController(v).navigate(R.id.action_detalle_to_gestionInscriptos, args);

    });

    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);

      //enviamos el estado
      args.putString("estadoEvento", viewModel.getEventoRaw().getValue().getEstado());

      Navigation.findNavController(v).navigate(R.id.action_detalle_to_mapaEditor, args);
    });

    binding.btnEditarInfo.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      try { Navigation.findNavController(v).navigate(R.id.action_detalle_to_editarEvento, args); }
      catch (Exception e) {
        try { Navigation.findNavController(v).navigate(R.id.nav_crear_evento, args); } catch(Exception ex){}
      }
    });

    // BOTON RESULTADOS
    binding.btnResultados.setOnClickListener(v -> viewModel.solicitarMenuResultados());
  }

  private void abrirDialogoCarga() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_carga_resultados, null);

    Button btnElegir = view.findViewById(R.id.btnSeleccionarArchivo);
    btnSubirEnDialog = view.findViewById(R.id.btnSubirArchivo);
    tvNombreEnDialog = view.findViewById(R.id.tvNombreArchivo);

    btnSubirEnDialog.setEnabled(false);
    tvNombreEnDialog.setText("Selecciona un archivo CSV");

    btnElegir.setOnClickListener(v -> selectorArchivo.launch("*/*"));

    btnSubirEnDialog.setOnClickListener(v -> {
      btnSubirEnDialog.setEnabled(false);
      btnSubirEnDialog.setText("Enviando...");
      viewModel.subirArchivoGuardado(idEvento);
    });

    builder.setView(view);
    dialogCarga = builder.create();

    dialogCarga.setOnDismissListener(d -> {
      tvNombreEnDialog = null;
      btnSubirEnDialog = null;
    });

    dialogCarga.show();
  }

  private void navegarAVerResultados() {
    Bundle args = new Bundle();
    args.putInt("idEvento", idEvento);
    Navigation.findNavController(requireView()).navigate(R.id.action_detalle_to_resultados, args);
  }

  private void mostrarDialogoRunners(CategoriaResponse categoria) {
    viewModel.cargarRunnersDeCategoria(idEvento, categoria.getIdCategoria());
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Inscriptos");
    View view = getLayoutInflater().inflate(R.layout.dialog_lista_runners, null);
    RecyclerView rv = view.findViewById(R.id.rvRunnersDialog);

    //instancia del adapter
    runnersAdapterDialog = new RunnerSimpleAdapter(runner -> confirmarBajaRunner(runner, categoria.getIdCategoria()));

    //verificamos estado del evento
    if (viewModel.getEventoRaw().getValue() != null) {
      String estadoActual = viewModel.getEventoRaw().getValue().getEstado();

      // Si es finalizado o cancelado, DESHABILITAR la opcion de borrar runners
      if ("finalizado".equalsIgnoreCase(estadoActual) || "cancelado".equalsIgnoreCase(estadoActual)) {
        runnersAdapterDialog.setHabilitarEliminacion(false);
      } else {
        runnersAdapterDialog.setHabilitarEliminacion(true);
      }
    }
    rv.setLayoutManager(new LinearLayoutManager(getContext()));
    rv.setAdapter(runnersAdapterDialog);

    builder.setView(view);
    builder.setPositiveButton("Cerrar", null);
    dialogRunners = builder.create();
    dialogRunners.show();
  }

  private void confirmarBajaRunner(InscriptoEventoResponse runner, int idCat) {
    new AlertDialog.Builder(requireContext())
      .setTitle("Dar de baja")
      .setMessage("¿Confirmar baja de " + runner.getRunner().getNombre() + "?")
      .setPositiveButton("Sí", (d, w) -> viewModel.darDeBajaRunner(runner.getIdInscripcion(), "Baja organizador", idEvento, idCat))
      .setNegativeButton("No", null)
      .show();
  }

  private void mostrarDialogoEstado() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
    builder.setView(view);
    android.widget.RadioGroup rg = view.findViewById(R.id.rgEstado);
    android.widget.EditText et = view.findViewById(R.id.etMotivo);

    if (viewModel.getEventoRaw().getValue() != null) {
      int idCheck = viewModel.calcularPreseleccionRadio(viewModel.getEventoRaw().getValue().getEstado());
      if(idCheck != -1) rg.check(idCheck);
    }

    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cerrar", null);
    dialogEstado = builder.create();
    dialogEstado.show();

    dialogEstado.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      int selected = rg.getCheckedRadioButtonId();
      String motivo = et.getText().toString();
      try { ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), 0); } catch(Exception e){}
      new Handler(Looper.getMainLooper()).postDelayed(() -> viewModel.procesarCambioEstado(idEvento, selected, motivo), 100);
    });

    viewModel.getDialogError().observe(getViewLifecycleOwner(), error -> {
      if (error != null && dialogEstado.isShowing()) { et.setError(error); et.requestFocus(); }
    });
  }
}