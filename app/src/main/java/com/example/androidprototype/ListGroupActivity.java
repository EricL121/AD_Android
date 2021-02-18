package com.example.androidprototype;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidprototype.adpater.GroupAdapter;
import com.example.androidprototype.adpater.HomeAdapter;
import com.example.androidprototype.model.Group;
import com.example.androidprototype.model.GroupList;
import com.example.androidprototype.model.RecipeList;
import com.example.androidprototype.model.User;
import com.example.androidprototype.model.UserGroup;
import com.example.androidprototype.model.UserGroupList;
import com.example.androidprototype.service.APIService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListGroupActivity extends AppCompatActivity
        implements View.OnClickListener{

    private ArrayList<Group> myGroups;
    private RecyclerView rvGroup;
    private GroupAdapter groupAdapter;
    private int userId; // get from intent when coming in from userprofile
    private APIService service;
    private SharedPreferences pref;
    private Button btnShowAll;
    private String username;
    private TextView tvMg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_group);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        service = RetrofitClient.getRetrofitInstance().create(APIService.class);
        pref = getSharedPreferences("user_info", MODE_PRIVATE);
        btnShowAll = findViewById(R.id.btnShowAll);
        tvMg = findViewById(R.id.mG);

        // for user to view groups that other users have joined
        userId = getIntent().getIntExtra("userId", 0);

        // display the groups that the logged in user has joined
        if (userId == 0) {
            userId = pref.getInt("UserId", 0);
        }

        SearchView simpleSearchView = (SearchView) findViewById(R.id.simpleSearchView);
        simpleSearchView.setIconifiedByDefault(true);
        simpleSearchView.setQueryHint("Search groups");

        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // do something on text submit
                Intent intent1 = new Intent(ListGroupActivity.this, ListGroupActivity.class);
                intent1.setAction("SEARCH");
                intent1.putExtra("query", query);
                startActivity(intent1);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
// do something when text changes
                return false;
            }
        });

        Intent intent = getIntent();
        String search = intent.getAction();
        if (search != null && search.equals("SEARCH")) {
            if (search.equals("SEARCH")) {

                String query = intent.getStringExtra("query");
                System.out.println(query);

                Call<GroupList> call = service.searchGroups(query);

                if (tvMg != null) {
                    tvMg.setText("Results");
                }

                call.enqueue(new Callback<GroupList>() {

                    @Override
                    public void onResponse(Call<GroupList> call, Response<GroupList> response) {

                        GroupList groups = response.body();
                        if (groups != null) {
                            myGroups = groups.getGrouplist();
                        }

                        if (myGroups.size() > 0) {
                            // binding adpater and layout manager with steps recyclerview
                            rvGroup = (RecyclerView) findViewById(R.id.GroupRecycler);
                            groupAdapter = new GroupAdapter(myGroups, ListGroupActivity.this);

                            rvGroup.setAdapter(groupAdapter);
                            LinearLayoutManager lym_rs = new LinearLayoutManager(ListGroupActivity.this);
                            lym_rs.setStackFromEnd(false);
                            rvGroup.setLayoutManager(lym_rs);
                            rvGroup.addItemDecoration(new DividerItemDecoration(ListGroupActivity.this, DividerItemDecoration.VERTICAL));
                        }
                    }

                    @Override
                    public void onFailure(Call<GroupList> call, Throwable t) {
                        System.out.println(t.getMessage());
                        Toast.makeText(ListGroupActivity.this, "No groups to show", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        else if (getIntent().getAction() == "showAll") {
            getGroups();
        }

        else if (userId != 0) {
            getUserGroup(userId);
        }
        else {
            getGroups();
        }

        /*Button test = findViewById(R.id.test);
        test.setOnClickListener(this);*/

        ImageButton home = findViewById(R.id.refreshHome);
        home.setOnClickListener(this);

        ImageButton groups = findViewById(R.id.groups);
        groups.setOnClickListener(this);

        ImageButton myProfile = findViewById(R.id.myProfile);
        myProfile.setOnClickListener(this);

        Button cG = findViewById(R.id.createGroup);
        cG.setOnClickListener(this);

        btnShowAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        /*if (id == R.id.test) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }*/

        if (id == R.id.refreshHome) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setAction("REFRESH");
            startActivity(intent);
        }

        if (id == R.id.groups) {
            Intent intent = new Intent(this, ListGroupActivity.class);
            intent.putExtra("userId", userId);
            intent.setAction("showAll");
            startActivity(intent);
        }

        if (id == R.id.myProfile) {
            if (pref.getInt("UserId", 0) == 0) {
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(this, ViewUserProfile.class);
                startActivity(intent);
            }
        }

        if (id == R.id.createGroup) {
            SharedPreferences pref = getSharedPreferences("user_info", MODE_PRIVATE);
            if (pref.getInt("UserId", 0) != 0) {
                Intent intent = new Intent(this, CreateGroupActivity.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "Need to login to create groups", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
            }
        }

        if (id == R.id.btnShowAll) {
            Intent intent = new Intent(this, ListGroupActivity.class);
            intent.setAction("showAll");
            startActivity(intent);
        }

    }

    public void getUserGroup(int userId) {
        Call<User> call = service.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    username = user.getUsername();

                    List<UserGroup> ug = user.getGroups().getUsergrouplist();
                    if (ug.size() > 0) {
                        myGroups = new ArrayList<>();

                        for (UserGroup u : ug) {
                            myGroups.add(u.getGroup());
                        }

                        // binding adpater and layout manager with steps recyclerview
                        rvGroup = (RecyclerView) findViewById(R.id.GroupRecycler);
                        groupAdapter = new GroupAdapter(myGroups, ListGroupActivity.this);

                        rvGroup.setAdapter(groupAdapter);
                        LinearLayoutManager lym_rs = new LinearLayoutManager(ListGroupActivity.this);
                        lym_rs.setStackFromEnd(false);
                        rvGroup.setLayoutManager(lym_rs);
                        rvGroup.addItemDecoration(new DividerItemDecoration(ListGroupActivity.this, DividerItemDecoration.VERTICAL));
                    }
                   if (tvMg != null) {
                       tvMg.setText(username + "'s Groups");
                   }
//                    if (btnShowAll != null) {
//                        btnShowAll.setVisibility(View.GONE);
//                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(ListGroupActivity.this, "No groups to show", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getGroups() {
        Call<GroupList> call = service.getAllGroups();
        call.enqueue(new Callback<GroupList>() {
            @Override
            public void onResponse(Call<GroupList> call, Response<GroupList> response) {
                if (response.isSuccessful()) {
                    ArrayList<Group> ug = response.body().getGrouplist();
                    if (ug.size() > 0) {

                        // binding adpater and layout manager with steps recyclerview
                        rvGroup = (RecyclerView) findViewById(R.id.GroupRecycler);
                        groupAdapter = new GroupAdapter(ug, ListGroupActivity.this);

                        rvGroup.setAdapter(groupAdapter);
                        LinearLayoutManager lym_rs = new LinearLayoutManager(ListGroupActivity.this);
                        lym_rs.setStackFromEnd(false);
                        rvGroup.setLayoutManager(lym_rs);
                        rvGroup.addItemDecoration(new DividerItemDecoration(ListGroupActivity.this, DividerItemDecoration.VERTICAL));

                        if (btnShowAll != null) {
                            btnShowAll.setVisibility(View.GONE);
                        }

                        if (tvMg != null ) {
                            tvMg.setText("All Groups");
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Not able to show group. Please try again later", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GroupList> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Not able to show group. Please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }
}