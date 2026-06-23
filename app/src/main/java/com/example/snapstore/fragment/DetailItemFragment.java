package com.example.snapstore.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailItemFragment extends Fragment {

    private ImageView imageViewModel, imageViewMinus,imageViewPlus;
    private TextView textViewTitle , textViewRating, textViewQuantity;
    private Button buttonCart,buttonSmall,buttonLarge,buttonMedium,buttonExtraLarge;
    private ImageButton imageButtonBack;
    private DetailItemFragmentArgs args;
    private int quantity = 1;
    private String size = "Small";
    private  DatabaseReference ref;
    private List<Button> buttonList;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_item, container, false);
        init(view);
        setOnClickListener();
        getData();
        return view;
    }

    public void init(View view)
    {
        imageViewModel = view.findViewById(R.id.iv_model);
        imageViewMinus = view.findViewById(R.id.iv_minus);
        imageViewPlus = view.findViewById(R.id.iv_plus);
        textViewTitle = view.findViewById(R.id.tv_title);
        textViewRating = view.findViewById(R.id.tv_rating);
        textViewQuantity = view.findViewById(R.id.tv_quantity);
        buttonCart = view.findViewById(R.id.btn_cart);
        imageButtonBack = view.findViewById(R.id.ib_back);
        buttonSmall = view.findViewById(R.id.btn_small);
        buttonMedium = view.findViewById(R.id.btn_medium);
        buttonLarge = view.findViewById(R.id.btn_large);
        buttonExtraLarge = view.findViewById(R.id.btn_extra_large);
        buttonList = Arrays.asList(buttonSmall, buttonLarge, buttonMedium, buttonExtraLarge);
        args = DetailItemFragmentArgs.fromBundle(getArguments());
        progressBar = view.findViewById(R.id.pb_progress);

    }

    public void setOnClickListener()
    {
        imageButtonBack.setOnClickListener(v -> showExitConfirmation());

        imageViewPlus.setOnClickListener(v -> {
            quantity++;
            textViewQuantity.setText(String.valueOf(quantity));

        });

        imageViewMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textViewQuantity.setText(String.valueOf(quantity));
            }

        });

        buttonSmall.setOnClickListener(v -> {
            handleCategoryClick((AppCompatButton) v);
            size = "Small";

        });

        buttonMedium.setOnClickListener(v -> {
            handleCategoryClick((AppCompatButton) v);
            size = "Medium";

        });

        buttonLarge.setOnClickListener(v -> {
            handleCategoryClick((AppCompatButton) v);
            size = "Large";

        });

        buttonExtraLarge.setOnClickListener(v -> {
            handleCategoryClick((AppCompatButton) v);
            size = "Extra Large";

        });

    }// end of click listener

    public void getData()
    {
        //get data using direction
        String title = args.getTitle();
        String rating = args.getRating();
        String prize = args.getPrize();
        String imageValue = args.getImageUrl();

        textViewTitle.setText(title);
        textViewRating.setText(rating);
        buttonCart.setText(getString(R.string.str_Add_to_cart) + prize);

        // Detect if URI or Drawable
        try {
            byte[] decodedBytes = Base64.decode(imageValue, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageViewModel.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            imageViewModel.setImageResource(R.drawable.ic_launcher_background); // fallback
        }

        buttonCart.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            addToCart(title,rating,prize,imageValue,size,quantity);
        });

    }// ed of getData

    public void addToCart(String title, String rating,String prize,String imageValue, String size,int quantity)
    {
         ref = FirebaseDatabase.getInstance()
                .getReference("cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        String key = title.trim().toLowerCase();;

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("title", title);
        cartItem.put("image", imageValue);
        cartItem.put("rating", rating);
        cartItem.put("price", prize);
        cartItem.put("size", size);

        ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                if (snapshot.exists()) {

                    // If product already exists, update the quantity
                    Long currentQty = snapshot.child("quantity").getValue(Long.class);
                    if (currentQty == null) currentQty = 0L;

                    ref.child(key).child("quantity").setValue(currentQty + 1)
                            .addOnSuccessListener(aVoid ->
                    Toast.makeText(getContext(), "Quantity updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                } else {
                    cartItem.put("quantity", 1);
                    ref.child(key).setValue(cartItem)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Added to Cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


    }// end of add to cart

    private void handleCategoryClick(AppCompatButton selectedButton) {
        for (Button button : buttonList) {
            if (button == selectedButton) {
                button.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.Black));
                button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.White));
            } else {
                button.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.White));
                button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.Black));
            }
        }
    }// end of handleCategoryClick
    private void showExitConfirmation() {

        NavController navController = NavHostFragment.findNavController(this);
        //Go back without recreating HomeFragment
        navController.popBackStack();
        // navController.navigate(R.id.fragment_home);
    }// end of exit function



}