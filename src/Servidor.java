
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Se encarga de la logica del programa para recibir y enviar datos al cliente.
 * Se comunica con el cliente mediante el uso de sockets con puertos y el envio
 * de paquetes, mas especificamente los "DatagramSocket" y "DatagramPacket".
 */
public class Servidor extends Thread {

    private int puerto = 4213;
    private int puertoCliente = 3198;
    private int puertoEntradaMensajesCliente = 108;
    private DatagramSocket socketUDP;
    private DatagramPacket enviarDatosCliente;
    private DatagramPacket recibirDatosCliente;
    private String directorioxDefectoCarpetaOculta = System.getProperty("user.dir") + File.separator + "jars";
    private LinkedList<String> nombresJarsExistentes = new LinkedList<>();

    public Servidor() {
    }

    /**
     * Ejecuta el servidor en un nuevo hilo.
     */
    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                iniciarServidor();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Crea e inicializa todo lo necesario para el funcionamiento del servidor,
     * específicamente el socket y los paquetes, y queda a la escucha de las órdenes
     * del cliente.
     * 
     * Nota: Al iniciar se mantendrá encendido siempre, a menos que se finalice el
     * proceso desde el administrador de tareas.
     * 
     * @throws SocketException      en caso de ocurrir un erro con el socket
     * @throws UnknownHostException en caso de que ocurra un error con el host
     */
    private void iniciarServidor() throws SocketException, UnknownHostException {
        socketUDP = new DatagramSocket(puerto);
        recibirDatosCliente = new DatagramPacket(new byte[1], 1);
        enviarDatosCliente = new DatagramPacket(new byte[1], 1, InetAddress.getLocalHost(), puertoCliente);

        String mensajeUsuario;
        while (true) {
            mensajeUsuario = obtenerEleccionUsuario();
            if (mensajeUsuario.endsWith("|")) {
                ejecutarJar(mensajeUsuario.substring(0, mensajeUsuario.length() - 1));
            } else if (mensajeUsuario.equals("Estado conexion")) {
                enviarDatosCliente.setData("recibido".getBytes(), 0, 8);
                try {
                    socketUDP.send(enviarDatosCliente);
                } catch (IOException e) {
                }
            } else if (mensajeUsuario.equals("Establecer conexion")) {
                leerJars();
                enviarMensajeACliente("Conectado.");
            } else if (mensajeUsuario.endsWith(":")) {
                recepcionDeJar(mensajeUsuario.substring(0, mensajeUsuario.length() - 1));
            }
        }
    }

    /**
     * Envía mensajes al cliente, principalmente para informar sobre el estado de
     * las solicitudes recibidas del cliente.
     * 
     * @param mensaje texto a enviar
     */
    private void enviarMensajeACliente(String mensaje) {
        int portaux = enviarDatosCliente.getPort();
        enviarDatosCliente.setData(mensaje.getBytes(), 0, mensaje.length());
        enviarDatosCliente.setPort(puertoEntradaMensajesCliente);
        try {
            socketUDP.send(enviarDatosCliente);
        } catch (IOException e) {
        }
        enviarDatosCliente.setPort(portaux);
    }

    /**
     *
     * Inicia la tarea de recepción y guardado del archivo jar recibido por el
     * usuarion desde el cliente.
     * 
     * @param nombreJar nombre del jar a guardar
     */
    private void recepcionDeJar(String nombreJar) {
        guardarJar(nombreJar.replaceAll("&.*$", ""),
                Integer.parseInt(nombreJar.substring(nombreJar.lastIndexOf("&") + 1)));
        leerJars();
    }

