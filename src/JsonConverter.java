import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("serial")
public class JsonConverter extends JPanel {

    public static void main(String[] args) {

        JsonConverter jsonC = new JsonConverter();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(jsonC);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            convertJson(selectedFile.getAbsolutePath());
        }

    }

    private static void convertJson(String absolutePath) {

        String content = null;
        try (Scanner scanner = new Scanner(new File(absolutePath)).useDelimiter("\\Z")) {
            content = scanner.next();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Selected file not found!", "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        JsonObject json_root = (JsonObject) new JsonParser().parse(content);

        json_root = doChanges(json_root);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json_root);
        
        try {
            Files.write(Paths.get(absolutePath), prettyJson.getBytes());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not write file!", "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

    }

    private static JsonObject doChanges(JsonObject json_root) {

        if (json_root.has("rigidBodies")) {
            JsonElement rigidBody = json_root.get("rigidBodies");
            json_root.remove("rigidBodies");
            json_root.add("rigidBody", rigidBody);
        }

        if (json_root.get("rigidBody").isJsonArray()) {
            // God awful casting right here
            JsonElement rigidBody = ((JsonArray) json_root.get("rigidBody")).get(0);
            json_root.add("rigidBody", rigidBody);
        }

        JsonObject rigidBody_object = json_root.get("rigidBody").getAsJsonObject();
        if (rigidBody_object.has("imagePath")) {
            rigidBody_object.remove("imagePath");
            json_root.add("rigidBody", rigidBody_object);
        }

        if (rigidBody_object.has("name")) {
            rigidBody_object.remove("name");
            json_root.add("rigidBody", rigidBody_object);
        }

        return json_root;

    }

}
