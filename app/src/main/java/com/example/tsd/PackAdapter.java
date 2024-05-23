package com.example.tsd;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PackAdapter extends RecyclerView.Adapter<PackAdapter.PackViewHolder> {

    public static class Pack {
        String time;
        String marka;
        double kvo;
        String pallet;
        String rab;
        String master;
        int id_src;

        Pack(String time, String marka, double kvo, String pallet, String rab, String master, int id_src)
        {
            this.time = time;
            this.marka = marka;
            this.kvo = kvo;
            this.pallet = pallet;
            this.rab = rab;
            this.master = master;
            this.id_src = id_src;
        }
    }

    private List<Pack> list;

    interface OnStateClickListener{
        void onStateClick(Pack a, int position);
    }
    private final OnStateClickListener onClickListener;

    PackAdapter(OnStateClickListener onClickListener) {
        this.list = new ArrayList<>();
        this.onClickListener=onClickListener;
    }

    public void refresh(List<Pack> cex){
        this.list.clear();
        this.list.addAll(cex);
        notifyDataSetChanged();
    }

    public Pack getItem(int pos){
        return list.get(pos);
    }

    public List<Pack> getItemList(){
        return list;
    }

    @NonNull
    @Override
    public PackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pack_data_view, parent, false);
        PackViewHolder avh = new PackViewHolder(v);
        return avh;
    }

    @Override
    public void onBindViewHolder(@NonNull PackViewHolder holder, int position) {
        Pack a = list.get(holder.getAdapterPosition());
        holder.time.setText(a.time);
        holder.marka.setText(a.marka);
        holder.pallet.setText(a.pallet);
        holder.rab.setText(a.rab);
        holder.master.setText(a.master);
        DecimalFormat ourForm = new DecimalFormat("###,##0.00");
        holder.kvo.setText(ourForm.format(a.kvo)+" кг");

        if (a.id_src!=2){
            holder.cv.setBackgroundColor(Color.rgb(170,255,170));
        } else {
            holder.cv.setBackgroundColor(Color.rgb(0,200,255));
        }

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

    public static class PackViewHolder extends RecyclerView.ViewHolder  {
        CardView cv;
        TextView time;
        TextView marka;
        TextView kvo;
        TextView pallet;
        TextView rab;
        TextView master;

        PackViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.pack_cv);
            time = (TextView) itemView.findViewById(R.id.pack_data_time);
            marka = (TextView) itemView.findViewById(R.id.pack_data_mark);
            kvo = (TextView) itemView.findViewById(R.id.pack_data_kvo);
            pallet = (TextView) itemView.findViewById(R.id.pack_data_cont);
            rab = (TextView) itemView.findViewById(R.id.pack_data_rab);
            master = (TextView) itemView.findViewById(R.id.pack_data_master);
        }
    }
}
