package org.firstinspires.ftc.teamcode.base;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A halsim_ws client's connection: WPILib's simulation protocol, one JSON
 * text message per device's values, over a WebSocket.
 *
 * <p>A message is {@code {"type": T, "device": D, "data": {name: value}}},
 * each value a number, a boolean or a string. The names a robot program sends
 * start {@code <}, the ones it is sent {@code >}.
 *
 * <p>Only what that needs: text frames, masked as a client's must be, the
 * server's pings answered and its close taken as the end.
 *
 * <p>Passes when: SimHalsimWsTest.
 */
final class SimHalsimWs implements AutoCloseable {
    /** One message: a device's type, its name and the values it carries. */
    static final class Message {
        final String type;
        final String device;
        final Map<String, Object> data;

        Message(String type, String device, Map<String, Object> data) {
            this.type = type;
            this.device = device;
            this.data = data;
        }
    }

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final SecureRandom random = new SecureRandom();

    private SimHalsimWs(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = socket.getOutputStream();
    }

    /** How long apart the tries to connect are, while nothing is listening. */
    static final long RETRY_MS = 250;

    /**
     * Connects to {@code ws://host:port} + {@code path}, trying again while
     * nothing listens there, as a WPILib robot's halsim_ws client does, and
     * waiting as long as the handshake takes: a server held at a breakpoint
     * answers once it goes on. Nothing here times out.
     */
    static SimHalsimWs connect(String host, int port, String path) throws IOException {
        Socket s;
        while (true) {
            s = new Socket();
            try {
                s.connect(new InetSocketAddress(host, port));
                break;
            } catch (ConnectException e) {
                s.close();
                OpModeHarness.sleep(RETRY_MS);
            }
        }
        s.setTcpNoDelay(true);
        SimHalsimWs ws = new SimHalsimWs(s);
        ws.handshake(host + ":" + port, path);
        return ws;
    }

    private void handshake(String hostPort, String path) throws IOException {
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        String request = "GET " + path + " HTTP/1.1\r\nHost: " + hostPort
                + "\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Key: "
                + Base64.getEncoder().encodeToString(nonce) + "\r\nSec-WebSocket-Version: 13\r\n\r\n";
        out.write(request.getBytes(StandardCharsets.US_ASCII));
        out.flush();
        String status = null;
        for (String line; !(line = readLine()).isEmpty(); ) {
            if (status == null) {
                status = line;
            }
        }
        if (status == null || !status.contains(" 101")) {
            throw new IOException("the server refused " + path + ": " + status);
        }
    }

    private String readLine() throws IOException {
        StringBuilder b = new StringBuilder();
        for (int c; (c = in.read()) != '\n'; ) {
            if (c < 0) {
                throw new EOFException("the server closed the connection");
            }
            if (c != '\r') {
                b.append((char) c);
            }
        }
        return b.toString();
    }

    /** Sends a message. */
    void send(String type, String device, Map<String, Object> data) throws IOException {
        frame(0x1, encode(type, device, data).getBytes(StandardCharsets.UTF_8));
    }

