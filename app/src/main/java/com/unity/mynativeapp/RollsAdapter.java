package com.unity.mynativeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RollsAdapter extends RecyclerView.Adapter<RollsAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;

        public ViewHolder(final View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.rollsNodeText);
        }
    }

    private List<Roll> rolls;

    public RollsAdapter(List<Roll> rol) {
        rolls = rol;
    }

    @NonNull
    @Override
    public RollsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View rollView = inflater.inflate(R.layout.rolls_node, parent, false);

        RollsAdapter.ViewHolder viewHolder = new RollsAdapter.ViewHolder(rollView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RollsAdapter.ViewHolder holder, int position) {
        Roll roll = rolls.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(roll.getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
