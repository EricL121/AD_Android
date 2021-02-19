package com.example.androidprototype;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidprototype.model.Recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.androidprototype.adpater.HomeAdapter;
import com.example.androidprototype.model.RecipeList;
import com.example.androidprototype.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.androidprototype.service.APIService;

public class HomeActivity extends AppCompatActivity
        implements View.OnClickListener{

    private RecyclerView rvHome;
    private HomeAdapter homeAdapter;
    private ArrayList<Recipe> recipeList;
    private User loggedUser;
    private int loggedId;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        APIService service = RetrofitClient.getRetrofitInstance().create(APIService.class);

        pref = getSharedPreferences("user_info", MODE_PRIVATE);
        loggedId = pref.getInt("UserId", 0);

        if (loggedId != 0) {
            Call<User> call1 = service.getUser(loggedId);
            call1.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        loggedUser = response.body();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    System.out.println("Fail to get user. redirect to login");
                }
            });
        }


        SearchView simpleSearchView = (SearchView) findViewById(R.id.simpleSearchView);
        simpleSearchView.setIconifiedByDefault(true);
        simpleSearchView.setQueryHint("Search recipes");

        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // do something on text submit
                Intent intent1 = new Intent(HomeActivity.this, HomeActivity.class);
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
        if (search != null && search.equals("SEARCH"))
        {
            //do something
            String query = intent.getStringExtra("query");
            System.out.println(query);

            Call<RecipeList> call = service.searchRecipes(query);


            call.enqueue(new Callback<RecipeList>() {

                @Override
                public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {

                    RecipeList recipes = response.body();
                    if (recipes != null) {
                        recipeList = recipes.getRecipelist();
                    }
                    if (recipeList.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No recipe Found", Toast.LENGTH_LONG).show();
                    }


                    // binding adpater and layout manager with steps recyclerview
                    rvHome = (RecyclerView) findViewById(R.id.HomeRecycler);
                    homeAdapter = new HomeAdapter(recipeList, HomeActivity.this, loggedUser);

                    rvHome.setAdapter(homeAdapter);
                    LinearLayoutManager lym_rs = new LinearLayoutManager(HomeActivity.this);
                    lym_rs.setStackFromEnd(false);
                    rvHome.setLayoutManager(lym_rs);
                    rvHome.addItemDecoration(new DividerItemDecoration(HomeActivity.this, DividerItemDecoration.VERTICAL));

                }

                @Override
                public void onFailure(Call<RecipeList> call, Throwable t) {
                    System.out.println(t.getMessage());
                    Toast.makeText(HomeActivity.this, "No recipes to show", Toast.LENGTH_SHORT).show();
                }
            });


        }
        else {


            Call<RecipeList> call = service.getAllRecipes();
            call.enqueue(new Callback<RecipeList>() {

                @Override
                public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {

                    RecipeList recipes = response.body();
                    if (recipes != null) {
                        recipeList = recipes.getRecipelist();
                    }


                    // binding adpater and layout manager with steps recyclerview
                    rvHome = (RecyclerView) findViewById(R.id.HomeRecycler);
                    homeAdapter = new HomeAdapter(recipeList, HomeActivity.this, loggedUser);

                    rvHome.setAdapter(homeAdapter);
                    LinearLayoutManager lym_rs = new LinearLayoutManager(HomeActivity.this);
                    lym_rs.setStackFromEnd(false);
                    rvHome.setLayoutManager(lym_rs);
                    rvHome.addItemDecoration(new DividerItemDecoration(HomeActivity.this, DividerItemDecoration.VERTICAL));

                }

                @Override
                public void onFailure(Call<RecipeList> call, Throwable t) {
                    System.out.println(t.getMessage());
                    Toast.makeText(HomeActivity.this, "No recipes to show", Toast.LENGTH_SHORT).show();
                }
            });
        }

        FloatingActionButton fab = findViewById(R.id.fabCreate);
        fab.setOnClickListener(this);

        /*Button test = findViewById(R.id.test);
        test.setOnClickListener(this);*/

        ImageButton home = findViewById(R.id.refreshHome);
        home.setOnClickListener(this);

        ImageButton groups = findViewById(R.id.groups);
        groups.setOnClickListener(this);

        ImageButton myProfile = findViewById(R.id.myProfile);
        myProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.fabCreate){
            if (loggedId == 0) {
                Toast.makeText(this, "Need to login to create a recipe", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(this, CreateRecipe.class);
                intent.setAction("CREATE_RECIPE");
                intent.putExtra("userId", loggedId);
                startActivity(intent);
            }
        }

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
            intent.putExtra("userId", pref.getInt("UserId", 0));
            intent.setAction("view");
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
    }
}