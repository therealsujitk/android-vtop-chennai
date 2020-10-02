package tk.therealsuji.vtopchennai;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
}