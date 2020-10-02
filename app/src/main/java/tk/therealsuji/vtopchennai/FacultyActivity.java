package tk.therealsuji.vtopchennai;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class FacultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
}