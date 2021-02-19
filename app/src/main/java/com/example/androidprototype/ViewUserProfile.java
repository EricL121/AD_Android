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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidprototype.adpater.HomeAdapter;
import com.example.androidprototype.service.APIService;
import com.example.androidprototype.adpater.RecipeUserProfileAdaptor;
import com.example.androidprototype.model.Recipe;
import com.example.androidprototype.model.User;
import com.example.androidprototype.service.DownloadImageTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewUserProfile extends AppCompatActivity {

    private Button btnlogout;
    private TextView tvUserProfileHeader;
    private TextView tvNoOfRecipe;
    private TextView tvNoOfGroup;
    private int viewUserId;
    private SharedPreferences pref;
    private User user;
    private User loggedUser;
    private int loggedUserId;
    private APIService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        pref = getSharedPreferences("user_info", MODE_PRIVATE);
        service = RetrofitClient.getRetrofitInstance().create(APIService.class);

        viewUserId = getIntent().getIntExtra("userId", 0);
        loggedUserId = pref.getInt("UserId", 0);

        if (loggedUserId != 0) {
            getLoggedUser();
        }

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        tvUserProfileHeader = findViewById(R.id.tvUserProfileHeader);
        tvNoOfRecipe = findViewById(R.id.tvNoOfRecipes);
        tvNoOfGroup = findViewById(R.id.tvNoOfGroup);
        btnlogout = findViewById(R.id.btnlogout);

        if (viewUserId != 0) {
            display(viewUserId);
            btnlogout.setVisibility(View.GONE);
        }
        else {
            display(loggedUserId);
        }

        tvNoOfGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListGroupActivity.class);
                int viewThisGroup;
                if (viewUserId == 0) {
                    viewThisGroup = loggedUserId;
                }
                else {
                    viewThisGroup = viewUserId;
                }
                intent.putExtra("userId", viewThisGroup);
                startActivity(intent);
            }
        });

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });

        ImageButton home = findViewById(R.id.refreshHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewUserProfile.this, HomeActivity.class);
                intent.setAction("REFRESH");
                startActivity(intent);
            }
        });

        ImageButton groups = findViewById(R.id.groups);
        groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewUserProfile.this, ListGroupActivity.class);
                intent.setAction("view");
                startActivity(intent);
            }
        });

        ImageButton myProfile = findViewById(R.id.myProfile);
        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pref.getInt("UserId", 0) == 0) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), ViewUserProfile.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void display(int userId) {
        Call<User> call = service.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    user = response.body();
                    String userName = response.body().getUsername();
                    int noOfRecipes = response.body().getRecipes().getRecipelist().size();
                    int noOfGroup = response.body().getGroups().getUsergrouplist().size();
                    tvUserProfileHeader.setText(userName + "'s profile");
                    tvNoOfRecipe.setText("Recipes: " + Integer.toString(noOfRecipes));
                    tvNoOfGroup.setText("Groups: " + Integer.toString(noOfGroup));

                    ArrayList<Recipe> recipeList = response.body().getRecipes().getRecipelist();

                    if (recipeList != null) {
                        displayRecipe(recipeList);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                System.out.println("onFailure");
            }
        });
    }

    public void displayRecipe(ArrayList<Recipe> recipeList) {
        HomeAdapter homeAdapter;
        if (loggedUser != null) {
            homeAdapter = new HomeAdapter(recipeList, ViewUserProfile.this, loggedUser);
        }
        else {
            homeAdapter = new HomeAdapter(recipeList, ViewUserProfile.this, user);
        }

        //RecipeUserProfileAdaptor adaptor = new RecipeUserProfileAdaptor(ViewUserProfile.this, 0);
        //adaptor.setData(recipeList);

        //ListView listview = findViewById(R.id.lvRecipes);

        RecyclerView recyclerView = findViewById(R.id.lvRecipes);

        if (recyclerView != null) {
            recyclerView.setAdapter(homeAdapter);
            LinearLayoutManager lym_rs = new LinearLayoutManager(ViewUserProfile.this);
            lym_rs.setStackFromEnd(false);
            recyclerView.setLayoutManager(lym_rs);
            recyclerView.addItemDecoration(new DividerItemDecoration(ViewUserProfile.this, DividerItemDecoration.VERTICAL));
            /*recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    int recipeId = recipeList.get(i).getId();
                    Intent intent = new Intent(ViewUserProfile.this, ViewRecipe.class);
                    intent.putExtra("RecipeId", recipeId);
                    startActivity(intent);
                }
            });*/
        }
    }
    public void getLoggedUser() {
        Call<User> call1 = service.getUser(pref.getInt("UserId", 0));
        call1.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    loggedUser = response.body();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
}