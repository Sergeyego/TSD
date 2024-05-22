package com.example.tsd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CexAdapter extends RecyclerView.Adapter<CexAdapter.CexViewHolder> {

    public static class Cex {
        int id;
        String nam;
        Cex(String nam, int id) {
            this.nam = nam;
            this.id = id;
        }
    }

    private List<Cex> list;

    interface OnStateClickListener{
        void onStateClick(Cex a, int position);
    }
    private final OnStateClickListener onClickListener;

    CexAdapter(OnStateClickListener onClickListener) {
        this.list = new ArrayList<>();
        this.onClickListener=onClickListener;
    }

    public void refresh(List<Cex> cex){
        this.list.clear();
        this.list.addAll(cex);
        notifyDataSetChanged();
    }

    public Cex getItem(int pos){
        return list.get(pos);
    }

    public List<Cex> getItemList(){
        return list;
    }

    @NonNull
    @Override
    public CexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cex_view, parent, false);
        CexViewHolder avh = new CexViewHolder(v);
        return avh;
    }

    @Override
    public void onBindViewHolder(@NonNull CexViewHolder holder, int position) {
        Cex a = list.get(holder.getAdapterPosition());
        holder.nam.setText(a.nam);

        if (onClickListener!=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onClickListener.onStateClick(a, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CexViewHolder extends RecyclerView.ViewHolder  {
        CardView cv;
        TextView nam;

        CexViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cex_cv);
            nam = (TextView) itemView.findViewById(R.id.cex_nam);
        }
    }
}
