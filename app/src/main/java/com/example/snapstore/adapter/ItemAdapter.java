package com.example.snapstore.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapstore.R;
import com.example.snapstore.fragment.HomeFragmentDirections;
import com.example.snapstore.model.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;
    private Fragment fragment;
    private DatabaseReference ref;



    public ItemAdapter(List<Item> itemList,Context context, Fragment fragment)
    {
        this.context = context;
        this.itemList = itemList;
        this.fragment = fragment;
        notifyDataSetChanged();
    }
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ItemViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewCategory.setText(item.getCategory());
        holder.textViewRating.setText(item.getRating());
        holder.textViewPrice.setText(item.getPrize());

        try {
            byte[] decodedBytes = Base64.decode(item.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            holder.imageViewItem.setImageBitmap(bitmap);

            //  give each image a random height
            ViewGroup.LayoutParams params = holder.imageViewItem.getLayoutParams();
            int minHeight = 300;  // in px
            int maxHeight = 400;
            params.height = minHeight + new Random().nextInt(maxHeight - minHeight + 1);
            holder.imageViewItem.setLayoutParams(params);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // use direction for transfer from one fragment to another
        String safePrize = item.getPrize() != null ? item.getPrize() : "N/A";
        String safeTitle = item.getTitle() != null ? item.getTitle() : "N/A";
        String safeRating = item.getRating() != null ? item.getRating() : "N/A";
        String imageToSend = item.getImage();
        holder.itemView.setOnClickListener(v -> {
            NavDirections action = HomeFragmentDirections
                    .actionHomeFragmentToDetailItemFragment(safeTitle, imageToSend,safeRating, safePrize);

            NavHostFragment.findNavController(fragment).navigate(action);
        });

        if (item.getIsFavorite()) {
            holder.imageViewHeart.setImageResource(R.drawable.ic_fav_dark2); // red heart
        } else {
            holder.imageViewHeart.setImageResource(R.drawable.ic_fav); // dark/unfilled
        }

        holder.imageViewHeart.setOnClickListener(v -> {
            item.setIsFavorite(!item.getIsFavorite());

            ref = FirebaseDatabase.getInstance()
                    .getReference("favorite")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(item.getTitle());
            ;
            if (item.getIsFavorite()) {

                Map<String, Object> favItem = new HashMap<>();
                favItem.put("title", item.getTitle());
                favItem.put("rating", item.getRating());
                favItem.put("price", item.getPrize());
                favItem.put("image", item.getImage());

                ref.setValue(favItem)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    );
            holder.imageViewHeart.setImageResource(R.drawable.ic_fav_dark2);
        } else {
            ref.removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    );
            holder.imageViewHeart.setImageResource(R.drawable.ic_heart_white);
        }

        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewCategory, textViewRating , textViewPrice;
        ImageView imageViewItem, imageViewHeart;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewCategory = itemView.findViewById(R.id.tv_category);
            textViewPrice = itemView.findViewById(R.id.tv_price);
            textViewRating = itemView.findViewById(R.id.tv_Rating);
            imageViewItem = itemView.findViewById(R.id.iv_item);
            imageViewHeart = itemView.findViewById(R.id.btn_heart);
        }
    }

}
