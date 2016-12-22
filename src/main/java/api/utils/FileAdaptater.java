package api.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Created by loucass003 on 21/12/16.
 */
public class FileAdaptater implements JsonDeserializer<File>
{
    public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        return new File(json.getAsJsonPrimitive().getAsString());
    }
}
