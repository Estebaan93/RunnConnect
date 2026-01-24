package com.example.runnconnect.ui.organizador.crearEvento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.runnconnect.R;
import com.example.runnconnect.data.request.CrearCategoriaRequest;
import java.util.ArrayList;
import java.util.List;

public class CategoriasTemporalAdapter extends RecyclerView.Adapter<CategoriasTemporalAdapter.ViewHolder> {

  private List<CrearCategoriaRequest> lista = new ArrayList<>();
  private final OnItemClick onDelete;

  public interface OnItemClick {
    void onClick(int posicion);
  }

  public CategoriasTemporalAdapter(OnItemClick onDelete) {
    this.onDelete = onDelete;
  }

  public void setLista(List<CrearCategoriaRequest> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria_simple, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CrearCategoriaRequest item = lista.get(position);
    holder.tvNombre.setText(item.getNombre()); // Asegúrate que tu CrearCategoriaRequest tenga getNombre()
    holder.tvPrecio.setText("$" + item.getCostoInscripcion());

    holder.btnEliminar.setOnClickListener(v -> onDelete.onClick(position));
  }

  @Override
  public int getItemCount() {
    return lista.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvPrecio;
    View btnEliminar;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvCatNombre);
      tvPrecio = itemView.findViewById(R.id.tvCatPrecio);
      btnEliminar = itemView.findViewById(R.id.btnEliminar);
    }
  }
}