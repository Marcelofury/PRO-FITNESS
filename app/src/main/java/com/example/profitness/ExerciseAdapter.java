package com.example.profitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    public static class ExerciseItem {
        public final String name;
        public final String muscleGroup;
        public final String difficulty;
        public final int defaultDurationMinutes;

        public ExerciseItem(String name, String muscleGroup, String difficulty, int defaultDurationMinutes) {
            this.name = name;
            this.muscleGroup = muscleGroup;
            this.difficulty = difficulty;
            this.defaultDurationMinutes = defaultDurationMinutes;
        }
    }

    private final List<ExerciseItem> items = new ArrayList<>();

    public void submit(List<ExerciseItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvMeta.setText(item.muscleGroup + " • " + item.difficulty + " • " + item.defaultDurationMinutes + " min");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvMeta;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMeta = itemView.findViewById(R.id.tv_meta);
        }
    }
}
