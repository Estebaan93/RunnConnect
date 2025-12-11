//ui/runner/inicio/InicioFragment
package com.example.runnconnect.ui.runner.inicio;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentInicioBinding;

public class InicioFragment extends Fragment {
  private InicioViewModel viewModel;
  private RecyclerView rvNoticias;
  private NoticiaAdapter adapter;
  private ProgressBar progressBar;
  private TextView tvEstado;


  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    View root = inflater.inflate(R.layout.fragment_inicio, container, false);

    // UI References
    rvNoticias = root.findViewById(R.id.rvNoticias);
    progressBar = root.findViewById(R.id.progressBarInicio);
    tvEstado = root.findViewById(R.id.tvEstado);

    // Configuración RecyclerView
    rvNoticias.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new NoticiaAdapter(url -> abrirNoticiaEnNavegador(url));
    rvNoticias.setAdapter(adapter);

    // ViewModel
    viewModel = new ViewModelProvider(this).get(InicioViewModel.class);

    // Observadores
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    viewModel.getListaNoticias().observe(getViewLifecycleOwner(), noticias -> {
      if (noticias != null && !noticias.isEmpty()) {
        tvEstado.setVisibility(View.GONE);
        rvNoticias.setVisibility(View.VISIBLE);
        adapter.setNoticias(noticias);
      } else {
        tvEstado.setVisibility(View.VISIBLE);
        rvNoticias.setVisibility(View.GONE);
      }
    });

    viewModel.getMensajeError().observe(getViewLifecycleOwner(), error -> {
      if(error != null) Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    });

    return root;
  }

  private void abrirNoticiaEnNavegador(String url) {
    try {
      CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
      // Color de la barra del navegador (puedes usar R.color.purple_500 o el que definas)
      builder.setToolbarColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
      CustomTabsIntent customTabsIntent = builder.build();
      customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    } catch (Exception e) {
      Toast.makeText(getContext(), "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
      Log.d("Error abrir card", "abrirNoticiaEnNavegador: " +e.getMessage());
    }
  }

}