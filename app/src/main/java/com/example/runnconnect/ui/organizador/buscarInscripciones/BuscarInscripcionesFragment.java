package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView; // Importante: androidx, no android.widget
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.data.response.BusquedaInscripcionResponse;
// Asegurate que el nombre del Binding coincida con tu archivo XML
// Si el XML es fragment_buscar_inscripciones.xml -> FragmentBuscarInscripcionesBinding
import com.example.runnconnect.databinding.FragmentBuscarInscripcionesBinding;

import java.util.ArrayList;

public class BuscarInscripcionesFragment extends Fragment {

    private BuscarInscripcionesViewModel mViewModel;
    private FragmentBuscarInscripcionesBinding binding;
    private BuscarAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBuscarInscripcionesBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(BuscarInscripcionesViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupSearchView(); // Configuración del SearchView

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        // Inicializamos el adapter con una lista vacía y el listener
        adapter = new BuscarAdapter(new ArrayList<>(), item -> {
            mostrarConfirmacionBaja(item);
        });

        binding.recyclerViewBusqueda.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBusqueda.setAdapter(adapter);
    }

    private void setupObservers() {
        mViewModel.getResultados().observe(getViewLifecycleOwner(), resultados -> {
            if (resultados != null) {
                adapter.setResultados(resultados);
            }
        });

        mViewModel.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        
        mViewModel.getMensajeExito().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        // Configuramos el listener del SearchView (ID: searchViewBusqueda)
        binding.searchViewBusqueda.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Se ejecuta al presionar "Buscar" en el teclado
                if (query != null && !query.trim().isEmpty()) {
                    mViewModel.buscar(query);
                }
                binding.searchViewBusqueda.clearFocus(); // Ocultar teclado
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Se ejecuta cada vez que escribes una letra.
                // Opcional: Si borran todo, limpiamos la lista.
                if (newText == null || newText.trim().isEmpty()) {
                    adapter.setResultados(new ArrayList<>());
                }
                return false;
            }
        });
    }

    private void mostrarConfirmacionBaja(BusquedaInscripcionResponse item) {
        // Aquí va tu lógica de Dialog (AlertDialog) para confirmar
        // Reutiliza la lógica que ya tenías:
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Dar de Baja")
            .setMessage("¿Deseas cancelar la inscripción de " + 
                        (item.getRunner() != null ? item.getRunner().getNombreCompleto() : "Runner") + "?")
            .setPositiveButton("Sí", (d, w) -> {
                // Llamamos al ViewModel para dar de baja
                // Pasamos el texto actual del buscador para que recargue la lista tras eliminar
                String textoBusqueda = binding.searchViewBusqueda.getQuery().toString();
                mViewModel.darDeBaja(item.getIdInscripcion(), "Cancelado por Organizador", textoBusqueda);
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}