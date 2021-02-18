package com.example.androidprototype.adpater;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidprototype.CreateRecipe;
import com.example.androidprototype.HomeActivity;
import com.example.androidprototype.Login;
import com.example.androidprototype.R;
import com.example.androidprototype.RetrofitClient;
import com.example.androidprototype.ViewRecipe;
import com.example.androidprototype.ViewUserProfile;
import com.example.androidprototype.model.Recipe;
import com.example.androidprototype.model.RecipeTag;
import com.example.androidprototype.model.SavedRecipe;
import com.example.androidprototype.model.SavedRecipeJson;
import com.example.androidprototype.model.User;
import com.example.androidprototype.model.booleanJson;
import com.example.androidprototype.service.APIService;
import com.example.androidprototype.service.DownloadImageTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

public class HomeAdapter extends
        RecyclerView.Adapter<HomeAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView author;
        public ImageView mainMedia;
        public TextView duration;
        public TextView allergens;
        public TextView calories;
        public TextView tags;
        public Recipe recipe;
        public ImageView saveRecipe;
        public LinearLayout allergenContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recipetitlehome);
            author = itemView.findViewById(R.id.recipeownerhome);
            mainMedia = itemView.findViewById(R.id.recipepichome);
            duration = itemView.findViewById(R.id.recipedurationhome);
            allergens = itemView.findViewById(R.id.recipeallergyhome);
            calories = itemView.findViewById(R.id.recipecalorieshome);
            tags = itemView.findViewById(R.id.recipehometags);
            saveRecipe = itemView.findViewById(R.id.btnSaveRecipe);
            allergenContainer = itemView.findViewById(R.id.allergyContainer);

            itemView.findViewById(R.id.recipepichome).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ViewRecipe.class);
                    int i = getAdapterPosition();
                    intent.putExtra("RecipeId", recipeList.get(i).getId());
                    context.startActivity(intent);
                }
            });

            saveRecipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (user == null) {
                        Toast.makeText(context, "Need to login before saving a recipe", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, Login.class);
                        context.startActivity(intent);
                    }
                    else {
                        int i = getAdapterPosition();
                        int recipeId = recipeList.get(i).getId();

                        SavedRecipeJson savedRecipeJson = new SavedRecipeJson(user.getId(), recipeId);
                        saveSelectedRecipe(savedRecipeJson);
                    }
                }
            });

            author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    int userId = recipeList.get(i).getUserId();
                    Intent intent = new Intent(context, ViewUserProfile.class);
                    intent.putExtra("userId", userId);
                    context.startActivity(intent);
                }
            });
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getAuthor() {
            return author;
        }

        public ImageView getMainMedia() {
            return mainMedia;
        }

        public TextView getDuration() {
            return duration;
        }

        public TextView getAllergens() {
            return allergens;
        }

        public TextView getCalories() {
            return calories;
        }

        public TextView getTags() {
            return tags;
        }

        public ImageView getSaveRecipe() {
            return saveRecipe;
        }

        public void saveButtonswitcher() {
            /*new Thread(new Runnable() {
                @Override
                public void run() {*/
                    if (saveRecipe.getTag().toString().equalsIgnoreCase("unsaved")) {
                        saveRecipe.setImageDrawable(context.getDrawable(R.drawable.ic_saved));
                        saveRecipe.setTag("saved");
                    }
                    else {
                        saveRecipe.setTag("unsaved");
                        saveRecipe.setImageDrawable(context.getDrawable(R.drawable.ic_unsaved));
                    }
            /*    }
            }).start();*/
        }

        public void saveSelectedRecipe(SavedRecipeJson savedRecipeJson) {
            service = RetrofitClient.getRetrofitInstance().create(APIService.class);
            Call<booleanJson> call = service.saveRecipeList(savedRecipeJson);
            call.enqueue(new Callback<booleanJson>() {
                @Override
                public void onResponse(Call<booleanJson> call, Response<booleanJson> response) {
                    System.out.println("In response");
                    saveButtonswitcher();
                }

                @Override
                public void onFailure(Call<booleanJson> call, Throwable t) {
                    System.out.println("In failure");
                }
            });
        }
    }

    private ArrayList<Recipe> recipeList;
    private Context context;
    private APIService service;
    public User user;

    public HomeAdapter(ArrayList<Recipe> recipeList, Context context, User user) {
        this.recipeList = recipeList;
        this.context = context;
        this.user = user;
    }

    public HomeAdapter(ArrayList<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View homeView = inflater.inflate(R.layout.home_one_recipe, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(homeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {

        holder.getTitle().setText(recipeList.get(position).getTitle());
        holder.allergenContainer.setVisibility(View.GONE);

        if (user != null) {
            String loggedInUsername = user.getUsername();
            if (loggedInUsername.equalsIgnoreCase(recipeList.get(position).getUser().getUsername())) {
                holder.getSaveRecipe().setVisibility(View.GONE);
            }
            List<SavedRecipe> userSavedRecipe = user.getSavedRecipeList().getsavedRecipe();
            List<Integer> userSavedRecipeId = new ArrayList();
            for (SavedRecipe savedRecipe : userSavedRecipe) {
                int i = savedRecipe.getRecipeId();
                userSavedRecipeId.add(i);
            }
            int currentRecipeId = recipeList.get(position).getId();
            if (userSavedRecipeId.contains(currentRecipeId)) {
                holder.getSaveRecipe().setTag("saved");
                holder.saveRecipe.setImageDrawable(context.getDrawable(R.drawable.ic_saved));
            }
        }
        holder.getAuthor().setText("By " + recipeList.get(position).getUser().getUsername());
        //holder.getDuration().setText(Integer.toString(recipeList.get(position).getDurationInMins()) + "mins");

        int durationFlag = recipeList.get(position).getDurationInMins();

        switch (durationFlag) {
            case 1:
                holder.getDuration().setText("15mins");
                break;
            case 2:
                holder.getDuration().setText("15 ~ 30mins");
                break;
            case 3:
                holder.getDuration().setText("30 ~ 60mins");
                break;
            case 4:
                holder.getDuration().setText("> 60mins");
                break;
            default:
                holder.getDuration().setText(Integer.toString(recipeList.get(position).getDurationInMins()) + "mins");
                break;
        }

        holder.getCalories().setText(Integer.toString(recipeList.get(position).getCalories()) + " kcal");

        new DownloadImageTask((ImageView) holder.getMainMedia())
                .execute(recipeList.get(position).getMainMediaUrl());

        String warnings = "";
        String tags = "";
        List<RecipeTag> retag = recipeList.get(position).getRecipeTags().getRecipeTags();
        if (retag != null && !retag.isEmpty()) {
            for (RecipeTag r : retag)
            {
                if (r.getTagXXId() != null) {

                    tags += "#" + r.getTagXXId().getTagName() + "\t";

                    if (r.getAllergenTag())
                    {
                        warnings += r.getTagXXId().getWarning();
                    }
                }
            }
            holder.getTags().setText(tags);

            if (!warnings.isEmpty())
            {
                holder.allergenContainer.setVisibility(View.VISIBLE);
                holder.getAllergens().setText("May cause " + warnings);
            }
            else {
                holder.getAllergens().setText(warnings);
            }
        }


    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }
}
