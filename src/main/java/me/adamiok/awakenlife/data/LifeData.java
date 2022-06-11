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

public final class LifeData {
    
    private static Path PATH = Path.of(AwakenLife.getInstance().getDataFolder().getPath() + "/lifeData.json");
    
    public static void createData() {
        File file = PATH.toFile();
        if (file.exists()) { return; }
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isBanned(UUID uuid) {
        try {
            String id = uuid.toString();
            Reader reader = newBufferedReader(PATH);
    
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            if (jsonObject == null) { return false; }
            JsonElement jsonElement = jsonObject.get(id);
            reader.close();
            return !(jsonElement == null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static void addPlayer(UUID uuid) {
        try {
            String id = uuid.toString();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            BufferedReader reader = newBufferedReader(PATH);
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            if (jsonObject == null) { jsonObject = new JsonObject(); }
            if (jsonObject.get(id) != null) { return; }
            reader.close();
            BufferedWriter writer = newBufferedWriter(PATH);
            jsonObject.addProperty(id, 1);
            gson.toJson(jsonObject, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void removePlayer(UUID uuid) {
        try {
            String id = uuid.toString();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            BufferedReader reader = newBufferedReader(PATH);
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            reader.close();
            BufferedWriter writer = newBufferedWriter(PATH);
            jsonObject.remove(id);
            gson.toJson(jsonObject, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
