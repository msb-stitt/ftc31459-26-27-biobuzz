package org.firstinspires.ftc.teamcode.base;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * One halsim_ws connection's server side, for tests: the handshake, frames
 * sent unmasked as a server's are, and frames read unmasked. It answers no
 * ping of its own and keeps the path the client asked for.
 */
final class FakeHalsimServer implements AutoCloseable {
    private final ServerSocket listener;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean ponged;
    String path;

    FakeHalsimServer() throws IOException {
        this(0);
    }

    /** Listening on {@code port}, 0 for any free one. */
    FakeHalsimServer(int port) throws IOException {
        listener = new ServerSocket(port, 1, InetAddress.getLoopbackAddress());
        listener.setSoTimeout(5000);
    }

    int port() {
        return listener.getLocalPort();
    }

    /** Takes the connection and answers its handshake. */
    void accept() throws IOException {
        socket = listener.accept();
        socket.setSoTimeout(5000);
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
        String request = line();
        path = request.split(" ")[1];
        while (!line().isEmpty()) {
            // the headers; the client checks none of the answer's
        }
        out.write(("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\n"
                + "Connection: Upgrade\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    void send(String type, String device, Map<String, Object> data) throws IOException {
        frame(0x1, SimHalsimWs.encode(type, device, data).getBytes(StandardCharsets.UTF_8));
    }

    void ping() throws IOException {
        frame(0x9, "p".getBytes(StandardCharsets.US_ASCII));
    }

    /** Whether a pong has come back while receiving. */
    boolean ponged() {
        return ponged;
    }

    /** The next text message, passing over pongs. */
    SimHalsimWs.Message receive() throws IOException {
        while (true) {
            int b0 = read();
            int b1 = read();
            if ((b1 & 0x80) == 0) {
                throw new IOException("a client's frame must be masked");
            }
            long len = b1 & 0x7F;
            if (len == 126) {
                len = (read() << 8) | read();
            } else if (len == 127) {
                len = 0;
                for (int i = 0; i < 8; i++) {
                    len = (len << 8) | read();
                }
            }
            byte[] mask = {(byte) read(), (byte) read(), (byte) read(), (byte) read()};
            byte[] payload = new byte[(int) len];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (read() ^ mask[i % 4]);
            }
            if ((b0 & 0x0F) == 0xA) {
                ponged = true;
                continue;
            }
            return SimHalsimWs.decode(new String(payload, StandardCharsets.UTF_8));
        }
    }

    private void frame(int opcode, byte[] payload) throws IOException {
        ByteArrayOutputStream f = new ByteArrayOutputStream();
        f.write(0x80 | opcode);
        if (payload.length < 126) {
            f.write(payload.length);
        } else if (payload.length < 65536) {
            f.write(126);
            f.write(payload.length >> 8);
            f.write(payload.length & 0xFF);
        } else {
            f.write(127);
            for (int i = 7; i >= 0; i--) {
                f.write((int) ((long) payload.length >> (8 * i)) & 0xFF);
            }
        }
        f.write(payload);
        out.write(f.toByteArray());
        out.flush();
    }

    private String line() throws IOException {
        StringBuilder b = new StringBuilder();
        for (int c; (c = read()) != '\n'; ) {
            if (c != '\r') {
                b.append((char) c);
            }
        }
        return b.toString();
    }

    private int read() throws IOException {
        int b = in.read();
        if (b < 0) {
            throw new EOFException();
        }
        return b;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        listener.close();
    }
}
