package com.example.runnconnect.ui.organizador.misEventos;

import android.content.Context;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DetalleEventoFragment extends Fragment {
  private FragmentDetalleEventoBinding binding;
  private DetalleEventoViewModel viewModel;

  //adapter
  private CategoriasInfoAdapter categoriasInfoAdapter;
  private RunnerSimpleAdapter runnersAdapterDialog; // EL NUEVO ADAPTER
  private AlertDialog dialogRunners;
  private AlertDialog dialogEstado;
  private AlertDialog dialogCarga;
  private File archivoTemporal;
  private TextView tvNombreEnDialog;
  private Button btnSubirEnDialog;

  private int idEvento = 0;

  // Launcher para buscar archivo
  private final ActivityResultLauncher<String> selectorArchivo = registerForActivityResult(
    new ActivityResultContracts.GetContent(),
    uri -> {
      if (uri != null) {
        // Usamos getContext() seguro en lugar de requireContext() para evitar crashes
        Context context = getContext();
        if (context == null) return;

        Toast.makeText(context, "Procesando archivo...", Toast.LENGTH_SHORT).show();

        // Bloquear botón mientras procesa
        if(btnSubirEnDialog != null) btnSubirEnDialog.setEnabled(false);

        new Thread(() -> {
          // Copia en segundo plano
          File tempFile = copiarUriAArchivo(context, uri);

          new Handler(Looper.getMainLooper()).post(() -> {
            // Volvemos al hilo principal
            archivoTemporal = tempFile;

            if (archivoTemporal != null) {
              String nombre = archivoTemporal.getName().toLowerCase();
              if (tvNombreEnDialog != null) tvNombreEnDialog.setText(archivoTemporal.getName());

              if (nombre.endsWith(".csv") || nombre.endsWith(".txt")) {
                if (btnSubirEnDialog != null) btnSubirEnDialog.setEnabled(true);
              } else {
                Toast.makeText(getContext(), "Formato incorrecto (Solo .csv)", Toast.LENGTH_SHORT).show();
              }
            } else {
              Toast.makeText(getContext(), "Error al leer archivo", Toast.LENGTH_SHORT).show();
            }
          });
        }).start();
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
      binding.tvMensajeGlobal.setText("Error: ID de evento inválido");
      binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
    }

    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupObservers() {
    // 1. Visibilidad y Carga
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    // 2. MENSAJES EN PANTALLA
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      if (binding.tvMensajeGlobal != null) {
        binding.tvMensajeGlobal.setText(msg);
        binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
      }
    });

    // 3. Data Binding
    viewModel.getUiTitulo().observe(getViewLifecycleOwner(), binding.tvTituloDetalle::setText);
    viewModel.getUiFecha().observe(getViewLifecycleOwner(), binding.tvFechaDetalle::setText);
    viewModel.getUiLugar().observe(getViewLifecycleOwner(), binding.tvLugarDetalle::setText);
    viewModel.getUiDescripcion().observe(getViewLifecycleOwner(), binding.tvDescripcion::setText);
    viewModel.getUiInscriptos().observe(getViewLifecycleOwner(), binding.tvInscriptosCount::setText);
    viewModel.getUiCupo().observe(getViewLifecycleOwner(), binding.tvCupoTotal::setText);

    viewModel.getUiEstadoTexto().observe(getViewLifecycleOwner(), binding.tvEstadoDetalle::setText);
    viewModel.getUiEstadoColor().observe(getViewLifecycleOwner(), color -> binding.tvEstadoDetalle.setTextColor(color));

    viewModel.getUiDistanciaTipo().observe(getViewLifecycleOwner(), binding.tvDistanciaTipo::setText);
    viewModel.getUiGeneroPrecio().observe(getViewLifecycleOwner(), binding.tvGeneroPrecio::setText);

    viewModel.getUiVisibilidadDatosCategoria().observe(getViewLifecycleOwner(), visibility -> {
      binding.tvDistanciaTipo.setVisibility(visibility);
      binding.tvGeneroPrecio.setVisibility(visibility);
    });

    // 4. Control del Dialogo
    viewModel.getDialogDismiss().observe(getViewLifecycleOwner(), shouldDismiss -> {
      if (shouldDismiss && dialogEstado != null && dialogEstado.isShowing()) {
        dialogEstado.dismiss();
      }
    });

    // CONFIGURAR RECYCLERVIEW
    categoriasInfoAdapter = new CategoriasInfoAdapter();
    binding.rvCategoriasDetalle.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
    binding.rvCategoriasDetalle.setAdapter(categoriasInfoAdapter);

    // OBSERVAR LA LISTA
    viewModel.getListaCategorias().observe(getViewLifecycleOwner(), lista -> {
      if (lista != null) {
        categoriasInfoAdapter.setLista(lista);
        // Si la lista está vacía, ocultamos el título "Categorías"
        binding.tvTituloCategorias.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
      }
    });

    categoriasInfoAdapter = new CategoriasInfoAdapter();
    // ASIGNAR LISTENER:
    categoriasInfoAdapter.setOnCategoriaClickListener(categoria -> {
      mostrarDialogoRunners(categoria);
    });

    binding.rvCategoriasDetalle.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvCategoriasDetalle.setAdapter(categoriasInfoAdapter);

    // 2. Observer para la lista del Dialog
    viewModel.getListaRunnersDialog().observe(getViewLifecycleOwner(), runners -> {
      if (runnersAdapterDialog != null) {
        runnersAdapterDialog.setLista(runners);
        if (dialogRunners != null && dialogRunners.isShowing()) {
          dialogRunners.setTitle("Inscriptos: " + runners.size());
        }
      }
    });

    // 1. Mostrar boton "Gestion Resultados" solo si finalizo
    viewModel.getVisibilityBtnResultados().observe(getViewLifecycleOwner(), v -> {
      binding.btnResultados.setVisibility(v);
    });

    /*
    // 2. Respuesta de la subida del CSV
    viewModel.getMensajeCargaArchivo().observe(getViewLifecycleOwner(), msg -> {
      if ("EXITO".equals(msg)) {
        Toast.makeText(getContext(), "Resultados cargados correctamente", Toast.LENGTH_SHORT).show();
        if (dialogCarga != null) dialogCarga.dismiss();
      } else if (msg != null) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
      }
      viewModel.resetMensajeCarga();
    });*/


  }

  private void mostrarDialogoRunners(CategoriaResponse categoria) {
    // A. Pedir datos al VM
    viewModel.cargarRunnersDeCategoria(idEvento, categoria.getIdCategoria());

    // B. Construir Dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Cargando inscriptos...");

    // Inflar layout personalizado
    View view = getLayoutInflater().inflate(R.layout.dialog_lista_runners, null);
    RecyclerView rv = view.findViewById(R.id.rvRunnersDialog);
    TextView tvVacio = view.findViewById(R.id.tvSinInscriptos); // Opcional

    // C. Configurar Adapter del Dialog
    runnersAdapterDialog = new RunnerSimpleAdapter(runner -> {
      // Click en botón "Dar de Baja"
      confirmarBajaRunner(runner, categoria.getIdCategoria());
    });

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
      .setMessage("¿Estás seguro de cancelar la inscripción de " + runner.getRunner().getNombre() + "?\nEsta acción es irreversible.")
      .setPositiveButton("Sí, dar de baja", (d, w) -> {
        viewModel.darDeBajaRunner(runner.getIdInscripcion(), "Baja por Organizador", idEvento, idCat);
      })
      .setNegativeButton("Cancelar", null)
      .show();
  }


  private void setupListeners() {
    binding.btnCambiarEstado.setOnClickListener(v -> mostrarDialogoEstado());

    // NAVEGAR A GESTION DE INSCRIPTOS
    binding.btnGestionInscriptos.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_gestionInscriptos, args);
    });
    // ----------------------------------------------

    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_mapaEditor, args);
    });

    binding.btnEditarInfo.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      try {
        Navigation.findNavController(v).navigate(R.id.action_detalle_to_editarEvento, args);
      } catch (Exception e) {
        // Fallback por si la navegación cambia, aunque con el XML unificado no debería fallar
        try { Navigation.findNavController(v).navigate(R.id.nav_crear_evento, args); } catch (Exception ex) {}
      }
    });

    // Click en boton (Resultados)
    binding.btnResultados.setOnClickListener(v -> mostrarOpcionesResultados());

  }

  // Menu: Cargar vs Ver
  private void mostrarOpcionesResultados() {
    String[] opciones = {"Cargar Resultados (CSV)", "Ver Resultados"};
    new AlertDialog.Builder(requireContext())
      .setTitle("Gestión de Resultados")
      .setItems(opciones, (dialog, which) -> {
        if (which == 0) {
          abrirDialogoCarga(); // Usa dialog_carga_resultados.xml
        } else {
          navegarAVerResultados(); // Va a fragment_lista_resultados.xml
        }
      })
      .show();
  }

  // Logica del XML dialog_carga_resultados.xml
  private void abrirDialogoCarga() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_carga_resultados, null);

    Button btnElegir = view.findViewById(R.id.btnSeleccionarArchivo);
    btnSubirEnDialog = view.findViewById(R.id.btnSubirArchivo);
    tvNombreEnDialog = view.findViewById(R.id.tvNombreArchivo);

    archivoTemporal = null;
    if (btnSubirEnDialog != null) btnSubirEnDialog.setEnabled(false);

    btnElegir.setOnClickListener(v -> selectorArchivo.launch("*/*"));

    btnSubirEnDialog.setOnClickListener(v -> {
      if (archivoTemporal != null) {
        // A. Cerramos el diálogo PRIMERO para que se vea el ProgressBar del Fragment
        dialogCarga.dismiss();

        // B. Iniciamos la subida
        viewModel.subirArchivoCsv(idEvento, archivoTemporal);
      }
    });

    builder.setView(view);
    dialogCarga = builder.create();
    dialogCarga.show();
  }

  private void navegarAVerResultados() {
    Bundle args = new Bundle();
    args.putInt("idEvento", idEvento);
    // accion en nav_graph
    Navigation.findNavController(requireView()).navigate(R.id.action_detalle_to_resultados, args);
  }

  // Helper: Uri -> File
  private File copiarUriAArchivo(Context context, android.net.Uri uri) {
    try {
      InputStream is = context.getContentResolver().openInputStream(uri);
      if (is == null) return null;

      // creamos un nombre unico para evitar conflictos de cache
      String nombreArchivo = "upload_" + System.currentTimeMillis() + ".csv";
      File temp = new File(context.getCacheDir(), nombreArchivo);

      FileOutputStream out = new FileOutputStream(temp);

      // buffer de 8KB para mayor velocidad
      byte[] buffer = new byte[8 * 1024];
      int len;
      while ((len = is.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }

      out.flush();
      out.close();
      is.close();

      return temp;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }



  private void mostrarDialogoEstado() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
    builder.setView(view);

    android.widget.RadioGroup rgEstado = view.findViewById(R.id.rgEstado);
    android.widget.EditText etMotivo = view.findViewById(R.id.etMotivo);

    // Preselección
    if (viewModel.getEventoRaw().getValue() != null) {
      String estadoActual = viewModel.getEventoRaw().getValue().getEstado();
      int idParaMarcar = viewModel.calcularPreseleccionRadio(estadoActual);
      if (idParaMarcar != -1) rgEstado.check(idParaMarcar);
    }

    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cerrar", null);

    dialogEstado = builder.create();
    dialogEstado.show();

    dialogEstado.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      int selectedId = rgEstado.getCheckedRadioButtonId();
      String motivo = etMotivo.getText().toString();

      try {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etMotivo.getWindowToken(), 0);
      } catch (Exception e) {}

      new Handler(Looper.getMainLooper()).postDelayed(() -> {
        viewModel.procesarCambioEstado(idEvento, selectedId, motivo);
      }, 100);
    });

    viewModel.getDialogError().observe(getViewLifecycleOwner(), error -> {
      if (error != null && dialogEstado != null && dialogEstado.isShowing()) {
        etMotivo.setError(error);
        etMotivo.requestFocus();
      }
    });
  }
}