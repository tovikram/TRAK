package com.android.trak;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Frag3 extends Fragment {
    Calendar cal = Calendar.getInstance();
    public int year = cal.get(Calendar.YEAR);
    public int month = cal.get(Calendar.MONTH);
    public int day = cal.get(Calendar.DAY_OF_MONTH);
    public String currentDateStr = day+""+(month+1)+""+year+"";
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    public Button setPriceButton,setProductionButtton;
    public EditText currentPrice30,currentPrice15;
    public EditText production30,production15,productionDate;
    public EditText stock30,stock15;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public GlobalClass globalClass = new GlobalClass();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag3_layout,container,false);
        setPriceButton = v.findViewById(R.id.setPriceButton);
        currentPrice15 = v.findViewById(R.id.currentPrice15);
        currentPrice30 = v.findViewById(R.id.currentPrice30);
        setProductionButtton = v.findViewById(R.id.setProductionButton);
        production15 = v.findViewById(R.id.production15);
        production30 = v.findViewById(R.id.production30);
        productionDate = v.findViewById(R.id.productionDate);
        stock15 = v.findViewById(R.id.stock15);
        stock30 = v.findViewById(R.id.stock30);

        //fet and display price
        getProductPrice(currentPrice30,currentPrice15);
        getProductionCount(currentDateStr,true);
        productionDate.setText(day+"-"+(month+1)+"-"+year+"");
        getStock();

        //set price auto fetch
        final DocumentReference dr = db.collection("price").document("pashuAahar");
        dr.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                getProductPrice(currentPrice30,currentPrice15);
            }
        });
        //set production auto fetch
        final DocumentReference dr2 = db.collection("production").document(currentDateStr);
        dr2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                getProductionCount(currentDateStr,false);
            }
        });
        //set stock auto fetch
        final DocumentReference dr3 = db.collection("summary").document("stock");
        dr3.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                getStock();
            }
        });

        //Datebox dialog
        productionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] str = productionDate.getText().toString().split("-");
                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        Integer.parseInt(str[2]),Integer.parseInt(str[1])-1,Integer.parseInt(str[0]));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year2, int month2, int day2) {
                String date = day2 + "-" + (month2+1) + "-" + year2;
                productionDate.setText(date);
            }
        };

        setPriceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int price30=0,price15=0;
                Boolean allGood=true;
                if (currentPrice30.getText().length() >0){
                    price30 = Integer.parseInt(currentPrice30.getText().toString());
                }else{
                    allGood=false;
                    globalClass.showAlertMsg("Please Write price for 30 Kg",getContext());
                }

                if (currentPrice15.getText().length() >0){
                    price15 = Integer.parseInt(currentPrice15.getText().toString());
                }else{
                    allGood=false;
                    globalClass.showAlertMsg("Please Write price for 15 Kg",getContext());
                }

                if(allGood){
                    Boolean status = updatePrice(price30,price15);
                    if(status) {
                        globalClass.showAlertMsg("Price Updated Successfully",getContext());
                    }else{
                        globalClass.showAlertMsg("Something is wrong! Price Not Updated",getContext());
                    }
                }
            }
        });

        productionDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String selectedDateStr = productionDate.getText().toString().trim();
                String[] seperatedDate = selectedDateStr.split("-");
                int sd1=Integer.parseInt(seperatedDate[0]),sm1=Integer.parseInt(seperatedDate[1]),sy1=Integer.parseInt(seperatedDate[2]);
                String dateStr = sd1+""+sm1+""+sy1+"";
                getProductionCount(dateStr,false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        setProductionButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting selected date
                String selectedDateStr = productionDate.getText().toString().trim();
                String[] seperatedDate = selectedDateStr.split("-");
                int sd1=Integer.parseInt(seperatedDate[0]),sm1=Integer.parseInt(seperatedDate[1]),sy1=Integer.parseInt(seperatedDate[2]);
                Boolean allGood = true;

                if(sy1>year){
                    allGood=false;
                    globalClass.showAlertMsg("Wrong Date !",getContext());
                }else if(sy1==year & sm1>(month+1)){
                    allGood=false;
                    globalClass.showAlertMsg("Wrong Date !",getContext());
                }else if(sy1==year & sm1==(month+1) & sd1>day){
                    allGood=false;
                    globalClass.showAlertMsg("Wrong Date !",getContext());
                }else{
                    selectedDateStr = sd1+""+sm1+""+sy1+"";
                }

                int qnt30=0,qnt15=0;
                if (production30.getText().toString().length() >0){
                    qnt30 = Integer.parseInt(production30.getText().toString());
                }else{
                    allGood=false;
                    globalClass.showAlertMsg("Please Write quantity for 30 Kg",getContext());
                }

                if (production15.getText().length() >0){
                    qnt15 = Integer.parseInt(production15.getText().toString());
                }else{
                    allGood=false;
                    globalClass.showAlertMsg("Please Write quantity for 15 Kg",getContext());
                }

                if(allGood){
                    Boolean status = getDifferenceAndUpdateStock(qnt30,qnt15,selectedDateStr);
                    if(status) {
                        globalClass.showAlertMsg("Saved !",getContext());
                    }else{
                        globalClass.showAlertMsg("Something is wrong! Not Saved.",getContext());
                    }
                }
            }
        });

        return v;
    }

    public void getProductPrice(final EditText currentPrice30,final EditText currentPrice15){
        final DocumentReference docRef = db.collection("price").document("pashuAahar");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int val1 = Integer.parseInt(fdata.get("30kg")+"");
                        currentPrice30.setText(val1+"");
                        int val2 = Integer.parseInt(fdata.get("15kg")+"");
                        currentPrice15.setText(val2+"");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public boolean updatePrice(int price30,int price15){
        Map<String, Object> data = new HashMap<>();
        data.put("30kg", price30);
        data.put("15kg", price15);
        Boolean returnValue = true;
        try {
            db.collection("price").document("pashuAahar")
                    .set(data, SetOptions.merge());
        } catch (Exception e) {
            returnValue = false;
        }

        return returnValue;
    }

    public void getProductionCount(final String date,final Boolean isStartup){
        final DocumentReference docRef = db.collection("production").document(date);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int val1 = Integer.parseInt(fdata.get("30kg")+"");
                        production30.setText(val1+"");
                        int val2 = Integer.parseInt(fdata.get("15kg")+"");
                        production15.setText(val2+"");
                    } else {
                        production30.setText("0");
                        production15.setText("0");
                        if(isStartup){updateProduction(0,0,date);}
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public boolean updateProduction(final int qnt30,final int qnt15,final String date){
        Map<String, Object> data = new HashMap<>();
        data.put("30kg", qnt30);
        data.put("15kg", qnt15);
        Boolean returnValue = true;

        db.collection("production").document(date)
                .set(data, SetOptions.merge());

        return returnValue;
    }

    public boolean getDifferenceAndUpdateStock(final int qnt30,final int qnt15,final String date){
        final Boolean returnValue = true;

        final DocumentReference docRef = db.collection("production").document(date);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                final int diff30,diff15;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        diff30 = qnt30 - Integer.parseInt(fdata.get("30kg")+"");
                        diff15 = qnt15 - Integer.parseInt(fdata.get("15kg")+"");
                        updateStock(diff30,diff15,qnt30,qnt15,date);
                    } else {
                        diff30=qnt30 - 0;
                        diff15=qnt15 - 0;
                        updateStock(diff30,diff15,qnt30,qnt15,date);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return returnValue;
    }

    public void updateStock(final int diff30,final int diff15,final int qnty30,final int qnty15,final String date){
        final DocumentReference docRefStock = db.collection("summary").document("stock");
        docRefStock.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int newQnty30 = Integer.parseInt(fdata.get("30kg")+"") + diff30;
                        int newQnty15 = Integer.parseInt(fdata.get("15kg")+"") + diff15;

                        Map<String, Object> data = new HashMap<>();
                        data.put("30kg", newQnty30);
                        data.put("15kg", newQnty15);
                        db.collection("summary").document("stock")
                                .set(data, SetOptions.merge());
                        updateProduction(qnty30,qnty15,date);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getStock(){
        final DocumentReference docRef = db.collection("summary").document("stock");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int val1 = Integer.parseInt(fdata.get("30kg")+"");
                        stock30.setText(val1+"");
                        int val2 = Integer.parseInt(fdata.get("15kg")+"");
                        stock15.setText(val2+"");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}
