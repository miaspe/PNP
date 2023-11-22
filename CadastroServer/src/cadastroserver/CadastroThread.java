package cadastroserver;

import controller.MovimentacaoJpaController;
import controller.PessoaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.Movimentacao;
import model.Pessoa;
import model.Produto;

public class CadastroThread extends Thread {
    private final ProdutoJpaController ctrlProduto;
    private final UsuarioJpaController ctrlUsuario;
    private final PessoaJpaController ctrlPessoa;
    private final MovimentacaoJpaController ctrlMovimento;
    private final Socket socket;
    private volatile boolean isRunning = true; 

    public CadastroThread(
        ProdutoJpaController ctrlProduto,
        UsuarioJpaController ctrlUsuario,
        PessoaJpaController ctrlPessoa,
        MovimentacaoJpaController ctrlMovimento,
        Socket socket
    ) {
        this.ctrlProduto = ctrlProduto;
        this.ctrlUsuario = ctrlUsuario;
        this.ctrlPessoa = ctrlPessoa;
        this.ctrlMovimento = ctrlMovimento;
        this.socket = socket;
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
                out.println("Conexão autorizada.");

                while (isRunning) { 
                    String command = in.readLine();
                    if (command != null) {
                        if (command.equals("L")) {
                            // Enviar conjunto de produtos
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
                out.println("Dados inválidos.");
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(CadastroThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Encerre a conexão e a thread
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateCredentials(String username, String password) {
        return true; // Temporariamente, retorna true para fins de teste
    }

    private void sendProductList(PrintWriter out) {
        List<Produto> productList = ctrlProduto.findProdutoEntities();
        out.println("Conjunto de produtos disponíveis:");

        for (Produto product : productList) {
            out.println(product.getNome());
        }
       
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

            Pessoa person = ctrlPessoa.findPessoa(personId);
            Produto product = ctrlProduto.findProduto(productId);

            if (person != null && product != null) {
                // Verificar a quantidade disponível para saída
                if (type.equals("S") && product.getQuantidade() < quantity) {
                    out.println("Operação cancelada.");
                } else {
                    // Criar o objeto Movimento
                    Movimentacao movement = new Movimentacao();
                    movement.setIdPessoa(person);
                    movement.setIdProduto(product);
                    movement.setQuantidade(quantity);
                    movement.setValorUnitario(unitPrice);
                    movement.setTipo(type);

                                      ctrlMovimento.create(movement);

                                      if (type.equals("E")) {
                        product.setQuantidade(product.getQuantidade() + quantity);
                    } else if (type.equals("S")) {
                        product.setQuantidade(product.getQuantidade() - quantity);
                    }
                    ctrlProduto.edit(product);

                    out.println("Operação realizada com sucesso.");
                }
            } else {
                out.println("Pessoa ou produto não encontrados.");
            }
        } catch (NumberFormatException e) {
            out.println("Operação cancelada.");
        }
    }

  
    public void stopThread() {
        isRunning = false;
    }
}


