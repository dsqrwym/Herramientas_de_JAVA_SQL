package mariadbSql;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.io.Serial;

public class ConectarPanel extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    private static ConectarPanel instance;
    private final JPanel contentPane;
    private final JTextField ipInput;
    private final JTextField puertoInput;
    private final JTextField bdInput;
    private final JTextField userInput;
    private final JPasswordField passInput;
    private boolean conectado = false;

    public static ConectarPanel getInstance(FuncionesARealizar funcion) {
        if (instance == null) {
            instance = new ConectarPanel(funcion);
        }
        return instance;
    }

    @Override
    public void dispose() {
        super.dispose();
        instance = null;
    }
    private ConectarPanel(FuncionesARealizar funcion) {
    	setTitle("ConectarPanel");
    	this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

        setSize(450, 352);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        
		contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
		
        JLabel ipLabel = new JLabel("IP");
        ipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        ipLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
        ipLabel.setBounds(49, 11, 46, 28);
        contentPane.add(ipLabel);
		
        JLabel puertoLabel = new JLabel("PUERTO");
        puertoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        puertoLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
        puertoLabel.setBounds(10, 53, 85, 28);
        contentPane.add(puertoLabel);

		
        JLabel basesdadosLabel = new JLabel("BD");
        basesdadosLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        basesdadosLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
        basesdadosLabel.setBounds(10, 110, 85, 28);
        contentPane.add(basesdadosLabel);
        
        JLabel userLabel = new JLabel("USER");
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
        userLabel.setBounds(10, 162, 85, 28);
        contentPane.add(userLabel);
        
        JLabel passLabel = new JLabel("PASS");
        passLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
        passLabel.setBounds(10, 222, 85, 28);
        contentPane.add(passLabel);

		JButton conectarButton = new JButton("CONECTAR");
        conectarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ip = ipInput.getText();
                String puerto = puertoInput.getText();
                String BD = bdInput.getText();
                String user = userInput.getText();
                String pass = new String(passInput.getPassword());
                if (examinarCampos(ip, puerto, BD, user, pass)) {
                    ConectarBD.setHost(ip);
                    ConectarBD.setPort(puerto);
                    ConectarBD.setDatabase(BD);
                    ConectarBD.setUser(user);
                    ConectarBD.setPassword(pass);
                   
                    conectado = ConectarBD.connectDatabase(false);
                    if (conectado) {
                    	funcion.realizarFuncion();
                        dispose();
                    } else {
                        SwingUtilities.invokeLater(() -> errorMensaje("No se puede establecer la conexión"));
                    }
                }
            }
            
			private boolean examinarCampos(String ip, String puerto, String bD, String user, String pass) {
				if(ip.isBlank()) {
					errorMensaje("No has introducido IP");
					return false;
				}
				
				if(puerto.isBlank()) {
					errorMensaje("No has introducido PUERTO");
					return false;
				}
				
				if(bD.isBlank()) {
					errorMensaje("No has introducido BASES DE DADOS");
					return false;
				}
				
				if(user.isBlank()) {
					errorMensaje("No has introducido USER");
					return false;
				}
				
				if(pass.isBlank()) {
					errorMensaje("No has introducido PASSWORD");
					return false;
				}
				return true;
			}
		});

		conectarButton.setBounds(268, 274, 131, 28);
		contentPane.add(conectarButton);

		ipInput = new JTextField();
		ipInput.setBounds(140, 11, 260, 28);
		ipInput.setText("localhost");
		contentPane.add(ipInput);

		puertoInput = new JTextField();
		puertoInput.setColumns(10);
		puertoInput.setBounds(140, 53, 260, 28);
		puertoInput.setText("3306");
		contentPane.add(puertoInput);

		bdInput = new JTextField();
		bdInput.setColumns(10);
		bdInput.setBounds(140, 110, 260, 28);
		contentPane.add(bdInput);

		userInput = new JTextField();
		userInput.setColumns(10);
		userInput.setBounds(140, 162, 260, 28);
		userInput.setText("root");
		contentPane.add(userInput);

		passInput = new JPasswordField();
		passInput.setColumns(10);
		passInput.setBounds(140, 222, 260, 28);
		contentPane.add(passInput);
		
		setVisible(true);
	}

	private void errorMensaje(String string) {
		JOptionPane.showInternalMessageDialog(contentPane, string, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
