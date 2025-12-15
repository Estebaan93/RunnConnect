//ui/runner/inicio/NoticiaAdapter
package com.example.runnconnect.ui.inicio;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.runnconnect.R;
import com.example.runnconnect.data.model.Noticia;

import java.util.ArrayList;
import java.util.List;

public class NoticiaAdapter extends RecyclerView.Adapter<NoticiaAdapter.NoticiaViewHolder> {
  private List<Noticia> noticias = new ArrayList<>();
  private Context context;
  private final OnNoticiaClickListener listener;
  public interface OnNoticiaClickListener {
    void onNoticiaClick(String url);
  }

  public NoticiaAdapter(OnNoticiaClickListener listener) {

    this.listener = listener;
  }

  public void setNoticias(List<Noticia> noticias) {
    this.noticias = noticias;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public NoticiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    context = parent.getContext();
    View view = LayoutInflater.from(context).inflate(R.layout.item_noticia, parent, false);
    return new NoticiaViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull NoticiaViewHolder holder, int position) {
    Noticia noticia = noticias.get(position);
    holder.titulo.setText(noticia.getTitulo());

    if (noticia.getImagenUrl() != null) {
      Glide.with(context)
              .load(noticia.getImagenUrl())
              .centerCrop()
              .placeholder(android.R.drawable.ic_menu_gallery)
              .error(android.R.drawable.ic_delete)
              .into(holder.imagen);
    } else {
      // Imagen por defecto si no hay en el RSS
      holder.imagen.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    holder.itemView.setOnClickListener(v -> {
      if (listener != null) listener.onNoticiaClick(noticia.getLink());
    });
  }

  @Override
  public int getItemCount() {
    return noticias.size();
  }

  public static class NoticiaViewHolder extends RecyclerView.ViewHolder {
    TextView titulo;
    ImageView imagen;

    public NoticiaViewHolder(@NonNull View itemView) {
      super(itemView);
      titulo = itemView.findViewById(R.id.tvNoticiaTitulo);
      imagen = itemView.findViewById(R.id.ivNoticiaImagen);
      Log.d("srcImagen", "recurso de img card: "+imagen);
    }
  }

}
