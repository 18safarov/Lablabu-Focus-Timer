package lablabu.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lablabu.model.AppState;
import lablabu.model.Category;
import lablabu.model.Session;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StorageService {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String FILE_NAME = "data.json";

    public void save(AppState state) {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(state, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AppState load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new AppState();
        }

        try (Reader reader = new FileReader(FILE_NAME)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            if (jsonObject.has("categories")) {
                JsonArray categoriesArray = jsonObject.getAsJsonArray("categories");

                if (categoriesArray.size() > 0) {
                    JsonElement firstCategory = categoriesArray.get(0);

                    if (firstCategory.isJsonPrimitive() && firstCategory.getAsJsonPrimitive().isString()) {
                        return migrateOldFormat(jsonObject);
                    }
                }
            }

            return gson.fromJson(jsonObject, AppState.class);

        } catch (Exception e) {
            return new AppState();
        }
    }

    private AppState migrateOldFormat(JsonObject oldData) {
        AppState newState = new AppState();

        try {
            if (oldData.has("categories")) {
                JsonArray oldCategories = oldData.getAsJsonArray("categories");
                List<Category> newCategories = new ArrayList<>();
                String[] defaultColors = {"#79f5b0", "#65f7a1", "#9feacb", "#ffb86b", "#ff8c42", "#a29bfe"};

                for (int i = 0; i < oldCategories.size(); i++) {
                    String categoryName = oldCategories.get(i).getAsString();
                    String color = defaultColors[i % defaultColors.length];
                    newCategories.add(new Category(categoryName, color));
                }

                newState.setCategories(newCategories);
            }

            if (oldData.has("sessions")) {
                JsonArray sessionsArray = oldData.getAsJsonArray("sessions");
                List<Session> sessions = new ArrayList<>();

                for (JsonElement sessionElement : sessionsArray) {
                    JsonObject sessionObj = sessionElement.getAsJsonObject();
                    String category = sessionObj.get("category").getAsString();
                    long duration = sessionObj.get("durationSeconds").getAsLong();
                    String date = sessionObj.get("date").getAsString();
                    sessions.add(new Session(category, duration, date));
                }

                newState.setSessions(sessions);
            }

            if (oldData.has("streak")) {
                newState.setStreak(oldData.get("streak").getAsInt());
            }

            if (oldData.has("lastSessionDate")) {
                newState.setLastSessionDate(oldData.get("lastSessionDate").getAsString());
            }

            newState.setTheme("dark");
            save(newState);

            return newState;

        } catch (Exception e) {
            return new AppState();
        }
    }
}