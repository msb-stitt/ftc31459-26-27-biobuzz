package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * halsim_ws's messages and the WebSocket under them, against
 * {@link FakeHalsimServer}.
 */
public final class SimHalsimWsTest {

    /** Connects while the server accepts on a thread of its own, as the handshake needs both. */
    static SimHalsimWs connect(FakeHalsimServer server, String path) throws Exception {
        Thread accept = new Thread(() -> {
            try {
                server.accept();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
        accept.start();
        SimHalsimWs ws = SimHalsimWs.connect("localhost", server.port(), path);
        accept.join();
        return ws;
    }

    @Test
    public void aMessageGoesAsHalsimWsJsonAndComesBackTheSame() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("<motorVoltage", 6.5);
        data.put("<done_seq", 21000L);
        data.put("<engaged", true);
        data.put("<name", "a \"b\"");
        String json = SimHalsimWs.encode("ScrimmageMotor", "Front_Left", data);
        assertEquals("{\"type\":\"ScrimmageMotor\",\"device\":\"Front_Left\",\"data\":{"
                + "\"<motorVoltage\":6.5,\"<done_seq\":21000,\"<engaged\":true,"
                + "\"<name\":\"a \\\"b\\\"\"}}", json);
        SimHalsimWs.Message m = SimHalsimWs.decode(json);
        assertEquals("ScrimmageMotor", m.type);
        assertEquals("Front_Left", m.device);
        assertEquals(data, m.data);
    }

    @Test
    public void aWholeNumberIsALongAndAnyOtherADouble() {
        SimHalsimWs.Message m = SimHalsimWs.decode(
                "{\"type\": \"SimDevice\", \"device\": \"x\", \"data\": {\">a\": 3, \">b\": 3.0, \">c\": -1e3}}");
        assertEquals(3L, m.data.get(">a"));
        assertEquals(3.0, m.data.get(">b"));
        assertEquals(-1000.0, m.data.get(">c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void anythingButAMessageIsRefused() {
        SimHalsimWs.decode("{\"type\": \"SimDevice\", \"data\": {}}");
    }

    @Test
    public void itConnectsAtItsPathSendsMaskedAndReadsLongFramesAndPings() throws Exception {
        try (FakeHalsimServer server = new FakeHalsimServer();
                SimHalsimWs ws = connect(server, "/wpilibws/37523")) {
            assertEquals("/wpilibws/37523", server.path);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("<x", 1.5);
            ws.send("SimDevice", "d", data);
            assertEquals(data, server.receive().data);
            server.ping();
            StringBuilder big = new StringBuilder();
            while (big.length() < 70_000) {
                big.append("long value ");
            }
            Map<String, Object> bigData = new LinkedHashMap<>();
            bigData.put(">s", big.toString());
            server.send("SimDevice", "d", bigData);
            assertEquals(big.toString(), ws.receive().data.get(">s"));
            ws.send("SimDevice", "d", data);
            assertEquals(data, server.receive().data);
            assertTrue("the ping was answered", server.ponged());
        }
    }

    @Test
    public void itTriesAgainUntilTheServerListens() throws Exception {
        int port;
        try (java.net.ServerSocket free = new java.net.ServerSocket(0)) {
            port = free.getLocalPort();
        }
        final int at = port;
        SimHalsimWs[] ws = new SimHalsimWs[1];
        Exception[] failed = new Exception[1];
        Thread client = new Thread(() -> {
            try {
                ws[0] = SimHalsimWs.connect("localhost", at, "/wpilibws/37523");
            } catch (Exception e) {
                failed[0] = e;
            }
        });
        client.start();
        Thread.sleep(4 * SimHalsimWs.RETRY_MS);
        assertTrue("still trying while nothing listens", client.isAlive());
        try (FakeHalsimServer server = new FakeHalsimServer(at)) {
            server.accept();
            client.join(5000);
            assertNull(failed[0]);
            assertEquals("/wpilibws/37523", server.path);
            ws[0].close();
        }
    }

    @Test(expected = EOFException.class)
    public void theServersCloseEndsTheConnection() throws Exception {
        try (FakeHalsimServer server = new FakeHalsimServer();
                SimHalsimWs ws = connect(server, "/wpilibws")) {
            server.close();
            ws.receive();
        }
    }
}
