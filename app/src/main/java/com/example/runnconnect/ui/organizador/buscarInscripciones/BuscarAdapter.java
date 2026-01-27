package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;
import com.example.runnconnect.data.request.BusquedaInscripcionResponse;

import java.util.ArrayList;
import java.util.List;

public class BuscarAdapter extends RecyclerView.Adapter<BuscarAdapter.ViewHolder> {
  private List<BusquedaInscripcionResponse> lista = new ArrayList<>();
  private final OnItemAction listener;

  public interface OnItemAction {
    void onEliminar(BusquedaInscripcionResponse item);
  }

  public BuscarAdapter(OnItemAction listener) {
    this.listener = listener;
  }

  public void setLista(List<BusquedaInscripcionResponse> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // Usaremos el layout que definiremos más abajo
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_busqueda_global, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    BusquedaInscripcionResponse item = lista.get(position);

    // 1. Datos del Corredor
    if (item.getRunner() != null) {
      holder.tvNombre.setText(item.getRunner().getNombreCompleto());
      holder.tvDni.setText("DNI: " + item.getRunner().getDni());
    }

    // 2. Contexto (¿En qué carrera está anotado?)
    holder.tvEvento.setText(item.getNombreEvento() + " (" + item.getNombreCategoria() + ")");

    // 3. Estado y Color
    String estado = item.getEstadoPago() != null ? item.getEstadoPago().toUpperCase() : "DESCONOCIDO";
    holder.tvEstado.setText(estado);

    switch (estado) {
      case "PAGADO":
        holder.tvEstado.setTextColor(Color.parseColor("#2E7D32")); // Verde
        break;
      case "CANCELADO":
        holder.tvEstado.setTextColor(Color.RED);
        break;
      default: // Pendiente, Procesando
        holder.tvEstado.setTextColor(Color.parseColor("#FF9800")); // Naranja
        break;
    }

    // 4. Botón Eliminar: Solo visible si NO está cancelado ya
    if ("CANCELADO".equals(estado)) {
      holder.btnEliminar.setVisibility(View.GONE);
    } else {
      holder.btnEliminar.setVisibility(View.VISIBLE);
      holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
    }
  }

  @Override
  public int getItemCount() { return lista.size(); }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvDni, tvEvento, tvEstado;
    ImageButton btnEliminar;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvRunnerNombre);
      tvDni = itemView.findViewById(R.id.tvRunnerDni);
      tvEvento = itemView.findViewById(R.id.tvEventoCategoria);
      tvEstado = itemView.findViewById(R.id.tvEstadoPago);
      btnEliminar = itemView.findViewById(R.id.btnEliminar);
    }
  }


}
