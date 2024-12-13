import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class NoteAppTest {

    private static NoteApp server;

    @BeforeAll
    public static void setUp() throws IOException {
        server = new NoteApp();
        server.start();
    }

    @AfterAll
    public static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    private String sendGetRequest(String endpoint) throws IOException {
        URL url = new URL("http://localhost:8080" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        final InputStream in;
        if (connection.getResponseCode() >= 400) {
            in = connection.getErrorStream();
        }
        else {
            in = connection.getInputStream();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private String sendPostRequest(String endpoint, String content) throws IOException {
        URL url = new URL("http://localhost:8080" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(content.getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    @Test
    public void testCreateNote() throws IOException {
        String content = "This is a new note";
        String response = sendPostRequest("/notes/create", content);

        assertTrue(response.contains("This is a new note"));
    }

    @Test
    public void testGetAllNotes() throws IOException {
        sendPostRequest("/notes/create", "First note");
        sendPostRequest("/notes/create", "Second note");

        String response = sendGetRequest("/notes");

        assertTrue(response.contains("First note"));
        assertTrue(response.contains("Second note"));
    }

    @Test
    public void testGetNoteById() throws IOException {
        String createdNoteResponse = sendPostRequest("/notes/create", "Test note");
        long noteId = extractIdFromResponse(createdNoteResponse);

        String response = sendGetRequest("/notes/" + noteId);

        assertTrue(response.contains("Test note"));
    }

    @Test
    public void testGetNoteByInvalidId() throws IOException {
        String response = sendGetRequest("/notes/999");

        assertEquals("Note not found", response);
    }

    private long extractIdFromResponse(String response) {
        String idString = response.split(",")[0].split(":")[1].trim();
        return Long.parseLong(idString);
    }
}
