package com.example.runnconnect.ui.organizador.misEventos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.runnconnect.R;
import com.example.runnconnect.data.response.CategoriaResponse; //
import java.util.ArrayList;
import java.util.List;

public class CategoriasInfoAdapter extends RecyclerView.Adapter<CategoriasInfoAdapter.ViewHolder> {
  private List<CategoriaResponse> lista = new ArrayList<>();

  public void setLista(List<CategoriaResponse> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria_info, parent, false);
    return new ViewHolder(v);
  }

  public interface OnCategoriaClickListener {
    void onCategoriaClick(CategoriaResponse categoria);
  }

  private OnCategoriaClickListener listener;

  public void setOnCategoriaClickListener(OnCategoriaClickListener listener) {
    this.listener = listener;
  }


  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CategoriaResponse item = lista.get(position);

    holder.tvNombre.setText(item.getNombre());
    holder.tvPrecio.setText("$ " + item.getPrecio()); //

    String genero = "Mixto";
    if("F".equalsIgnoreCase(item.getGenero())) genero = "Fem";
    if("M".equalsIgnoreCase(item.getGenero())) genero = "Masc";

    String info = item.getEdadMinima() + "-" + item.getEdadMaxima() + " años | " + genero;
    holder.tvInfo.setText(info);

    holder.tvInscriptos.setText("Inscriptos: "+item.getInscriptosActuales());

    holder.itemView.setOnClickListener(v->{
      if(listener != null) listener.onCategoriaClick(item);
    });
  }

  @Override public int getItemCount() { return lista.size(); }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvPrecio, tvInfo, tvInscriptos;
    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvCatNombre);
      tvPrecio = itemView.findViewById(R.id.tvCatPrecio);
      tvInfo = itemView.findViewById(R.id.tvCatInfo);
      tvInscriptos= itemView.findViewById(R.id.tvCatInscriptos);
    }
  }
}