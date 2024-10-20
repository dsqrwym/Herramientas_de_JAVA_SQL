package mariadbSql;
import java.util.Scanner;

public class ConectarConsola {
    private static final Scanner teclado = new Scanner(System.in);
    private ConectarConsola(){}
    public static boolean conectarBD(){
        System.out.println("Para connectar se necesita siguentes datos: ");

        do {
            ConectarBD.setHost(obtenerIP());
            ConectarBD.setPort(obtenerPuerto());
            ConectarBD.setDatabase(obtenerBD());
            ConectarBD.setUser(obtenerUser());
            ConectarBD.setPassword(obtenerPass());

            if (ConectarBD.connectDatabase(false)) {
                return true;
            }

        } while (respuestaSN("No ha podido conectar Bases de Dados.\n¿Quieres intentarlo de nuevo? (S/N)\n\tS --> Si\n\tN --> No"));
        return false;
    }

    private static String obtenerPass() {
        String respuestaPass;
        do {
            System.out.println("Introduce el contrasenia de Bases de Datos que quieres conectar: ");
            respuestaPass = teclado.next();
            //respuestaPass = new String(System.console().readPassword("Introduce el contrasenia de Bases de Datos que quieres conectar: ")); //No funciona en IDE ya que no es entorno operativo real
        }while (respuestaPass.isBlank());
        return respuestaPass;
    }

    private static String obtenerUser() {
        boolean cambiarUser = respuestaSN("User predeterminado -> root\n¿Quieres cambiarla? Solo se acepta(S/N)");
        if (!cambiarUser) {
            return "root";
        }
        return leerUser();
    }

    private static String leerUser() {
        String nombreUsuario;
        do {
            System.out.println("Introduce el nombre Usuario (No puede ser nulo): ");
            nombreUsuario = teclado.next();
        }while (nombreUsuario.isBlank());
        return nombreUsuario;
    }

    private static String obtenerBD() {
        String respuestaBD;
        do {
            System.out.println("Introduce el nombre de Bases de Datos que quieres conectar: ");
            respuestaBD = teclado.next();
        }while (!esValidoNombreBD(respuestaBD));
        return respuestaBD;
    }

    private static boolean esValidoNombreBD(String nombreBD) {
        String[] PALABRAS_RESERVADAS = {
                "SELECT", "INSERT", "DELETE", "UPDATE", "TABLE", "WHERE", "FROM", "JOIN", "INNER", "OUTER", "LEFT", "RIGHT"
        };
        // Verificar si el nombre es nulo, vacío o demasiado largo (máximo 64 caracteres)
        if (nombreBD == null || nombreBD.isEmpty() || nombreBD.length() > 64) {
            return false;
        }

        // Verificar si comienza con una letra o un guion bajo
        if (!Character.isLetter(nombreBD.charAt(0)) && nombreBD.charAt(0) != '_') {
            return false;
        }

        // Verificar si contiene caracteres no permitidos (solo letras, números, guiones bajos y signo de dólar)
        if (!nombreBD.matches("^[a-zA-Z0-9_$]+$")) {
            return false;
        }

        // Verificar si el nombre es una palabra reservada
        for (String palabra : PALABRAS_RESERVADAS) {
            if (nombreBD.toUpperCase().equals(palabra)) {
                return false;
            }
        }

        return true;
    }

    private static String obtenerPuerto() {
        boolean cambiarPuerto = respuestaSN("Puerto predeterminado -> 3306\n¿Quieres cambiarla? Solo se acepta(S/N)");
        if (!cambiarPuerto) {
            return "3306";
        }
        return leerPuerto();
    }

    private static String leerPuerto() {
        String puerto;
        do {
            System.out.println("Introduce el puerto (Puerto -> numero Entero entre 0 hasta 65535): ");
            puerto = teclado.next();
        }while (!esEntero(puerto));
        return puerto;
    }

    private static String obtenerIP() {
        boolean cambiarIP = respuestaSN("IP predeterminado -> localhost\n¿Quieres cambiarla? Solo se acepta(S/N)");
        if (!cambiarIP) {
            return "localhost";
        }
        return leerIP();
    }

    private static String leerIP() {
        String ip;
        do {
            System.out.println("Introduce IP, no puede ser vacio");
            ip = teclado.next();
        }while (ip.isBlank());
       return ip;
    }

    private static boolean respuestaSN(String mensaje) {
        String respuesta;
        do {
            System.out.println(mensaje);
            respuesta = teclado.next().toUpperCase();
        } while (!respuesta.equals("S") && !respuesta.equals("N"));
        return respuesta.equals("S");
    }
    private static boolean esEntero(String numero) {
        return numero.matches("\\d+");
    }
}
