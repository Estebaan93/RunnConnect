package com.example.runnconnect.ui.organizador.misEventos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;
import com.example.runnconnect.data.response.ResultadosEventoResponse;

import java.util.ArrayList;
import java.util.List;

public class ResultadosAdapter extends RecyclerView.Adapter<ResultadosAdapter.ViewHolder> {

  private List<ResultadosEventoResponse.ResultadoEventoItem> lista = new ArrayList<>();

  public void setLista(List<ResultadosEventoResponse.ResultadoEventoItem> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_resultado_ranking, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ResultadosEventoResponse.ResultadoEventoItem item = lista.get(position);

    //mapeo de ids de item_resultado_ranking
    holder.tvPosicion.setText(String.valueOf(item.getPosicionGeneral() != null ? item.getPosicionGeneral() : "-"));
    holder.tvNombre.setText(item.getNombreRunner());

    String cat = item.getNombreCategoria() != null ? item.getNombreCategoria() : "";
    if (item.getGenero() != null) cat += " (" + item.getGenero() + ")";
    holder.tvCategoria.setText(cat);

    holder.tvTiempo.setText(item.getTiempoOficial());
  }

  @Override
  public int getItemCount() {
    return lista != null ? lista.size() : 0;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvPosicion, tvNombre, tvCategoria, tvTiempo;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      // ids basados en item_resultado_ranking.xml
      tvPosicion = itemView.findViewById(R.id.tvPosicion);
      tvNombre = itemView.findViewById(R.id.tvNombreRunner);
      tvCategoria = itemView.findViewById(R.id.tvCategoriaRunner);
      tvTiempo = itemView.findViewById(R.id.tvTiempoOficial);
    }
  }
}