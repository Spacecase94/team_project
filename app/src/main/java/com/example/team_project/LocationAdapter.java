package com.example.team_project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final List<String> locs;
    private final List<String> names;
    private final List<String> ids;
    private final int category;

    public LocationAdapter(ArrayList<String> locs, ArrayList<String> names, ArrayList<String> ids, int category) {
        this.locs = locs;
        this.names = names;
        this.ids = ids;
        this.category = category;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tvName);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(v.getContext(), SearchActivity.class);
                intent.putExtra("newLocation", locs.get(position));
                intent.putExtra("isCurLoc", false);
                intent.putExtra("category", category);
                v.getContext().startActivity(intent);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_location,viewGroup,false);
        return new LocationAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.tvName.setText(names.get(i));

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

}
