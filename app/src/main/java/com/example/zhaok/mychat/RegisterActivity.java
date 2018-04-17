package com.example.zhaok.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    //conncect database
    private DatabaseReference storeUserDefaultDataReference;


    private Toolbar mToolbar;
    private EditText register_name;
    private EditText register_email;
    private EditText register_password;
    private Button create_account_button;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        register_name = (EditText) findViewById(R.id.register_name);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        create_account_button = (Button) findViewById(R.id.create_account_button);
        loadingBar = new ProgressDialog(this);

        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = register_name.getText().toString();
                String email = register_email.getText().toString();
                String password = register_password.getText().toString();

                registerAccount(name, email, password);

            }
        });
    }

    private void registerAccount(final String name, String email, String password) {
        if(TextUtils.isEmpty(name)){
            Toast.makeText(getApplicationContext(),"Please write your name",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Please write your email",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please write your password",Toast.LENGTH_SHORT).show();
        }

        else {

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating account for you");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserID = mAuth.getCurrentUser().getUid();
                            storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

                            storeUserDefaultDataReference.child("userName").setValue(name);
                            storeUserDefaultDataReference.child("userStatus").setValue("Hi, I am using MyChat app!");
                            storeUserDefaultDataReference.child("userImage").setValue("default_profile");
                            storeUserDefaultDataReference.child("device_token").setValue(deviceToken);
                            storeUserDefaultDataReference.child("userThumbImage").setValue("default_image").
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(),"error for store data and sign up",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Error , try again", Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }
}
