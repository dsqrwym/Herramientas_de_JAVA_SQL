package sqlite3Sql;

import java.io.File;
import java.util.Scanner;

public class ConectarConsola {
    private static final Scanner teclado = new Scanner(System.in);

    private ConectarConsola() {
    }

    public static boolean conectarBD() {
        ConectarBD.setDirectorio(obtenerDirectorio());

        if (ConectarBD.connectDatabase(false)) {
            System.out.println("Conectado");
            return true;
        }
        System.out.println("No ha podido conectar Bases de Dados.\nDebe conectarse a la base de datos para realizar las siguientes operaciones.");
        return false;
    }

    private static String obtenerDirectorio() {
        File basesDatosFile;

        do {
            System.out.println("Por favor, introduce el directorio del archivo.db de la Bases de Datos :");
            basesDatosFile = new File(teclado.nextLine());
            if (compruebaArchivoDB(basesDatosFile)){
                return basesDatosFile.getAbsolutePath();
            }
        }while (true);

    }

    private static boolean compruebaArchivoDB(File basesDatosFile) {
        if (!basesDatosFile.exists()){
            System.out.println("El archivo no existe. O falta permiso.");
            return false;
        }
        if (basesDatosFile.isDirectory()){
            System.out.println("La ruta ingresada no corresponde a un archivo.");
            return false;
        }
        if (!basesDatosFile.getName().toLowerCase().endsWith(".db")){
            System.out.println("No es un archivo .db del sqLite.");
            return false;
        }
        return true;
    }
}


