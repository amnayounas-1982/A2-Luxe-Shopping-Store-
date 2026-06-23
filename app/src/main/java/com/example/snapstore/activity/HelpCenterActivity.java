package com.example.snapstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snapstore.R;
import com.example.snapstore.fragment.ProfileFragment;

public class HelpCenterActivity extends BaseActivity {
    private ImageButton imageButtonBack;
    private AutoCompleteTextView autoCompleteTextViewCustomerService, autoCompleteTextViewWhatsapp;
    private String[] items = {"03258684548", "03298765438"};
    private String[] whatsapp = {"03258684548", "03298765438"};

    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapter1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_center);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setOnClickListener();
    }

    public void init()
    {
        imageButtonBack = findViewById(R.id.ib_back);
        autoCompleteTextViewCustomerService = findViewById(R.id.auto_dropdown_customer_services);
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                items
        );
        autoCompleteTextViewWhatsapp = findViewById(R.id.auto_dropdown_whatsapp);
        adapter1 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                whatsapp
        );

    }
    public void setOnClickListener()
    {
        imageButtonBack.setOnClickListener(v -> finish());
        autoCompleteTextViewCustomerService.setAdapter(adapter);
        autoCompleteTextViewCustomerService.setOnClickListener(v -> autoCompleteTextViewCustomerService.showDropDown());

        autoCompleteTextViewWhatsapp.setAdapter(adapter1);
        autoCompleteTextViewWhatsapp.setOnClickListener(v -> autoCompleteTextViewWhatsapp.showDropDown());



    }
}