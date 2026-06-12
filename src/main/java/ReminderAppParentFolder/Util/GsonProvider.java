package ReminderAppParentFolder.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

public class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls() // CRITICAL: Forces Gson to write the file even if some fields are null
            .setPrettyPrinting()
            .create();

    public static Gson getGson() {
        return GSON;
    }
}