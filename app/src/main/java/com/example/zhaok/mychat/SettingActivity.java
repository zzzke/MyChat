package com.example.zhaok.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView settingDisplayImage;
    private TextView settingDisplayUsername;
    private TextView settingDisplayStatus;
    private Button settingChangeImage;
    private Button settingChangeStatus;

    private final static int Gallery_Pick = 1;

    private StorageReference storeProflieImageStorageReference;
    private StorageReference thumbImageStorageReference;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;

    Bitmap thumbBitmap = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        String onlineUserID = mAuth.getCurrentUser().getUid();

        // connect to database nodes
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserID);
        getUserDataReference.keepSynced(true);

        storeProflieImageStorageReference = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumbImageStorageReference = FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        settingChangeImage = (Button) findViewById(R.id.setting_change_image_button);
        settingChangeStatus =(Button)findViewById(R.id.setting_change_status_button);
        settingDisplayImage =(CircleImageView)findViewById(R.id.setting_profile_image);
        settingDisplayUsername = (TextView) findViewById(R.id.setting_username);
        settingDisplayStatus = (TextView)findViewById(R.id.setting_user_status);
        progressDialog = new ProgressDialog(this);



        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("userName").getValue().toString();
                String status = dataSnapshot.child("userStatus").getValue().toString();
                final String image = dataSnapshot.child("userImage").getValue().toString();
                String thumbImage = dataSnapshot.child("userThumbImage").getValue().toString();


                settingDisplayUsername.setText(name);
                settingDisplayStatus.setText(status);

                if(!image.equals("default_profile")){

                    Picasso.with(SettingActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_profile).into(settingDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.default_profile).into(settingDisplayImage);
                        }
                    });
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });

        settingChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldStatus = settingDisplayStatus.getText().toString();
                Intent statusIntent = new Intent(getApplicationContext(),StatusActivity.class);
                statusIntent.putExtra("userStatus",oldStatus);
                startActivity(statusIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Updating Profile Image");
                progressDialog.setMessage("Please wait, while we are updating your profile image.");
                progressDialog.show();

                Uri resultUri = result.getUri();

                File thumbFilePath = new File(resultUri.getPath());
                String userID = mAuth.getCurrentUser().getUid();

                try{
                   thumbBitmap = new Compressor(this)
                           .setMaxWidth(200)
                           .setMaxHeight(200)
                           .setQuality(50)
                           .compressToBitmap(thumbFilePath);
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                final byte[] thumbByte = byteArrayOutputStream.toByteArray();

                StorageReference filePath = storeProflieImageStorageReference.child(userID +".jpg");
                final StorageReference thumbImagePath = thumbImageStorageReference.child(userID +".jpg");



                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"saving your image successfully to Firebase Storage", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbImagePath.putBytes(thumbByte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                    String thumbDownloadUrl = thumbTask.getResult().getDownloadUrl().toString();
                                    if(thumbTask.isSuccessful()){
                                        Map updateUserData = new HashMap();
                                        updateUserData.put("userImage", downloadUrl);
                                        updateUserData.put("userThumbImage",thumbDownloadUrl);

                                        getUserDataReference.updateChildren(updateUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getApplicationContext(),"Image Upload successfully", Toast.LENGTH_SHORT).show();

                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                                    }
                                }
                            });




                        }
                        else{
                            Toast.makeText(getApplicationContext(),"eeror saving your image to Firebase Storage", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
