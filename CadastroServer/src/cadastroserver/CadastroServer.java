package cadastroserver;

import controller.MovimentacaoJpaController;
import controller.PessoaJpaController;
import controller.ProdutoJpaController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.Movimentacao;
import model.Pessoa;
import model.Produto;

public class CadastroServer {
    //private static final int PORT = 4321; // Porta do servidor
   private static final int PORT = 12345; 

    public static void main(String[] args) {
        System.out.println("Servidor CadastroServer iniciado.");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Inicializa controladores
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("CadastroServerPU");
            ProdutoJpaController produtoController = new ProdutoJpaController(emf);
            MovimentacaoJpaController movimentoController = new MovimentacaoJpaController(emf);
            PessoaJpaController pessoaController = new PessoaJpaController(emf);

            while (true) {
                System.out.println("Aguardando conexão de cliente...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                // Cria uma thread para lidar com o cliente com os controladores
                ClientHandler clientHandler = new ClientHandler(clientSocket, produtoController, movimentoController, pessoaController);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final ProdutoJpaController produtoController;
        private final MovimentacaoJpaController movimentoController;
        private final PessoaJpaController pessoaController;

        public ClientHandler(Socket socket, ProdutoJpaController produtoController, MovimentacaoJpaController movimentoController, PessoaJpaController pessoaController) {
            this.socket = socket;
            this.produtoController = produtoController;
            this.movimentoController = movimentoController;
            this.pessoaController = pessoaController;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Autenticação
                String username = in.readLine();
                String password = in.readLine();

                if (validateCredentials(username, password)) {
                    out.println("Autenticação bem-sucedida. Aguardando comandos...");
                    while (true) {
                        String command = in.readLine();
                        if (command != null) {
                            if (command.equals("L")) {
                                // Enviar conjunto de produtos do banco de dados
                                sendProductList(out);
                            } else if (command.equals("E") || command.equals("S")) {
                                // Processar entrada (E) ou saída (S) de produtos
                                processMovement(command, in, out);
                            } else if (command.equals("X")) {
                                // Comando para sair
                                break;
                            }
                        }
                    }
                } else {
                    out.println("Credenciais inválidas. Conexão encerrada.");
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private boolean validateCredentials(String username, String password) {
            // Adicione sua lógica de validação de credenciais aqui
            // Você pode usar ctrlUsuario para verificar as credenciais do usuário no banco de dados
            return true; // Temporariamente, retorna true para fins de teste
        }

        private void sendProductList(PrintWriter out) {
            List<Produto> productList = produtoController.findProdutoEntities();
            out.println("Conjunto de produtos disponíveis:");

            for (Produto product : productList) {
                out.println(product.getNome());
            }

            // Adicione uma linha em branco para indicar o fim da lista
            out.println();
        }

        private void processMovement(String type, BufferedReader in, PrintWriter out) throws IOException, Exception {
            try {
                String personIdStr = in.readLine();
                String productIdStr = in.readLine();
                String quantityStr = in.readLine();
                String unitPriceStr = in.readLine();

                int personId = Integer.parseInt(personIdStr);
                int productId = Integer.parseInt(productIdStr);
                int quantity = Integer.parseInt(quantityStr);
                double unitPrice = Double.parseDouble(unitPriceStr);

                Pessoa person = pessoaController.findPessoa(personId);
                Produto product = produtoController.findProduto(productId);

                if (person != null && product != null) {
                    // Verificar a quantidade disponível para saída
                    if (type.equals("S") && product.getQuantidade() < quantity) {
                        out.println("Quantidade insuficiente para saída. Operação cancelada.");
                                            } else {
                        // Criar o objeto Movimento
                        Movimentacao movement = new Movimentacao();
                        movement.setIdPessoa(person);
                        movement.setIdProduto(product);
                        movement.setQuantidade(quantity);
                        movement.setValorUnitario(unitPrice);
                        movement.setTipo(type);

                        // Persistir o movimento usando o novo controlador movimentoController
                        movimentoController.create(movement);

                        // Atualizar a quantidade do produto de acordo com o tipo de movimento
                        if (type.equals("E")) {
                            product.setQuantidade(product.getQuantidade() + quantity);
                        } else if (type.equals("S")) {
                            product.setQuantidade(product.getQuantidade() - quantity);
                        }
                        produtoController.edit(product);

                        out.println("Operação concluída com sucesso.");
                    }
                } else {
                    out.println("Pessoa ou produto não encontrados. Operação cancelada.");
                }
            } catch (NumberFormatException e) {
                out.println("Entrada inválida. Operação cancelada.");
            }
        }
    }
}

                
