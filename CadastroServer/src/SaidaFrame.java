/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author wfeli
 */
import javax.swing.*;
import java.awt.*;

public class SaidaFrame extends JDialog {
    public JTextArea texto;

    public SaidaFrame() {
        setTitle("Saída do Servidor");
        setBounds(100, 100, 400, 300);
        setModal(false); // Defina o status modal como false

        texto = new JTextArea();
        texto.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(texto);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    public JTextArea getTextArea() {
        return texto;
    }
}

