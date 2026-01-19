package com.example.runnconnect.ui.organizador.inscriptos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;
import com.example.runnconnect.data.response.InscriptoEventoResponse;

import java.util.ArrayList;
import java.util.List;

public class InscriptosAdapter extends RecyclerView.Adapter<InscriptosAdapter.ViewHolder> {

  // Interface para manejar el click desde el Fragment
  public interface OnItemClickListener {
    void onItemClick(InscriptoEventoResponse item);
  }

  private List<InscriptoEventoResponse> lista = new ArrayList<>();
  private final OnItemClickListener listener;

  public InscriptosAdapter(OnItemClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<InscriptoEventoResponse> nuevaLista) {
    this.lista = new ArrayList<>(nuevaLista);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_inscripto, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    InscriptoEventoResponse item = lista.get(position);

    // Datos del Runner (Nombre y DNI)
    if (item.getRunner() != null) {
      holder.tvNombre.setText(item.getRunner().getNombreCompleto());
      holder.tvDni.setText("DNI: " + item.getRunner().getDni());
    } else {
      holder.tvNombre.setText("Usuario Desconocido");
      holder.tvDni.setText("-");
    }

    // Datos de Inscripción
    holder.tvCategoria.setText(item.getNombreCategoria());

    String talle = item.getTalleRemera();
    holder.tvTalle.setText(talle != null ? "Talle: " + talle : "Talle: -");

    // Lógica visual del Estado
    String estado = item.getEstadoPago() != null ? item.getEstadoPago().toLowerCase() : "pendiente";
    holder.tvEstado.setText(estado.toUpperCase());

    switch (estado) {
      case "pagado":
        holder.tvEstado.setTextColor(Color.parseColor("#2E7D32")); // Verde
        holder.imgAction.setVisibility(View.GONE); // Ya no requiere acción
        break;
      case "procesando":
        holder.tvEstado.setTextColor(Color.parseColor("#EF6C00")); // Naranja
        holder.imgAction.setVisibility(View.VISIBLE); // Lupa para revisar
        break;
      case "rechazado":
        holder.tvEstado.setTextColor(Color.parseColor("#C62828")); // Rojo
        holder.imgAction.setVisibility(View.GONE);
        break;
      default: // pendiente
        holder.tvEstado.setTextColor(Color.GRAY);
        holder.imgAction.setVisibility(View.GONE);
        break;
    }

    // Click Listener
    holder.itemView.setOnClickListener(v -> {
      if (listener != null) listener.onItemClick(item);
    });
  }

  @Override
  public int getItemCount() {
    return lista.size();
  }

  // ViewHolder interno
  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvDni, tvCategoria, tvTalle, tvEstado;
    ImageView imgAction;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvNombreRunner);
      tvDni = itemView.findViewById(R.id.tvDni);
      tvCategoria = itemView.findViewById(R.id.tvCategoria);
      tvTalle = itemView.findViewById(R.id.tvTalle);
      tvEstado = itemView.findViewById(R.id.tvEstadoValor);
      imgAction = itemView.findViewById(R.id.imgAction);
    }
  }
}