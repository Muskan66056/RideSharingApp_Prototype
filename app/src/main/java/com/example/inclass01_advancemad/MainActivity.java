package com.example.inclass01_advancemad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mauth;
    Button btnLogin, btnSignUp;
    TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI elements
        forgotPasswordText = findViewById(R.id.forgot_password);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        mauth = FirebaseAuth.getInstance();
        setTitle("Login");

        // Forgot password redirect
        forgotPasswordText.setOnClickListener(view -> {
            Intent forgotPasswordIntent = new Intent(getApplicationContext(), forgot_password.class);
            startActivity(forgotPasswordIntent);
        });

        btnLogin.setOnClickListener(view -> Login());

        btnSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser != null) {
            Intent chatroom_intent = new Intent(getApplicationContext(), ChatroomActivity.class);
            startActivity(chatroom_intent);
            finish(); // So MainActivity doesn't stay in backstack
        }
    }

    public void Login() {
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter a password", Toast.LENGTH_SHORT).show();
        } else {
            mauth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Login", "User logged in");
                            startActivity(new Intent(MainActivity.this, ChatroomActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid user. Please sign up.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
