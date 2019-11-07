package qrscanner.mobkini.com;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    EditText editText_username,editText_password;
    Button button_login;
    StandardProgressDialog standardProgressDialog;
    PreferenceManagerLogin session;
    private static long back_pressed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);

        standardProgressDialog = new StandardProgressDialog(this.getWindow().getContext());
        session = new PreferenceManagerLogin(getApplicationContext());
        editText_username = findViewById(R.id.editText_username);
        editText_password = findViewById(R.id.editText_password);
        button_login = findViewById(R.id.button_login);


        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText_username.getText().toString().equals("")){
                    editText_username.setError("Required");
                }else if(editText_password.getText().toString().equals("")){
                    editText_password.setError("Required");
                }else{
                    standardProgressDialog.show();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("login");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(editText_username.getText().toString())) {
                                Log.d("EXIST","EXIST");

                                DatabaseReference references = FirebaseDatabase.getInstance().getReference("login").child(editText_username.getText().toString());

                                references.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.d("CHILD",dataSnapshot.toString());

                                        Gson gson = new Gson();
                                        String s1 = gson.toJson(dataSnapshot.getValue());
                                        JSONObject object = null;
                                        try {
                                            object = new JSONObject(s1);
                                            if(object.getString("username").equals(editText_username.getText().toString()) && object.getString("password").equals(editText_password.getText().toString())){
                                                standardProgressDialog.dismiss();
                                                session.createLoginSession(editText_username.getText().toString(),editText_password.getText().toString());
                                                Toast.makeText(getApplicationContext(),"Login Success",Toast.LENGTH_LONG).show();
                                                Intent next = new Intent(getApplicationContext(),QrCodeActivity.class);
                                                startActivity(next);
                                            }else{
                                                standardProgressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(),"Username or password wrong",Toast.LENGTH_LONG).show();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else{
                                standardProgressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Username or password wrong",Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });
    }


    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())  moveTaskToBack(true);
        else Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

}

