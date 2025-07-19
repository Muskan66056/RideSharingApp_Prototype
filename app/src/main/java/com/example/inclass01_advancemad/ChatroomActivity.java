package com.example.inclass01_advancemad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ChatroomActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private DatabaseReference mroot;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase database;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private View headerView;
    private FirebaseAuth mauth;
    private FirebaseUser firebaseUser;
    private String userId;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mroot = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();

        setUpNavigationBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatroomFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.chatroom);
        }

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            getUserDetails();
        }
    }

    private void setUpNavigationBar() {
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void getUserDetails() {
        DatabaseReference myRef = database.getReference("Users/" + userId);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);

                if (user != null) {
                    TextView headerUserName = headerView.findViewById(R.id.txtUserName_NAVHEADER);
                    TextView headerEmail = headerView.findViewById(R.id.txtUSEREMAIL_NAVHEADER);
                    ImageView userProfile = headerView.findViewById(R.id.imgProfile_NAVHEADER);

                    headerUserName.setText(user.firstName + " " + user.lastName);
                    headerEmail.setText(user.email);

                    StorageReference storageReference = firebaseStorage.getReference();
                    StorageReference imageRef = storageReference.child("MessageImages/" + userId + ".jpg");

                    final long ONE_MEGABYTE = 5 * 1024 * 1024;
                    imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        userProfile.setImageBitmap(bmp);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 300);
                        userProfile.setLayoutParams(layoutParams);
                        Log.d("FirebaseImage", "Profile image loaded successfully");
                    }).addOnFailureListener(e -> Log.d("FirebaseImage", "Failed to load profile image"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FirebaseError", "Failed to read user data: " + error.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void logout() {
        mauth.signOut();
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        } else if (id == R.id.user) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UserFragment())
                    .commit();
        } else if (id == R.id.chatroom) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatroomFragment())
                    .commit();
        } else if (id == R.id.logout) {
            logout();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
