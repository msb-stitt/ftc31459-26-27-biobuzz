package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import io.github.mikestitt.corbelsflightlog.nt.Nt4Server;

import org.firstinspires.ftc.teamcode.LiveNetworkTablesSwitch;
import org.firstinspires.ftc.teamcode.base.Tracker.LiveSetting;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

/**
 * The switch that turns live NetworkTables on or off: a lesson is live unless
 * it is off, any other OpMode only when it is on. The server here is one of
 * the test's own on a free port, counted each time {@code serveLive()} asks
 * for it, so a server another test left running on 5810 changes nothing.
 */
public class LiveNetworkTablesTest {

    /** A competition OpMode, which serves the way StarterBot TeleOp does. */
    public static class CompetitionTeleOp extends CorbelsTeleOp {
        boolean serving;

        @Override public void init() {
            initBefore();
            serving = Tracker.serveLive();
            initAfter();
        }

        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    private Callable<Nt4Server> robotServers;
    private Nt4Server server;
    private int serversAskedFor;

    @Before
    public void countTheServersAskedFor() {
        robotServers = Tracker.liveServers;
        Tracker.liveServers = () -> {
            serversAskedFor++;
            if (server == null) server = Nt4Server.start(0);
            return server;
        };
    }

    @After
    public void putTheRobotsServerBack() {
        Tracker.liveServers = robotServers;
        Tracker.setLive(LiveSetting.NOT_SET);
        if (server != null) server.close();
    }

    @Test
    public void notSetALessonIsLiveAndACompetitionOpModeIsNot() throws Exception {
        OpModeHarness lesson = run(lesson("L110FollowerOpMode"));
        assertTrue(lesson.driverStation.text(),
                lesson.driverStation.shows("Live NetworkTables on. AdvantageScope: connect to 192.168.43.1"));
        assertEquals("the lesson asked for the server", 1, serversAskedFor);

        CompetitionTeleOp competition = new CompetitionTeleOp();
        OpModeHarness h = run(competition);
        assertFalse(competition.serving);
        assertTrue(h.driverStation.text(), h.driverStation.shows("Live NetworkTables off"));
        assertEquals("the competition OpMode did not", 1, serversAskedFor);
    }

    @Test
    public void offALessonStartsNoServer() throws Exception {
        Tracker.setLive(LiveSetting.OFF);
        OpModeHarness h = run(lesson("L110FollowerOpMode"));
        assertTrue(h.driverStation.text(), h.driverStation.shows("Live NetworkTables off"));
        assertFalse(h.driverStation.text(), h.driverStation.shows("AdvantageScope"));
        assertEquals(0, serversAskedFor);
    }

    @Test
    public void onACompetitionOpModeServes() {
        Tracker.setLive(LiveSetting.ON);
        CompetitionTeleOp competition = new CompetitionTeleOp();
        OpModeHarness h = run(competition);
        assertTrue(competition.serving);
        assertTrue(h.driverStation.text(), h.driverStation.shows("Live NetworkTables on"));
        assertEquals(1, serversAskedFor);
    }

    @Test
    public void theSwitchTurnsItTheOtherWayEachTimeItIsStarted() {
        OpModeHarness first = run(new LiveNetworkTablesSwitch());
        assertTrue(first.driverStation.text(), first.driverStation.shows(
                "Live NetworkTables is currently off, but on for a lesson. Press Start to turn it on."));
        assertTrue(first.driverStation.text(), first.driverStation.shows("Live NetworkTables is now on."));
        assertEquals(LiveSetting.ON, Tracker.liveSetting());

        OpModeHarness second = run(new LiveNetworkTablesSwitch());
        assertTrue(second.driverStation.text(), second.driverStation.shows(
                "Live NetworkTables is currently on. Press Start to turn it off."));
        assertEquals(LiveSetting.OFF, Tracker.liveSetting());

        OpModeHarness third = run(new LiveNetworkTablesSwitch());
        assertTrue(third.driverStation.text(), third.driverStation.shows(
                "Live NetworkTables is currently off. Press Start to turn it on."));
        assertEquals(LiveSetting.ON, Tracker.liveSetting());
        assertEquals("the switch serves nothing itself", 0, serversAskedFor);
    }

    /** INIT, Start and one loop, which sends the Driver Station its lines, then Stop. */
    private static OpModeHarness run(OpMode opMode) {
        OpModeHarness h = new OpModeHarness(opMode);
        h.init();
        h.start();
        h.loop();
        h.stop();
        return h;
    }

    /** A starter from {@code lessons}, which is package-private there. */
    private static OpMode lesson(String name) throws Exception {
        Constructor<?> c = Class.forName("org.firstinspires.ftc.teamcode.lessons." + name)
                .getDeclaredConstructor();
        c.setAccessible(true);
        return (OpMode) c.newInstance();
    }
}
