package com.example.snapstore.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapstore.R;
import com.example.snapstore.activity.MyProfileActivity;
import com.example.snapstore.activity.OwnerHomeActivity;
import com.example.snapstore.adapter.ItemAdapter;
import com.example.snapstore.adapter.SliderAdapter;
import com.example.snapstore.model.Item;
import com.example.snapstore.util.NetworkUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HomeFragment extends Fragment {

private Button buttonAll, buttonWomen, buttonMen, buttonChildren;
private RecyclerView recyclerViewItemList;
private ItemAdapter itemAdapter;
private List<Item> itemList = new ArrayList<>();
private EditText editTextSearch;
private TextView textViewNoDataFound,textViewUserName;
private  List<Button> buttonList;
private StaggeredGridLayoutManager layoutManager ;
private DatabaseReference dbRef;
private ProgressBar progressBar;
private ImageView imageViewDrawer;
private DrawerLayout drawerLayout;
private ViewPager2 viewPager;
private  DatabaseReference favRef;
private FirebaseRemoteConfig mFirebaseRemoteConfig;
private String selectedCategory = null; // null = All


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = requireActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);
        setOnClickListener();
        banner();
        setupRecyclerView();
        showData("");
        getUserName();

        handleCategoryClick((AppCompatButton) buttonAll);

        return view;
    }

    public void init(View view)
    {
        buttonAll = view.findViewById(R.id.btn_all);
        buttonWomen = view.findViewById(R.id.btn_women);
        buttonMen = view.findViewById(R.id.btn_men);
        buttonChildren = view.findViewById(R.id.btn_children);
        recyclerViewItemList = view.findViewById(R.id.rv_item_list);
        buttonList = Arrays.asList(buttonAll, buttonWomen, buttonMen, buttonChildren);
        textViewNoDataFound = view.findViewById(R.id.tv_no_data);
        itemAdapter = new ItemAdapter(itemList, requireContext(),this);
        progressBar = view.findViewById(R.id.pb_progress);
        imageViewDrawer = view.findViewById(R.id.iv_bottom_drawer);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        textViewUserName = view.findViewById(R.id.tv_user_name);
        editTextSearch = view.findViewById(R.id.sv_search_bar);
        viewPager = view.findViewById(R.id.imageSlider);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // TabLayout tabLayout = view.findViewById(R.id.tabIndicator);
    }

    public void banner()
    {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String bannersJson = mFirebaseRemoteConfig.getString("banner_images");

                        try {
                            JSONArray jsonArray = new JSONArray(bannersJson);
                            List<String> bannerUrls = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                bannerUrls.add(jsonArray.getString(i));
                            }

                            // Use adapter with URLs
                            SliderAdapter adapter = new SliderAdapter(bannerUrls, requireContext());
                            viewPager.setAdapter(adapter);

                            //  Auto-scroll
                            final Handler handler = new Handler(Looper.getMainLooper());
                            final Runnable runnable = new Runnable() {
                                int currentPosition = 0;
                                @Override
                                public void run() {
                                    if (currentPosition == bannerUrls.size()) {
                                        currentPosition = 0;
                                    }
                                    viewPager.setCurrentItem(currentPosition++, true);
                                    handler.postDelayed(this, 3000); // 3 sec delay
                                }
                            };
                            handler.postDelayed(runnable, 3000);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void setupRecyclerView()
    {
        // Create custom StaggeredGridLayoutManager that disables vertical scrolling
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) {
                    @Override
                    public boolean canScrollVertically() {
                        return false; // Disable scroll, let NestedScrollView handle it
                    }
                };

        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        recyclerViewItemList.setLayoutManager(layoutManager);
        recyclerViewItemList.setNestedScrollingEnabled(false);
        recyclerViewItemList.setHasFixedSize(true); // allow dynamic height
        recyclerViewItemList.setAdapter(itemAdapter);

    }

    // upload data from realtime database
    public void showData(String category)
    {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewItemList.setVisibility(View.GONE);

        itemList.clear();
        itemAdapter.notifyDataSetChanged();



        if (NetworkUtil.isConnected(requireContext())) {

            dbRef = FirebaseDatabase.getInstance().getReference("items");
            if (category != null && !category.isEmpty()) {
                dbRef = dbRef.child(category);
            }
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progressBar.setVisibility(View.GONE);
                    itemList.clear();

                    // Case: All category
                    if (category == null || category.isEmpty()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            for (DataSnapshot itemSnap : child.getChildren()) {
                                Item item = itemSnap.getValue(Item.class);

                                if (item != null) {
                                    item.setIsFavorite(false);
                                    itemList.add(item);
                                }
                            }
                        }
                    } else {
                        // Case: Specific category
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Item item = child.getValue(Item.class);
                            if (item != null) {
                                item.setIsFavorite(false);
                                itemList.add(item);
                            }
                        }
                    }


                    if (itemList.isEmpty()) {
                        textViewNoDataFound.setVisibility(View.VISIBLE);
                        recyclerViewItemList.setVisibility(View.GONE);
                    } else {
                        textViewNoDataFound.setVisibility(View.GONE);
                        recyclerViewItemList.setVisibility(View.VISIBLE);
                    }

                    favRef = FirebaseDatabase.getInstance()
                            .getReference("favorite")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                            for (Item item : itemList) {
                                if (favSnapshot.hasChild(item.getTitle())) {
                                    item.setIsFavorite(true);
                                }
                            }
                            itemAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);

                }
            });
        }
        else {
            Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();

        }
    }// end of showData


    public void setOnClickListener() {

        imageViewDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    Log.e("DrawerDebug", "DrawerLayout not found in this activity");
                }

            }
        });

       buttonAll.setOnClickListener(v -> {
           selectedCategory = null;
           showData(null); //  for all
           handleCategoryClick((AppCompatButton) v);
       });

       buttonWomen.setOnClickListener(v -> {
           selectedCategory = "Women";
           showData("Women");
           handleCategoryClick((AppCompatButton) v);
       });

       buttonMen.setOnClickListener(v -> {
           selectedCategory = "Men";
           showData("Men");
           handleCategoryClick((AppCompatButton) v);
       });

        buttonChildren.setOnClickListener(v -> {
            selectedCategory = "Children";
            showData("Children");
            handleCategoryClick((AppCompatButton) v);
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                    searchProducts(query);

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }// End of click listener

    public void getUserName()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("Name").exists()) {
                        textViewUserName.setText(snapshot.child("Name").getValue(String.class));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    } // end of getUserName

    private void searchProducts(String query) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("items");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();

                String searchQuery = query.toLowerCase().trim();

                if (selectedCategory == null) {
                    // 🔎 Search across ALL categories
                    for (DataSnapshot categorySnap : snapshot.getChildren()) { // Women, Men, Children
                        for (DataSnapshot itemSnap : categorySnap.getChildren()) {
                            Item product = itemSnap.getValue(Item.class);
                            if (product != null && product.getTitle() != null) {
                                if (searchQuery.isEmpty() ||
                                        product.getTitle().toLowerCase().contains(searchQuery)) {
                                    itemList.add(product);
                                }
                            }
                        }
                    }
                } else {
                    // 🔎 Search only in the selectedCategory
                    DataSnapshot categorySnap = snapshot.child(selectedCategory);
                    for (DataSnapshot itemSnap : categorySnap.getChildren()) {
                        Item product = itemSnap.getValue(Item.class);
                        if (product != null && product.getTitle() != null) {
                            if (searchQuery.isEmpty() ||
                                    product.getTitle().toLowerCase().contains(searchQuery)) {
                                itemList.add(product);
                            }
                        }
                    }
                }

                // Show/hide "no data"
                if (itemList.isEmpty()) {
                    textViewNoDataFound.setVisibility(View.VISIBLE);
                    recyclerViewItemList.setVisibility(View.GONE);
                } else {
                    textViewNoDataFound.setVisibility(View.GONE);
                    recyclerViewItemList.setVisibility(View.VISIBLE);
                }

                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Search", "Error: ", error.toException());
            }
        });
    } // end of searchProduct

    // Handel Colouring Schema
    private void handleCategoryClick(AppCompatButton selectedButton) {
        for (Button button : buttonList) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.custum_rounded_selected);
                button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.White));
                button.setCompoundDrawableTintList(ContextCompat.getColorStateList(requireContext(), R.color.White));


            } else {
                button.setBackgroundResource(R.drawable.custum_rounded);
                button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.buttonSelected));
                button.setCompoundDrawableTintList(ContextCompat.getColorStateList(requireContext(), R.color.buttonSelected));

            }
        }
    }// end of handleCategoryClick
}