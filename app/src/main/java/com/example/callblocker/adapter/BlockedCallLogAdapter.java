package com.example.callblocker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.callblocker.R;
import com.example.callblocker.db.BlockedCallLogEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BlockedCallLogAdapter extends RecyclerView.Adapter<BlockedCallLogAdapter.LogViewHolder> {

    private final List<BlockedCallLogEntity> items = new ArrayList<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE);

    public void submitList(List<BlockedCallLogEntity> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blocked_call, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        BlockedCallLogEntity item = items.get(position);
        Context context = holder.itemView.getContext();

        String displayNumber = item.rawNumber == null || item.rawNumber.trim().isEmpty()
                ? "numéro masqué"
                : item.rawNumber;

        String date = formatter.format(new Date(item.timestamp));
        holder.textBlockedCall.setText(context.getString(
                R.string.blocked_call_item,
                displayNumber,
                item.matchedPrefix,
                date
        ));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        final TextView textBlockedCall;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textBlockedCall = itemView.findViewById(R.id.textBlockedCall);
        }
    }
}
