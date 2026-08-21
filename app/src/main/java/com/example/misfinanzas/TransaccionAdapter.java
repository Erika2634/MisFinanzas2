package com.example.misfinanzas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * Adapter personalizado que conecta la lista de Transacciones (obtenida
 * en tiempo real desde Firestore mediante un SnapshotListener) con el
 * RecyclerView de la pantalla principal.
 * Usa el patrón ViewHolder para reciclar vistas de forma eficiente.
 */
public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder> {

    /**
     * Interfaz para notificar a la Activity cuando el usuario toca un ítem
     * (por ejemplo, para abrir la pantalla de edición usando su Document ID).
     */
    public interface OnItemClickListener {
        void onItemClick(Transaccion transaccion);
    }

    private List<Transaccion> listaTransacciones;
    private final OnItemClickListener listener;

    public TransaccionAdapter(List<Transaccion> listaTransacciones, OnItemClickListener listener) {
        this.listaTransacciones = listaTransacciones;
        this.listener = listener;
    }

    /**
     * Reemplaza la lista completa cuando el SnapshotListener de Firestore
     * notifica un cambio (documento creado, editado o eliminado) y refresca
     * el RecyclerView.
     */
    public void actualizarLista(List<Transaccion> nuevaLista) {
        this.listaTransacciones = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaccion, parent, false);
        return new TransaccionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion transaccion = listaTransacciones.get(position);

        holder.tvConcepto.setText(transaccion.getConcepto());
        holder.tvTipo.setText(transaccion.getTipo());

        String montoFormateado = String.format(Locale.getDefault(), "$ %,.2f", transaccion.getMonto());

        if (transaccion.esIngreso()) {
            holder.tvMonto.setText("+ " + montoFormateado);
            holder.tvMonto.setTextColor(holder.itemView.getContext().getColor(R.color.color_ingreso));
            holder.frameIcono.setBackgroundResource(R.drawable.circulo_ingreso);
            holder.ivIcono.setImageResource(R.drawable.ic_ingreso);
        } else {
            holder.tvMonto.setText("- " + montoFormateado);
            holder.tvMonto.setTextColor(holder.itemView.getContext().getColor(R.color.color_gasto));
            holder.frameIcono.setBackgroundResource(R.drawable.circulo_gasto);
            holder.ivIcono.setImageResource(R.drawable.ic_gasto);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(transaccion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTransacciones == null ? 0 : listaTransacciones.size();
    }

    /**
     * ViewHolder: guarda las referencias a las vistas de cada tarjeta
     * para evitar llamar findViewById repetidamente (optimización de recursos).
     */
    static class TransaccionViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto;
        TextView tvTipo;
        TextView tvMonto;
        FrameLayout frameIcono;
        ImageView ivIcono;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            frameIcono = itemView.findViewById(R.id.frameIcono);
            ivIcono = itemView.findViewById(R.id.ivIcono);
        }
    }
}
