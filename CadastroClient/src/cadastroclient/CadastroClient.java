package cadastroclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CadastroClient {

    private static final String SERVER_ADDRESS = "localhost"; // Endereço do servidor
    //private static final int SERVER_PORT = 4321; // Porta do servidor
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Conectado ao servidor CadastroServer.");
            System.out.print("Digite seu nome de usuário: ");
            String username = consoleIn.readLine();
            System.out.print("Digite sua senha: ");
            String password = consoleIn.readLine();

            out.println(username);
            out.println(password);

            String response = in.readLine();
            System.out.println(response);

            if (response.equals("Autenticação bem-sucedida. Aguardando comandos...")) {
                while (true) {
                    System.out.print("Digite 'Listar' para apresentar produtos ou 'Sair' para sair: ");
                    String command = consoleIn.readLine();
                    out.println(command);

                    if (command.equalsIgnoreCase("Sair")) {
                        break;
                    }

                    if (command.equalsIgnoreCase("Listar")) {
                        // Receba e exiba todos os produtos de uma vez
                        receiveAndDisplayProductList(in);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveAndDisplayProductList(BufferedReader in) throws IOException {
        System.out.println("Conjunto de produtos");
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            System.out.println(line);
        }
    }
}
