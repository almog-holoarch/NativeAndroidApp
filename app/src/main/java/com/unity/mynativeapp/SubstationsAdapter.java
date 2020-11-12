package com.unity.mynativeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SubstationsAdapter extends RecyclerView.Adapter<SubstationsAdapter.ViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {

        void onItemClick(View itemView, int position);

        void onButtonClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nameTextView;
        public Button editButton;

        public ViewHolder(final View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nodeText);
            editButton = itemView.findViewById(R.id.nodeButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });

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

    private List<Substation> substations;

    public SubstationsAdapter(List<Substation> sub) {
        substations = sub;
    }

    @Override
    public SubstationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View substationView = inflater.inflate(R.layout.node, parent, false);

        ViewHolder viewHolder = new ViewHolder(substationView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SubstationsAdapter.ViewHolder holder, int position) {
        Substation substation = substations.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(substation.getName());
    }

    @Override
    public int getItemCount() {
        return substations.size();
    }

}

