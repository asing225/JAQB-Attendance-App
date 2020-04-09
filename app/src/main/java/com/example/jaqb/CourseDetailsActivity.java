package com.example.jaqb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jaqb.data.model.Course;
import com.example.jaqb.data.model.LoggedInUser;
import com.example.jaqb.services.FireBaseDBServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author amanjotsingh
 *
 * This activity class displays the details of the course and helps user register
 * for that course.
 * */

public class CourseDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private String courseCode;
    private String courseName;
    private String courseDays;
    private String courseInstructor;
    private String time;
    private TextView courseCodeTextView;
    private TextView courseNameTextView;
    private TextView courseDaysTextView;
    private TextView courseInstructorTextView;
    private TextView courseLocation;
    private TextView courseTime;
    private Button registerButton;
    private DialogInterface.OnClickListener dialogClickListener;
    private FireBaseDBServices fireBaseDBServices;
    private DatabaseReference databasereference;
    private LoggedInUser currentUser;
    private Course registerCourse;
    private List<Course> courseList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        courseCode = (String) getIntent().getCharSequenceExtra("code");
        courseName = (String) getIntent().getCharSequenceExtra("name");
        courseDays = (String) getIntent().getCharSequenceExtra("days");
        courseInstructor = (String) getIntent().getCharSequenceExtra("instructor");
        time = (String) getIntent().getCharSequenceExtra("time");
        String registered = (String) getIntent().getCharSequenceExtra("registered");

        courseCodeTextView = (TextView) findViewById(R.id.code);
        courseNameTextView = (TextView) findViewById(R.id.name);
        courseDaysTextView = (TextView) findViewById(R.id.days);
        courseInstructorTextView = (TextView) findViewById(R.id.instructor);
        courseLocation = (TextView) findViewById(R.id.location);
        courseTime = (TextView) findViewById(R.id.time);

        courseCodeTextView.setText(courseCode);
        courseNameTextView.setText(courseName);
        courseDaysTextView.setText(courseDays);
        courseInstructorTextView.setText(courseInstructor);
        courseLocation.setText("Poly AGBC 150");
        courseTime.setText(time);

        registerCourse = new Course();
        registerCourse.setDays(courseDays);
        registerCourse.setCode(courseCode);
        registerCourse.setInstructorName(courseInstructor);
        registerCourse.setCourseName(courseName);
        registerCourse.setTime(time);

        fireBaseDBServices = FireBaseDBServices.getInstance();
        databasereference = fireBaseDBServices.getReference();
        courseList = new ArrayList<>();
        courseList.addAll(fireBaseDBServices.getAllCourses());
        currentUser = fireBaseDBServices.getCurrentUser();


        registerButton = (Button) findViewById(R.id.register);
        if("true".equalsIgnoreCase(registered)){
            registerButton.setEnabled(false);
            Toast.makeText(this, "Already Registered", Toast.LENGTH_LONG).show();
        }
        else if("false".equalsIgnoreCase(registered)){
            registerButton.setOnClickListener(this);
        }

        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // update database with this course for that user
                        int res = fireBaseDBServices.registerCourse(registerCourse, currentUser);
                        Intent intent = new Intent();
                        if(res == 1){
                            updateDatabase();
                            registerCourse.setInstructorName(currentUser.getfName() + " " + currentUser.getlName());
                            currentUser.updateCourse(registerCourse);
                            System.out.println("Registered in new course");
                            intent.setClass(getApplicationContext(), MyCoursesActivity.class);
                            Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_LONG).show();

                        }
                        else if(res == 0){
                            System.out.println("Error in registering course");
                            intent.setClass(getApplicationContext(), CourseRegistrationActivity.class);
                            break;
                        };
                        startActivity(intent);
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + which);
                }
            }
        };
    }

    private void updateDatabase() {
        final String[] updateKey = new String[1];
        databasereference = FireBaseDBServices.getReference().child("Course");

        databasereference.orderByChild("code").equalTo(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childShot: dataSnapshot.getChildren()) {
                    updateKey[0] = childShot.child("code").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Boolean ans =  (updateKey[0] == null);
        Toast.makeText(getApplicationContext(), ans.toString(), Toast.LENGTH_LONG).show();
        //databasereference.child("Course").child(updateKey[0]).child("courseInstructor").setValue(currentUser.getfName() + " " + currentUser.getlName());
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        if((courseInstructor.equals(currentUser.getfName() + " " + currentUser.getlName())) &&  (currentUser.getLevel()).equals("INSTRUCTOR")) {
            Toast.makeText(getApplicationContext(), "You are already registered as instructor of this course", Toast.LENGTH_LONG).show();
        } else {
            builder.setMessage("Register for " + courseCode + "?").setPositiveButton("Register", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener).show();
        }
    }
}