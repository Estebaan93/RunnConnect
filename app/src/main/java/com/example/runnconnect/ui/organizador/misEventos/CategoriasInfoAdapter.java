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
  public static class CategoriaUI {
    public final String nombre;
    public final String precio;
    public final String info;
    public final String inscriptos;
    public final int colorFondo;
    public final CategoriaResponse original;

    public CategoriaUI(String nombre, String precio, String info, String inscriptos, int colorFondo, CategoriaResponse original) {
      this.nombre = nombre;
      this.precio = precio;
      this.info = info;
      this.inscriptos = inscriptos;
      this.colorFondo = colorFondo;
      this.original = original;
    }
  }

  private List<CategoriaUI> lista = new ArrayList<>();

  public void setLista(List<CategoriaUI> nuevaLista) {
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
  public interface OnCategoriaLongClickListener {
    void onCategoriaLongClick(CategoriaResponse categoria);
  }

  private OnCategoriaClickListener listener;
  private OnCategoriaLongClickListener longListener;

  //click corto
  public void setOnCategoriaClickListener(OnCategoriaClickListener listener) {
    this.listener = listener;
  }

  //clic largo
  public void setOnCategoriaLongClickListener(OnCategoriaLongClickListener longListener) {
    this.longListener = longListener;
  }


  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CategoriaUI item = lista.get(position);

    holder.tvNombre.setText(item.nombre);
    holder.tvPrecio.setText(item.precio);
    holder.tvInfo.setText(item.info);
    holder.tvInscriptos.setText(item.inscriptos);
    
    holder.itemView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(item.colorFondo));

    holder.itemView.setOnClickListener(v -> listener.onCategoriaClick(item.original));

    holder.itemView.setOnLongClickListener(v -> {
      longListener.onCategoriaLongClick(item.original);
      return true;
    });
  }


  @Override
  public int getItemCount() { return lista.size(); }

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