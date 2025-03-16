import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookServer {
    private List<String> bookTitles; //List to store book title
    private ServerSocket serverSocket; //Socket listening for client connection


    //Modified BookServer method
    public BookServer(int port) {
        bookTitles = new ArrayList<>();
        loadBooksFromFile("books.txt");

        try {
            serverSocket = new ServerSocket(port); // Start the server on the specified port
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
            System.exit(1); //If server is unable to start
        }
    }

    //Method to load book titles from a file - file reading logic
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

    //Method to handle client requests
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine(); //Read the client request
            if (request == null) return; //Exit if empty

            System.out.println("Received request: " + request);

            //Request handling
            if (request.startsWith("GET")) {
                handleGetRequest(out);
            } else if (request.startsWith("ADD")) {
                handleAddRequest(in, out, request);
            } else {

                out.println("ERR");
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            //Closing of socket
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    //Method handling GET requests (No titles available
    private void handleGetRequest(PrintWriter out) {
        if (bookTitles.isEmpty()) {
            out.println("ERR");
        } else {
            Random random = new Random();
            String randomTitle = bookTitles.get(random.nextInt(bookTitles.size())); //Pick a random title
            out.println("OK");
            out.println(randomTitle);
        }
    }

    //Method handling ADD requests (Invalid commands or Authorization)
    private void handleAddRequest(BufferedReader in, PrintWriter out, String request) {
        String[] parts = request.split(" ");
        if (parts.length < 2) {
            out.println("NOTOK");
            return;
        }

        String username = parts[1];
        if (!isAuthorized(username)) {
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
            out.println("OK"); //Indicator for successfully added titles
        } catch (IOException e) {
            System.err.println("Error reading titles: " + e.getMessage());
            out.println("ERR"); // Error occurred
        }
    }

    //Method to check user authorization
    private boolean isAuthorized(String username) {
        //List of Authorized Users
        List<String> authorizedUsers = List.of("alice", "bob");
        return authorizedUsers.contains(username);
    }

    //Acceptance of connection and handling of the request - updated to persistent state
    public void start() {
        while (true) {
            try {
                System.out.println("Waiting for client connection...");
                Socket clientSocket = serverSocket.accept(); // Accept a client connection
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                handleClient(clientSocket); // Handle the client's request
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