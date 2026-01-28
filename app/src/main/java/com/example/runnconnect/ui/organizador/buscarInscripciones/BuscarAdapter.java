package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// IMPORTANTE: Asegúrate de importar el modelo desde .response
import com.example.runnconnect.data.response.BusquedaInscripcionResponse;
import com.example.runnconnect.databinding.ItemBusquedaGlobalBinding;

import java.util.ArrayList; // Agregado
import java.util.List;

public class BuscarAdapter extends RecyclerView.Adapter<BuscarAdapter.BusquedaViewHolder> {

  private List<BusquedaInscripcionResponse> resultados;
  private final OnItemActionListener listener;

  public interface OnItemActionListener {
    void onItemClick(BusquedaInscripcionResponse item);
  }

  // Constructor corregido para aceptar lista vacía inicial si es null
  public BuscarAdapter(List<BusquedaInscripcionResponse> resultados, OnItemActionListener listener) {
    this.resultados = resultados != null ? resultados : new ArrayList<>();
    this.listener = listener;
  }

  @NonNull
  @Override
  public BusquedaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // Asegúrate que tu XML se llame item_busqueda_global.xml
    ItemBusquedaGlobalBinding binding = ItemBusquedaGlobalBinding.inflate(LayoutInflater.from(parent.getContext()),
        parent, false);
    return new BusquedaViewHolder(binding, listener);
  }

  @Override
  public void onBindViewHolder(@NonNull BusquedaViewHolder holder, int position) {
    holder.bind(resultados.get(position));
  }

  @Override
  public int getItemCount() {
    return resultados.size();
  }

  public void setResultados(List<BusquedaInscripcionResponse> nuevosResultados) {
    this.resultados = nuevosResultados != null ? nuevosResultados : new ArrayList<>();
    notifyDataSetChanged();
  }

  static class BusquedaViewHolder extends RecyclerView.ViewHolder {
    private final ItemBusquedaGlobalBinding binding;
    private final OnItemActionListener listener;

    public BusquedaViewHolder(ItemBusquedaGlobalBinding binding, OnItemActionListener listener) {
      super(binding.getRoot());
      this.binding = binding;
      this.listener = listener;
    }

    public void bind(final BusquedaInscripcionResponse item) {
      // CORRECCIÓN 1: Usar los IDs reales del XML (item_busqueda_global.xml)
      // CORRECCIÓN 2: Usar los Getters del Modelo (BusquedaInscripcionResponse.java)

      // Nombre y DNI (Validando nulos para evitar crash)
      if (item.getRunner() != null) {
        binding.tvRunnerNombre.setText(item.getRunner().getNombreCompleto());
        binding.tvRunnerDni.setText("DNI: " + item.getRunner().getDni());
      } else {
        binding.tvRunnerNombre.setText("Usuario Desconocido");
        binding.tvRunnerDni.setText("DNI: -");
      }

      // Evento y Categoría
      String infoEvento = item.getNombreEvento() + " (" + item.getNombreCategoria() + ")";
      binding.tvEventoCategoria.setText(infoEvento);

      // Estado (Texto y Color)
      String estado = item.getEstadoPago() != null ? item.getEstadoPago().toUpperCase() : "-";
      binding.tvEstadoPago.setText(estado);

      // Colores según estado
      if ("PAGADO".equals(estado)) {
        binding.tvEstadoPago.setTextColor(Color.parseColor("#2E7D32")); // Verde
      } else if ("CANCELADO".equals(estado)) {
        binding.tvEstadoPago.setTextColor(Color.RED);
      } else {
        binding.tvEstadoPago.setTextColor(Color.parseColor("#FF9800")); // Naranja
      }

      // Click listener
      binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));

      // Botón eliminar (si lo tienes en el XML)
      if (binding.btnEliminar != null) {
        binding.btnEliminar.setOnClickListener(v -> listener.onItemClick(item));
      }
    }
  }
}