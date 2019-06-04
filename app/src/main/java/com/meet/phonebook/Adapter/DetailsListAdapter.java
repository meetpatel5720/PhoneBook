package com.meet.phonebook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meet.phonebook.Data.PersonData;
import com.meet.phonebook.R;

import java.util.ArrayList;

public class DetailsListAdapter extends RecyclerView.Adapter<DetailsListAdapter.DetailsViewHolder> {

    public Context context;
    public ArrayList<PersonData> personDataArrayList;

    public DetailsListAdapter(Context context, ArrayList<PersonData> personDataArrayList) {
        this.context = context;
        this.personDataArrayList = personDataArrayList;
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view =layoutInflater.inflate(R.layout.list_item,viewGroup,false);
        return (new DetailsViewHolder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsViewHolder detailsViewHolder, int position) {
        PersonData personData = personDataArrayList.get(position);
        detailsViewHolder.name.setText(personData.getName());
        detailsViewHolder.address.setText(personData.getAddress());
        detailsViewHolder.mobileNo.setText(personData.getMobileNo() + "");
        if (personData.getPhoneNo()!=0) {
            detailsViewHolder.phoneNo.setText(personData.getPhoneNo()+"");
        }
        else {
            detailsViewHolder.phoneNo.setVisibility(View.GONE);
            detailsViewHolder.textView2.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return personDataArrayList.size();
    }

    public class DetailsViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView address;
        TextView mobileNo;
        TextView phoneNo;
        TextView textView1;
        TextView textView2;

        public DetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            mobileNo = itemView.findViewById(R.id.mobile_no);
            phoneNo = itemView.findViewById(R.id.phone_no);
            textView1 = itemView.findViewById(R.id.text1);
            textView2 = itemView.findViewById(R.id.text2);
        }
    }

    public void updateList(ArrayList<PersonData> personData){
        personDataArrayList = new ArrayList<>();
        personDataArrayList.addAll(personData);
        notifyDataSetChanged();
    }
}
