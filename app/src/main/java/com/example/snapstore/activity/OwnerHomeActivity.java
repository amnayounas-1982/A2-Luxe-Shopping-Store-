package com.example.snapstore.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.snapstore.R;
import com.example.snapstore.fragment.HomeFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class OwnerHomeActivity extends BaseActivity {
    private ImageView imageViewItem;
    private Button buttonCamera, buttonGallery, buttonAdd;
    private EditText editTextTitle, editTextRating, editTextPrize;
    private final int cameraRequestCode = 100;
    private final int galleryRequestCode = 1000;
    private final String[] item = {"Women", "Men", "Children"};
    private ArrayAdapter<String> adapterCategory;
    private AutoCompleteTextView autoDropdownCategory;
    private Toolbar toolBarAddData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_owner_home);
        initView();
        setupClickListeners();

    }

    public void initView() {
        imageViewItem = findViewById(R.id.iv_camera);
        buttonCamera = findViewById(R.id.btn_camera);
        buttonGallery = findViewById(R.id.btn_gallery);
        buttonAdd = findViewById(R.id.btn_add);
        editTextTitle = findViewById(R.id.et_title);
        editTextRating = findViewById(R.id.et_rating);
        editTextPrize = findViewById(R.id.et_prize);
        autoDropdownCategory = findViewById(R.id.auto_dropdown_category);
        adapterCategory = new ArrayAdapter<>(this, R.layout.category_list, item);
        autoDropdownCategory.setAdapter(adapterCategory);
        toolBarAddData = findViewById(R.id.tb_add_data);
        setSupportActionBar(toolBarAddData);
    } // End of init

    public void setupClickListeners() {
        autoDropdownCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = adapterCategory.getItem(position);
                Toast.makeText(OwnerHomeActivity.this, "Selected: " + selected, Toast.LENGTH_SHORT).show();
            }
        });

        // Button Camera Logic
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, cameraRequestCode);
                } else {
                    openCamera();
                }
            }
        });

        // Button Gallery Logic
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK); // action to pick your data from gallery
                gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //show external data
                startActivityForResult(gallery, galleryRequestCode);
            }
        });

        // tool bar logic
        toolBarAddData.setNavigationOnClickListener(v -> {
            if (hasUnsavedData()) {
                showExitConfirmation();
            } else {
                finish();
            }
        });



        buttonAdd.setOnClickListener(v -> {
            Object tag = imageViewItem.getTag();
            String title = editTextTitle.getText().toString().trim();
            String rating = editTextRating.getText().toString().trim();
            String prize = editTextPrize.getText().toString().trim();
            String category = autoDropdownCategory.getText().toString().trim();

            if (title.isEmpty() || rating.isEmpty() || prize.isEmpty() || category.isEmpty()|| tag == null) {
                Toast.makeText(OwnerHomeActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = (Bitmap) tag;
            String imageBase64 = encodeImageToBase64(bitmap);

            // Store data under category
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("items").child(category);
            String key = dbRef.push().getKey();

            Map<String, String> map = new HashMap<>();
            map.put("title", title);
            map.put("rating", rating);
            map.put("prize", prize);
            map.put("image", imageBase64);

            dbRef.child(key).setValue(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(OwnerHomeActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OwnerHomeActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            });

        });

    }// end of click listener

    // use if user write any thing in text box this function trigger to ask user to confirm back to home
    public boolean hasUnsavedData()
    {
        String title = editTextTitle.getText().toString().trim();
        String rating = editTextRating.getText().toString().trim();
        String category = autoDropdownCategory.getText().toString().trim();
        return !title.isEmpty() ||  !rating.isEmpty() || !category.isEmpty();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle((getString(R.string.str_confirm_exit)))
                .setMessage((getString(R.string.str_are_you_sure_you_want_to_exit_all_data_will_be_lost)))
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(OwnerHomeActivity.this, HomeFragment.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, cameraRequestCode);

    }

    // Handel camera permission without this function after reinstall camera function not work well
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == cameraRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // compress to JPEG
        byte[] imageBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {

            // get camera result using Bitmap
            if (requestCode == cameraRequestCode) {
                Bitmap img = (Bitmap) (data.getExtras().get("data"));
                imageViewItem.setImageBitmap(img);
                imageViewItem.setTag(img);
                // get image result
            } else if (requestCode == galleryRequestCode) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageViewItem.setImageBitmap(bitmap);
                    imageViewItem.setTag(bitmap); // store bitmap in tag
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

}