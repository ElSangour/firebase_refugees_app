package com.example.firebase_refugees_app.Activity.Refugees;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.firebase_refugees_app.R;
import com.example.firebase_refugees_app.Utils.ReadWriteRefugeeDetails;

import java.util.ArrayList;

public class RefugeeAdapter extends ArrayAdapter<ReadWriteRefugeeDetails> {
    private ArrayList<ReadWriteRefugeeDetails> refugeeList;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener; // Interface for handling delete button clicks

    public RefugeeAdapter(Context context, ArrayList<ReadWriteRefugeeDetails> refugeeList) {
        super(context, 0, refugeeList);
        this.context = context;
        this.refugeeList = refugeeList;
    }

    // Interface for handling delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(ReadWriteRefugeeDetails refugee);
    }

    // Method to set the listener
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_refugee, parent, false);
        }

        final ReadWriteRefugeeDetails currentRefugee = refugeeList.get(position);

        TextView nameTextView = listItemView.findViewById(R.id.textView_refugee_name);
        nameTextView.setText(currentRefugee.name);

        TextView dobTextView = listItemView.findViewById(R.id.textView_refugee_dob);
        dobTextView.setText(currentRefugee.country);

        // Add more TextViews for other details of the refugee

        Button deleteButton = listItemView.findViewById(R.id.button_delete_refugee);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(currentRefugee);
                }
            }
        });

        return listItemView;
    }
}
