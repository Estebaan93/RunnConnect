package com.example.runnconnect.ui.organizador.misEventos;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

  // UI Dialogs
  private AlertDialog dialogCarga;
  private TextView tvNombreEnDialog;
  private Button btnSubirEnDialog;

  private AlertDialog dialogRunners;
  private AlertDialog dialogEstado;

  private AlertDialog dialogEstadoCategoria;
  private android.widget.EditText etMotivoCategoriaDialog;

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
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
    );

    // MVVM Puro: La vista no juzga el texto. Lo recibe y lo pone.
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null && !msg.isEmpty()) {
        mostrarMensajeEnPantalla(msg);
        viewModel.limpiarMensajeGlobal();
      }
    });

    viewModel.getMensajeColorTexto().observe(getViewLifecycleOwner(), color -> {
      if(binding != null && binding.tvMensajeGlobal != null && color != null) binding.tvMensajeGlobal.setTextColor(color);
    });

    viewModel.getMensajeColorFondo().observe(getViewLifecycleOwner(), color -> {
      if(binding != null && binding.tvMensajeGlobal != null && color != null) binding.tvMensajeGlobal.setBackgroundColor(color);
    });

    // MVVM Puro: Reacciona a un booleano en lugar de parsear "EXITO"
    viewModel.getExitoCargaArchivo().observe(getViewLifecycleOwner(), exito -> {
      if (exito == null) return;
      if (Boolean.TRUE.equals(exito)) {
        //Toast.makeText(getContext(), "¡Resultados cargados!", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
          if (dialogCarga != null && dialogCarga.isShowing()) dialogCarga.dismiss();
        }, 200);
      }
      viewModel.resetExitoCarga();
    });

    // NUEVO: Observador para mostrar el resumen de la carga (errores de DNI)
    viewModel.getResumenCargaArchivo().observe(getViewLifecycleOwner(), resumen -> {
      if (resumen != null) {
        new AlertDialog.Builder(requireContext())
          .setTitle("Resultado de la carga")
          .setMessage(resumen)
          .setPositiveButton("Entendido", null)
          .show();
        viewModel.resetResumenCargaArchivo();
      }
    });

    viewModel.getNombreArchivoSeleccionado().observe(getViewLifecycleOwner(), nombre -> {
      if (tvNombreEnDialog != null && dialogCarga != null && dialogCarga.isShowing()) {
        tvNombreEnDialog.setText(nombre);
      }
    });

    viewModel.getArchivoEsValido().observe(getViewLifecycleOwner(), valido -> {
      if (btnSubirEnDialog != null && dialogCarga != null && dialogCarga.isShowing()) {
        btnSubirEnDialog.setEnabled(Boolean.TRUE.equals(valido));
      }
    });

    viewModel.getOpcionesMenuResultados().observe(getViewLifecycleOwner(), opciones -> {
      if (opciones != null && opciones.length > 0) {
        viewModel.resetOpcionesMenu();
        new AlertDialog.Builder(requireContext())
          .setTitle("Gestión de Resultados")
          .setItems(opciones, (dialog, which) -> {
            // AHORA PASAMOS EL TEXTO, NO EL INDICE
            viewModel.onOpcionMenuSeleccionada(opciones[which]);
          })
          .show();
      }
    });

    // MVVM Puro: Enum en lugar de Magic Strings ("CARGAR", "VER")
    viewModel.getAccionNavegacionResultados().observe(getViewLifecycleOwner(), accion -> {
      if (accion == null) return;

      // ESTE ES EL CORRECTO PARA LIMPIAR LA NAVEGACION (Evita el bucle infinito)
      viewModel.resetAccionNavegacion();

      if (accion == DetalleEventoViewModel.AccionResultados.CARGAR) abrirDialogoCarga();
      else if (accion == DetalleEventoViewModel.AccionResultados.VER) navegarAVerResultados();
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

    viewModel.observarEventoRunners(getViewLifecycleOwner(), this::abrirDialogoRunnersDesdeVM);
    viewModel.observarEventoEstadoCategoria(getViewLifecycleOwner(), this::abrirDialogoEstadoCategoriaDesdeVM);

    viewModel.getListaCategorias().observe(getViewLifecycleOwner(), lista -> {
      if (lista != null) categoriasInfoAdapter.setLista(lista);
    });

    viewModel.getListaRunnersDialog().observe(getViewLifecycleOwner(), runners -> {
      if (runnersAdapterDialog != null) runnersAdapterDialog.setLista(runners);
    });

    viewModel.getDialogDismiss().observe(getViewLifecycleOwner(), dismiss -> {
      if (Boolean.TRUE.equals(dismiss) && dialogEstado != null && dialogEstado.isShowing()) dialogEstado.dismiss();
    });

    // Observador Reactivo Categoria
    viewModel.getErrorMotivoCategoria().observe(getViewLifecycleOwner(), error -> {
      if (error == null || dialogEstadoCategoria == null || !dialogEstadoCategoria.isShowing() || etMotivoCategoriaDialog == null) return;

      if ("DISMISS".equals(error)) {
        try {
          InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(etMotivoCategoriaDialog.getWindowToken(), 0);
        } catch (Exception e) { e.printStackTrace(); }
        dialogEstadoCategoria.dismiss();
      } else {
        etMotivoCategoriaDialog.setError(error);
        etMotivoCategoriaDialog.requestFocus();
      }
    });
  }

  // La Vista ya no parsea texto. Solo lo pinta y lo oculta a los 4 seg.
  private void mostrarMensajeEnPantalla(String msg) {
    if (binding == null || binding.tvMensajeGlobal == null) return;

    binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
    binding.tvMensajeGlobal.bringToFront();
    binding.tvMensajeGlobal.setText(msg);

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

      // MVVM Puro: Usamos la propiedad preparada por el VM, no le hacemos ".getValue().get..." al raw.
      String estadoAct = viewModel.getEstadoActualEvento().getValue();
      if (estadoAct != null && !estadoAct.isEmpty()) {
        args.putString("estadoEvento", estadoAct);
      }
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_gestionInscriptos, args);
    });

    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      String estadoAct = viewModel.getEstadoActualEvento().getValue();
      if (estadoAct != null) args.putString("estadoEvento", estadoAct);
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

    binding.btnResultados.setOnClickListener(v -> viewModel.solicitarMenuResultados());
  }

  private void abrirDialogoCarga() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_carga_resultados, null);

    Button btnElegir = view.findViewById(R.id.btnSeleccionarArchivo);
    btnSubirEnDialog = view.findViewById(R.id.btnSubirArchivo);
    tvNombreEnDialog = view.findViewById(R.id.tvNombreArchivo);

    // NUEVO: Vinculamos el Spinner
    android.widget.Spinner spCategoriaResultados = view.findViewById(R.id.spCategoriaResultados);

    // Recuperamos SOLO las categorías que no tienen resultados
    List<CategoriaResponse> pendientes = viewModel.getCategoriasPendientesCarga().getValue();

    // Validacion de seguridad (no deberia pasar por la regla del menú, pero por las dudas)
    if (pendientes == null || pendientes.isEmpty()) {
      //Toast.makeText(getContext(), "Todas las categorías tienen resultados", Toast.LENGTH_SHORT).show();
      viewModel.lanzarMensaje("Todas las categorias tienen resultados",0);
      return;
    }

    // Configuramos el Spinner visualmente extrayendo solo los nombres
    List<String> nombresCat = new ArrayList<>();
    for(CategoriaResponse c : pendientes) {
      nombresCat.add(c.getNombre());
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombresCat);
    spCategoriaResultados.setAdapter(adapter);

    btnSubirEnDialog.setEnabled(false);
    tvNombreEnDialog.setText("Selecciona un archivo CSV");

    btnElegir.setOnClickListener(v -> selectorArchivo.launch("*/*"));

    btnSubirEnDialog.setOnClickListener(v -> {
      // Leemos qué posición seleccionó el usuario en el Spinner
      int pos = spCategoriaResultados.getSelectedItemPosition();
      // Buscamos el ID real de esa categoría en nuestra lista 'pendientes'
      int idCatSeleccionada = pendientes.get(pos).getIdCategoria();

      btnSubirEnDialog.setEnabled(false);
      btnSubirEnDialog.setText("Enviando...");

      // Enviamos AMBOS datos al ViewModel
      viewModel.subirArchivoGuardado(idEvento, idCatSeleccionada);
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

    runnersAdapterDialog = new RunnerSimpleAdapter(runner -> confirmarBajaRunner(runner, categoria.getIdCategoria()));

    // VISTA TONTA: Lee directamente el valor de verdad calculado por el VM
    Boolean permitirEliminar = viewModel.getHabilitarEliminacionRunners().getValue();
    runnersAdapterDialog.setHabilitarEliminacion(permitirEliminar != null ? permitirEliminar : false);

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

    // VISTA TONTA: Vincula su UI basándose en el string limpio expuesto
    String est = viewModel.getEstadoActualEvento().getValue();
    if(est != null) {
      if ("publicado".equalsIgnoreCase(est)) rg.check(R.id.rbPublicado);
      else if ("suspendido".equalsIgnoreCase(est)) rg.check(R.id.rbSuspendido);
      else if ("finalizado".equalsIgnoreCase(est)) rg.check(R.id.rbFinalizado);
      else if ("cancelado".equalsIgnoreCase(est)) rg.check(R.id.rbCancelado);
    }

    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cerrar", null);
    dialogEstado = builder.create();
    dialogEstado.show();

    dialogEstado.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      int selected = rg.getCheckedRadioButtonId();
      String estadoNuevo = "";

      // La vista lee sus propios controles UI y extrae los datos planos
      if (selected == R.id.rbPublicado) estadoNuevo = "publicado";
      else if (selected == R.id.rbSuspendido) estadoNuevo = "suspendido";
      else if (selected == R.id.rbFinalizado) estadoNuevo = "finalizado";
      else if (selected == R.id.rbCancelado) estadoNuevo = "cancelado";

      String motivo = et.getText().toString();
      try { ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), 0); } catch(Exception e){}

      final String finalNuevo = estadoNuevo;
      new Handler(Looper.getMainLooper()).postDelayed(() -> viewModel.procesarCambioEstadoEvento(idEvento, finalNuevo, motivo), 100);
    });

    viewModel.getDialogError().observe(getViewLifecycleOwner(), error -> {
      if (error != null && dialogEstado.isShowing()) { et.setError(error); et.requestFocus(); }
    });
  }

  private void abrirDialogoRunnersDesdeVM() {
    CategoriaResponse categoria = viewModel.getCategoriaSeleccionada();
    if (categoria != null) {
      mostrarDialogoRunners(categoria);
    }
  }

  private void abrirDialogoEstadoCategoriaDesdeVM() {
    String[] opcionesEstados = viewModel.getEstadosCategoriasValidos().getValue();
    if (opcionesEstados == null) return;

    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Cambiar estado de la categoría");

    View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado_categoria, null);
    android.widget.Spinner spEstado = dialogView.findViewById(R.id.spEstadoCategoria);
    etMotivoCategoriaDialog = dialogView.findViewById(R.id.etMotivoCategoria);

    builder.setView(dialogView);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
      requireContext(),
      android.R.layout.simple_spinner_dropdown_item,
      opcionesEstados
    );
    spEstado.setAdapter(adapter);

    Integer posicionPrevia = viewModel.getPosicionPreseleccionadaCategoria().getValue();
    spEstado.setSelection(posicionPrevia != null ? posicionPrevia : 0);

    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

    dialogEstadoCategoria = builder.create();

    dialogEstadoCategoria.setOnDismissListener(d -> {
      etMotivoCategoriaDialog = null;
      dialogEstadoCategoria = null;
    });

    dialogEstadoCategoria.show();

    dialogEstadoCategoria.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      int posicionSeleccionada = spEstado.getSelectedItemPosition();
      String motivoInput = etMotivoCategoriaDialog.getText().toString();
      viewModel.guardarNuevoEstadoCategoria(posicionSeleccionada, motivoInput);
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}