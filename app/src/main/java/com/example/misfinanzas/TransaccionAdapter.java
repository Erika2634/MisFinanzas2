package com.example.misfinanzas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.ViewHolder> {

    public interface OnItemDeleteListener {
        void onDeleteClick(Transaccion transaccion);
    }

    private final List<Transaccion> transacciones;
    private final boolean esAdmin;
    private final OnItemDeleteListener deleteListener;

    public TransaccionAdapter(List<Transaccion> transacciones, boolean esAdmin, OnItemDeleteListener deleteListener) {
        this.transacciones = transacciones;
        this.esAdmin = esAdmin;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaccion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaccion t = transacciones.get(position);
        holder.tvTitulo.setText(t.getTitulo());
        holder.tvMonto.setText(String.format(java.util.Locale.getDefault(), "$%.2f", t.getMonto()));
        holder.tvDireccion.setText(t.getDireccion() != null && !t.getDireccion().isEmpty() ? t.getDireccion() : "Sin ubicación");

        // Carga eficiente con Glide previniendo bugs de reciclaje
        if (t.getImageUrl() != null && !t.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(t.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .centerCrop()
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Restricción RBAC: Solo el Administrador puede ver el botón de eliminación
        if (esAdmin) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(t);
                }
            });
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvTitulo, tvMonto, tvDireccion;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivItemFoto);
            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvMonto = itemView.findViewById(R.id.tvItemMonto);
            tvDireccion = itemView.findViewById(R.id.tvItemDireccion);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}