package mariadbSql;

import java.sql.*;
import java.util.ArrayList;

import javax.swing.*;

public class ConectarBD {
    private static Connection connection = null;
    private static PreparedStatement st = null;
    private static ResultSet rs = null;
    private static String host;
    private static String port;
    private static String database;
    private static String user;
    private static String password;

    private ConectarBD() {
    }

    public static void setHost(String host) {
        ConectarBD.host = host;
    }

    public static void setPort(String port) {
        ConectarBD.port = port;
    }

    public static void setDatabase(String database) {
        ConectarBD.database = database;
    }

    public static void setUser(String user) {
        ConectarBD.user = user;
    }

    public static void setPassword(String password) {
        ConectarBD.password = password;
    }

    public static boolean connectDatabase(boolean cerrar) {
        String url = "";
        boolean valida = false;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            url = "jdbc:mariadb://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + password;
            connection = DriverManager.getConnection(url);
            valida = connection.isValid(10);
            if (valida && cerrar) {
                JOptionPane.showMessageDialog(null, "Conexion creada con exito");
            } else if (!valida) {
                JOptionPane.showMessageDialog(null, "No se ha podido establecer conexion", "INFO",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al conectar con la base de datos de Mariadb (" + url + "): " + e);
        } catch (ClassNotFoundException ex) {
            System.out.println("Error al registrar el driver de Mariadb: " + ex);
        } finally {
            if (cerrar) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    mensaje(e.getMessage());
                }
            }
        }
        return valida;
    }

    public static void cerrarPreparedStatement() {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                System.out.println("No ha podido cerrar la sentencia por " + e.getMessage());
            }
        }
    }

    public static void cerrarResultSet() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                mensaje("No ha podido cerrar ResultSet por " + e.getMessage());
            }
        }
    }

    public static ResultSet ejecutarSelect(String query, Object... valores) {
        boolean exito = (estaConectado() || connectDatabase(false));

        if (!exito) {
            return null;
        }

        try {
            st = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            for (int i = 0; i < valores.length; i++) {
                Object valor = valores[i];
                if (valor == null) {
                    st.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    st.setObject(i + 1, valor);
                }
            }

            rs = st.executeQuery();
        } catch (SQLException e) {
            mensaje(e.getMessage());
        }

        return rs;
    }

    public static boolean isDDL(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return lowerCaseQuery.contains("create") || lowerCaseQuery.contains("drop") || lowerCaseQuery.contains("alter");
    }

    public static boolean existe(String query, Object... parametros) {
        ResultSet resultSet = ejecutarSelect(query, parametros);
        try {
            if (resultSet != null) {
                return resultSet.next();
            } else {
                System.out.println("Error: La consulta no devolvió un resultado válido.");
            }
        } catch (SQLException e) {
            System.out.println("Error en la consulta SQL: " + e.getMessage());
        } finally {
            cerrarResultSet();
            cerrarPreparedStatement();
        }
        return false;
    }

    public static boolean ejecutarDDL(String query, Object... valores) {
        return ejecutarQuery(query, true, valores);
    }

    public static boolean ejecutarDML(String query, Object... valores) {
        return ejecutarQuery(query, false, valores);
    }

    private static boolean ejecutarQuery(String query, boolean isDDL, Object... valores) {
        boolean exito = (estaConectado() || connectDatabase(false));
        boolean hecho = false;

        if (exito) {
            try {
                st = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

                for (int i = 0; i < valores.length; i++) {
                    Object valor = valores[i];
                    if (valor == null) {
                        st.setNull(i + 1, java.sql.Types.NULL);
                    } else {
                        st.setObject(i + 1, valor);
                    }
                }

                int updateCount = st.executeUpdate();

                hecho = (isDDL) ? (updateCount == 0) : (updateCount > 0);

            } catch (SQLException e) {
                mensaje(e.getMessage());
                return false;
            } finally {
                cerrarPreparedStatement();
                cerrarConnection();
            }
        }
        return hecho;
    }

    public static void cerrarConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            mensaje(e.getMessage());
        }
    }

    public static String obtenerInformacionEnFila(String queryDeConsulta, Object... valores) {
        StringBuilder contenidos = new StringBuilder();

        if (estaConectado() || connectDatabase(false)) {
            try {
                st = connection.prepareStatement(queryDeConsulta, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                for (int i = 0; i < valores.length; i++) {
                    Object valor = valores[i];
                    if (valor == null) {
                        st.setNull(i + 1, java.sql.Types.NULL);
                    } else {
                        st.setObject(i + 1, valor);
                    }
                }
                rs = st.executeQuery();

                ResultSetMetaData metaData = rs.getMetaData();
                int numColumna = metaData.getColumnCount();

                // Para calcular el ancho maxcimo de cada columna
                // Inicializa con el tamanio de la columna
                int[] maxAnchoColumna = new int[numColumna];
                for (int i = 1; i <= numColumna; i++) {
                    maxAnchoColumna[i - 1] = metaData.getColumnLabel(i).length();
                }

                int filas = 0;
                // Actualiza el tamanio depende del ancho de los valoresNoAceptables de cada
                // columna
                while (rs.next()) {
                    for (int i = 1; i < numColumna; i++) {
                        // si es null no accede .length para evitar nullPoint
                        String valor = rs.getString(i);
                        if (valor != null) {
                            maxAnchoColumna[i - 1] = Math.max(maxAnchoColumna[i - 1], valor.length());
                        }
                    }
                    filas++;
                }

                if (filas == 0) {
                    contenidos.append("No hay resultado, fila total es 0\n");
                } else {
                    rs.beforeFirst();

                    contenidos.append("Resultados:\nNumero de FILAS: ").append(filas).append("\n");
                    for (int i = 1; i <= numColumna; i++) {
                        contenidos.append(
                                String.format("| %-" + maxAnchoColumna[i - 1] + "s |-", metaData.getColumnLabel(i)));
                    }
                    contenidos.append("\n");

                    while (rs.next()) {
                        for (int i = 1; i <= numColumna; i++) {
                            String valor = rs.getString(i);
                            if (valor == null) {
                                valor = "null";
                            }
                            contenidos.append(String.format("| %-" + maxAnchoColumna[i - 1] + "s |-", valor));
                        }
                        contenidos.append("\n");
                    }
                }
            } catch (SQLException e) {
                mensaje("Error al obtener información de la tabla: " + e.getMessage());
            } finally {
                cerrarResultSet();
                cerrarPreparedStatement();
                cerrarConnection();
            }
        }
        return contenidos.toString();
    }

    public static boolean estaConectado() {
        if (connection != null) {
            try {
                return !connection.isClosed();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static ArrayList<String> obtenerNombresTablas() {
        ArrayList<String> nombresTablas = new ArrayList<>();
        if (estaConectado() || connectDatabase(false)) {
            try {
                DatabaseMetaData meta = connection.getMetaData();
                rs = meta.getTables(null, null, "%", new String[] { "TABLE" });
                while (rs.next()) {
                    nombresTablas.add(rs.getString("TABLE_NAME"));
                }
            } catch (SQLException e) {
                mensaje(e.getMessage());
                return null;
            } finally {
                cerrarResultSet();
                cerrarConnection();
            }
            return nombresTablas;
        } else {
            return null;
        }
    }

    private static void mensaje(String string) {
        JScrollPane scrollPane = getScrollPane(string);
        JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
        // Crear JDialog para mostrar el JOptionPane
        JDialog dialog = optionPane.createDialog("Error");
        // Hacer que el diálogo esté siempre en la parte superior
        dialog.setAlwaysOnTop(true); // Mantener el diálogo encima de otros
        // Mostrar el diálogo
        dialog.setVisible(true);
        // JOptionPane.showInternalMessageDialog(null, string, "Error",
        // JOptionPane.INFORMATION_MESSAGE);
    }

    private static JScrollPane getScrollPane(String string) {
        JTextArea textArea = new JTextArea(string);
        textArea.setEditable(false); // El texto no se puede editar
        textArea.setWrapStyleWord(true); // Ajusta las palabras a la línea
        textArea.setLineWrap(true); // Habilita el ajuste de línea
        textArea.setCaretPosition(0); // Coloca el cursor al principio del texto

        // Envuelve el JTextArea en un JScrollPane para manejar textos largos
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 200)); // Ajusta el tamaño del cuadro de diálogo
        return scrollPane;
    }
}