    /** The next message, answering pings on the way; an {@link EOFException} once the server closes. */
    Message receive() throws IOException {
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        while (true) {
            int b0 = read();
            int b1 = read();
            long len = b1 & 0x7F;
            if (len == 126) {
                len = (read() << 8) | read();
            } else if (len == 127) {
                len = 0;
                for (int i = 0; i < 8; i++) {
                    len = (len << 8) | read();
                }
            }
            byte[] mask = null;
            if ((b1 & 0x80) != 0) {
                mask = new byte[]{(byte) read(), (byte) read(), (byte) read(), (byte) read()};
            }
            byte[] payload = new byte[(int) len];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (read() ^ (mask == null ? 0 : mask[i % 4]));
            }
            int opcode = b0 & 0x0F;
            if (opcode == 0x8) {
                throw new EOFException("the server closed the connection");
            } else if (opcode == 0x9) {
                frame(0xA, payload);
            } else if (opcode == 0x1 || opcode == 0x0) {
                text.write(payload);
                if ((b0 & 0x80) != 0) {
                    return decode(new String(text.toByteArray(), StandardCharsets.UTF_8));
                }
            }
        }
    }

    private int read() throws IOException {
        int b = in.read();
        if (b < 0) {
            throw new EOFException("the server closed the connection");
        }
        return b;
    }

    private synchronized void frame(int opcode, byte[] payload) throws IOException {
        ByteArrayOutputStream f = new ByteArrayOutputStream(payload.length + 14);
        f.write(0x80 | opcode);
        if (payload.length < 126) {
            f.write(0x80 | payload.length);
        } else if (payload.length < 65536) {
            f.write(0x80 | 126);
            f.write(payload.length >> 8);
            f.write(payload.length & 0xFF);
        } else {
            f.write(0x80 | 127);
            for (int i = 7; i >= 0; i--) {
                f.write((int) ((long) payload.length >> (8 * i)) & 0xFF);
            }
        }
        byte[] mask = new byte[4];
        random.nextBytes(mask);
        f.write(mask, 0, 4);
        for (int i = 0; i < payload.length; i++) {
            f.write(payload[i] ^ mask[i % 4]);
        }
        out.write(f.toByteArray());
        out.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    /** A message as halsim_ws's JSON. */
    static String encode(String type, String device, Map<String, Object> data) {
        StringBuilder b = new StringBuilder("{\"type\":");
        string(b, type);
        b.append(",\"device\":");
        string(b, device);
        b.append(",\"data\":{");
        String sep = "";
        for (Map.Entry<String, Object> e : data.entrySet()) {
            b.append(sep);
            string(b, e.getKey());
            b.append(':');
            Object v = e.getValue();
            if (v instanceof Boolean) {
                b.append(v);
            } else if (v instanceof Long || v instanceof Integer) {
                b.append(((Number) v).longValue());
            } else if (v instanceof Number) {
                double d = ((Number) v).doubleValue();
                b.append(Double.isFinite(d) ? Double.toString(d) : "0.0");
            } else {
                string(b, String.valueOf(v));
            }
            sep = ",";
        }
        return b.append("}}").toString();
    }

    private static void string(StringBuilder b, String s) {
        b.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                b.append('\\').append(c);
            } else if (c < 0x20) {
                b.append(String.format("\\u%04x", (int) c));
            } else {
                b.append(c);
            }
        }
        b.append('"');
    }

    /**
     * A halsim_ws message read back: a whole number as a Long, any other number
     * as a Double. Anything but a message's own shape is an
     * {@link IllegalArgumentException}.
     */
    static Message decode(String json) {
        Reader r = new Reader(json);
        Map<String, Object> top = r.object();
        Object type = top.get("type");
        Object device = top.get("device");
        Object data = top.get("data");
        if (!(type instanceof String) || !(device instanceof String) || !(data instanceof Map)) {
            throw new IllegalArgumentException("not a halsim_ws message: " + json);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) data;
        return new Message((String) type, (String) device, values);
    }

    /** JSON's objects, strings, numbers, booleans and null; no arrays, which halsim_ws sends none of. */
    private static final class Reader {
        private final String s;
        private int i;

        Reader(String s) {
            this.s = s;
        }

        Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            expect('{');
            if (peek() == '}') {
                i++;
                return m;
            }
            while (true) {
                String key = string();
                expect(':');
                m.put(key, value());
                char c = next();
                if (c == '}') {
                    return m;
                }
                if (c != ',') {
                    throw error();
                }
            }
        }

        Object value() {
            char c = peek();
            if (c == '{') {
                return object();
            } else if (c == '"') {
                return string();
            } else if (s.startsWith("true", i)) {
                i += 4;
                return Boolean.TRUE;
            } else if (s.startsWith("false", i)) {
                i += 5;
                return Boolean.FALSE;
            } else if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            String n = s.substring(start, i);
            if (n.isEmpty()) {
                throw error();
            }
            if (n.indexOf('.') < 0 && n.indexOf('e') < 0 && n.indexOf('E') < 0) {
                return Long.parseLong(n);
            }
            return Double.parseDouble(n);
        }

        String string() {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (true) {
                if (i >= s.length()) {
                    throw error();
                }
                char c = s.charAt(i++);
                if (c == '"') {
                    return b.toString();
                }
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n': b.append('\n'); break;
                        case 't': b.append('\t'); break;
                        case 'r': b.append('\r'); break;
                        case 'b': b.append('\b'); break;
                        case 'f': b.append('\f'); break;
                        case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                        default: b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
        }

        private char peek() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            if (i >= s.length()) {
                throw error();
            }
            return s.charAt(i);
        }

        private char next() {
            char c = peek();
            i++;
            return c;
        }

        private void expect(char c) {
            if (next() != c) {
                throw error();
            }
        }

        private IllegalArgumentException error() {
            return new IllegalArgumentException("not JSON at " + i + ": " + s);
        }
    }
}
