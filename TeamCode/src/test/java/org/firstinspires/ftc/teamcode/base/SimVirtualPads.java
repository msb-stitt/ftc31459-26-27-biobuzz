package org.firstinspires.ftc.teamcode.base;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gamepads a script drives, for a video with nobody at a gamepad.
 *
 * <p>{@code SIMRUN_VIRTUAL_PADS} names a port on localhost, and each line sent
 * to it sets one control of one virtual gamepad: {@code pad 1 left_stick_y
 * -0.73}. The first line naming a number attaches a gamepad through
 * {@link SimGamepad}, and from there it is a gamepad like any other to
 * {@link SimPads}. simRun attaches gamepad 1 before it looks for gamepads,
 * with {@link #attachFirst}. A value holds until it is set again; nothing here moves a
 * control over time.
 *
 * <p>Read on the loop's own thread, once per pass, so SDL is only ever called
 * from one thread. Every socket is non-blocking and is asked, never waited on.
 *
 * <p>Passes when: SimVirtualPadsTest.
 */
final class SimVirtualPads implements AutoCloseable {

    static final String VARIABLE = "SIMRUN_VIRTUAL_PADS";

    /** One command, checked. */
    static final class Command {
        final int pad;
        final String control;
        final float value;

        Command(int pad, String control, float value) {
            this.pad = pad;
            this.control = control;
            this.value = value;
        }
    }

    /** One connection, and the part of a line it has sent so far. */
    private static final class Client {
        final SocketChannel channel;
        final StringBuilder pending = new StringBuilder();

        Client(SocketChannel channel) {
            this.channel = channel;
        }
    }

    private final ServerSocketChannel server;

    private final List<Client> clients = new ArrayList<>();

    /** The virtual gamepads, by the number the script gave each. */
    private final Map<Integer, SimGamepad.Virtual> pads = new LinkedHashMap<>();

    private final ByteBuffer buffer = ByteBuffer.allocate(4096);

    private SimVirtualPads(ServerSocketChannel server) {
        this.server = server;
    }

    /**
     * Opens the port {@code variable} names, or returns null and says why not.
     *
     * <p>Null and silent when the variable is unset. A run that takes no
     * gamepads, a value that is not a port, and a port that cannot be opened
     * each print one line, and the run goes on with no virtual gamepads.
     * Port 0 asks the system for a free one, which {@link #port} then names.
     * An opened port prints nothing.
     */
    static SimVirtualPads open(String variable, boolean usePads, PrintStream out) {
        if (variable == null) {
            return null;
        }
        if (!usePads) {
            out.println("  " + VARIABLE + " is set, but only --pad and --pad-check take"
                    + " virtual gamepads, so it is ignored.");
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(variable.trim());
        } catch (NumberFormatException e) {
            port = -1;
        }
        if (port < 0 || port > 65535) {
            out.println("  " + VARIABLE + "=" + variable + " is not a port number, so there"
                    + " are no virtual gamepads this run.");
            return null;
        }
        ServerSocketChannel server = null;
        try {
            server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
            server.configureBlocking(false);
        } catch (IOException e) {
            closeQuietly(server);
            out.println("  Port " + port + " could not be opened (" + e + "), so there are no"
                    + " virtual gamepads this run.");
            return null;
        }
        return new SimVirtualPads(server);
    }

    /**
     * Attaches gamepad 1, which a run attaches before it looks for gamepads, so
     * the census finds it as if it were plugged in and never says none is.
     */
    void attachFirst() {
        pads.put(1, SimGamepad.attachVirtual());
    }

    int port() {
        return server.socket().getLocalPort();
    }

    /**
     * Takes every connection and every whole line waiting, applies each line,
     * and answers it. Once per pass, before the gamepads are read.
     */
    void poll() {
        try {
            SocketChannel channel;
            while ((channel = server.accept()) != null) {
                channel.configureBlocking(false);
                clients.add(new Client(channel));
            }
        } catch (IOException e) {
            // A connection that failed to arrive is the script's to retry.
        }
        for (Iterator<Client> it = clients.iterator(); it.hasNext(); ) {
            Client client = it.next();
            if (!read(client)) {
                closeQuietly(client.channel);
                it.remove();
            }
        }
    }

    /** Reads what one client has sent and answers each whole line; false once it has gone. */
    private boolean read(Client client) {
        try {
            int n;
            while ((n = client.channel.read(buffer)) > 0) {
                buffer.flip();
                client.pending.append(StandardCharsets.UTF_8.decode(buffer));
                buffer.clear();
            }
            int end;
            while ((end = client.pending.indexOf("\n")) >= 0) {
                String line = client.pending.substring(0, end);
                client.pending.delete(0, end + 1);
                answer(client.channel, apply(line));
            }
            return n >= 0;
        } catch (IOException e) {
            return false;
        }
    }

    /** Applies one line, and returns the line to answer it with. */
    private String apply(String line) {
        Object parsed = parse(line);
        if (parsed instanceof String) {
            return "error " + parsed;
        }
        Command command = (Command) parsed;
        SimGamepad.Virtual pad = pads.get(command.pad);
        if (pad == null) {
            try {
                pad = SimGamepad.attachVirtual();
            } catch (IllegalStateException e) {
                return "error " + e.getMessage();
            }
            pads.put(command.pad, pad);
        }
        SimGamepad.setVirtual(pad, command.control, command.value);
        return "ok";
    }

    private static void answer(SocketChannel channel, String reply) throws IOException {
        ByteBuffer out = StandardCharsets.UTF_8.encode(reply + "\n");
        while (out.hasRemaining()) {
            channel.write(out);
        }
    }

    /**
     * One line as a {@link Command}, or as the reason it is not one.
     *
     * <p>Pure, so every rule a line is held to is a unit test.
     */
    static Object parse(String line) {
        String[] words = line.trim().split("\\s+");
        if (words.length != 4 || !words[0].equals("pad")) {
            return "a command is: pad <number> <control> <value>, not \"" + line.trim() + "\"";
        }
        int pad;
        try {
            pad = Integer.parseInt(words[1]);
        } catch (NumberFormatException e) {
            pad = 0;
        }
        if (pad < 1) {
            return "a gamepad's number is a whole number from 1, not " + words[1];
        }
        String control = words[2];
        boolean axis = SimArgs.AXES.contains(control);
        if (!axis && !SimArgs.BUTTONS.contains(control)) {
            return "no control is called " + control;
        }
        float value;
        try {
            value = Float.parseFloat(words[3]);
        } catch (NumberFormatException e) {
            value = Float.NaN;
        }
        if (!axis) {
            if (!words[3].equals("0") && !words[3].equals("1")) {
                return control + " takes 0 or 1, not " + words[3];
            }
        } else if (control.endsWith("trigger")) {
            if (!(value >= 0f && value <= 1f)) {
                return control + " takes 0 to 1, not " + words[3];
            }
        } else if (!(value >= -1f && value <= 1f)) {
            return control + " takes -1 to 1, not " + words[3];
        }
        return new Command(pad, control, value);
    }

    @Override
    public void close() {
        for (Client client : clients) {
            closeQuietly(client.channel);
        }
        clients.clear();
        for (SimGamepad.Virtual pad : pads.values()) {
            SimGamepad.detachVirtual(pad);
        }
        pads.clear();
        closeQuietly(server);
    }

    private static void closeQuietly(java.io.Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            // Closing on the way out; nothing is left to tell.
        }
    }
}
