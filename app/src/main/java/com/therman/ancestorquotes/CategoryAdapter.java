package com.therman.ancestorquotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<String> categories;
    private ItemClicked activity;

    public interface ItemClicked {
        void onItemClicked(String category);
    }

    public CategoryAdapter(Context context, ArrayList<String> categories) {
        this.categories = categories;
        this.activity = (ItemClicked) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            itemView.setOnClickListener(v -> activity.onItemClicked((String)v.getTag()));
        }
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(categories.get(position));
        holder.tvCategoryName.setText(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
