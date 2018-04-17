package com.example.zhaok.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button saveChangeButton;
    private EditText statusInput;

    private FirebaseAuth mAuth;
    private DatabaseReference changeStatusReference;

    private ProgressDialog mProgessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();
        changeStatusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("userStatus");

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveChangeButton = (Button) findViewById(R.id.save_status_change_button);
        statusInput = (EditText)findViewById(R.id.status_input);

        mProgessDialog = new ProgressDialog(this);

        String oldStatus = getIntent().getExtras().getString("userStatus");
        statusInput.setText(oldStatus);


        saveChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newStatus = statusInput.getText().toString();
                changeProfileStatus(newStatus);
            }
        });
    }

    private void changeProfileStatus(String newStatus)
    {
        if(TextUtils.isEmpty(newStatus)){
            Toast.makeText(getApplicationContext(),"Please write your status",Toast.LENGTH_SHORT).show();
        }
        else{
            mProgessDialog.setTitle("Change Profile Status");
            mProgessDialog.setMessage("Please wait, while updating your status");
            mProgessDialog.show();
            changeStatusReference.setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        mProgessDialog.dismiss();
                        Intent settingIntent = new Intent(getApplicationContext(),SettingActivity.class);
                        startActivity(settingIntent);

                        Toast.makeText(getApplicationContext(),"Profile Status Updated Successfully...", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Error happened", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


    }
}
