package com.example.snapstore.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.snapstore.R;
import com.example.snapstore.adapter.CartAdapter;
import com.example.snapstore.adapter.ItemAdapter;
import com.example.snapstore.model.CartItem;
import com.example.snapstore.model.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> favtList = new ArrayList<>();
    private StaggeredGridLayoutManager layoutManager;
    private ImageButton imageButtonBack;
    private TextView textViewNoData;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        init(view);
        setOnClickListener();
        loadData();
        return view;
    }

    public void init(View view)
    {
        recyclerView = view.findViewById(R.id.rv_item_list);
        layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        favtList = new ArrayList<>();
        adapter = new ItemAdapter(favtList, requireContext(),this);
        recyclerView.setAdapter(adapter);
        imageButtonBack = view.findViewById(R.id.ib_back);
        textViewNoData = view.findViewById(R.id.tv_no_data);
        progressBar = view.findViewById(R.id.pb_progress);

    }

    public void setOnClickListener()
    {
        imageButtonBack.setOnClickListener(v -> showExitConfirmation() );
    }

    public void loadData() {

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("favorite")
                .child(uid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favtList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {

                    Item item = child.getValue(Item.class);
                    if (item != null) {
                        favtList.add(item);
                        item.setIsFavorite(false);

                    }
                }
                if (favtList.isEmpty()) {
                    textViewNoData.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    textViewNoData.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }


                DatabaseReference favRef = FirebaseDatabase.getInstance()
                        .getReference("favorite")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                        for (Item item : favtList) {
                            if (favSnapshot.hasChild(item.getTitle())) {
                                // Better to use unique id if possible
                                item.setIsFavorite(true);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                         progressBar.setVisibility(View.GONE);
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);

            }
        });

    }// end of load data

    private void showExitConfirmation() {

        NavController navController = NavHostFragment.findNavController(this);
        //Go back without recreating HomeFragment
        navController.popBackStack();
        // navController.navigate(R.id.fragment_home);
    }// end of exit function
}