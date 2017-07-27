package com.android.cjae.smsalert.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.cjae.smsalert.R;
import com.android.cjae.smsalert.model.Bank;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osagieomon on 7/27/17.
 */

public class BankAdapter extends RecyclerView.Adapter<BankAdapter.BankViewHolder> {

    private List<Bank> feeds = new ArrayList<>();

    private OnCheckboxClickedListener onCheckboxClickedListener;

    public interface OnCheckboxClickedListener {
        void onCheckboxClicked(boolean check, Bank bank);
    }

    public BankAdapter(OnCheckboxClickedListener onCheckboxClickedListener) {
        this.onCheckboxClickedListener = onCheckboxClickedListener;
    }

    @Override
    public BankViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bank_item, parent, false);
        return new BankViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BankViewHolder holder, int position) {
        final Bank feedItem = feeds.get(position);

        switch (feedItem.getBankName()) {
            case "GTBank":
                holder.bankLogo.setImageResource(R.drawable.bank_logo);
                break;
            case "StanbicIBTC":
                holder.bankLogo.setImageResource(R.drawable.stanbic);
                break;
            case "STERLING":
                holder.bankLogo.setImageResource(R.drawable.sterling);
                break;
            case "FirstBank":
                holder.bankLogo.setImageResource(R.drawable.fb);
                break;
            case "ECOBANK":
                holder.bankLogo.setImageResource(R.drawable.eco);
                break;
            case "AccessBank":
                holder.bankLogo.setImageResource(R.drawable.access);
                break;
            case "UBA":
                holder.bankLogo.setImageResource(R.drawable.uba);
                break;
            default:
                holder.bankLogo.setImageResource(R.drawable.bank);
                break;
        }

        holder.bankName.setText(feedItem.getBankName());
        holder.bankAcctNumber.setText(feedItem.getBankAccount());

        holder.bankCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onCheckboxClickedListener.onCheckboxClicked(b, feedItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public void setFeeds(List<Bank> feeds) {
        this.feeds = feeds;
        notifyDataSetChanged();
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {

        private ImageView bankLogo;
        private TextView bankName;
        private TextView bankAcctNumber;
        private CheckBox bankCheckbox;

        BankViewHolder(View itemView) {
            super(itemView);

            bankLogo = (ImageView) itemView.findViewById(R.id.account_logo);
            bankName = (TextView) itemView.findViewById(R.id.bank_name);
            bankAcctNumber = (TextView) itemView.findViewById(R.id.bank_acct_number);
            bankCheckbox = (CheckBox) itemView.findViewById(R.id.bank_checkbox);
        }
    }
}