    /**
     * Ejecuta un jar.
     * 
     * @param nombreJar nombre del jar a ejecutar
     */
    private void ejecutarJar(String nombreJar) {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", directorioxDefectoCarpetaOculta + File.separator
                + nombreJar);
        try {
            pb.start();
            enviarMensajeACliente("Se ejecuto \"" + nombreJar + "\" correctamente.");
        } catch (IOException e) {
            enviarMensajeACliente("#No se ha ejecutado el archivo.");
        }
    }

    /**
     * Recibe una orden enviada por el cliente y la procesa.
     */
    private String obtenerEleccionUsuario() {
        recibirDatosCliente.setData(new byte[1024]);
        try {
            socketUDP.receive(recibirDatosCliente);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return new String(recibirDatosCliente.getData(), 0, recibirDatosCliente.getLength());
    }

    /**
     * Lee los jars almacenados en el directorio establecido por defecto y los envía
     * al cliente.
     */
    private void leerJars() {
        File carpetaJars = new File(directorioxDefectoCarpetaOculta);
        nombresJarsExistentes.clear();
        if (carpetaJars.exists()) {
            for (File archivosEnCarpeta : carpetaJars.listFiles()) {
                if (archivosEnCarpeta.getName().endsWith(".jar")) {
                    nombresJarsExistentes.add(archivosEnCarpeta.getName());
                }
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = null;
                oos = new ObjectOutputStream(baos);
                oos.writeObject(nombresJarsExistentes);

                byte[] nombresArvhivosJar = baos.toByteArray();
                enviarDatosCliente.setData(nombresArvhivosJar);
                try {
                    socketUDP.send(enviarDatosCliente);
                    try {
                        baos.close();
                        oos.close();
                    } catch (IOException e) {
                    }
                } catch (IOException e) {
                    enviarMensajeACliente("#No se a podido enviar el paquete de nombres." + e.toString());
                }
            } catch (IOException e) {
                enviarMensajeACliente("#No se ha podido detectar los jars en la carpeta oculta." + e.toString());
            }
        } else {
            Path directorioCarpetaOculta = Paths.get(directorioxDefectoCarpetaOculta);
            try {
                Files.createDirectories(directorioCarpetaOculta);
                Files.setAttribute(directorioCarpetaOculta, "dos:hidden", true);
            } catch (IOException e) {
                enviarMensajeACliente("#Ha ocurrido un error al intentar crear la carpeta oculta." + e.toString());
            }
        }
    }

    /**
     * Crea el archivo jar recibido por el cliente en el directorio por defecto.
     * 
     * @param recibirDatosCliente "DatagramPacket" que contiene el nombre del
     *                            archivo
     * @param tamanoArchivo       peso del archivo
     * 
     * @throws IOException si ocurre un error al crear el archivo o la carpeta
     *                     oculta.
     */
    private void guardarJar(String nombreArchivo, int tamanoArchivo) {
        try {
            // Se crea un objeto File con la ruta donde se guardará el archivo
            File jarEnCarpetaOculta = new File(directorioxDefectoCarpetaOculta, nombreArchivo);
            // Se crea el archivo en la ruta especificada
            jarEnCarpetaOculta.createNewFile();
            // Se ecriben los datos del jar en el archivo creado
            escribirEnArchivo(jarEnCarpetaOculta.getAbsolutePath(), tamanoArchivo);
            enviarMensajeACliente("Jar recibido y guardado con exito :=).");
        } catch (Exception e) {
            enviarMensajeACliente("#Ha ocurrido un error guardar el archivo" + e.toString());
        }
    }

    /**
     * Obtiene del cliente el jar y lo guarda en el disco.
     * 
     * @param ubicArchivo   ubicación del archivo donde se va a escribir el jar
     * @param tamanoArchivo peso del archivo
     * 
     * @throws IOException si ocurre un error al escribir en el archivo
     */
    private void escribirEnArchivo(String ubicArchivo, int tamanoArchivo) throws IOException {
        // Se obtiene el contenido del paquete
        recibirDatosCliente.setData(new byte[tamanoArchivo]);
        socketUDP.receive(recibirDatosCliente);
        byte archivoJar[] = new byte[recibirDatosCliente.getLength()];
        for (int i = 0; i < archivoJar.length; i++) {
            archivoJar[i] = recibirDatosCliente.getData()[i];
        }
        // Se escribe el contenido en el archivo
        FileOutputStream fos = new FileOutputStream(ubicArchivo, true);
        fos.write(archivoJar);
        fos.close();
    }

    /**
     * Cierra el servidor.
     */
    public void cerrar() {
        socketUDP.close();
    }
}