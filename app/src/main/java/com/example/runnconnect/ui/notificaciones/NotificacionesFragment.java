package com.example.runnconnect.ui.notificaciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.databinding.FragmentNotificacionesBinding;

public class NotificacionesFragment extends Fragment {

  private FragmentNotificacionesBinding binding;
  private NotificacionesViewModel viewModel;
  private NotificacionesAdapter adapter;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentNotificacionesBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    viewModel = new ViewModelProvider(this).get(NotificacionesViewModel.class);

    setupRecyclerView();
    setupObservers();
  }

  private void setupRecyclerView() {
    adapter = new NotificacionesAdapter(item -> {
      if (item.idEvento > 0) {
        Bundle bundle = new Bundle();
        bundle.putInt("idEvento", item.idEvento);
        bundle.putString("estadoEvento", item.estadoEvento);
        Navigation.findNavController(requireView()).navigate(item.destinoNavId, bundle);
      }
    });
    binding.rvNotificaciones.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvNotificaciones.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getListaNotificaciones().observe(getViewLifecycleOwner(), adapter::setLista);
    viewModel.getProgressBarVisibility().observe(getViewLifecycleOwner(), binding.progressBarNotificaciones::setVisibility);
    viewModel.getVacioVisibility().observe(getViewLifecycleOwner(), binding.tvVacioNotificaciones::setVisibility);
    viewModel.getVacioTexto().observe(getViewLifecycleOwner(), binding.tvVacioNotificaciones::setText);
    viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), binding.tvErrorNotificaciones::setVisibility);
    viewModel.getErrorTexto().observe(getViewLifecycleOwner(), binding.tvErrorNotificaciones::setText);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
