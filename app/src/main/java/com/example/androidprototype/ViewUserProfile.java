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

    Button btnlogout;
    TextView tvUserProfileHeader;
    TextView tvNoOfRecipe;
    TextView tvNoOfGroup;
    int userId;
    SharedPreferences pref;

    User loggedinu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        pref = getSharedPreferences("user_info", MODE_PRIVATE);
        userId = getIntent().getIntExtra("userId", 0);
        int userIdd = pref.getInt("UserId",0);

        //apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);

        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });

        thread.start();*/

        //Call<User> call = apiService.getUser(pref.getInt("UserId", 0));
        //call.enqueue(new Callback<User>() {
        //    @Override
        //    public void onResponse(Call<User> call, Response<User> response) {
        //        user = response.body();
        //    }

       //     @Override
       //     public void onFailure(Call<User> call, Throwable t) {

      //      }
       // });


        int userIdAcc = pref.getInt("UserId", 0);
        APIService service = RetrofitClient.getRetrofitInstance().create(APIService.class);
        if (userIdAcc != 0) {
            Call<User> call1 = service.getUser(userIdAcc);
            call1.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        loggedinu = response.body();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    System.out.println("Fail to get user. redirect to login");
                }
            });
        }

        tvUserProfileHeader = findViewById(R.id.tvUserProfileHeader);
        tvNoOfRecipe = findViewById(R.id.tvNoOfRecipes);
        tvNoOfGroup = findViewById(R.id.tvNoOfGroup);
        btnlogout = findViewById(R.id.btnlogout);

        if (userId == 0) {
            userId = pref.getInt("UserId", 0);
        }
        if (userId != 0) {
            display(userId);
            if (getIntent().getIntExtra("userId", 0) != 0) {
                btnlogout.setVisibility(View.GONE);
            }
        }

        tvNoOfGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListGroupActivity.class);
                intent.putExtra("userId", userId);
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
        APIService service = RetrofitClient.getRetrofitInstance().create(APIService.class);
        Call<User> call = service.getUser(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
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


        HomeAdapter homeAdapter = new HomeAdapter(recipeList, ViewUserProfile.this, loggedinu);


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
}