package sqlite3Sql;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.Serial;

public class ConectarPanel extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    private final JLabel directorioLabel;
    private final JButton conectarButton;
    private boolean conectado = false;

    public ConectarPanel(FuncionesARealizar funcion) {
        setTitle("Conectar a Base de Datos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(!conectado) {
                    System.out.println("No has conectado Bases de Datos.");
                }
                funcion.realizarFuncion();
                dispose();
            }
        });
        setSize(450, 200);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JButton seleccionarButton = new JButton("Seleccionar Base de Datos");
        seleccionarButton.setBounds(100, 30, 250, 30);
        contentPane.add(seleccionarButton);

        directorioLabel = new JLabel("Directorio: No seleccionado");
        directorioLabel.setBounds(20, 80, 400, 30);
        contentPane.add(directorioLabel);

        conectarButton = new JButton("Conectar");
        conectarButton.setBounds(150, 120, 150, 30);
        conectarButton.setEnabled(false);
        contentPane.add(conectarButton);

        seleccionarButton.addActionListener(e -> seleccionarArchivo());

        conectarButton.addActionListener(e -> {
            conectado = ConectarBD.connectDatabase(false);
            if (conectado) {
                JOptionPane.showMessageDialog(ConectarPanel.this, "Conectado exitosamente a la Base de Datos.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                funcion.realizarFuncion();
                ConectarPanel.this.dispose(); // Cerrar la ventana después de la conexión exitosa.
            } else {
                ConectarPanel.this.errorMensaje("No se pudo establecer la conexión a la Base de Datos.");
            }
        });

        setVisible(true);
    }

    private void seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            if (compruebaArchivoDB(archivoSeleccionado)) {
                String directorio = archivoSeleccionado.getAbsolutePath();
                directorioLabel.setText("Directorio: " + directorio);
                directorioLabel.setToolTipText(directorio);
                ConectarBD.setDirectorio(directorio);
                conectarButton.setEnabled(true);
            } else {
                errorMensaje("El archivo seleccionado no es válido.");
                conectarButton.setEnabled(false);
            }
        }
    }

    private boolean compruebaArchivoDB(File archivo) {
        if (!archivo.exists()) {
            errorMensaje("El archivo no existe o falta permiso.");
            return false;
        }
        if (!archivo.getName().toLowerCase().endsWith(".db")) {
            errorMensaje("No es un archivo .db del SQLite.");
            return false;
        }
        return true;
    }

    private void errorMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}