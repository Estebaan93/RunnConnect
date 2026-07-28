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

import com.example.runnconnect.ui.organizador.inscriptos.GestionInscriptosViewModel.InscriptoItemUIState;

import java.util.ArrayList;
import java.util.List;

public class InscriptosAdapter extends RecyclerView.Adapter<InscriptosAdapter.ViewHolder> {

  // Interface para manejar el click desde el Fragment
  public interface OnItemClickListener {
    void onItemClick(InscriptoItemUIState item);
  }

  private List<InscriptoItemUIState> lista = new ArrayList<>();
  private final OnItemClickListener listener;

  public InscriptosAdapter(OnItemClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<InscriptoItemUIState> nuevaLista) {
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
    InscriptoItemUIState uiState = lista.get(position);
    InscriptoEventoResponse.RunnerInscriptoInfo r = uiState.data.getRunner();

    // Datos del Runner (Garantizados por el negocio)
    holder.tvNombre.setText(r.getNombreCompleto());
    holder.tvDni.setText(r.getDniFormateado());

    // Datos de Inscripción
    holder.tvCategoria.setText(uiState.data.getNombreCategoria());
    holder.tvTalle.setText("Talle: " + uiState.data.getTalleRemera());

    // UI pre-calculada
    holder.tvEstado.setText(uiState.estadoTexto);
    holder.tvEstado.setTextColor(uiState.estadoColor);
    holder.imgAction.setVisibility(uiState.actionVisibility);

    // Click Listener
    holder.itemView.setOnClickListener(v -> listener.onItemClick(uiState));
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