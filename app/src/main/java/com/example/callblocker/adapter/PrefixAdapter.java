package com.example.callblocker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.callblocker.R;
import com.example.callblocker.model.BlockedPrefix;

import java.util.ArrayList;
import java.util.List;

public class PrefixAdapter extends RecyclerView.Adapter<PrefixAdapter.PrefixViewHolder> {

    public interface Listener {
        void onPrefixEnabledChanged(String prefix, boolean enabled);

        void onPrefixDelete(String prefix);
    }

    private final List<BlockedPrefix> items = new ArrayList<>();
    private final Listener listener;

    public PrefixAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<BlockedPrefix> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrefixViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prefix, parent, false);
        return new PrefixViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrefixViewHolder holder, int position) {
        BlockedPrefix item = items.get(position);
        holder.textPrefix.setText(item.getPrefix());

        holder.switchPrefix.setOnCheckedChangeListener(null);
        holder.switchPrefix.setChecked(item.isEnabled());
        holder.switchPrefix.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPrefixEnabledChanged(items.get(adapterPosition).getPrefix(), isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPrefixDelete(items.get(adapterPosition).getPrefix());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PrefixViewHolder extends RecyclerView.ViewHolder {
        final TextView textPrefix;
        final Switch switchPrefix;
        final ImageButton btnDelete;

        PrefixViewHolder(@NonNull View itemView) {
            super(itemView);
            textPrefix = itemView.findViewById(R.id.textPrefix);
            switchPrefix = itemView.findViewById(R.id.switchPrefix);
            btnDelete = itemView.findViewById(R.id.btnDeletePrefix);
        }
    }
}
