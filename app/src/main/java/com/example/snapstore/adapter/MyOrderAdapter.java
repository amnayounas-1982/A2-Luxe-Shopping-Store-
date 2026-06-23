package com.example.snapstore.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapstore.R;
import com.example.snapstore.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.CartViewHolder> {

    private List<CartItem> cartList;
    private Context context;

    public MyOrderAdapter(Context context, List<CartItem> cartList) {
        this.context = context;
        this.cartList = cartList;

    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_orders, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText("$"+ item.getPrice());
        holder.tvRating.setText("⭐ " + item.getRating());

        // Decode image
        try {
            byte[] decodedBytes = Base64.decode(item.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.ivItem.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.ibDelete.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String orderId = item.getOrderId();
            String itemId = item.getId();

            if (orderId == null || itemId == null) {
                Toast.makeText(context, "Missing orderId or itemId", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("orders")
                    .child(uid)
                    .child(orderId)
                    .child(itemId);

            ref.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Removed from Order List", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


    }



    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvQuantity, tvRating;
        ImageView ivItem;
        Button ibDelete;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ivItem = itemView.findViewById(R.id.iv_item1);
            ibDelete = itemView.findViewById(R.id.ib_delete);
        }
    }
}

