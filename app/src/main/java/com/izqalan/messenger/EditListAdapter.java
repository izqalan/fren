package com.izqalan.messenger;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class EditListAdapter extends RecyclerView.Adapter<EditListAdapter.EditListViewHolder> {

    private ArrayList<ListItem> mListItem;
    private static final String TAG = "EditListAdapter";

    public static class EditListViewHolder extends RecyclerView.ViewHolder{

        public TextView itemList;
        public ImageButton removeListBtn;


        public EditListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemList = itemView.findViewById(R.id.item);
            removeListBtn = itemView.findViewById(R.id.remove_list_btn);
        }
    }

    public EditListAdapter(ArrayList<ListItem> item){
        mListItem = item;
    }

    @NonNull
    @Override
    public EditListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, viewGroup, false);
        EditListViewHolder viewHolder = new EditListViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final EditListViewHolder holder, int i) {
        ListItem currentItem = mListItem.get(i);

        holder.itemList.setText(currentItem.getItem());
        final int pos = holder.getAdapterPosition();

        holder.removeListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "position: "+pos+" size: "+mListItem.size());
                mListItem.remove(pos);
                // notify to array data has been changed to update the viewholder
                notifyItemRemoved(pos);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mListItem.size();
    }
}

