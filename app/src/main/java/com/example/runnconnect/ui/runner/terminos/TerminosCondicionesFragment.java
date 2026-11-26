package com.example.runnconnect.ui.runner.terminos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.databinding.FragmentTerminosCondicionesBinding;

public class TerminosCondicionesFragment extends Fragment {

  private FragmentTerminosCondicionesBinding binding;
  private TerminosCondicionesViewModel viewModel;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentTerminosCondicionesBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(TerminosCondicionesViewModel.class);

    setupObservers();

    return binding.getRoot();
  }

  private void setupObservers() {
    viewModel.getTitulo().observe(getViewLifecycleOwner(), binding.tvTituloDeslinde::setText);
    viewModel.getContenido().observe(getViewLifecycleOwner(), binding.tvContenidoDeslinde::setText);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
