
/**
 * JarNet - Este programa permite la transferencia de archivos JAR a un dispositivo remoto, así como su ejecución desde el dispositivo local. 
 * Copyright (C) 2023 Ignacio Inzerilli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For further information on how to apply and follow the GNU GPL, please
 * visit <https://www.gnu.org/licenses/>.
 * 
 * Contact information: soporteIgnacio@hotmail.com
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Establece la ejecución automática del programa al iniciar sesión en
 * diferentes sistemas operativos.
 */
public class ProgramadorTareas {

    /**
     * Configura la ejecución automática del programa al iniciar sesión en un
     * sistema operativo específico, determinado por el sistema operativo actual.
     *
     * @param rutaArchivo la ruta del archivo ejecutable del programa
     */
    public static void configurarInicioAutomatico(String rutaArchivo) {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            configurarInicioWindows(rutaArchivo);
        } else if (osName.contains("mac")) {
            configurarInicioMac(rutaArchivo);
        } else if (osName.contains("linux")) {
            configurarInicioLinux(rutaArchivo);
        } else {
            JOptionPane.showConfirmDialog(null,
                    "No se reconoce el sistema, por lo que no se puede configurar el inicio automático del programa en este sistema operativo.",
                    "Error de reconocimiento", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Configura la ejecución automática del programa al iniciar sesión en un
     * sistema operativo Windows.
     *
     * @param rutaArchivo la ruta del archivo ejecutable del programa
     */
    private static void configurarInicioWindows(String rutaArchivo) {
        try {
            Runtime.getRuntime()
                    .exec("schtasks /create /sc onlogon /tn \"JarNetServidor\" /tr \"" + rutaArchivo + "\"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura la ejecución automática del programa al iniciar sesión en un
     * sistema
     * operativo macOS.
     *
     * @param rutaArchivo la ruta del archivo ejecutable del programa
     */
    private static void configurarInicioMac(String rutaArchivo) {
        String rutaScriptInicio = System.getProperty("user.home") + "/Library/LaunchAgents/com.miPrograma.plist";
        String nombreArchivo = new File(rutaArchivo).getName();
        String comandoJava = "java -jar \"" + rutaArchivo + "\"";

        try {
            FileWriter writer = new FileWriter(rutaScriptInicio);
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
                    +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "    <key>Label</key>\n" +
                    "    <string>com." + nombreArchivo + "</string>\n" +
                    "    <key>ProgramArguments</key>\n" +
                    "    <array>\n" +
                    "        <string>/bin/sh</string>\n" +
                    "        <string>-c</string>\n" +
                    "        <string>" + comandoJava + "</string>\n" +
                    "    </array>\n" +
                    "    <key>RunAtLoad</key>\n" +
                    "    <true/>\n" +
                    "</dict>\n" +
                    "</plist>");
            writer.close();

            String comandoConfiguracion = "launchctl load " + rutaScriptInicio;
            Runtime.getRuntime().exec(comandoConfiguracion);

            System.out.println("Se ha configurado el programa para ejecutarse al inicio del sistema (macOS).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura la ejecución automática del programa al iniciar sesión en un
     * sistema operativo Linux.
     *
     * @param rutaArchivo la ruta del archivo ejecutable del programa
     */
    private static void configurarInicioLinux(String rutaArchivo) {
        String rutaScriptInicio = System.getProperty("user.home") + "/.config/autostart/miPrograma.desktop";
        String nombreArchivo = new File(rutaArchivo).getName();
        String comandoJava = "java -jar \"" + rutaArchivo + "\"";

        try {
            FileWriter writer = new FileWriter(rutaScriptInicio);
            writer.write("[Desktop Entry]\n" +
                    "Type=Application\n" +
                    "Exec=" + comandoJava + "\n" +
                    "Hidden=false\n" +
                    "NoDisplay=false\n" +
                    "X-GNOME-Autostart-enabled=true\n" +
                    "Name[en_US]=" + nombreArchivo + "\n" +
                    "Name=" + nombreArchivo + "\n" +
                    "Comment[en_US]=Programa de ejemplo\n" +
                    "Comment=Programa de ejemplo\n");
            writer.close();

            System.out.println("Se ha configurado el programa para ejecutarse al inicio del sistema (Linux).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
