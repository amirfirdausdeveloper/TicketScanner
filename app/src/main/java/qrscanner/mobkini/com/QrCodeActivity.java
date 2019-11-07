package qrscanner.mobkini.com;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

public class QrCodeActivity extends AppCompatActivity {
    CodeScannerView scannerView;
    private CodeScanner mCodeScanner;
    LinearLayout linear_result;
    Button button_scanAgain,button_exitApp;
    StandardProgressDialog standardProgressDialog;
    PreferenceManagerLogin session;
    TextView textView_name,textView_zone,textView_seat,textView_message;
    String url,nuke,concertName,userId;
    TextView textView_concertName;
    private static long back_pressed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);

        standardProgressDialog = new StandardProgressDialog(this.getWindow().getContext());
        session = new PreferenceManagerLogin(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(PreferenceManagerLogin.KEY_UID);
        if(session.checkLogin()){
            finish();
        }

        textView_name = findViewById(R.id.textView_name);
        textView_zone = findViewById(R.id.textView_zone);
        textView_seat = findViewById(R.id.textView_seat);
        textView_message = findViewById(R.id.textView_message);
        button_exitApp = findViewById(R.id.button_exitApp);
        textView_concertName = findViewById(R.id.textView_concertName);

        linear_result = findViewById(R.id.linear_result);
        linear_result.setVisibility(View.GONE);
        scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linear_result.setVisibility(View.VISIBLE);
                        checkData(result.getText());
                    }
                });
            }
        });
        mCodeScanner.startPreview();
//        scannerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mCodeScanner.startPreview();
//            }
//        });

        button_scanAgain = findViewById(R.id.button_scanAgain);
        button_scanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linear_result.setVisibility(View.GONE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCodeScanner.startPreview();
                    }
                }, 1000);

            }
        });

        button_exitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(QrCodeActivity.this)
                        .setMessage("Are you sure want to exit apps?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                session.logoutUser();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("login").child("changes");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("CHILD",dataSnapshot.toString());
                    Gson gson = new Gson();
                    String s1 = gson.toJson(dataSnapshot.getValue());
                    JSONObject object = null;
                    try {
                        object = new JSONObject(s1);
                        url = object.getString("url");
                        nuke = object.getString("nuke");
                        concertName = object.getString("concertName");
                        textView_concertName.setText(concertName);

                        Log.d("object0",object.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkData(String text) {
        NukeSSLCerts.nuke(nuke);

        standardProgressDialog.show();
        JSONObject data = new JSONObject();
        try {
            data.put("serial",text);
            data.put("user_id",userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        standardProgressDialog.dismiss();
                        Log.d("DETAILS",response.toString());

                        try {
                            if(response.getString("success").equals("0")){
                                if(response.getString("message").equals("Ticket no found")){
                                    textView_name.setText(response.getString("name"));
                                    textView_zone.setText(response.getString("zone"));
                                    textView_seat.setText(response.getString("seat"));
                                    textView_message.setText(response.getString("message"));
                                    textView_message.setTextColor(Color.RED);
                                }else {
                                    textView_name.setText(response.getString("name"));
                                    textView_zone.setText(response.getString("zone"));
                                    textView_seat.setText(response.getString("seat"));
                                    textView_message.setText(response.getString("message"));
                                    textView_message.setTextColor(Color.RED);
                                }
                            }else if(response.getString("success").equals("1")){
                                textView_name.setText(response.getString("name"));
                                textView_zone.setText(response.getString("zone"));
                                textView_seat.setText(response.getString("seat"));
                                textView_message.setText(response.getString("message"));
                                textView_message.setTextColor(Color.GREEN);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            public String getBodyContentType(){
                return "application/json";
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> responseHeaders = response.headers;
                return super.parseNetworkResponse(response);
            }
        };
        mQueue.add(jsonObjectRequest);
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) System.exit(0);
        else Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }


}
