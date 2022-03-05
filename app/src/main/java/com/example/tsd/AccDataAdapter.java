package com.example.tsd;

import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class AccDataAdapter extends RecyclerView.Adapter<AccDataAdapter.AccViewHolder> {

    public static final int MENU_ACC_DATA_EDT = 0;
    public static final int MENU_ACC_DATA_DEL = 1;

    public static class AccData {
        String marka;
        String parti;
        String namcont;
        int numcont;
        int kvom;
        int id;
        int id_part;
        double kvo;
        boolean ok;

        AccData(String marka, String parti, String namcont, int numcont, int id, int id_part, double kvo, int kvom, boolean ok) {
            this.marka = marka;
            this.parti = parti;
            this.namcont = namcont;
            this.numcont = numcont;
            this.id = id;
            this.id_part = id_part;
            this.kvo = kvo;
            this.kvom=kvom;
            this.ok=ok;
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
        holder.nn.setText(String.valueOf(holder.getAdapterPosition()+1)+".");
        holder.marka.setText(a.marka);
        holder.parti.setText("партия №"+a.parti);
        DecimalFormat ourForm = new DecimalFormat("###,##0.00");
        holder.kvo.setText(ourForm.format(a.kvo)+" кг");
        holder.numcont.setText("поддон №"+String.valueOf(a.numcont));

        if (a.ok){
            holder.cv.setBackgroundColor(Color.rgb(170,255,170));
        }

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

    public static class AccViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        CardView cv;
        TextView nn;
        TextView marka;
        TextView parti;
        TextView kvo;
        TextView numcont;

        AccViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv_data);
            nn = (TextView) itemView.findViewById(R.id.acc_data_num);
            marka = (TextView) itemView.findViewById(R.id.acc_data_mark);
            parti = (TextView) itemView.findViewById(R.id.acc_data_part);
            kvo = (TextView) itemView.findViewById(R.id.acc_data_kvo);
            numcont = (TextView) itemView.findViewById(R.id.acc_data_numcont);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Выберите действие");
            contextMenu.add(this.getAdapterPosition(), MENU_ACC_DATA_EDT, 1, "Редактировать");
            contextMenu.add(this.getAdapterPosition(), MENU_ACC_DATA_DEL, 2, "Удалить");
        }
    }
}
