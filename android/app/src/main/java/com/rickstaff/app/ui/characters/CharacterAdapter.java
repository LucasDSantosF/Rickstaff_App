package com.rickstaff.app.ui.characters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.rickstaff.app.R;
import com.rickstaff.app.data.model.Character;
import java.util.ArrayList;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder> {

    private List<Character> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Character character);
    }

    public CharacterAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Character> newList) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return items.get(o).getId() == newList.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return items.get(o).getId() == newList.get(n).getId();
            }
        });
        items = new ArrayList<>(newList);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Character c = items.get(position);
        holder.bind(c);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(c));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvStatus, tvSpecies, tvGender, tvLocation;

        ViewHolder(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.ivPhoto);
            tvName = v.findViewById(R.id.tvName);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvSpecies = v.findViewById(R.id.tvSpecies);
            tvGender = v.findViewById(R.id.tvGender);
            tvLocation = v.findViewById(R.id.tvLocation);
        }

        void bind(Character c) {
            Context context = itemView.getContext();
            String status = context.getString(R.string.character_status) + c.getStatus();
            String especie = context.getString(R.string.character_especie) + c.getSpecies();
            String genero = context.getString(R.string.character_genero) + c.getGender();

            tvName.setText(c.getName());
            tvStatus.setText(status);
            tvSpecies.setText(especie);
            tvGender.setText(genero);
            tvLocation.setText(c.getLocation() != null ? c.getLocation().getName() : "-");
            Glide.with(itemView.getContext()).load(c.getImage()).into(ivPhoto);
        }
    }
}