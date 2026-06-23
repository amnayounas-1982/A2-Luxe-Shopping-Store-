package com.example.snapstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snapstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class SelectionActivity extends BaseActivity {
    private RadioGroup radioGroupRole;
    private Button btnNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setUpOnClickListner();
    }

    public void init() {
        radioGroupRole = findViewById(R.id.rg_group_role);
        btnNext = findViewById(R.id.btn_next);
    }

    public void setUpOnClickListner() {
        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroupRole.getCheckedRadioButtonId();
            String role = "";

            if (selectedId == R.id.rb_radio_owner) {
                role = "owner";
            } else if (selectedId == R.id.rb_radio_user) {
                role = "user";
            } else {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, SinUpActivity.class);
            intent.putExtra("role", role);
            startActivity(intent);
        });

    }

}