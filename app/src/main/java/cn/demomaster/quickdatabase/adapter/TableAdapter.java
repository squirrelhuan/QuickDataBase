package cn.demomaster.quickdatabase.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.quickdatabase.R;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    private List<String[]> data;
    private Context context;

    public TableAdapter(Context context) {
        this.context = context;
    }

    TabAttrs mTabAttrs;
    public void updateList(TabAttrs tabAttrs, List<String[]> data) {
        this.data = data;
        this.mTabAttrs = tabAttrs;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       // View view = LayoutInflater.from(context).inflate(R.layout.item_tab, parent, false);
        TableRow tableRow = new TableRow(context);
        return new ViewHolder(tableRow);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            resetChild();
        }

        public void resetChild() {
            ((ViewGroup) itemView).removeAllViews();
            for (int i = 0; i < mTabAttrs.getTabCount(); i++) {
                TextView textView = new TextView(itemView.getContext());
                textView.setPadding(20,20,20,20);
                //textView.setTextColor(Color.RED);
                ((ViewGroup) itemView).addView(textView);
            }
        }

        public void onBind(final int position) {
            this.itemView.setBackgroundColor(position%2==0?Color.GRAY:Color.WHITE);
            if (mTabAttrs != null && ((ViewGroup) this.itemView).getChildCount() != mTabAttrs.getTabCount()) {
                resetChild();
            }

            for (int i = 0; i < mTabAttrs.getTabCount(); i++) {
                TextView view = (TextView) ((ViewGroup) this.itemView).getChildAt(i);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = mTabAttrs.getTabWidth(i);
                //ViewGroup.LayoutParams layoutParams = new TableRow.LayoutParams(mTabAttrs.getTabWidth(i), view.getLayoutParams());
                view.setLayoutParams(layoutParams);
                view.setText(data.get(position)[i]);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int p = getAdapterPosition();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, p);
                    }
                }
            });
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}