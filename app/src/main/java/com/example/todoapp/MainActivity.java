package com.example.todoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private EditText inputTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        inputTask = findViewById(R.id.inputTask);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        taskList = db.getAllTasks();
        adapter = new TaskAdapter(taskList, db);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = inputTask.getText().toString().trim();
                if (!title.isEmpty()) {
                    db.addTask(title);
                    taskList.clear();
                    taskList.addAll(db.getAllTasks());
                    adapter.notifyDataSetChanged();
                    inputTask.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}