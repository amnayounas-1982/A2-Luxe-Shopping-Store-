package com.example.snapstore.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapstore.R;
import com.example.snapstore.activity.MyOrdersActivity;
import com.example.snapstore.adapter.CartAdapter;
import com.example.snapstore.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList = new ArrayList<>();
    private TextView textViewSubtotal, textViewShipping, textViewTotal,textViewNoData;
    private ImageButton imageButtonBack;
    private ProgressBar progressBar;
    private LinearLayout linearLayoutTotal;
    private Button buttonCheckOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        init(view);
        setOnClickListener();
        loadData();
        updateTotals();
        return view;
    }
    public void init(View view)
    {
        recyclerView = view.findViewById(R.id.rv_item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(getContext(), cartList,this::updateTotals);
        recyclerView.setAdapter(adapter);
        textViewShipping = view.findViewById(R.id.tv_shipping_rate);
        textViewSubtotal = view.findViewById(R.id.tv_subtotal_rate);
        textViewTotal = view.findViewById(R.id.tv_total_rate);
        imageButtonBack = view.findViewById(R.id.ib_back);
        textViewNoData = view.findViewById(R.id.tv_no_data);
        progressBar = view.findViewById(R.id.pb_progress);
        linearLayoutTotal = view.findViewById(R.id.ll_total);
        buttonCheckOut = view.findViewById(R.id.btn_checkout);
    }

    public void setOnClickListener()
    {
        imageButtonBack.setOnClickListener(v -> showExitConfirmation());

        buttonCheckOut.setOnClickListener(v -> {
            DatabaseReference cartRef = FirebaseDatabase.getInstance()
                    .getReference("cart")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                    .getReference("orders")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String orderId = ordersRef.push().getKey();
                        if (orderId == null) return;

                        for (DataSnapshot itemSnap : snapshot.getChildren()) {
                            CartItem item = itemSnap.getValue(CartItem.class);
                            if (item != null) {
                                // Generate a unique itemId under this order
                                String itemId = ordersRef.child(orderId).push().getKey();

                                // Set IDs so they are stored in Firebase
                                item.setId(itemId);
                                item.setOrderId(orderId);

                                // Save item inside order
                                ordersRef.child(orderId).child(itemId).setValue(item);
                            }
                        }

                        // Clear cart after checkout
                        cartRef.removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getContext(), MyOrdersActivity.class));
                        });

                    } else {
                        Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
    public void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("cart")
                .child(uid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CartItem item = child.getValue(CartItem.class);
                    if (item != null) {
                        item.setId(child.getKey());
                        cartList.add(item);
                    }
                }

                if (cartList.isEmpty()) {
                    textViewNoData.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    linearLayoutTotal.setVisibility(View.GONE);
                } else {
                    textViewNoData.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    linearLayoutTotal.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
                updateTotals();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);

            }
        });


    }// end of load data
    private void updateTotals() {
        double subtotal = 0;
        for (CartItem item : cartList) {
            try {
                double price = Double.parseDouble(item.getPrice().replace("$", ""));
                subtotal += price * item.getQuantity();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        double shipping = 10.0;
        double total = subtotal + shipping;

        textViewSubtotal.setText(" $" + String.format("%.2f", subtotal));
        textViewShipping.setText(" $" + String.format("%.2f", shipping));
        textViewTotal.setText(" $" + String.format("%.2f", total));
    }// end of updateTotal

    private void showExitConfirmation() {

        NavController navController = NavHostFragment.findNavController(this);
        //Go back without recreating HomeFragment
        navController.popBackStack();
        // navController.navigate(R.id.fragment_home);
    }// end of exit function
}