package com.example.jaqb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jaqb.data.model.Course;
import com.example.jaqb.ui.courses.CourseAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseRegistrationActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener,
        ValueEventListener,
        AdapterView.OnItemClickListener {

    private DatabaseReference databaseReference;
    private ListView listView;
    private List<Course> courseList;
    private CourseAdapter courseAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_registration);
        courseList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Course");
        listView = (ListView) findViewById(R.id.course_list);
        searchView = (SearchView) findViewById(R.id.seach);
        courseAdapter = new CourseAdapter(this, courseList);
        listView.setAdapter(courseAdapter);
        databaseReference.addValueEventListener(this);
        searchView.setOnQueryTextListener(this);
        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        courseAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        List<String> keys = new ArrayList<String>();
        for(DataSnapshot keyNode : dataSnapshot.getChildren()){
            keys.add(keyNode.getKey());
            Course course = keyNode.getValue(Course.class);
            courseList.add(course);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, CourseDetailsActivity.class);
        intent.putExtra("code", (String) courseList.get((int) id).getCode());
        intent.putExtra("name", (String) courseList.get((int) id).getCourseName());
        intent.putExtra("instructor", (String) courseList.get((int) id).getInstructorName());
        intent.putExtra("days", (String) courseList.get((int) id).getDays());
        startActivity(intent);
    }
}
