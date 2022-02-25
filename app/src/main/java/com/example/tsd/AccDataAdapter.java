package com.example.tsd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AccDataAdapter extends RecyclerView.Adapter<AccDataAdapter.AccViewHolder> {

    public static class AccData {
        String marka;
        String parti;
        int numcont;
        int id;
        int id_part;
        double kvo;

        AccData(String marka, String parti, int numcont, int id, int id_part, double kvo) {
            this.marka = marka;
            this.parti = parti;
            this.numcont = numcont;
            this.id = id;
            this.id_part = id_part;
            this.kvo = kvo;
        }
    }

    List<AccData> list;

    interface OnStateClickListener{
        void onStateClick(AccData a, int position);
    }
    private final OnStateClickListener onClickListener;

    AccDataAdapter(List<AccData> acc, OnStateClickListener onClickListener) {
        this.list = acc;
        this.onClickListener=onClickListener;
    }

    @NonNull
    @Override
    public AccViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.acc_data_view, parent, false);
        AccViewHolder avh = new AccViewHolder(v);
        return avh;
    }

    @Override
    public void onBindViewHolder(@NonNull AccViewHolder holder, int position) {
        AccData a = list.get(holder.getAdapterPosition());
        holder.marka.setText(a.marka);
        holder.parti.setText(a.parti);
        holder.kvo.setText(String.format("%.0f", a.kvo));
        holder.numcont.setText(String.valueOf(a.kvo));

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onStateClick(a, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class AccViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView marka;
        TextView parti;
        TextView kvo;
        TextView numcont;

        AccViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv_data);
            marka = (TextView) itemView.findViewById(R.id.acc_data_mark);
            parti = (TextView) itemView.findViewById(R.id.acc_data_part);
            kvo = (TextView) itemView.findViewById(R.id.acc_data_kvo);
            numcont = (TextView) itemView.findViewById(R.id.acc_data_numcont);
        }
    }
}
