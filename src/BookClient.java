import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BookClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter a command (GET, ADD, or EXIT):");
            String command = scanner.nextLine().trim().toUpperCase();

            if (command.equals("EXIT")) {
                System.out.println("Exiting client.");
                break;
            }

            if (command.equals("GET") || command.startsWith("ADD")) {
                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    // Send command to server
                    if (command.equals("GET")) {
                        out.println("GET");
                        handleGetResponse(in);
                    } else if (command.startsWith("ADD")) {
                        handleAddRequest(scanner, in, out);
                    }
                } catch (IOException e) {
                    System.err.println("Error communicating with server: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid command. Please enter GET, ADD, or EXIT.");
            }
        }

        scanner.close();
    }

    private static void handleGetResponse(BufferedReader in) throws IOException {
        String response = in.readLine();
        if (response.equals("OK")) {
            String title = in.readLine();
            System.out.println("Received book title: " + title);
        } else {
            System.out.println("Error: " + response);
        }
    }

    private static void handleAddRequest(Scanner scanner, BufferedReader in, PrintWriter out) throws IOException {
        System.out.println("Enter username:");
        String username = scanner.nextLine().trim();
        out.println("ADD " + username);

        String response = in.readLine();
        if (response.equals("NOTOK")) {
            System.out.println("Error: You are not authorized to add titles.");
            return;
        }

        System.out.println("Enter book titles to add (one per line, blank line to finish):");
        while (true) {
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                out.println();
                break;
            }
            out.println(title);
        }

        response = in.readLine();
        if (response.equals("OK")) {
            System.out.println("Titles added successfully.");
        } else {
            System.out.println("Error: " + response);
        }
    }
}