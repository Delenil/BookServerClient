import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BookClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter a command (GET, ADD <username>, or EXIT):");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("EXIT")) {
                System.out.println("Exiting client.");
                break;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toUpperCase();

            if (command.equals("GET")) {

                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    out.println("GET");
                    handleGetResponse(in);

                } catch (IOException e) {
                    System.err.println("Error communicating with server: " + e.getMessage());
                }

            } else if (command.equals("ADD")) {

                if (parts.length < 2) {
                    System.out.println("Invalid ADD command. Usage: ADD <username>");
                    continue;
                }

                String username = parts[1]; // Extract username from the input
                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {


                    out.println("ADD " + username);
                    handleAddRequest(in, out, scanner);

                } catch (IOException e) {
                    System.err.println("Error communicating with server: " + e.getMessage());
                }

            } else {
                System.out.println("Invalid command. Please enter GET, ADD <username>, or EXIT.");
            }
        }

        scanner.close();
    }

    private static void handleGetResponse(BufferedReader in) throws IOException {
        String response = in.readLine();
        System.out.println("Server response: " + response);
        if (response.equals("OK")) {
            String title = in.readLine();
            System.out.println("Received book title: " + title);
        } else {
            System.out.println("Error: " + response);
        }
    }

    private static void handleAddRequest(BufferedReader in, PrintWriter out, Scanner scanner) throws IOException {
        String response = in.readLine();
        System.out.println("Server response: " + response);
        if (response.equals("NOTOK")) {
            System.out.println("Error: You are not authorized to add titles.");
            System.exit(1);
            return;
        }


        System.out.println("Enter book titles to add (one per line, blank line to finish):");
        while (true) {
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                out.println();
                break;
            }
            if (!title.isBlank()) {
                out.println(title);
            }
        }


        response = in.readLine();
        if (response.equals("OK")) {
            System.out.println("Titles added successfully.");
        } else {
            System.out.println("Error: " + response);
        }
    }
}