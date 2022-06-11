package me.adamiok.awakenlife.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.adamiok.awakenlife.AwakenLife;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

public final class AwakenData {

    private static final String PATH = AwakenLife.getInstance().getDataFolder() + "/awakenData.json";

    public static void createData() {
        File file = new File(PATH);
        if (file.exists()) { return; }
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAlive(UUID uuid) {
        try {
            String id = uuid.toString();
            Reader reader = newBufferedReader(Path.of(PATH));

            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            if (jsonObject == null) { return true; }
            JsonElement killer = jsonObject.get(id);
            reader.close();
            return killer == null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getKiller(UUID uuid) {
        try {
            String id = uuid.toString();
            BufferedReader reader = newBufferedReader(Path.of(PATH));
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            if (jsonObject == null) {
                throw new NullPointerException("File is empty");
            }
            JsonElement killer = jsonObject.get(id);
            reader.close();
            if (killer == null) {
                throw new NullPointerException("Player is not stored in file");
            }
            return killer.toString().replaceAll("\"", "");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException in " + AwakenData.class.getSimpleName());
        }
    }

    public static void addPlayer(UUID player, String killer) {
        try {
            String id = player.toString();
            Path path = Path.of(PATH);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            // Create reader
            BufferedReader reader = newBufferedReader(path);

            // Get json object from file
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            if (jsonObject == null) { jsonObject = new JsonObject(); }
            if (jsonObject.get(id) != null) { return; }
            // Close reader and open writer
            reader.close();
            BufferedWriter writer = newBufferedWriter(path);

            // Add to json object
            jsonObject.addProperty(id, killer);
            // Write to file
            gson.toJson(jsonObject, writer);

            // Close, to make sure it is saved
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void removePlayer(UUID uuid) {
        try {
            String id = uuid.toString();
            Path path = Path.of(PATH);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            // Create reader
            BufferedReader reader = newBufferedReader(path);

            // Get json from file
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            // Close reader and open writer
            reader.close();
            BufferedWriter writer = newBufferedWriter(path);

            // Remove player from json
            jsonObject.remove(id);
            // Write to file
            gson.toJson(jsonObject, writer);

            // Close to make sure it is saved
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
