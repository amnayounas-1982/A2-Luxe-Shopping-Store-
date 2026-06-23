package com.example.snapstore.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapstore.R;
import com.example.snapstore.activity.HelpCenterActivity;
import com.example.snapstore.activity.MyOrdersActivity;
import com.example.snapstore.activity.MyProfileActivity;
import com.example.snapstore.activity.PrivacyPolicyActivity;
import com.example.snapstore.activity.SettingActivity;
import com.example.snapstore.activity.SinInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private TextView buttonLogOut,textViewSettings, textViewMyOrders, textViewMyProfile, textViewName,textViewPrivacy, textViewHelp;
    private ImageView imageViewItem;
    private ImageButton imageButtonBack;
    private DatabaseReference userRef;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        setOnClickListener();
        getData();
        return view;
    }

    public void init(View view) {
        buttonLogOut = view.findViewById(R.id.btn_log_out);
        textViewMyProfile = view.findViewById(R.id.tv_my_profile);
        textViewName = view.findViewById(R.id.tv_name);
        imageViewItem = view.findViewById(R.id.iv_item1);
        imageButtonBack = view.findViewById(R.id.ib_back);
        textViewPrivacy = view.findViewById(R.id.tv_privacy);
        textViewHelp = view.findViewById(R.id.tv_help);
        textViewMyOrders = view.findViewById(R.id.tv_my_orders);
        textViewSettings = view.findViewById(R.id.tv_settings);
    }

    public void setOnClickListener() {
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), SinInActivity.class);
                                startActivity(intent);
                                requireActivity().finish(); // close current activity
                                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null) // dismiss dialog if No
                        .show();
            }
        });


        textViewMyProfile.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), MyProfileActivity.class);
            startActivity(intent);
        });

        textViewPrivacy.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), PrivacyPolicyActivity.class);
            startActivity(intent);

        });

        textViewHelp.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HelpCenterActivity.class);
            startActivity(intent);
        });

        textViewMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyOrdersActivity.class);
            startActivity(intent);
        });

        textViewSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingActivity.class);
            startActivity(intent);
        });


        imageButtonBack.setOnClickListener(v -> showExitConfirmation());


    }// end of setOnClickListener

    public void getData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users profile").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("name").exists()) {
                        textViewName.setText(snapshot.child("name").getValue(String.class));
                    }
                    if (snapshot.child("image").exists()) {
                        String base64Image = snapshot.child("image").getValue(String.class);

                        if (base64Image != null) {
                            Bitmap bitmap = decodeBase64ToImage(base64Image);
                            imageViewItem.setImageBitmap(bitmap);

                            // also store the bitmap in the tag for later
                            imageViewItem.setTag(bitmap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }// end of getData

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void showExitConfirmation() {

        NavController navController = NavHostFragment.findNavController(this);
        //Go back without recreating HomeFragment
        navController.popBackStack();
        // navController.navigate(R.id.fragment_home);
    }// end of exit function
}


