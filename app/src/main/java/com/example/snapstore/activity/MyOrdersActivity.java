package com.example.snapstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapstore.R;
import com.example.snapstore.adapter.CartAdapter;
import com.example.snapstore.adapter.MyOrderAdapter;
import com.example.snapstore.fragment.HomeFragment;
import com.example.snapstore.fragment.ProfileFragment;
import com.example.snapstore.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private MyOrderAdapter adapter;
    private List<CartItem> cartList = new ArrayList<>();
    private TextView textViewNoData;
    private ImageButton imageButtonBack;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setOnClickListener();
        loadData();
    }

    public void init()
    {
        recyclerView = findViewById(R.id.rv_item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyOrderAdapter(this, cartList);
        recyclerView.setAdapter(adapter);
        imageButtonBack = findViewById(R.id.ib_back);
        textViewNoData = findViewById(R.id.tv_no_data);
        progressBar = findViewById(R.id.pb_progress);

    }

    public void setOnClickListener()
    {
        imageButtonBack.setOnClickListener(v -> finish());

    }
    public void loadData()

    {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(uid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    for (DataSnapshot itemSnap : orderSnap.getChildren()) {
                        CartItem item = itemSnap.getValue(CartItem.class);
                        if (item != null) {
                            cartList.add(item);
                        }
                    }
                }


                if (cartList.isEmpty()) {
                    textViewNoData.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    textViewNoData.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }


    }