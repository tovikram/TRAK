package com.android.trak;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class DailySellView extends AppCompatActivity {
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public EditText dateBox,dateBoxTo;
    Calendar cal = Calendar.getInstance();
    public int year = cal.get(Calendar.YEAR);
    public int month = cal.get(Calendar.MONTH);
    public int day = cal.get(Calendar.DAY_OF_MONTH);
    public String currentDate="",dateAsCollection="";
    public LinearLayout dailySellViewLayout =null;
    public Button increaseFromDate,searchSellDataButton,increaseToDate;
    public ImageView decreaseFromDate,decreaseToDate;
    public TextView customerFoundCount,vendorFoundCount;
    public RadioGroup sellTypeFilter;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatePickerDialog.OnDateSetListener mDateSetListenerTo;

    public GlobalClass globalClass = new GlobalClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_sell_view);

        Intent intent = getIntent();
        currentDate = intent.getStringExtra("currentDate");

        dateBox = this.findViewById(R.id.dateBox);
        dateBoxTo = this.findViewById(R.id.dateBoxTo);
        dateBox.setText(currentDate);
        dateBoxTo.setText(currentDate);

        decreaseFromDate = DailySellView.this.findViewById(R.id.decreaseFromDate);
        increaseFromDate = DailySellView.this.findViewById(R.id.increaseFromDate);
        decreaseToDate = DailySellView.this.findViewById(R.id.decreaseToDate);
        increaseToDate = DailySellView.this.findViewById(R.id.increaseToDate);
        customerFoundCount = DailySellView.this.findViewById(R.id.customerFoundCount);
        vendorFoundCount = DailySellView.this.findViewById(R.id.vendorFoundCount);
        searchSellDataButton = DailySellView.this.findViewById(R.id.searchSellDataButton);
        sellTypeFilter = DailySellView.this.findViewById(R.id.sellType);

        decreaseFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDate("decrease",dateBox,"");
            }
        });

        increaseFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDate("increase",dateBox,"");
            }
        });

        decreaseToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDate("decrease",dateBoxTo,"");
            }
        });

        increaseToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDate("increase",dateBoxTo,"");
            }
        });


        //Datebox dialog
        dateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] str = dateBox.getText().toString().split("-");
                DatePickerDialog dialog = new DatePickerDialog(
                        DailySellView.this,
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
                dateBox.setText(date);
            }
        };

        //Datebox 2 dialog
        dateBoxTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] str = dateBoxTo.getText().toString().split("-");
                DatePickerDialog dialog = new DatePickerDialog(
                        DailySellView.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerTo,
                        Integer.parseInt(str[2]),Integer.parseInt(str[1])-1,Integer.parseInt(str[0]));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerTo = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year2, int month2, int day2) {
                String date = day2 + "-" + (month2+1) + "-" + year2;
                dateBoxTo.setText(date);
            }
        };


        dailySellViewLayout = (LinearLayout) this.findViewById(R.id.sellDataLinearLayout);

        dateBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customerFoundCount.setText("-");
                vendorFoundCount.setText("-");
                searchSellDataButton.setEnabled(true);
                searchSellDataButton.setBackgroundColor(DailySellView.this.getResources().getColor(R.color.enabledSellSearch));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        dateBoxTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customerFoundCount.setText("-");
                vendorFoundCount.setText("-");
                searchSellDataButton.setEnabled(true);
                searchSellDataButton.setBackgroundColor(DailySellView.this.getResources().getColor(R.color.enabledSellSearch));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sellTypeFilter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                customerFoundCount.setText("-");
                vendorFoundCount.setText("-");
                searchSellDataButton.setEnabled(true);
                searchSellDataButton.setBackgroundColor(DailySellView.this.getResources().getColor(R.color.enabledSellSearch));
            }
        });

        searchSellDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSellDataButton.setEnabled(false);
                searchSellDataButton.setBackgroundColor(DailySellView.this.getResources().getColor(R.color.disabledSellSearch));

                LinearLayout linearLayout = (LinearLayout)DailySellView.this.findViewById(R.id.sellDataLinearLayout);
                linearLayout.removeAllViews();

                //getting selected sell type string
                int selectedId = 0;String sellTypeStr;
                try {
                    selectedId = sellTypeFilter.getCheckedRadioButtonId();
                } catch (Exception e) {
                    selectedId = 0;
                }
                switch (selectedId){
                    case R.id.bothSell:
                        sellTypeStr = "bothSell";
                        break;
                    case R.id.customer:
                        sellTypeStr = "customer";
                        break;
                    case R.id.vendor:
                        sellTypeStr = "vendor";
                        break;
                    default:
                        sellTypeStr="NA";
                        break;
                }

                if(dateBox.getText().toString().equals(dateBoxTo.getText().toString())) {
                    //converting date dting into collection format for firestore
                    String[] str = dateBox.getText().toString().split("-");
                    dateAsCollection = str[0] + "" + str[1] + "" + str[2];
                    getSellData(dateAsCollection,sellTypeStr);
                }else{
                    String[] str1 = dateBox.getText().toString().split("-");
                    String[] str2 = dateBoxTo.getText().toString().split("-");
                    int d1=Integer.parseInt(str1[0]),m1=Integer.parseInt(str1[1]),y1=Integer.parseInt(str1[2]);
                    int d2=Integer.parseInt(str2[0]),m2=Integer.parseInt(str2[1]),y2=Integer.parseInt(str2[2]);
                    if(y1>y2){
                        globalClass.showAlertMsg("Wrong format", DailySellView.this);
                    }else if(y1==y2 & m1>m2){
                        globalClass.showAlertMsg("Wrong format", DailySellView.this);
                    }else if(y1==y2 & m1==m2 & d1>d2){
                        globalClass.showAlertMsg("Wrong format", DailySellView.this);
                    }else{
                        EditText nullBox=null;
                        String endDate = changeDate("increase",nullBox,dateBoxTo.getText().toString());
                        String gettingDate = dateBox.getText().toString();
                        int i=0;String[] getDateArray=null;
                        while(!gettingDate.equals(endDate)) {
                            getDateArray = gettingDate.split("-");
                            dateAsCollection = getDateArray[0] + "" + getDateArray[1] + "" + getDateArray[2];
                            //globalClass.showAlertMsg(i+"", DailySellView.this);
                            getSellData(dateAsCollection,sellTypeStr);
                            gettingDate = changeDate("increase",nullBox,gettingDate);
                            i++;
                        }
                    }
                }
            }
        });
    }

    public void getSellData(final String date,String filterTyperStr){
        if(filterTyperStr.equals("bothSell") || filterTyperStr.equals("customer")) {
            final CollectionReference collRef = db.collection("dailySell").document("customer").collection(date);
            collRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            String ids = "";
                            int numberOfDocuments = documents.getDocuments().size();
                            if (numberOfDocuments > 0) {
                                for (int i = 0; i < numberOfDocuments; i++) {
                                    ids += documents.getDocuments().get(i).getId().toString() + "/";
                                    Button newButton = globalClass.designTextView(DailySellView.this, i, documents, "customer",date,searchSellDataButton);
                                    dailySellViewLayout.addView(newButton);
                                }
                                if (customerFoundCount.getText().toString().equals("-")) {
                                    customerFoundCount.setText(numberOfDocuments + "");
                                } else {
                                    customerFoundCount.setText((Integer.parseInt(customerFoundCount.getText().toString()) + numberOfDocuments) + "");
                                }
                            } else {
                                if (customerFoundCount.getText().toString().equals("-")) {
                                    customerFoundCount.setText("0");
                                }
                            }
                        }
                    }
                }
            });
        }

        if(filterTyperStr.equals("bothSell") || filterTyperStr.equals("vendor")) {
            final CollectionReference collRef2 = db.collection("dailySell").document("vendor").collection(date);
            collRef2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            String ids = "";
                            int numberOfDocuments = documents.getDocuments().size();
                            if (numberOfDocuments > 0) {
                                for (int i = 0; i < numberOfDocuments; i++) {
                                    ids += documents.getDocuments().get(i).getId().toString() + "/";
                                    Button newButton = globalClass.designTextView(DailySellView.this, i, documents, "vendor",date,searchSellDataButton);
                                    dailySellViewLayout.addView(newButton);
                                }
                                if (vendorFoundCount.getText().toString().equals("-")) {
                                    vendorFoundCount.setText(numberOfDocuments + "");
                                } else {
                                    vendorFoundCount.setText((Integer.parseInt(vendorFoundCount.getText().toString()) + numberOfDocuments) + "");
                                }
                            } else {
                                if (vendorFoundCount.getText().toString().equals("-")) {
                                    vendorFoundCount.setText("0");
                                }
                            }
                        }
                    }
                }
            });
        }

    }

    public String changeDate(String type,EditText dateBox,String startDate){
        String[] str=null;
        if(startDate.length()<=0) {
            str = dateBox.getText().toString().split("-");
        }else{
            str = startDate.toString().split("-");
        }
        int d=Integer.parseInt(str[0]),m=Integer.parseInt(str[1]);
        if(d<10){str[0]="0"+d;}if(m<10){str[1]="0"+m;}
        String sDate = str[0]+""+str[1]+""+str[2];
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        Date date = null;
        try {
            date = dateFormat.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if(type.equals("decrease")){calendar.add(Calendar.DATE, -1);}
        else {calendar.add(Calendar.DATE, +1);}
        String yesterdayAsString = dateFormat.format(calendar.getTime());
        String day=yesterdayAsString.substring(0,2),
                month=yesterdayAsString.substring(2,4),
                year=yesterdayAsString.substring(4);
        String previousDate = Integer.parseInt(day)+"-"+Integer.parseInt(month)+"-"+Integer.parseInt(year);
        if(startDate.length()<=0) {
            dateBox.setText(previousDate);
            return null;
        }else{
            return previousDate;
        }
    }
}
