package com.example.callblocker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.callblocker.R;

import java.util.ArrayList;
import java.util.List;

public class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.WhitelistViewHolder> {

    public interface Listener {
        void onWhitelistDelete(String number);
    }

    private final List<String> items = new ArrayList<>();
    private final Listener listener;

    public WhitelistAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<String> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WhitelistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_whitelist, parent, false);
        return new WhitelistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WhitelistViewHolder holder, int position) {
        String number = items.get(position);
        holder.textNumber.setText(number);
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onWhitelistDelete(items.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WhitelistViewHolder extends RecyclerView.ViewHolder {
        final TextView textNumber;
        final ImageButton btnDelete;

        WhitelistViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumber = itemView.findViewById(R.id.textWhitelistNumber);
            btnDelete = itemView.findViewById(R.id.btnDeleteWhitelist);
        }
    }
}
