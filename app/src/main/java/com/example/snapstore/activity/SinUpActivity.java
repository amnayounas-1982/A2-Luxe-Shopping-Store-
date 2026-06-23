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
import com.example.snapstore.fragment.HomeFragment;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SinUpActivity extends BaseActivity{

   private EditText editTextEmail, editTextPassword, editTextUserName;
   private Button buttonSignUp;
   private FirebaseAuth mAuth;
   private FirebaseFirestore fstore;
   private ProgressBar progressBar;
   private String userID;
   private String role;
   private TextView textViewSignUp;
   private ImageView imageViewGoogle, imageViewPhone,imageViewBack;
   private GoogleSignInOptions gso;
   private static final int RC_SIGN_IN = 100;
   private GoogleSignInClient mGoogleSignInClient;
   private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sin_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setUpOnClickListner();
    }

    public void init()
    {
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        editTextUserName = findViewById(R.id.et_username);
        buttonSignUp = findViewById(R.id.btn_sign_up);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        imageViewGoogle = findViewById(R.id.iv_google);
        imageViewPhone = findViewById(R.id.iv_phone);
        imageViewBack = findViewById(R.id.iv_back);
        textViewSignUp = findViewById(R.id.tv_sign_in);
        progressBar = findViewById(R.id.pb_progress);
        role = getIntent().getStringExtra("role");
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // use Web Client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    } // End of init

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
                        Toast.makeText(SinUpActivity.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                userID = user.getUid();

                                // Firestore reference
                                databaseReference = FirebaseDatabase.getInstance().getReference("users");

                                // Store user data
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("Name", user.getDisplayName());
                                userMap.put("email", user.getEmail());
                                userMap.put("role", role); // role from intent

                                databaseReference.child(userID).setValue(userMap).addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(SinUpActivity.this, "User data saved Successfully", Toast.LENGTH_SHORT).show();

                                        if ("owner".equals(role)) {
                                            Intent intent = new Intent(getApplicationContext(), OwnerHomeActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(SinUpActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                                else

                            {
                                // If sign in fails
                                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            }

                });
                }

    // Click listener for handle clicks
    public void setUpOnClickListner()
    {
        // Email Verification
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = editTextEmail.getText().toString().trim();
                String pass = editTextPassword.getText().toString().trim();
                String name = editTextUserName.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SinUpActivity.this, "Please enter a email", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(SinUpActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(name)) {
                    Toast.makeText(SinUpActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SinUpActivity.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
                                    // Store data in firebase
                                    userID = mAuth.getCurrentUser().getUid();
                                     databaseReference = FirebaseDatabase.getInstance().getReference("users");

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("Name", name);
                                    userMap.put("email", email);
                                    userMap.put("role", role);

                                    databaseReference.child(userID).setValue(userMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(SinUpActivity.this, "User data saved Successfully", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(SinUpActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SinUpActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        // Google Button Click
        imageViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });

            }
        });

        // Phone Button Click
        imageViewPhone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SinUpActivity.this, PhoneAuthActivity.class);
                startActivity(intent);
                finish();

            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SinUpActivity.this, SinInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SinUpActivity.this, SelectionActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }


}