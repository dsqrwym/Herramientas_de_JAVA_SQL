package mariadbSql;

import java.io.*;
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

    private ConectarBD() { //evitar instanciacio
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

    // Método para conectar a la base de datos
    public static boolean connectDatabase(boolean cerrar) {
        String url = "";
        boolean valida = false;
        try {
            // Cargar el driver de MariaDB
            Class.forName("org.mariadb.jdbc.Driver");
            // Crear la URL de conexión
            url = "jdbc:mariadb://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + password;
            // Establecer la conexión
            connection = DriverManager.getConnection(url);
            valida = connection.isValid(10); // Verificar si la conexión es válida
            if (valida && cerrar) {
                // Mostrar un mensaje de éxito si la conexión se ha creado correctamente
                JOptionPane.showMessageDialog(null, "Conexion creada con exito");
            } else if (!valida) {
                // Mostrar un mensaje si no se ha podido conectar
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
                    // Cerrar la conexión si se especifica
                    connection.close();
                } catch (SQLException e) {
                    mensaje(e.getMessage());
                }
            }
        }
        return valida;
    }

    // Cerrar PreparedStatement si está abierto
    public static void cerrarPreparedStatement() {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                System.out.println("No ha podido cerrar la sentencia por " + e.getMessage());
            }
        }
    }

    // Cerrar ResultSet si está abierto
    public static void cerrarResultSet() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                mensaje("No ha podido cerrar ResultSet por " + e.getMessage());
            }
        }
    }

    // Ejecutar una consulta SELECT en la base de datos
    public static ResultSet ejecutarSelect(String query, Object... valores) {
        boolean exito = (estaConectado() || connectDatabase(false));

        if (!exito) {
            return null;
        }

        try {
            // Preparar la consulta con los valores dados
            st = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            for (int i = 0; i < valores.length; i++) {
                Object valor = valores[i];
                if (valor == null) {
                    st.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    st.setObject(i + 1, valor);
                }
            }

            // Ejecutar la consulta
            rs = st.executeQuery();
        } catch (SQLException e) {
            mensaje(e.getMessage());
        }

        return rs;
    }

    // Verificar si una consulta es DDL (CREATE, DROP, ALTER)
    public static boolean isDDL(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return lowerCaseQuery.contains("create") || lowerCaseQuery.contains("drop") || lowerCaseQuery.contains("alter");
    }

    // Verificar si un registro existe basado en la consulta dada
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

    // Ejecutar una consulta DDL (CREATE, DROP, ALTER)
    public static boolean ejecutarDDL(String query, Object... valores) {
        return ejecutarQuery(query, true, valores);
    }

    // Ejecutar una consulta DML (INSERT, UPDATE, DELETE)
    public static boolean ejecutarDML(String query, Object... valores) {
        return ejecutarQuery(query, false, valores);
    }

    // Ejecutar una consulta genérica
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

    // Cerrar la conexión a la base de datos
    public static void cerrarConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            mensaje(e.getMessage());
        }
    }

    // Obtener información de una tabla basada en una consulta SELECT
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

    // Verificar si hay conexión a la base de datos
    public static boolean estaConectado() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void ejecutarSqlScript(String archivo, String codificacion) {
        if (archivo == null || archivo.isEmpty()) {
            //Validar que el archivo no sea nulo o vacio
            System.out.println("El archivo proporcionado es nulo o vacio.");
            return;
        }

        // Verificar si hay conexión con la base de datos o conectarse
        if (estaConectado() || connectDatabase(false)) {
            //Usar try-with-resources para el BufferedReader y Statement se cierre automaticamente y adecuadamente
            try (
                    Statement statement = connection.createStatement();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), codificacion))
            ) {
                StringBuilder query = new StringBuilder();
                String linea;
                int contador = 0; //Para hacer referencia a cada las sentencias del SQL

                while ((linea = reader.readLine()) != null) {
                    //Eliminar espacios en blanco al principio y final de la linea
                    linea = linea.trim();

                    //Si la linea no esta vacio o no es un comentario
                    if (!linea.isEmpty() && !linea.startsWith("--")){
                        query.append(linea);
                        //Si setencia SQL esta completa o no
                        if (linea.endsWith(";")){
                            //Aniade setencia y reinicial StringBuilder
                            statement.addBatch(query.toString());
                            query.setLength(0);
                            contador++;
                        }
                    }
                }

                if (contador > 0) {
                    int[] resultado = statement.executeBatch();

                    for (int i = 0; i < resultado.length; i++){
                        int resultadoSetencia = resultado[i];
                        if (resultadoSetencia == Statement.SUCCESS_NO_INFO){
                            System.out.printf("Setencia %d : Ejecutada con exito, pero se desconoce el numero de filas afectadas.%n", (i+1));
                        } else if (resultadoSetencia == Statement.EXECUTE_FAILED) {
                            System.out.printf("Setencia %d : Ejecucion fallada%n", (i+1));
                        } else {
                            System.out.printf("Setencia %d : Ejecutada con exito, afecto a %d filas.", (i+1), resultadoSetencia);
                        }
                    }
                }else {
                    System.out.println("No hay setencias SQL para ejecutar.");
                }

            } catch (SQLException e) {
                System.err.println("Error ejecutando el script SQL -> " + e.getMessage());
            } catch (FileNotFoundException e) {
                System.err.printf("Archivo no encontrado -> %s%n%s%n", archivo, e.getMessage());
            } catch (UnsupportedEncodingException e) {
                System.err.printf("Codificación no soportada -> %s%n%s%n ", codificacion, e.getMessage());
            } catch (IOException e) {
                System.err.printf("Error de I/O leyendo el archivo -> %s%n", e.getMessage());
            }
        } else {
            System.out.println("Base de datos desconectada!");
        }
    }

    public static ArrayList<String> obtenerNombresTablas() {
        ArrayList<String> nombresTablas = new ArrayList<>();// Lista para almacenar los nombres de las tablas
        if (estaConectado() || connectDatabase(false)) {
            try {
                DatabaseMetaData meta = connection.getMetaData();
                rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
                // Añadir el nombre de cada tabla a la lista
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

    // Mostrar un mensaje de error
    private static void mensaje(String string) {
        JScrollPane scrollPane = getScrollPane(string);
        JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
        // Crear JDialog para mostrar el JOptionPane
        JDialog dialog = optionPane.createDialog("Error");
        // Hacer que el diálogo esté siempre en la parte superior
        dialog.setAlwaysOnTop(true); // Mantener el diálogo encima de otros
        // Mostrar el diálogo
        dialog.setVisible(true);
        // JOptionPane.showInternalMessageDialog(null, string, "Error",JOptionPane.INFORMATION_MESSAGE);
    }


    // Crear un JScrollPane para mostrar mensajes en un cuadro de diálogo
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
