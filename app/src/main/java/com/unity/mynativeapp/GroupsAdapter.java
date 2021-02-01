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

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder>{

    private GroupsAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {

        void onButtonClick(View itemView, int position);
    }

    public void setOnItemClickListener(GroupsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;
        public Button editButton;

        public ViewHolder(final View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.groupsNodeText);
            editButton = itemView.findViewById(R.id.groupsNodeButton);

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

    private List<Group> groups;

    public GroupsAdapter(List<Group> group) {
        groups = group;
    }

    @NonNull
    @Override
    public GroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View groupView = inflater.inflate(R.layout.groups_node, parent, false);

        GroupsAdapter.ViewHolder viewHolder = new GroupsAdapter.ViewHolder(groupView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.ViewHolder holder, int position) {
        Group group = groups.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(group.getName());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
}
