package postgreSql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class ConectarBD {
    private static Connection connection = null;
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

    public static ResultSet ejecutarSelect(String query) {
        boolean exito = connectDatabase(false);
        Statement st = null;
        ResultSet rs = null;
        if (!exito) {
            return null;
        } else {
            try {
            	st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = st.executeQuery(query);
            } catch (SQLException e) {
            	e.printStackTrace();
                mensaje(e.getMessage(),"Error");
            }
        }
        return rs;
    }

    public static boolean ejecutarQuerys(String query, int OP_SQL) {
        boolean exito = connectDatabase(false);
        Statement st = null;
        boolean hecho = false;
        if (exito) {
            try {
                st = connection.createStatement();
            	int updateCount = st.executeUpdate(query);
                if(OP_SQL == DML_SQL) {
                	hecho = (updateCount > 0);
                }else {
                	hecho = (updateCount == 0);
                }
            } catch (SQLException e) {
            	hecho = false;
            	e.printStackTrace();
                mensaje(e.getMessage(),"Error");
            } finally {
                try {
                    if (st != null) {
                        st.close();
                    }
                    cerrarConnection();
                } catch (SQLException e) {
                	e.printStackTrace();
                    mensaje(e.getMessage(),"Error");
                }
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
    
    public static ArrayList<String> obtenerNombresTablas() {
        ArrayList<String> nombresTablas = new ArrayList<>();
        if(connectDatabase(false)) {
	        ResultSet res = null;
	        try {
	            DatabaseMetaData meta = connection.getMetaData();
	            res = meta.getTables(null, null, "%", new String[]{"TABLE"});
	            while (res.next()) {
	                nombresTablas.add(res.getString("TABLE_NAME"));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            mensaje(e.getMessage(),"Error");
	        } finally {
	            if (res != null) {
	                try {
	                    res.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                    mensaje(e.getMessage(),"Error");
	                }
	            }
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
