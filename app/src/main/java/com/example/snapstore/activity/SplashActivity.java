package com.example.snapstore.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snapstore.R;
import com.example.snapstore.util.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends BaseActivity {

    private DatabaseReference userRef;
    private FirebaseUser user;
    private TextView textViewAppName;
    private FirebaseAuth mAuth;
    private Animation animationBottomUp;
    private Button btnRetry;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setOnClickListener();
    }

    private boolean checkAndResetThemeFlag() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean changed = prefs.getBoolean("theme_changed", false);
        if (changed) {
            prefs.edit().putBoolean("theme_changed", false).apply();
        }
        return changed;
    }

    private void restartSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    public void init()
    {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        textViewAppName = findViewById(R.id.tv_app_name);
        animationBottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_to_uo);
        btnRetry = findViewById(R.id.btn_retry);
        progressBar = findViewById(R.id.pb_progress);

    }

    public void setOnClickListener()
    {
        btnRetry.setOnClickListener(v -> {
            btnRetry.setVisibility(View.GONE);
            checkUserRole();
        });

        textViewAppName.setAnimation(animationBottomUp);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserRole, 2000);

    }
    private void checkUserRole() {
        progressBar.setVisibility(View.VISIBLE);

        if (!NetworkUtil.isConnected(this)) {
            progressBar.setVisibility(View.GONE);
            // No internet → show a message and maybe retry
            Toast.makeText(this, "No Internet Connection. Try ", Toast.LENGTH_SHORT).show();
            btnRetry.setVisibility(View.VISIBLE);

            return;
        }
        btnRetry.setVisibility(View.GONE);
        if (user != null) {
            String uid = user.getUid();
            progressBar.setVisibility(View.VISIBLE);
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);

                        if ("owner".equals(role)) {
                            startActivity(new Intent(this, OwnerHomeActivity.class));
                            finish();
                        } else if ("user".equals(role)) {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            // Unknown role → go to selection
                            startActivity(new Intent(this, SelectionActivity.class));
                            finish();
                        }
                    } else {
                        // No role yet → go to selection
                        startActivity(new Intent(this, SelectionActivity.class));
                        finish();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e("Role", "Error fetching role", task.getException());
                    startActivity(new Intent(this, SelectionActivity.class));
                    finish();
                }
            });
        } else {
            startActivity(new Intent(this, SelectionActivity.class));
            finish();
        }
    }
}
