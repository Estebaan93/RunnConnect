package com.example.runnconnect.ui.notificaciones;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.databinding.ItemNotificacionBinding;
import com.example.runnconnect.ui.notificaciones.NotificacionesViewModel.NotificacionUI;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

  public interface OnNotificacionClickListener {
    void onNotificacionClick(NotificacionUI item);
  }

  private List<NotificacionUI> lista = new ArrayList<>();
  private final OnNotificacionClickListener listener;

  public NotificacionesAdapter(OnNotificacionClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<NotificacionUI> nuevaLista) {
    this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(ItemNotificacionBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false
    ));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bind(lista.get(position));
  }

  @Override
  public int getItemCount() {
    return lista.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    private final ItemNotificacionBinding binding;

    public ViewHolder(ItemNotificacionBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(NotificacionUI item) {
      binding.tvNotifTitulo.setText(item.titulo);
      binding.tvNotifTiempo.setText(item.tiempoTranscurrido);
      binding.tvNotifMensaje.setText(item.mensaje);
      binding.tvNotifEvento.setText(item.eventoTexto);
      binding.tvNotifEvento.setVisibility(item.eventoVisibility);

      binding.getRoot().setOnClickListener(v -> {
        if (listener != null) {
          listener.onNotificacionClick(item);
        }
      });
    }
  }
}
