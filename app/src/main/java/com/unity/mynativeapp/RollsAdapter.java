package com.unity.mynativeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RollsAdapter extends RecyclerView.Adapter<RollsAdapter.ViewHolder>{

    private RollsAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {

        void onButtonClick(View itemView, int position);
    }

    public void setOnItemClickListener(RollsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;
        public Button editButton;

        public ViewHolder(final View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.rollsNodeText);
            editButton = itemView.findViewById(R.id.rollsNodeButton);

            editButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onButtonClick(itemView, position);
                        }
                    }
                }
            });
        }

        public void onClick(View view) {

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
        return rolls.size();
    }
}
