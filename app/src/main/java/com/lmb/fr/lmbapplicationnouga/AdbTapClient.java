package com.lmb.fr.lmbapplicationnouga;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AdbTapClient {

    private static final String TAG = "AdbTapClient";

    public static void executeTap(final int x, final int y) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. Essai d'exécution via le Shell local direct
                try {
                    Process p = Runtime.getRuntime().exec("input tap " + x + " " + y);
                    p.waitFor();
                    Log.d(TAG, "Command input tap executed locally.");
                } catch (Exception ignored) {
                }

                // 2. Connexion ADB TCP sur 127.0.0.1:5555 (Démon ADB local)
                try (Socket socket = new Socket("127.0.0.1", 5555)) {
                    socket.setSoTimeout(3000);
                    OutputStream os = socket.getOutputStream();
                    InputStream is = socket.getInputStream();

                    // ADB Header: A_CNXN (0x4e584e43), Arg0: 0x01000000 (Version), Arg1: 4096 (MaxData), Payload: "host::"
                    byte[] cnxn = createAdbPacket(0x4e584e43, 0x01000000, 4096, "host::".getBytes("UTF-8"));
                    os.write(cnxn);
                    os.flush();

                    // Lire la réponse CNXN
                    byte[] response = new byte[24];
                    int read = is.read(response);

                    if (read >= 24) {
                        // ADB Header: A_OPEN (0x4e45504f), Arg0: localId (1), Arg1: 0, Payload: "shell:input tap X Y\0"
                        String command = "shell:input tap " + x + " " + y + "\0";
                        byte[] openPacket = createAdbPacket(0x4e45504f, 1, 0, command.getBytes("UTF-8"));
                        os.write(openPacket);
                        os.flush();
                        Log.d(TAG, "Commande ADB shell transmise avec succès au démon local 5555 !");
                    }
                } catch (Exception e) {
                    Log.d(TAG, "ADB TCP non disponible sur 127.0.0.1:5555 (" + e.getMessage() + ")");
                }
            }
        }).start();
    }

    private static byte[] createAdbPacket(int command, int arg0, int arg1, byte[] payload) {
        int payloadLength = (payload != null) ? payload.length : 0;
        int checksum = 0;
        if (payload != null) {
            for (byte b : payload) {
                checksum += (b & 0xff);
            }
        }

        ByteBuffer buf = ByteBuffer.allocate(24 + payloadLength);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(command);
        buf.putInt(arg0);
        buf.putInt(arg1);
        buf.putInt(payloadLength);
        buf.putInt(checksum);
        buf.putInt(~command);

        if (payload != null) {
            buf.put(payload);
        }

        return buf.array();
    }
}
