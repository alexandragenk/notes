import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import com.google.gson.Gson;

public class NoteApp {
    private static final Gson gson = new Gson();
    private final NoteService noteService = new NoteService();
    private final HttpServer server;

    public static void main(String[] args) throws IOException {
        new NoteApp().start();
    }

    public NoteApp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8083), 0);

        server.createContext("/notes", (HttpExchange exchange) -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Note> notes = noteService.getAllNotes();
                String response = gson.toJson(notes);

                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        server.createContext("/notes/", (HttpExchange exchange) -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath();
                long id = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
                noteService.getNoteById(id).ifPresentOrElse(note -> {
                    String response = gson.toJson(note);
                    try {
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, () -> {
                    try {
                        String response = "Note not found";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        server.createContext("/notes/create", (HttpExchange exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }

                String content = requestBody.toString();
                if (content != null && !content.isEmpty()) {
                    Note note = noteService.createNote(content);
                    String response = gson.toJson(note);
                    exchange.sendResponseHeaders(201, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    String response = "Content is required";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        });
    }

    public void start() {
        server.start();
        System.out.println("Server started at http://localhost:8080");
    }

    public void stop() {
        server.stop(1);
        System.out.println("Server stopped");
    }
}
