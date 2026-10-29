package com.abhay.bloodradar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abhay.bloodradar.api.AppConfig;
import com.bumptech.glide.Glide;
import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    private ImageView ivProfilePic;
    private TextView tvProfileInitial;
    private String authToken;
    private String currentUserId;
    private final OkHttpClient client = new OkHttpClient();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfilePic(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set navigation icon (back arrow) to white
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
        }

        TextView tvProfileInitial = findViewById(R.id.tvProfileInitial);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvCity = findViewById(R.id.tvCity);
        TextView tvBloodGroup = findViewById(R.id.tvBloodGroup);
        TextView tvUserType = findViewById(R.id.tvUserType);
        TextView tvBio = findViewById(R.id.tvBio);

        // Get user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");
        String userData = prefs.getString("user", null);

        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        View layoutProfileImage = findViewById(R.id.layoutProfileImage);

        if (layoutProfileImage != null) {
            layoutProfileImage.setOnClickListener(v -> openImagePicker());
        }


        if (userData != null) {
            try {
                JSONObject user = new JSONObject(userData);
                String name = user.optString("name", "User");

                // Set profile picture or initial
                String profilePic = user.optString("profilePic", "");
                if (profilePic != null && !profilePic.isEmpty()) {
                    ivProfilePic.setVisibility(View.VISIBLE);
                    tvProfileInitial.setVisibility(View.GONE);
                    String imageUrl = AppConfig.BASE_URL + profilePic;
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_circle_bg)
                            .circleCrop()
                            .into(ivProfilePic);
                } else {
                    ivProfilePic.setVisibility(View.GONE);
                    tvProfileInitial.setVisibility(View.VISIBLE);
                    if (!name.isEmpty()) {
                        tvProfileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    }
                }

                tvName.setText(name);
                tvPhone.setText(user.optString("phone", "-"));
                tvCity.setText(user.optString("city", "-"));
                tvBloodGroup.setText(user.optString("bloodGroup", "-"));

                String role = user.optString("role", "user");
                role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                tvUserType.setText(role);

                tvBio.setText(user.optString("bio", "-"));
            } catch (Exception e) {
                tvName.setText("Error loading profile");
            }
        } else {
            tvName.setText("No user data found");
        }

        // Contact Requests button — donor ke liye
        Button btnContactRequests = findViewById(R.id.btnContactRequests);
        btnContactRequests.setOnClickListener(v -> startActivity(
            new Intent(ProfileActivity.this, ContactRequestsActivity.class)
        ));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfilePic(Uri uri) {
        try {
            // Show loading or something if needed
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

            // Create temporary file from Uri
            File tempFile = createTempFile(uri);
            if (tempFile == null) return;

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("profilePic", tempFile.getName(),
                            RequestBody.create(tempFile, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url(AppConfig.BASE_URL + "/api/auth/profile-pic")
                    .addHeader("Authorization", "Bearer " + authToken)
                    .patch(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            String newPicUrl = json.getJSONObject("data").getString("profilePic");

                            // Update local storage (cached user data)
                            updateCachedUserPic(newPicUrl);

                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "✅ Profile updated!", Toast.LENGTH_SHORT).show();
                                // Reload image
                                ivProfilePic.setVisibility(View.VISIBLE);
                                tvProfileInitial.setVisibility(View.GONE);
                                Glide.with(ProfileActivity.this)
                                        .load(AppConfig.BASE_URL + newPicUrl)
                                        .circleCrop()
                                        .into(ivProfilePic);
                            });
                        } catch (Exception e) { e.printStackTrace(); }
                    } else {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "temp_profile.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateCachedUserPic(String newUrl) {
        try {
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String userData = prefs.getString("user", null);
            if (userData != null) {
                JSONObject user = new JSONObject(userData);
                user.put("profilePic", newUrl);
                prefs.edit().putString("user", user.toString()).apply();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

