package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.databinding.DialogDetalleRunnerBinding;
import com.example.runnconnect.databinding.FragmentListaResultadosBinding;

import java.util.ArrayList;

public class ResultadosEventoFragment extends Fragment {

  private FragmentListaResultadosBinding binding;
  private ResultadosEventoViewModel viewModel;
  private ResultadosAdapter adapter;
  private ArrayAdapter<String> spinnerAdapter;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentListaResultadosBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(ResultadosEventoViewModel.class);

    setupRecyclerView();
    setupSpinner();
    setupObservers();

    int idEvento = requireArguments().getInt("idEvento");
    viewModel.cargarResultados(idEvento);

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    adapter = new ResultadosAdapter(position -> viewModel.solicitarFichaTecnica(position));
    binding.rvResultados.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvResultados.setAdapter(adapter);
  }

  private void setupSpinner() {
    spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.spCategoriasResultados.setAdapter(spinnerAdapter);

    binding.spCategoriasResultados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewModel.seleccionarCategoriaPorIndice(position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void setupObservers() {
    viewModel.getUiCategoriasNombres().observe(getViewLifecycleOwner(), nombres -> {
      spinnerAdapter.clear();
      spinnerAdapter.addAll(nombres);
      spinnerAdapter.notifyDataSetChanged();
    });
    viewModel.getUiVisibilidadSpinner().observe(getViewLifecycleOwner(), binding.spCategoriasResultados::setVisibility);

    viewModel.getListaResultados().observe(getViewLifecycleOwner(), adapter::setLista);
    viewModel.getUiVisibilidadSinResultados().observe(getViewLifecycleOwner(), binding.tvSinResultados::setVisibility);
    viewModel.getUiVisibilidadRecycler().observe(getViewLifecycleOwner(), binding.rvResultados::setVisibility);
    viewModel.getUiVisibilidadLoading().observe(getViewLifecycleOwner(), binding.progressBar::setVisibility);

    viewModel.getErrorTexto().observe(getViewLifecycleOwner(), binding.tvErrorResultados::setText);
    viewModel.getUiVisibilidadError().observe(getViewLifecycleOwner(), binding.tvErrorResultados::setVisibility);

    viewModel.getUiFichaTecnica().observe(getViewLifecycleOwner(), ficha -> {
      if (ficha != null) {
        mostrarFichaTecnica(ficha);
      }
    });
  }

  private void mostrarFichaTecnica(ResultadosEventoViewModel.FichaTecnicaUI ficha) {
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    DialogDetalleRunnerBinding dialogBinding = DialogDetalleRunnerBinding.inflate(getLayoutInflater());
    dialog.setContentView(dialogBinding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    dialogBinding.tvNombreCompleto.setText(ficha.nombreCompleto);
    dialogBinding.tvDniSexoEdad.setText(ficha.dniSexoEdad);
    dialogBinding.tvLocalidad.setText(ficha.localidad);
    dialogBinding.tvCategoriaTalle.setText(ficha.categoriaTalle);
    dialogBinding.tvEmail.setText(ficha.email);
    dialogBinding.tvTelefono.setText(ficha.telefono);
    dialogBinding.tvContactoEmergencia.setText(ficha.contactoEmergencia);
    dialogBinding.tvTelEmergencia.setText(ficha.telEmergencia);
    dialogBinding.btnDarDeBaja.setVisibility(View.GONE);

    dialogBinding.btnCerrarDetalle.setOnClickListener(v -> dialog.dismiss());
    dialog.setOnDismissListener(d -> viewModel.limpiarFichaTecnica());
    dialog.show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}