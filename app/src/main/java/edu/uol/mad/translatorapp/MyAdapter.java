package edu.uol.mad.translatorapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


    ArrayList<Model> mlist;
    Context context;

    public MyAdapter (Context context , ArrayList<Model> mlist)
    {
        this.mlist=mlist;
        this.context=context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item ,parent ,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Model model = mlist.get(position);
        holder.from.setText(model.getFrom());
        holder.to.setText(model.getTo());
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView  from,to;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            from = itemView.findViewById(R.id.From);
            to = itemView.findViewById(R.id.To);
        }
    }
}
