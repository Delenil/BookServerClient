import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookServer {
    private List<String> bookTitles;
    private List<String> authorizedUsers;
    private ServerSocket serverSocket;

    public BookServer(int port) {
        bookTitles = new ArrayList<>();
        authorizedUsers = new ArrayList<>();
        loadBooksFromFile("books.txt");
        loadAuthorizedUsers("users.txt");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void loadBooksFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bookTitles.add(line);
            }
            System.out.println("Loaded " + bookTitles.size() + " book titles.");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void loadAuthorizedUsers(String filename) {
        authorizedUsers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                authorizedUsers.add(line.trim());
            }
            System.out.println("Loaded " + authorizedUsers.size() + " authorized users.");
        } catch (IOException e) {
            System.err.println("Error reading authorized users file: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine();
            if (request == null) return;

            System.out.println("Received request: " + request);

            if (request.startsWith("GET")) {
                handleGetRequest(out);
            } else if (request.startsWith("ADD")) {
                handleAddRequest(in, out, request);
            } else {
                out.println("ERR");
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleGetRequest(PrintWriter out) {
        if (bookTitles.isEmpty()) {
            out.println("ERR");
        } else {
            Random random = new Random();
            String randomTitle = bookTitles.get(random.nextInt(bookTitles.size()));
            out.println("OK");
            out.println(randomTitle);
        }
    }

    private void handleAddRequest(BufferedReader in, PrintWriter out, String request) throws IOException {
        String[] parts = request.split(" ");
        if (parts.length < 2) {
            out.println("NOTOK");

            return;
        }

        String username = parts[1];
        if (!isAuthorized(username)) {
            System.out.println("Authorization failed for user: " + username);
            out.println("NOTOK");
            return;
        }

        out.println("OK");

        try {
            String title;
            while ((title = in.readLine()) != null && !title.isEmpty()) {
                bookTitles.add(title);
                System.out.println("Added title: " + title);
            }
            out.println("OK");
        } catch (IOException e) {
            System.err.println("Error reading titles: " + e.getMessage());
            out.println("ERR");
        }
    }
    private boolean isAuthorized(String username) {
        return authorizedUsers.contains(username);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        BookServer server = new BookServer(12345);
        server.start();
    }
}