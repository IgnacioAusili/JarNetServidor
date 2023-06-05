
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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

/**
 * Este proyecto Java se basa en una conexión entre cliente y servidor, los
 * cuales se comunican entre sí mediante el uso de sockets que utilizan puertos
 * de tu red LAN para establecer un flujo de datos.
 * 
 * El cliente puede enviar archivos .jar al servidor y también puede solicitar
 * al servidor la ejecución de algún archivo .jar almacenado previamente en el
 * servidor.
 * 
 * Autor: Ignacio Inzerilli
 * Fecha de creación: 12/04/2023
 * 
 */
public class JarNetServidor {
    /*
     * Crea e inicializa un servidor, establece una entrada trasera para manejar
     * fallos del servidor,
     * y permite la elección de la ejecución del servidor en cada inicio del
     * sistema.
     */
    public static void main(String[] args) {
        Path ruta = Paths
                .get(JarNetServidor.class.getProtectionDomain().getCodeSource().getLocation().getPath().toString()
                        .replaceFirst("/", ""));
        Path rutaComprobacion = Paths.get(ruta.getParent() + File.separator + "YaExisteEvento");

        if (!Files.exists(rutaComprobacion) && JOptionPane.showConfirmDialog(null,
                "¿Deseas que el servidor se ejecute cada vez que se encienda el dispositivo?", "Ejecución automática",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
            ProgramadorTareas.configurarInicioAutomatico(ruta.toString());
            try {
                rutaComprobacion.toFile().createNewFile();
            } catch (IOException e) {
            }
        }

        Servidor server = new Servidor();
        server.start();
        try {
            DatagramSocket entradaAuxiliar = new DatagramSocket(5431);
            try {
                DatagramPacket paquete = new DatagramPacket(new byte[1], 1);
                while (true) {
                    paquete.setData(new byte[1024]);
                    entradaAuxiliar.receive(paquete);
                    if ("Reiniciar".equals(new String(paquete.getData(), 0, paquete.getLength()))) {
                        server.cerrar();
                        server = new Servidor();
                        server.start();
                    }
                }
            } catch (Exception ex) {
            }
        } catch (SocketException e) {
        }
    }
}