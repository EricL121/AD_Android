package com.example.androidprototype;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidprototype.adpater.GroupAdapter;
import com.example.androidprototype.adpater.StepsAdapter;
import com.example.androidprototype.model.Recipe;
import com.example.androidprototype.model.booleanJson;
import com.example.androidprototype.service.APIService;
import com.example.androidprototype.model.RecipeIngredients;
import com.example.androidprototype.model.RecipeSteps;
import com.example.androidprototype.model.RecipeTag;
import com.example.androidprototype.service.DownloadImageTask;
import com.example.androidprototype.service.ViewRecipeIngredientAdapter;
import com.example.androidprototype.service.ViewRecipeStepAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewRecipe extends AppCompatActivity
    implements View.OnClickListener{

    private int rId;
    private Recipe recipe;
    private APIService service;
    private int loggedUserId;
    private RecyclerView rvSteps;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        // Need to change after user login is done
        pref = getSharedPreferences("user_info", MODE_PRIVATE);
        loggedUserId = pref.getInt("UserId", 0);

        Intent intent = getIntent();
        int recipeId = intent.getIntExtra("RecipeId",0);
        rId = recipeId;

        service = RetrofitClient.getRetrofitInstance().create(APIService.class);
        Call<Recipe> call = service.getRecipe(recipeId);

        /*Button back = findViewById(R.id.back);*/
        ImageButton edit = findViewById(R.id.edit);
        Button postToGrp = findViewById(R.id.post2Grp);
        ImageButton delete = findViewById(R.id.btnDeleteRecipe);

        // Prevent flickering
        postToGrp.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);

        /*back.setOnClickListener(this);*/
        edit.setOnClickListener(this);
        postToGrp.setOnClickListener(this);
        delete.setOnClickListener(this);

        ImageButton home = findViewById(R.id.refreshHome);
        home.setOnClickListener(this);

        ImageButton groups = findViewById(R.id.groups);
        groups.setOnClickListener(this);

        ImageButton myProfile = findViewById(R.id.myProfile);
        myProfile.setOnClickListener(this);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                recipe = response.body();
                TextView name = findViewById(R.id.recipeTitle);
                TextView description = findViewById(R.id.recipeDescription);
                TextView calories = findViewById(R.id.recipeCalories);
                TextView datecreated = findViewById(R.id.recipeDateCreated);
                TextView user = findViewById(R.id.recipeUser);
                TextView warning = findViewById(R.id.recipeWarning);
                TextView tag = findViewById(R.id.recipeTags);
                TextView duration = findViewById(R.id.recipeDuration);

                if (recipe.getUserId() == loggedUserId)
                {
                    postToGrp.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                }

                name.setText(recipe.getTitle());
                description.setText(recipe.getDescription());
                calories.setText(Integer.toString(recipe.getCalories()) + " kcal");
                datecreated.setText("Created on " + recipe.getDateCreated().toString());
                user.setText("By " + recipe.getUser().getUsername());

                int durationFlag = recipe.getDurationInMins();
                switch (durationFlag) {
                    case 1:
                        duration.setText("15mins");
                        break;
                    case 2:
                        duration.setText("15 ~ 30mins");
                        break;
                    case 3:
                        duration.setText("30 ~ 60mins");
                        break;
                    case 4:
                        duration.setText("> 60mins");
                        break;
                    default:
                        duration.setText(Integer.toString(durationFlag) + "mins");
                }

                new DownloadImageTask((ImageView) findViewById(R.id.recipeImage))
                        .execute(recipe.getMainMediaUrl());

                String warnings = "";
                String tags = "";
                List<RecipeTag> retag = recipe.getRecipeTags().getRecipeTags();
                for (RecipeTag r : retag)
                {
                    tags += "#" + r.getTagXXId().getTagName() + "\t";

                    if (r.getAllergenTag())
                    {
                        warnings += r.getTagXXId().getWarning();
                    }
                }
                tag.setText(tags);
                if (!warnings.isEmpty())
                {
                    warning.setText("May cause " + warnings);
                }
                else {
                    warning.setText(warnings);
                }




                List<RecipeIngredients> ingredients = recipe.getRecipeIngredients().getRecipeIngredients();

                if (ingredients != null)
                {
                    ViewRecipeIngredientAdapter adapter = new ViewRecipeIngredientAdapter(ViewRecipe.this, 0);
                    adapter.setData(ingredients);

                    ListView ingredlist = findViewById(R.id.ingredientList);
                    if (ingredlist != null)
                    {
                        ingredlist.setAdapter(adapter);
                        setListViewHeightBasedOnChildren(ingredlist);
                        ingredlist.setDivider(null);
                    }
                }

                List<RecipeSteps> steps = recipe.getRecipeSteps().getRecipeIngredients();

                if (steps != null)
                {
                    /*ViewRecipeStepAdapter adapter1 = new ViewRecipeStepAdapter(ViewRecipe.this, 0);
                    adapter1.setData(steps);

                    ListView steplist = findViewById(R.id.stepList);
                    if (steplist != null)
                    {
                        steplist.setAdapter(adapter1);
                        setListViewHeightBasedOnChildren(steplist);
                    }*/

                    rvSteps = (RecyclerView) findViewById(R.id.stepList);
                    StepsAdapter SA = new StepsAdapter(steps, ViewRecipe.this);

                    rvSteps.setAdapter(SA);
                    LinearLayoutManager lym_rs = new LinearLayoutManager(ViewRecipe.this);
                    lym_rs.setStackFromEnd(false);
                    rvSteps.setLayoutManager(lym_rs);
                    rvSteps.addItemDecoration(new DividerItemDecoration(ViewRecipe.this, DividerItemDecoration.VERTICAL));
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(ViewRecipe.this, "Unable to load recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        /*if (id == R.id.back) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setAction("nil");
            startActivity(intent);
        }*/

        if (id == R.id.edit) {
            Intent intent = new Intent(this, CreateRecipe.class);
            intent.setAction("EDIT_RECIPE");
            intent.putExtra("RecipeId", rId);
            startActivity(intent);
        }

        if (id == R.id.post2Grp) {
            Intent intent = new Intent(this, PostRecipeToGroupActivity.class);
            intent.putExtra("recipeId", rId);
            startActivity(intent);
        }

        if (id == R.id.btnDeleteRecipe) {
            int recipeId = recipe.getId();
            int recipeCreatedByUserId = recipe.getUserId();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure you want to delete this recipe?");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (loggedUserId == recipeCreatedByUserId) {
                        deleteRecipe(recipeId);
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

            /*if (userId == recipeCreatedByUserId) {
                deleteRecipe(recipeId);
            }*/
        }
        if (id == R.id.refreshHome) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setAction("REFRESH");
            startActivity(intent);
        }

        if (id == R.id.groups) {
            Intent intent = new Intent(this, ListGroupActivity.class);
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
    }

    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight=0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);

            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }


        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ((listView.getDividerHeight()) * (listAdapter.getCount()));

        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    public void deleteRecipe(int recipeId) {
        Call<booleanJson> call = service.deleteRecipe(recipeId);

        call.enqueue(new Callback<booleanJson>() {
            @Override
            public void onResponse(Call<booleanJson> call, Response<booleanJson> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), ViewUserProfile.class);
                    intent.putExtra("userId", loggedUserId);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<booleanJson> call, Throwable t) {
                System.out.println("Fail to delete");
            }
        });
    }
}