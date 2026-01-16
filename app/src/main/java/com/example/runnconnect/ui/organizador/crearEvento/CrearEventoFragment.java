package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation; // Usaremos esto para navegar

import com.example.runnconnect.R; // Asegúrate de importar R
import com.example.runnconnect.databinding.FragmentCrearEventoBinding;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class CrearEventoFragment extends Fragment {

  private FragmentCrearEventoBinding binding;
  private CrearEventoViewModel viewModel;

  // Variables de estado UI (Selección actual de Spinners)
  private String modalidadSel = "Calle";
  private String generoSel = "X";

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrearEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(CrearEventoViewModel.class);

    setupPickers();
    setupChips();
    setupSpinners();
    setupListeners();
    setupObservers();

    //detectar si es edicion
    if(getArguments() !=null){
      int idRecibido= getArguments().getInt("idEvento",0);

      //si es id es mayor a 0, le avisamos al viewModel que cargue los datos
      if(idRecibido>0){
        viewModel.verificarModoEdicion(idRecibido);
      }
    }


    return binding.getRoot();
  }

  private void setupChips() {
    // Comportamiento puramente de UI: Tocar chip -> Llenar caja de texto
    binding.chipGroupDistancias.setOnCheckedStateChangeListener((group, checkedIds) -> {
      if (!checkedIds.isEmpty()) {
        Chip chip = group.findViewById(checkedIds.get(0));
        if (chip != null) {
          // Quitamos la "K" visualmente solo para el EditText
          String texto = chip.getText().toString().replace("K", "").trim();
          binding.etDistanciaValor.setText(texto);
        }
      }
    });
  }

  private void setupSpinners() {
    // 1. Modalidades
    String[] modalidades = {"Calle", "Trail", "Cross", "Aventura", "Obstáculos", "Correcaminata", "Kids", "Triatlón"};
    ArrayAdapter<String> adapterMod = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modalidades);
    binding.spModalidad.setAdapter(adapterMod);

    binding.spModalidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { modalidadSel = modalidades[pos]; }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });

    // 2. Género
    String[] generosVisual = {"Mixto / General", "Femenino", "Masculino"};
    final String[] generosValor = {"X", "F", "M"};

    ArrayAdapter<String> adapterGen = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, generosVisual);
    binding.spGeneroCat.setAdapter(adapterGen);

    binding.spGeneroCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { generoSel = generosValor[pos]; }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });
  }

  private void setupListeners() {
    binding.btnContinuarMapa.setOnClickListener(v -> {
      // MVVM PURO: El Fragment no valida nada. Pasa todo crudo al VM.
      viewModel.procesarYContinuar(
        binding.etTitulo.getText().toString(),
        binding.etDescripcion.getText().toString(),
        binding.etUbicacion.getText().toString(),
        binding.etDatosPago.getText().toString(),
        binding.etDistanciaValor.getText().toString(), // Ej: "10" o vacío
        modalidadSel, // Ej: "Trail"
        generoSel,    // Ej: "X"
        binding.etEdadMin.getText().toString(),
        binding.etEdadMax.getText().toString(),
        binding.etCatPrecio.getText().toString(),
        binding.etCupo.getText().toString()
      );
    });
  }

  private void setupObservers() {
    // 1. Loading y Mensajes
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnContinuarMapa.setEnabled(!loading);
    });

    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      binding.tvMensajeGlobal.setText(msg);
      binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
    });

    // 2. Navegación
    viewModel.getIrAlMapa().observe(getViewLifecycleOwner(), idEvento -> {
      if (idEvento != null) {
        if (binding.btnContinuarMapa.getText().toString().contains("Guardar Cambios")) {
          Navigation.findNavController(requireView()).popBackStack();
        } else {
          Bundle args = new Bundle();
          args.putInt("idEvento", idEvento);
          try {
            Navigation.findNavController(requireView()).navigate(R.id.action_crear_a_mapaEditor, args);
          } catch (Exception e) { e.printStackTrace(); }
        }
        viewModel.resetearNav();
      }
    });

    // 3. CARGAR DATOS (LOGICA CORREGIDA PARA MOSTRAR TODO)
    viewModel.getEventoCargado().observe(getViewLifecycleOwner(), evento -> {
      if (evento == null) return;

      // A. Textos de Cabecera
      binding.tvTituloPagina.setText("Editar Evento");
      binding.btnContinuarMapa.setText("Guardar Cambios");

      // B. Campos Editables
      binding.etTitulo.setText(evento.getNombre());
      binding.etDescripcion.setText(evento.getDescripcion());
      binding.etUbicacion.setText(evento.getLugar());

      // C. Bloquear campos de integridad
      binding.etTitulo.setEnabled(false);
      binding.etUbicacion.setEnabled(false);

      // D. Mostrar y Bloquear Campos de Categoría
      binding.tvTituloCat.setVisibility(View.VISIBLE);
      binding.lblDistancia.setVisibility(View.VISIBLE);
      binding.etDistanciaValor.setVisibility(View.VISIBLE);
      binding.etDistanciaValor.setEnabled(false);
      binding.lblModalidad.setVisibility(View.VISIBLE);
      binding.spModalidad.setVisibility(View.VISIBLE);
      binding.spModalidad.setEnabled(false);
      binding.lblGenero.setVisibility(View.VISIBLE);
      binding.spGeneroCat.setVisibility(View.VISIBLE);
      binding.spGeneroCat.setEnabled(false);
      binding.lblEdad.setVisibility(View.VISIBLE);
      binding.etEdadMin.setVisibility(View.VISIBLE);
      binding.etEdadMax.setVisibility(View.VISIBLE);
      binding.etEdadMin.setEnabled(false);
      binding.etEdadMax.setEnabled(false);
      binding.lblCatPrecio.setVisibility(View.VISIBLE);
      binding.etCatPrecio.setVisibility(View.VISIBLE);
      binding.etCatPrecio.setEnabled(false);
      binding.scrollChips.setVisibility(View.GONE);

      // --- LLENAR DATOS DE LA CATEGORÍA (NUEVO) ---
      if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
        var cat = evento.getCategorias().get(0);

        // 1. Precio (Mapeado desde costoInscripcion)
        binding.etCatPrecio.setText(String.valueOf(cat.getPrecio()));

        // 2. Edades
        binding.etEdadMin.setText(String.valueOf(cat.getEdadMinima()));
        binding.etEdadMax.setText(String.valueOf(cat.getEdadMaxima()));

        // 3. Distancia y Modalidad (Parseo de "5K Calle")
        String nombreCompleto = cat.getNombre();
        if (nombreCompleto != null) {
          String[] partes = nombreCompleto.split(" ");
          if (partes.length > 0) {
            // Parte 1: Distancia (quitamos la K visualmente si quieres, o la dejas)
            binding.etDistanciaValor.setText(partes[0].replace("K", ""));
          }
          if (partes.length > 1) {
            // Parte 2: Modalidad (Selecciona en el spinner)
            seleccionarEnSpinner(binding.spModalidad, partes[1]);
          } else {
            // Fallback
            binding.etDistanciaValor.setText(nombreCompleto);
          }
        }

        // 4. Género
        seleccionarEnSpinner(binding.spGeneroCat, cat.getGenero());
      }

      // E. Resto de campos
      if (evento.getDatosPago() != null) binding.etDatosPago.setText(evento.getDatosPago());
      if (evento.getCupoTotal() != null) binding.etCupo.setText(String.valueOf(evento.getCupoTotal()));

      if (evento.getFechaHora() != null && evento.getFechaHora().contains("T")) {
        try {
          String[] partes = evento.getFechaHora().split("T");
          String fechaRaw = partes[0];
          String horaRaw = partes[1].substring(0, 5);
          String[] f = fechaRaw.split("-");
          binding.etFecha.setText(f[2] + "/" + f[1] + "/" + f[0]);
          binding.etHora.setText(horaRaw);
          viewModel.setFechaHoraInterna(fechaRaw, horaRaw);
        } catch (Exception e) { e.printStackTrace(); }
      }

      binding.tvAvisoMapa.setText("Nota: Precio, Distancia y Lugar no se editan para mantener integridad.");
    });
  }
  // --- HELPER PARA SPINNERS ---
  // Busca el texto en el spinner y lo selecciona visualmente
  private void seleccionarEnSpinner(Spinner spinner, String valorBuscado) {
    if (valorBuscado == null) return;
    ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
    for (int i = 0; i < adapter.getCount(); i++) {
      String item = adapter.getItem(i).toString();

      // Comparación flexible:
      // 1. Texto exacto (ej: "Trail" == "Trail")
      // 2. Valores internos (ej: "F" vs "Femenino")
      boolean coincide = item.equalsIgnoreCase(valorBuscado);

      // Caso especial Genero: "F" -> "Femenino"
      if (valorBuscado.equals("F") && item.contains("Femenino")) coincide = true;
      if (valorBuscado.equals("M") && item.contains("Masculino")) coincide = true;
      if (valorBuscado.equals("X") && item.contains("Mixto")) coincide = true;

      if (coincide) {
        spinner.setSelection(i);
        break;
      }
    }
  }

  // Pickers visuales (Fecha/Hora)
  private void setupPickers() {
    binding.etFecha.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();
      DatePickerDialog d = new DatePickerDialog(requireContext(), (view, y, m, d1) -> {
        binding.etFecha.setText(viewModel.procesarFecha(y, m, d1));
      }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
      d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
      d.show();
    });

    binding.etHora.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();
      new TimePickerDialog(requireContext(), (view, h, m) -> {
        binding.etHora.setText(viewModel.procesarHora(h, m));
      }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    });
  }


}