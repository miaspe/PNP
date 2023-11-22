
package cadastroserver;
import java.io.ObjectInputStream;
import java.util.List;
import javax.swing.JTextArea;

public class ThreadClient implements Runnable {
    private final ObjectInputStream entrada;
    private final JTextArea textArea;

    public ThreadClient(ObjectInputStream entrada, JTextArea textArea) {
        this.entrada = entrada;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object objetoRecebido = entrada.readObject();

                if (objetoRecebido instanceof String) {
                    // Se o objeto recebido for uma String, adicione ao JTextArea
                    String mensagem = (String) objetoRecebido;
                    adicionarTexto(mensagem);
                } else if (objetoRecebido instanceof List) {
                  
                    List<?> listaProdutos = (List<?>) objetoRecebido;
                    for (Object item : listaProdutos) {
                        if (item instanceof String) {
                            String nomeProduto = (String) item;
                            adicionarTexto(nomeProduto);
                        } else if (item instanceof Integer) {
                            Integer quantidade = (Integer) item;
                            adicionarTexto("Quantidade: " + quantidade);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adicionarTexto(String texto) {
        textArea.append(texto + "\n");
    }
}
