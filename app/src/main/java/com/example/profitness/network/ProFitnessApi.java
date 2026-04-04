package com.example.profitness.network;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ProFitnessApi {
    private final ApiClient apiClient;
    private final TokenStore tokenStore;

    public ProFitnessApi(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        this.apiClient = new ApiClient(tokenStore);
    }

    public void register(String name, String email, String password, ApiCallback<JsonObject> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);

        apiClient.post("/api/auth/register", body, false, parseAndStoreToken(callback));
    }

    public void login(String email, String password, ApiCallback<JsonObject> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiClient.post("/api/auth/login", body, false, parseAndStoreToken(callback));
    }

    public void getMe(ApiCallback<JsonObject> callback) {
        apiClient.get("/api/auth/me", true, parseJson(callback));
    }

    public void updateProfile(String name, Integer age, Integer heightCm, Integer weightKg, String goal, ApiCallback<JsonObject> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("age", age);
        body.put("heightCm", heightCm);
        body.put("weightKg", weightKg);
        body.put("goal", goal);

        apiClient.put("/api/users/me", body, true, parseJson(callback));
    }

    public void createWorkout(String workoutName, int durationMinutes, int caloriesBurned, ApiCallback<JsonObject> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("workoutName", workoutName);
        body.put("durationMinutes", durationMinutes);
        body.put("caloriesBurned", caloriesBurned);

        apiClient.post("/api/workouts", body, true, parseJson(callback));
    }

    public void addHydration(int amountMl, ApiCallback<JsonObject> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("amountMl", amountMl);

        apiClient.post("/api/hydration", body, true, parseJson(callback));
    }

    public void addNutrition(String mealName, int calories, int proteinGrams, int carbsGrams, int fatGrams, ApiCallback<JsonObject> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("mealName", mealName);
        body.put("calories", calories);
        body.put("proteinGrams", proteinGrams);
        body.put("carbsGrams", carbsGrams);
        body.put("fatGrams", fatGrams);

        apiClient.post("/api/nutrition", body, true, parseJson(callback));
    }

    public void getDashboardSummary(ApiCallback<JsonObject> callback) {
        apiClient.get("/api/dashboard/summary", true, parseJson(callback));
    }

    public void getExerciseLibrary(ApiCallback<JsonObject> callback) {
        apiClient.get("/api/exercises", false, parseJson(callback));
    }

    public void logout() {
        tokenStore.clearToken();
    }

    private ApiCallback<String> parseJson(ApiCallback<JsonObject> callback) {
        return new ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject json = apiClient.getGson().fromJson(result, JsonObject.class);
                callback.onSuccess(json);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        };
    }

    private ApiCallback<String> parseAndStoreToken(ApiCallback<JsonObject> callback) {
        return new ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject json = apiClient.getGson().fromJson(result, JsonObject.class);
                JsonObject data = json.getAsJsonObject("data");
                if (data != null && data.has("token")) {
                    tokenStore.saveToken(data.get("token").getAsString());
                }
                callback.onSuccess(json);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        };
    }
}
