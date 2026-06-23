package com.example.snapstore.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartList;
    private Context context;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }
    public CartAdapter(Context context, List<CartItem> cartList,  OnQuantityChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;

    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText("$"+ item.getPrice());
        holder.tvQuantity.setText(String.valueOf( item.getQuantity()));
        holder.tvRating.setText("⭐ " + item.getRating());

        // Decode image
        try {
            byte[] decodedBytes = Base64.decode(item.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.ivItem.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.ibPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            if (listener != null) listener.onQuantityChanged();
        });

        holder.ibMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                if (listener != null) listener.onQuantityChanged();
            }
        });

        holder.ibDelete.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("cart")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(item.getId());

            ref.removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Removed from Cart", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvQuantity, tvRating;
        ImageView ivItem;
        ImageButton ibMinus, ibPlus,ibDelete;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ivItem = itemView.findViewById(R.id.iv_item1);
            ibMinus = itemView.findViewById(R.id.ib_minus);
            ibPlus = itemView.findViewById(R.id.ib_plus);
            ibDelete = itemView.findViewById(R.id.ib_delete);
        }
    }
}

