package com.example.team_project.search;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.team_project.BottomNavActivity;
import com.example.team_project.R;
import com.example.team_project.details.DetailsActivity;
import com.example.team_project.fragments.EventsFragment;

import java.util.ArrayList;
import java.util.List;

// This adapter handles horizontal scrolling for the Tag and Suggestion CardViews. Used in Search Activity
public class HorizontalScrollAdapter extends RecyclerView.Adapter<HorizontalScrollAdapter.ViewHolder> {

    private final List<String> list;
    private final boolean isTags;
    public static ArrayList<String> addTagsToSearch;
    private SearchActivity mSearchActivity;

    public HorizontalScrollAdapter(List<String> horizontalList, boolean isTags, SearchActivity mSearchActivity) {
        this.mSearchActivity = mSearchActivity;
        this.list = horizontalList;
        this.isTags = isTags;
        addTagsToSearch = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private View view;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            tvName = (TextView) view.findViewById(R.id.tvName);
        }

        public void setUpListener() {
            if (isTags) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addTagsToSearch.add(tvName.getText().toString());
                        list.remove(tvName.getText().toString());
                        notifyDataSetChanged();
                        mSearchActivity.setNewSearchText(addTagsToSearch);
                    }
                });
            } else {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(BottomNavActivity.bottomNavAct, DetailsActivity.class);
                        intent.putExtra(DetailsActivity.EVENT_ID, EventsFragment.idList.get(getAdapterPosition()));
                        intent.putExtra(DetailsActivity.DISTANCE, EventsFragment.distances.get(getAdapterPosition()));
                        intent.putExtra(DetailsActivity.TYPE, EventsFragment.type);
                        BottomNavActivity.bottomNavAct.startActivity(intent);
                    }
                });
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(isTags ? R.layout.item_tag : R.layout.item_suggestion, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(list.get(position));
        holder.setUpListener();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
