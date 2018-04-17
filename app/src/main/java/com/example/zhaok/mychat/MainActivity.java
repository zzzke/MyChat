package com.example.zhaok.mychat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsPagerAdapter mTabsPagerAdapter;
    private FirebaseUser currentUser;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            String onlineUserID = mAuth.getCurrentUser().getUid();
            usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserID);
        }

        //Tabs for MainActivity
        mViewPager = (ViewPager)findViewById(R.id.main_tabs_pager);
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPagerAdapter);
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyChat");
    }

    @Override
    protected void onStart() {
        super.onStart();

       //currentUser = mAuth.getCurrentUser();
        if(currentUser == null){

            logOutUser();

        }
        else if(currentUser !=null){
            usersReference.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser !=null){
            usersReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void logOutUser() {
        Intent starPageIntent = new Intent(getApplicationContext(),StartPageActivity.class);
        starPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(starPageIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId() == R.id.main_logot_button){

             if(currentUser != null){
                 usersReference.child("online").setValue(ServerValue.TIMESTAMP);
             }
             mAuth.signOut();
             logOutUser();
         }

        if(item.getItemId() == R.id.main_all_user_button){
            Intent allUsersIntent = new Intent(getApplicationContext(),AllUsersActivity.class);
            startActivity(allUsersIntent);
        }

         if(item.getItemId() == R.id.main_account_settings_button){
             Intent settingIntent = new Intent(getApplicationContext(),SettingActivity.class);
             startActivity(settingIntent);
         }
         return true;
    }
}
