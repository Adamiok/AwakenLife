package me.adamiok.awakenlife.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidParameterException;
import java.util.UUID;

public class MojangApi {
    
    /**
     * Uses the Mojang API to convert names to uuid
     * <strong>Use this in an async thread only! To prevent lag and crashes.</strong>
     * @param name The player name to convert
     * @return UUID of the player
     * @throws IOException If Mojang api is down or connection failed
     * @throws InvalidParameterException If name is not valid
     */
    public static UUID playerNameToUuid(String name) throws IOException {
        try {
            if (name.length() < 3) {
                throw new InvalidParameterException("Name not valid");
            }
            if (name.length() > 16) {
                throw new InvalidParameterException("Name not valid");
            }
            char[] charName = name.toCharArray();
            for (char ch : charName) {
                if (ch == '_') { continue; }
                if (Character.isLetter(ch)) { continue; }
                if (Character.isDigit(ch)) { continue; }
                throw new InvalidParameterException("Name not valid");
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            if (jsonObject == null) {
                throw new IOException("Mojang servers down");
            }
            JsonElement id = jsonObject.get("id");
            if (id == null) {
                throw new InvalidParameterException("Name not valid");
            }
            StringBuilder builder = new StringBuilder(id.toString().replaceAll("\"", "").trim());
            // Backwards adding to avoid index adjustments
            try {
                builder.insert(20, "-");
                builder.insert(16, "-");
                builder.insert(12, "-");
                builder.insert(8, "-");
            } catch (StringIndexOutOfBoundsException ex) {
                throw new InvalidParameterException("Name not valid");
            }
            return UUID.fromString(builder.toString());
        } catch (InterruptedException e) {
            throw new IOException("Connection interrupted");
        }
    }
}
