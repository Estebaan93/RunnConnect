//ui/runner/inicio/InicioFragment
package com.example.runnconnect.ui.inicio;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.runnconnect.databinding.FragmentInicioBinding;

public class InicioFragment extends Fragment {
  private FragmentInicioBinding binding;
  private InicioViewModel viewModel;
  private NoticiaAdapter adapter;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    //inflar el binding
    binding= FragmentInicioBinding.inflate(inflater, container, false);
    View root= binding.getRoot();

    // Configuracion RecyclerView
    binding.rvNoticias.setLayoutManager(new LinearLayoutManager(getContext()));
    
    // ViewModel
    viewModel = new ViewModelProvider(this).get(InicioViewModel.class);

    adapter = new NoticiaAdapter(this::abrirNoticiaEnNavegador);
    binding.rvNoticias.setAdapter(adapter);

    // Observadores
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressBarInicio.setVisibility(loading ? View.VISIBLE : View.GONE)
    );
    //nuevo 22-06
    viewModel.getListaVacia().observe(getViewLifecycleOwner(), empty -> {
      binding.tvEstado.setVisibility(empty ? View.VISIBLE : View.GONE);
      binding.rvNoticias.setVisibility(empty ? View.GONE : View.VISIBLE);
    });

    //nuevo 22-06
    viewModel.getListaNoticias().observe(getViewLifecycleOwner(), adapter::setNoticias);

    viewModel.getErrorText().observe(getViewLifecycleOwner(), binding.tvErrorLoad::setText);
    viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), binding.tvErrorLoad::setVisibility);

    // Errores de navegacion externa
    viewModel.getErrorNavegacionText().observe(getViewLifecycleOwner(), binding.tvErrorAbrir::setText);
    viewModel.getErrorNavegacionVisibility().observe(getViewLifecycleOwner(), binding.tvErrorAbrir::setVisibility);

    return root;
  }

  private void abrirNoticiaEnNavegador(String url) {
    try {
      CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
      builder.setToolbarColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
      CustomTabsIntent customTabsIntent = builder.build();
      customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    } catch (Exception e) {
      Log.d("Error abrir card", "abrirNoticiaEnNavegador: " + e.getMessage());
      viewModel.onErrorAlAbrirNavegador();
    }
  }

}