package com.example.inclass01_advancemad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    ImageView userProfile;
    Spinner spinnerGender;
    Button btnSignUp, btnBack;
    EditText editFirstName, editLastName, editCity, editEmail, editPassword, editRetypePassword;

    FirebaseAuth mauth;
    DatabaseReference mroot;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Uri filePath;
    String imagePath;
    String userId;

    final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

        // Initialize Firebase
        mauth = FirebaseAuth.getInstance();
        mroot = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Bind views
        userProfile = findViewById(R.id.user_PROFILE);
        spinnerGender = findViewById(R.id.user_genderSpinner);
        btnSignUp = findViewById(R.id.buttonSignUp);
        btnBack = findViewById(R.id.buttonBacktoMain);
        editFirstName = findViewById(R.id.user_FirstName);
        editLastName = findViewById(R.id.user_LastName);
        editCity = findViewById(R.id.userCIty);
        editEmail = findViewById(R.id.user_editEmail);
        editPassword = findViewById(R.id.user_editPassword);
        editRetypePassword = findViewById(R.id.user_editRetypePass);

        fillDropdown();

        btnSignUp.setOnClickListener(view -> signUp());
        userProfile.setOnClickListener(view -> chooseImage());
        btnBack.setOnClickListener(view -> finish());
    }

    private void signUp() {
        final String name = editFirstName.getText().toString().trim();
        final String lastName = editLastName.getText().toString().trim();
        final String city = editCity.getText().toString().trim();
        final String gender = spinnerGender.getSelectedItem().toString();
        final String email = editEmail.getText().toString().trim();
        final String password = editPassword.getText().toString().trim();
        final String confirmPass = editRetypePassword.getText().toString().trim();

        // Basic input validation
        if (TextUtils.isEmpty(name)) {
            editFirstName.setError("Enter First Name");
            return;
        } else if (TextUtils.isEmpty(lastName)) {
            editLastName.setError("Enter Last Name");
            return;
        } else if (TextUtils.isEmpty(email)) {
            editEmail.setError("Enter Email");
            return;
        } else if (TextUtils.isEmpty(password)) {
            editPassword.setError("Enter Password");
            return;
        } else if (TextUtils.isEmpty(confirmPass)) {
            editRetypePassword.setError("Confirm your password");
            return;
        } else if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(city)) {
            editCity.setError("Enter City");
            return;
        } else if (filePath == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign up the user
        mauth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userId = mauth.getCurrentUser().getUid();

                        User user = new User();
                        user.userId = userId;
                        user.firstName = name;
                        user.lastName = lastName;
                        user.email = email;
                        user.password = password;
                        user.city = city;
                        user.gender = gender;

                        addUserImage(user);
                    } else {
                        // ✅ FIX: Show signup error
                        Toast.makeText(SignUpActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("SignUpError", "Signup failed", task.getException());
                    }
                });
    }

    private void addUserImage(final User user) {
        imagePath = "MessageImages/" + userId + ".jpg";
        final StorageReference userImageRef = storageReference.child(imagePath);

        userImageRef.putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.imageUrl = uri.toString();

                    mroot.child("Users").child(userId).setValue(user)
                            .addOnCompleteListener(saveTask -> {
                                Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, ChatroomActivity.class));
                                finish();
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ImageUpload", "Failed", e);
                });
    }

    private void fillDropdown() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinnerValues,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                userProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
