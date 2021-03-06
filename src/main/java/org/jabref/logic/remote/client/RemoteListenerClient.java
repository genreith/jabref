package org.jabref.logic.remote.client;

import java.net.InetAddress;
import java.net.Socket;

import org.jabref.logic.l10n.Localization;
import org.jabref.logic.remote.shared.Protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteListenerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteListenerClient.class);

    private static final int TIMEOUT = 2000;


    private RemoteListenerClient() {
    }

    /**
     * Attempt to send command line arguments to already running JabRef instance.
     *
     * @param args Command line arguments.
     * @return true if successful, false otherwise.
     */
    public static boolean sendToActiveJabRefInstance(String[] args, int remoteServerPort) {
        try (Socket socket = new Socket(InetAddress.getByName("localhost"), remoteServerPort)) {
            socket.setSoTimeout(TIMEOUT);

            Protocol protocol = new Protocol(socket);
            try {
                String identifier = protocol.receiveMessage();

                if (!Protocol.IDENTIFIER.equals(identifier)) {
                    String port = String.valueOf(remoteServerPort);
                    String error = Localization.lang("Cannot use port %0 for remote operation; another application may be using it. Try specifying another port.", port);
                    System.out.println(error);
                    return false;
                }
                protocol.sendMessage(String.join("\n", args));
                return true;
            } finally {
                protocol.close();
            }
        } catch (Exception e) {
            LOGGER.debug(
                    "Could not send args " + String.join(", ", args) + " to the server at port " + remoteServerPort, e);
            return false;
        }
    }
}
