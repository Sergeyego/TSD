package com.example.tsd;

import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccAdapter extends RecyclerView.Adapter<AccAdapter.AccViewHolder> {

    public static final int MENU_ACC_EDT = 0;
    public static final int MENU_ACC_DEL = 1;

    public static class Acc {
        String num;
        String type;
        Date dat;
        int id;
        int id_type;

        Acc(String num, String type, Date dat, int id, int id_type) {
            this.num = num;
            this.type = type;
            this.dat = dat;
            this.id = id;
            this.id_type = id_type;
        }
    }

    private List<Acc> list;

    interface OnStateClickListener{
        void onStateClick(Acc a, int position);
    }
    private final OnStateClickListener onClickListener;

    AccAdapter(OnStateClickListener onClickListener) {
        this.list = new ArrayList<>();
        this.onClickListener=onClickListener;
    }

    public void refresh(List<Acc> acc){
        this.list.clear();
        this.list.addAll(acc);
        notifyDataSetChanged();
    }

    public Acc getItem(int pos){
        return list.get(pos);
    }

    @NonNull
    @Override
    public AccViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.acc_view, parent, false);
        AccViewHolder avh = new AccViewHolder(v);
        return avh;
    }

    @Override
    public void onBindViewHolder(@NonNull AccViewHolder holder, int position) {
        Acc a = list.get(holder.getAdapterPosition());
        holder.num.setText(a.num);
        holder.dat.setText(DateFormat.format("dd.MM.yy", a.dat).toString());
        holder.type.setText(a.type);

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
        TextView num;
        TextView dat;
        TextView type;

        AccViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            num = (TextView) itemView.findViewById(R.id.acc_num);
            dat = (TextView) itemView.findViewById(R.id.acc_dat);
            type = (TextView) itemView.findViewById(R.id.acc_type);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Выберите действие");
            contextMenu.add(this.getAdapterPosition(), MENU_ACC_EDT, 1, "Редактировать");
            contextMenu.add(this.getAdapterPosition(), MENU_ACC_DEL, 2, "Удалить");
        }
    }
}
