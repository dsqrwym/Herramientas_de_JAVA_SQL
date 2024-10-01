package postgreSql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class ConectarBD {
    private static Connection connection = null;
    private static PreparedStatement st = null;
    private static ResultSet rs = null;
    private static String host;
    private static String port;
    private static String database;
    private static String user;
    private static String password;
    
    public static byte DDL_SQL = 0;
    public static byte DML_SQL = 1;

    private ConectarBD() {}

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
            Class.forName("org.postgresql.Driver");
            url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, user, password);
            valida = connection.isValid(5);
            if (valida && cerrar) {
                JOptionPane.showMessageDialog(null, "Conexion creada con exito");
                return valida;
            }else if(!valida) {
                JOptionPane.showMessageDialog(null, "No se ha podido establecer conexion", "INFO", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al conectar con la base de datos de PostgreSQL (" + url + "): " + e);
        } catch (ClassNotFoundException ex) {
            System.out.println("Error al registrar el driver de PostgreSQL: " + ex);
        } finally {
            if (cerrar) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	e.printStackTrace();
                    mensaje(e.getMessage(),"Error");
                }
            }
        }
        return valida;
    }

    public static void cerrarPreparedStatement(){
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                System.out.println("No ha podido cerrar la sentencia por " +e.getMessage());
            }
        }
    }

    public static void cerrarResultSet() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                mensaje("No ha podido cerrar ResultSet por "+e.getMessage());
            }
        }
    }


    public static ResultSet ejecutarSelect(String query, Object... valores) {
        boolean exito = connectDatabase(false);

        if (!exito) {
            return null;
        } else {
            try {
            	st = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

                for (int i = 0; i < valores.length; i++){
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
        }
        return rs;
    }

    public static boolean ejecutarQuerys(String query, int OP_SQL, Object... valores) {
        boolean exito = connectDatabase(false);
        boolean hecho = false;
        if (exito) {
            try {
                st = connection.prepareStatement(query);

                for (int i = 0; i < valores.length; i++){
                    Object valor = valores[i];
                    if (valor == null) {
                        st.setNull(i + 1, java.sql.Types.NULL);
                    } else {
                        st.setObject(i + 1, valor);
                    }
                }

            	int updateCount = st.executeUpdate();
                if(OP_SQL == DML_SQL) {
                	hecho = (updateCount > 0);
                }else {
                	hecho = (updateCount == 0);
                }
            } catch (SQLException e) {
                mensaje(e.getMessage());
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
            e.printStackTrace();
            mensaje(e.getMessage(),"Error");
        }
    }

    public static  String obtenerInformacionEnFila(String queryDeConsulta, Object... valores){
        StringBuilder contenidos = new StringBuilder();

        if (connectDatabase(false)){
            try {
                st = connection.prepareStatement(queryDeConsulta);

                for (int i = 0; i < valores.length; i++){
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

                //Para calcular el ancho maxcimo de cada columna
                //Inicializa con el tamanio de la columna
                int[] maxAnchoColumna = new int[numColumna];
                for (int i = 1; i<=numColumna; i++){
                    maxAnchoColumna[i-1] = metaData.getColumnName(i).length();
                }
                //Actualiza el tamanio depende del ancho de los valoresNoAceptables de cada columna
                while (rs.next()){
                    for (int i = 1; i<numColumna; i++){
                        //si es null no accede .length para evitar nullPoint
                        String valor = rs.getString(i);
                        if (valor != null){
                            maxAnchoColumna[i-1] = Math.max(maxAnchoColumna[i - 1], valor.length());
                        }
                    }
                }

                rs.beforeFirst();

                contenidos.append("Resultados: ").append("\n");
                for (int i = 1; i <= numColumna; i++){
                    contenidos.append(String.format("| %-" + maxAnchoColumna[i - 1] + "s |-", metaData.getColumnName(i)));
                }
                contenidos.append("\n");

                while (rs.next()){
                    for (int i = 1; i<=numColumna; i++){
                        String valor = rs.getString(i);
                        if (valor == null){
                            valor = "null";
                        }
                        contenidos.append(String.format("| %-" + maxAnchoColumna[i - 1] + "s |-", valor));
                    }
                    contenidos.append("\n");
                }
            } catch (SQLException e) {
                mensaje(e.getMessage());
            } finally {
                cerrarResultSet();
                cerrarPreparedStatement();
                ConectarBD.cerrarConnection();
            }
        }
        return contenidos.toString();
    }

    public static boolean estaConectado(){
        if (connection != null){
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
        if(connectDatabase(false)) {
	        try {
	            DatabaseMetaData meta = connection.getMetaData();
	            rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
	            while (rs.next()) {
	                nombresTablas.add(rs.getString("TABLE_NAME"));
	            }
	        } catch (SQLException e) {
	            mensaje(e.getMessage());
	        } finally {
                cerrarResultSet();
	            ConectarBD.cerrarConnection();
	        }
	        return nombresTablas;
        }else {
        	return null;
        }
    }
    
    private static void mensaje(String string, String titulo) {
		JOptionPane.showInternalMessageDialog(null, string, titulo, JOptionPane.INFORMATION_MESSAGE);
	}
}
