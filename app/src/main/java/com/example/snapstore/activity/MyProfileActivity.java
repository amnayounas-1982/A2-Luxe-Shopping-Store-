package com.example.snapstore.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snapstore.R;
import com.example.snapstore.fragment.HomeFragment;
import com.example.snapstore.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MyProfileActivity extends BaseActivity {
    private ImageView imageViewPerson;
    private ImageButton imageButtonEdit, imageButtonBack;
    private EditText editTextName, editTextPhone, editTextAddress;
    private Button buttonComplete;
    private final int galleryRequestCode = 1000;
    private DatabaseReference userRef;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);
        init();
        loadData();
        setOnClickListener();

    }
    private void init()
    {
        imageViewPerson = findViewById(R.id.iv_person);
        imageButtonEdit = findViewById(R.id.ib_edit);
        imageButtonBack = findViewById(R.id.ib_back);
        editTextName = findViewById(R.id.et_name);
        editTextAddress = findViewById(R.id.et_address);
        editTextPhone = findViewById(R.id.et_phone);
        buttonComplete = findViewById(R.id.btn_complete);
        progressBar = findViewById(R.id.pb_progress);
    }
    private void loadData()
    {
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users profile").child(uid);

        //  Load existing data (if any)
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    // Pre-fill fields with current values
                    if (snapshot.child("name").exists()) {
                        editTextName.setText(snapshot.child("name").getValue(String.class));
                    }
                    if (snapshot.child("address").exists()) {
                        editTextAddress.setText(snapshot.child("address").getValue(String.class));
                    }
                    if (snapshot.child("phone").exists()) {
                        editTextPhone.setText(snapshot.child("phone").getValue(String.class));
                    }
                    if (snapshot.child("image").exists()) {
                        String base64Image = snapshot.child("image").getValue(String.class);

                        if (base64Image != null) {
                            Bitmap bitmap = decodeBase64ToImage(base64Image);
                            imageViewPerson.setImageBitmap(bitmap);

                            // also store the bitmap in the tag for later
                            imageViewPerson.setTag(bitmap);
                        }
                    }

                }
                // else → first time, leave fields empty
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }// end of load data

    private void setOnClickListener()
    {
        buttonComplete.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String name = editTextName.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            Object tag = imageViewPerson.getTag();
            Bitmap bitmap = (Bitmap) tag;
            String imageBase64 = encodeImageToBase64(bitmap);

            Map<String, Object> updates = new HashMap<>();

            if (!name.isEmpty()) updates.put("name", name);
            if (!address.isEmpty()) updates.put("address", address);
            if (!phone.isEmpty()) updates.put("phone", phone);
            updates.put("image", imageBase64);


            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                    finish(); // go back to previous screen
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
                }
            });
        });

        imageButtonEdit.setOnClickListener(v ->{
            Intent gallery = new Intent(Intent.ACTION_PICK); // action to pick your data from gallery
            gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //show external data
            startActivityForResult(gallery, galleryRequestCode);
        } );

        imageButtonBack.setOnClickListener(v -> {
            if (hasUnsavedData()) {
                showExitConfirmation();
            } else {
                finish();
            }
        });
    }// end of onClickListener

    public boolean hasUnsavedData()
    {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        return !name.isEmpty() ||  !phone.isEmpty() || !address.isEmpty();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle((getString(R.string.str_confirm_exit)))
                .setMessage(("Are you sure you want to exit, Edited data will not save"))
                .setPositiveButton("Yes", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // compress to JPEG
        byte[] imageBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == galleryRequestCode) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageViewPerson.setImageBitmap(bitmap);
                    imageViewPerson.setTag(bitmap); // store bitmap in tag
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

}