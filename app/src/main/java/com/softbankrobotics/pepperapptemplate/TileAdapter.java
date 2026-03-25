package com.softbankrobotics.pepperapptemplate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {

    private final List<TileItem> items;
    private final OnTileClickListener listener;

    public interface OnTileClickListener {
        void onTileClick(int index);
    }

    public TileAdapter(List<TileItem> items, OnTileClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tile, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        TileItem item = items.get(position);
        holder.button.setText((position + 1) + ". " + item.getTitle());
        holder.button.setOnClickListener(v -> listener.onTileClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TileViewHolder extends RecyclerView.ViewHolder {
        Button button;

        TileViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.tile_button);
        }
    }
}
