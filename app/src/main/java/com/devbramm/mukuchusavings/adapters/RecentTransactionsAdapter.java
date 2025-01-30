package com.devbramm.mukuchusavings.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devbramm.mukuchusavings.R;
import com.devbramm.mukuchusavings.models.TransactionRecord;

import java.util.List;

public class RecentTransactionsAdapter extends RecyclerView.Adapter<RecentTransactionsAdapter.RecentTransactionViewHolder> {

    private List<TransactionRecord> transactionRecordList;

    public RecentTransactionsAdapter(List<TransactionRecord> transactionRecordList) {
        this.transactionRecordList = transactionRecordList;
    }

    @NonNull
    @Override
    public RecentTransactionsAdapter.RecentTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout., parent, false);
        return new RecentTransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentTransactionsAdapter.RecentTransactionViewHolder holder, int position) {
        holder.tvDate.setText(transactionRecordList.get(position).getTransactionDate());
    }

    @Override
    public int getItemCount() {
        return transactionRecordList.size();
    }

    public class RecentTransactionViewHolder extends RecyclerView.ViewHolder {

        private TextView tvType, tvDate, tvAmount;
        public RecentTransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAmount = itemView.findViewById(R.id.tv_item_amount);
            tvType = itemView.findViewById(R.id.tv_item_trans_type);
            tvDate = itemView.findViewById(R.id.tv_item_date);
        }
    }
}
