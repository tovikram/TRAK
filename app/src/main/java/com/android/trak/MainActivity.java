package com.android.trak;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.trak.ui.main.SectionsPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

  public FirebaseFirestore db = FirebaseFirestore.getInstance();
  Calendar cal = Calendar.getInstance();
  public int year = cal.get(Calendar.YEAR);
  public int month = cal.get(Calendar.MONTH);
  public int day = cal.get(Calendar.DAY_OF_MONTH);
  public String currentDateStr = day+""+(month+1)+""+year+"";
  public TextView title;

  public GlobalClass globalClass = new GlobalClass();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    title = findViewById(R.id.title);

      if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED) {
          globalClass.showAlertMsg("File Permission Needed !",MainActivity.this);
      }

    if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
    else {
      SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
      ViewPager viewPager = findViewById(R.id.view_pager);
      viewPager.setAdapter(sectionsPagerAdapter);
      TabLayout tabs = findViewById(R.id.tabs);
      tabs.setupWithViewPager(viewPager);

      //checking if any update for onlineStrings
        try {
            final String xmlFilePath = Environment.getExternalStorageDirectory().getPath() + "/TrakApp/update.xml";
            File fXmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            final String mainStringCode = doc.getElementById("mainStringCode").getTextContent();
            final String colorCode = doc.getElementById("colorCode").getTextContent();

            final DocumentReference dr = db.collection("onlineStrings").document("updateCodes");
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        if (!fdata.get("colorxml").toString().equals(colorCode)) {
                            //creating xml file from onlineStrings
                            //globalClass.updateOnlineString("color",MainActivity.this);
                            //globalClass.createUpdateCodeFile(mainStringCode,fdata.get("colorxml").toString(),MainActivity.this);
                        }
                        if (!fdata.get("mainStringXml").toString().equals(mainStringCode)) {
                            //creating xml file from onlineStrings
                            globalClass.updateOnlineString("main",MainActivity.this);
                            globalClass.createUpdateCodeFile(fdata.get("mainStringXml").toString(),colorCode,MainActivity.this);
                        }
                    }
                }
            });

        } catch (Exception e) {
            globalClass.createUpdateCodeFile("0","0",MainActivity.this);
            globalClass.updateOnlineString("main",MainActivity.this);
        }


      final DocumentReference dr = db.collection("dailySell").document("dailyCount");
      dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          DocumentSnapshot document = task.getResult();
          if (document.exists()) {
            Map<String, Object> fdata = new HashMap<>();
            fdata = document.getData();
            if(fdata.get("date").toString().equals(currentDateStr)){
              //do nothing
            }else{
              Map<String, Object> data = new HashMap<>();
              data.put("count", 0);
              data.put("date", currentDateStr);
              db.collection("dailySell").document("dailyCount")
                      .set(data, SetOptions.merge());

              final DocumentReference docRefStock = db.collection("summary").document("stock");
              docRefStock.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                  if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                      Map<String, Object> fdata = new HashMap<>();
                      fdata = document.getData();
                      int newQnty30 = Integer.parseInt(fdata.get("30kg")+"");
                      int newQnty15 = Integer.parseInt(fdata.get("15kg")+"");

                      Map<String, Object> data = new HashMap<>();
                      data.put("30kg", newQnty30);
                      data.put("15kg", newQnty15);
                      db.collection("summary").document("previousStock")
                              .set(data, SetOptions.merge());

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
        }
      });

      //opening mainStringFile
        Document doc=globalClass.openXmlFile("mainStringXml",MainActivity.this);

        //seting text
        if(doc==null){
            title.setText("TRAK");
        }else {
            title.setText(doc.getElementById("appName").getTextContent() + "");
        }
    }

  }

  @Override
  protected void onStart() {
    super.onStart();
    if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
    else {

    }
  }

  public boolean isConnected(Context context) {

    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netinfo = cm.getActiveNetworkInfo();

    if (netinfo != null && netinfo.isConnectedOrConnecting()) {
      android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

      if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
        else return false;
    } else
    return false;
  }

  public AlertDialog.Builder buildDialog(Context c) {

    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setTitle("No Internet Connection");
    builder.setMessage("Please turn on your internet. Press ok to Exit");

    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {

        finish();
      }
    });

    return builder;
  }
}