package com.example.runnconnect.ui.organizador.misEventos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.runnconnect.R;
import com.example.runnconnect.data.response.EventoResumenResponse;
import java.util.ArrayList;
import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {
  public interface OnEventoClickListener{
    void onEventoClick(int idEvento);
  }
  private List<MisEventosViewModel.EventoUI> lista = new ArrayList<>();
  private OnEventoClickListener listener;

  //carga inicial
  public void setEventos(List<MisEventosViewModel.EventoUI> nuevosEventos) {
    this.lista = new ArrayList<>(nuevosEventos); //copia
    notifyDataSetChanged();
  }

  public void setOnEventoClickListener(OnEventoClickListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento_organizador, parent, false);
    return new EventoViewHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
    MisEventosViewModel.EventoUI evento = lista.get(position);

    holder.tvNombre.setText(evento.nombre);
    holder.tvFecha.setText(evento.fecha);
    holder.tvLugar.setText(evento.lugar);
    holder.tvInscriptos.setText(evento.inscriptos);
    holder.tvCupo.setText(evento.cupo);
    
    holder.tvEstado.setText(evento.estadoTexto);
    holder.tvEstado.setTextColor(evento.estadoColorTexto);
    holder.tvEstado.setBackgroundColor(evento.estadoColorFondo);

    //conf de click en la tarjeta
    holder.itemView.setOnClickListener(v->{
      if(listener !=null){
        listener.onEventoClick(evento.idEvento);
      }
    });
  }

  @Override
  public int getItemCount() { return lista.size(); }

  static class EventoViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvFecha, tvLugar, tvEstado, tvCupo, tvInscriptos;
    public EventoViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvNombreEvento);
      tvFecha = itemView.findViewById(R.id.tvFecha);
      tvLugar = itemView.findViewById(R.id.tvLugar);
      tvEstado = itemView.findViewById(R.id.tvEstado);
      tvCupo = itemView.findViewById(R.id.tvCupos);
      tvInscriptos=itemView.findViewById(R.id.tvInscriptos);
    }
  }


}