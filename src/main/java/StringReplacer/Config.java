package StringReplacer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter
public class Config {
    private static String jsonName = "StringReplacer_config.json";
    private Boolean darkTheme = Boolean.TRUE;
    private String replaceString = "";
    private String replaceToString = "";
    private String pathToFolder = "";

    static void saveToJson(Config config) throws IOException {
        Writer writer = Files.newBufferedWriter(Paths.get(jsonName));
        new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
        writer.close();
    }

    static Config readFromJson() throws IOException {
        if (!Files.exists(Paths.get(jsonName)))
            saveToJson(new Config());

        Reader reader = Files.newBufferedReader(Paths.get(jsonName));
        Config config = new Gson().fromJson(reader, Config.class);
        reader.close();
        return config;
    }
}
