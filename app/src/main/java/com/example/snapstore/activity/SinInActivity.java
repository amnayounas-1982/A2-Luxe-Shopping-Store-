package com.example.snapstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snapstore.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SinInActivity extends BaseActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private ImageView imageViewGoogle,imageViewPhone;
    private TextView textViewSignUp;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private String role;
    private GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sin_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setUpOnClickListener();
    }
    private void init()
    {
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        buttonSignIn = findViewById(R.id.btn_sign_in);
        imageViewGoogle = findViewById(R.id.iv_google);
        imageViewPhone = findViewById(R.id.iv_phone);
        textViewSignUp = findViewById(R.id.tv_sign_up);
        mAuth = FirebaseAuth.getInstance();
        role = getIntent().getStringExtra("role");
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // use Web Client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        progressBar = findViewById(R.id.pb_progress);

    }

    private void setUpOnClickListener()
    {
        imageViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String user = editTextEmail.getText().toString().trim();
                String pass = editTextPassword.getText().toString().trim();
                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SinInActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(user, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    // Login successful
                                    Toast.makeText(SinInActivity.this, "SignIn Successfully", Toast.LENGTH_SHORT).show();
                                    if ("owner".equals(role)) {
                                        Intent intent = new Intent(getApplicationContext(), OwnerHomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();

                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    // Login failed
                                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Toast.makeText(SinInActivity.this, "Authentication failed: " + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SinInActivity.this, SinUpActivity.class);
                startActivity(intent);
            }
        });



    }

    // For Google Authentication
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Check if Google Authentication fail or not
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SinInActivity.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();

                                    if ("owner".equals(role)) {
                                        Intent intent = new Intent(getApplicationContext(), OwnerHomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SinInActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }



    }



