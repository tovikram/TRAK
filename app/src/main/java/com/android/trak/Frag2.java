package com.android.trak;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Frag2 extends Fragment {
    public GlobalClass globalClass = new GlobalClass();
    Calendar cal = Calendar.getInstance();
    public int year = cal.get(Calendar.YEAR);
    public int month = cal.get(Calendar.MONTH);
    public int day = cal.get(Calendar.DAY_OF_MONTH);
    public Button dailyEntry,dailySellViewButton,testingButton;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public AutoCompleteTextView testingText;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.frag2_layout,container,false);

        dailySellViewButton = v.findViewById(R.id.dailySellViewButton);
        dailyEntry = v.findViewById(R.id.dailyEntry);

        //Testing code close

        dailyEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getContext(), DailyEntry.class);
                startActivity(myIntent);
            }
        });

        dailySellViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getContext(), DailySellView.class);
                myIntent.putExtra("currentDate", day+"-"+(month+1)+"-"+year); //Optional parameters
                startActivity(myIntent);
            }
        });


        return v;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}
