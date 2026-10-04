package org.firstinspires.ftc.teamcode.base;

import edu.wpi.first.networktables.GenericSubscriber;
import edu.wpi.first.networktables.MultiSubscriber;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.TopicInfo;
import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;

import org.firstinspires.ftc.teamcode.example.NtApiCheckOpMode;
import org.firstinspires.ftc.teamcode.example.NtApiCheckOpMode.Entry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Reads {@link NtApiCheckOpMode} back over NetworkTables with WPILib's own
 * client, and exits 1 on any mismatch. Nobody touches the robot: start the
 * OpMode (Initialize, then Start) and run this.
 *
 * <pre>
 * ./gradlew :TeamCode:ntApiCheck --args="--runs 2 --save received.txt"
 * ./gradlew :TeamCode:ntApiCheck --args="--log run.wpilog --against received.txt"
 * </pre>
 *
 * <p>The first form connects to 127.0.0.1:5810 ({@code --host}, {@code --port}),
 * so on a robot joined by USB run {@code adb forward tcp:5810 tcp:5810} first.
 * With {@code --runs 2} it waits, after the first run's checks, for the OpMode
 * to be stopped and run again, and checks the second run too. {@code --save}
 * writes every value the last run sent. The second form checks that each of
 * those values is in the flight log the run wrote.
 *
 * <p>One client, one JVM: a second NetworkTables instance in a JVM can abort
 * it on macOS, as {@code SimPublisherTest}'s javadoc records.
 */
public final class NtApiCheck {

    private static final Pattern WORD = Pattern.compile("step [1-9][0-9]*");
    private static final String[] SCHEMAS = {"Translation2d", "Rotation2d", "Pose2d", "Twist2d",
            "ChassisSpeeds", "MecanumDriveWheelSpeeds", "Translation3d", "Quaternion", "Rotation3d",
            "Pose3d"};

    private final List<String> failures = new ArrayList<>();
    private int passes;

    private NtApiCheck() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Map<String, String> opts = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) opts.put(args[i], args[i + 1]);
        NtApiCheck check = new NtApiCheck();
        if (opts.containsKey("--log")) {
            check.compareLog(opts.get("--log"), opts.get("--against"));
        } else {
            check.live(opts.getOrDefault("--host", "127.0.0.1"),
                    Integer.parseInt(opts.getOrDefault("--port", "5810")),
                    Integer.parseInt(opts.getOrDefault("--runs", "1")),
                    opts.get("--save"));
        }
        System.out.printf("%n%d passed, %d failed%n", check.passes, check.failures.size());
        for (String f : check.failures) System.out.println("FAILED: " + f);
        System.exit(check.failures.isEmpty() ? 0 : 1);
    }

    private void expect(boolean ok, String what) {
        if (ok) {
            passes++;
            System.out.println("ok      " + what);
        } else {
            failures.add(what);
            System.out.println("FAILED  " + what);
        }
    }

    // ------------------------------------------------------------ live

    private void live(String host, int port, int runs, String save)
            throws IOException, InterruptedException {
        NtNatives.load();
        NetworkTableInstance inst = NetworkTableInstance.create();
        inst.startClient4("ntapicheck");
        inst.setServer(host, port);
        long until = System.currentTimeMillis() + 10_000;
        while (!inst.isConnected() && System.currentTimeMillis() < until) Thread.sleep(50);
        expect(inst.isConnected(), "connected to " + host + ":" + port);
        if (!inst.isConnected()) return;

        hierarchy(inst);

        Map<String, GenericSubscriber> subs = new LinkedHashMap<>();
        List<Entry> fixed = new ArrayList<>(NtApiCheckOpMode.FIXED);
        fixed.add(NtApiCheckOpMode.BEFORE_SERVE);
        for (Entry e : fixed) subs.put(e.name, subscribe(inst, e.name, e.type));
        String[][] changing = {{NtApiCheckOpMode.STEP, "int"}, {NtApiCheckOpMode.HALF, "double"},
                {NtApiCheckOpMode.EVEN, "boolean"}, {NtApiCheckOpMode.WORD, "string"},
                {NtApiCheckOpMode.TRIPLE, "int[]"}, {NtApiCheckOpMode.POSE, "struct:Pose2d"},
                {NtApiCheckOpMode.RUN, "string"}};
        for (String[] c : changing) subs.put(c[0], subscribe(inst, c[0], c[1]));

        String lastRun = null;
        long lastStep = -1;
        Map<String, Set<String>> received = new LinkedHashMap<>();
        for (int run = 1; run <= runs; run++) {
            if (run > 1) {
                System.out.println("\nStop the OpMode and run it again: Initialize, then Start.");
                lastRun = waitForNewRun(subs.get(NtApiCheckOpMode.RUN), lastRun);
                expect(lastRun != null, "run " + run + ": a new value of " + NtApiCheckOpMode.RUN);
                if (lastRun == null) break;
                waitForStep(subs.get(NtApiCheckOpMode.STEP), lastStep);
            } else {
                lastRun = waitForValue(subs.get(NtApiCheckOpMode.RUN)) == null ? null
                        : subs.get(NtApiCheckOpMode.RUN).get().getString();
            }
            System.out.println("\nrun " + run);
            received.clear();
            for (Entry e : fixed) {
                NetworkTableValue v = waitForValue(subs.get(e.name));
                String got = v == null ? "nothing" : canon(v);
                String want = canon(e);
                expect(want.equals(got) && e.type.equals(subs.get(e.name).getTopic().getTypeString()),
                        e.name + " (" + e.type + ") = " + want
                                + (want.equals(got) ? "" : ", got " + got));
                received.computeIfAbsent(e.name, k -> new TreeSet<>()).add(got);
            }
            long firstStep = changingValues(subs, received);
            if (run > 1) {
                expect(firstStep < lastStep, "run " + run + ": the step began again (" + firstStep
                        + " after " + lastStep + ")");
            }
            for (String s : received.getOrDefault(NtApiCheckOpMode.STEP, new TreeSet<>())) {
                lastStep = Math.max(lastStep, Long.parseLong(s));
            }
        }
        if (save != null) {
            StringBuilder out = new StringBuilder();
            for (Map.Entry<String, Set<String>> e : received.entrySet()) {
                for (String v : e.getValue()) out.append(e.getKey()).append('\t').append(v).append('\n');
            }
            Files.write(Paths.get(save), out.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("\nwrote " + save);
        }
    }

    private static GenericSubscriber subscribe(NetworkTableInstance inst, String name, String type) {
        return inst.getTopic(name).genericSubscribe(type, PubSubOption.periodic(0.02));
    }

    /**
     * Widens a {@code topicsonly} subscription one prefix at a time, and after
     * each checks that what the server announced is exactly what lies under the
     * prefixes asked for so far, with each type right.
     */
    private void hierarchy(NetworkTableInstance inst) throws InterruptedException {
        Map<String, String> all = new LinkedHashMap<>();
        for (Entry e : NtApiCheckOpMode.FIXED) all.put(e.name, e.type);
        all.put(NtApiCheckOpMode.BEFORE_SERVE.name, NtApiCheckOpMode.BEFORE_SERVE.type);
        all.put(NtApiCheckOpMode.RUN, "string");
        all.put(NtApiCheckOpMode.STEP, "int");
        all.put(NtApiCheckOpMode.HALF, "double");
        all.put(NtApiCheckOpMode.EVEN, "boolean");
        all.put(NtApiCheckOpMode.WORD, "string");
        all.put(NtApiCheckOpMode.TRIPLE, "int[]");
        all.put(NtApiCheckOpMode.POSE, "struct:Pose2d");

        String[] prefixes = {"/apicheck/nothing/", "/apicheck/deep/a/b/c/d/", "/apicheck/deep/",
                "/apicheck/types/scalar/", "/apicheck/types/", "/apicheck/", ""};
        List<String> asked = new ArrayList<>();
        List<MultiSubscriber> held = new ArrayList<>();
        for (String prefix : prefixes) {
            asked.add(prefix);
            held.add(new MultiSubscriber(inst, new String[]{prefix}, PubSubOption.topicsOnly(true)));
            Set<String> want = new TreeSet<>();
            for (String name : all.keySet()) {
                for (String p : asked) if (name.startsWith(p)) want.add(name);
            }
            Map<String, String> got = waitForTopics(inst, want);
            Set<String> gotApi = new TreeSet<>();
            boolean outside = false;
            for (String name : got.keySet()) {
                if (name.startsWith("/apicheck/")) gotApi.add(name);
                boolean under = false;
                for (String p : asked) under |= name.startsWith(p);
                outside |= !under;
            }
            boolean types = true;
            for (String name : gotApi) types &= all.containsKey(name) && all.get(name).equals(got.get(name));
            expect(want.equals(gotApi) && !outside && types, "prefix \"" + prefix + "\" announces "
                    + want.size() + " /apicheck topics, each with its type, and nothing outside "
                    + asked + (want.equals(gotApi) ? "" : "; got " + gotApi));
            if (prefix.isEmpty()) {
                boolean schemas = true;
                for (String s : SCHEMAS) schemas &= "structschema".equals(got.get("/.schema/struct:" + s));
                expect(schemas, "the " + SCHEMAS.length + " struct schemas are announced as structschema");
            }
        }
        for (MultiSubscriber m : held) m.close();
    }

    /** The topics announced so far, once {@code want} is among them or 3 s have passed, and 300 ms more. */
    private static Map<String, String> waitForTopics(NetworkTableInstance inst, Set<String> want)
            throws InterruptedException {
        long until = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < until && !topics(inst).keySet().containsAll(want)) {
            Thread.sleep(50);
        }
        Thread.sleep(300);
        return topics(inst);
    }

    private static Map<String, String> topics(NetworkTableInstance inst) {
        Map<String, String> out = new HashMap<>();
        for (TopicInfo t : inst.getTopicInfo()) out.put(t.name, t.typeStr);
        return out;
    }

    private static NetworkTableValue waitForValue(GenericSubscriber sub) throws InterruptedException {
        long until = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < until && !sub.get().isValid()) Thread.sleep(20);
        return sub.get().isValid() ? sub.get() : null;
    }

    private static String waitForNewRun(GenericSubscriber run, String last) throws InterruptedException {
        long until = System.currentTimeMillis() + 180_000;
        while (System.currentTimeMillis() < until) {
            NetworkTableValue v = run.get();
            if (v.isValid() && !v.getString().equals(last)) return v.getString();
            Thread.sleep(100);
        }
        return null;
    }

    /** Waits up to 30 s for the new run's first step, which is below the last run's. */
    private static void waitForStep(GenericSubscriber step, long lastStep) throws InterruptedException {
        long until = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < until) {
            NetworkTableValue v = step.get();
            if (v.isValid() && v.getInteger() < lastStep) return;
            Thread.sleep(20);
        }
    }

    /**
     * Three seconds of the changing values, each checked on its own against
     * the rule that made it. Returns the smallest step seen.
     */
    private long changingValues(Map<String, GenericSubscriber> subs, Map<String, Set<String>> received)
            throws InterruptedException {
        for (GenericSubscriber s : subs.values()) s.readQueue();
        List<NetworkTableValue> steps = new ArrayList<>();
        Map<String, List<NetworkTableValue>> seen = new LinkedHashMap<>();
        long until = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < until) {
            for (String name : new String[]{NtApiCheckOpMode.STEP, NtApiCheckOpMode.HALF,
                    NtApiCheckOpMode.EVEN, NtApiCheckOpMode.WORD, NtApiCheckOpMode.TRIPLE,
                    NtApiCheckOpMode.POSE}) {
                List<NetworkTableValue> list = seen.computeIfAbsent(name, k -> new ArrayList<>());
                list.addAll(Arrays.asList(subs.get(name).readQueue()));
            }
            Thread.sleep(50);
        }
        for (Map.Entry<String, List<NetworkTableValue>> e : seen.entrySet()) {
            for (NetworkTableValue v : e.getValue()) {
                received.computeIfAbsent(e.getKey(), k -> new TreeSet<>()).add(canon(v));
            }
        }
        steps.addAll(seen.get(NtApiCheckOpMode.STEP));
        boolean rising = true;
        long first = Long.MAX_VALUE;
        for (int i = 0; i < steps.size(); i++) {
            first = Math.min(first, steps.get(i).getInteger());
            if (i > 0) rising &= steps.get(i).getInteger() > steps.get(i - 1).getInteger();
        }
        expect(steps.size() >= 10 && rising, NtApiCheckOpMode.STEP + " (int) rose through "
                + steps.size() + " values in 3 s");

        boolean half = !seen.get(NtApiCheckOpMode.HALF).isEmpty();
        for (NetworkTableValue v : seen.get(NtApiCheckOpMode.HALF)) {
            double twice = v.getDouble() * 2;
            half &= twice >= 1 && twice == Math.rint(twice);
        }
        expect(half, NtApiCheckOpMode.HALF + " (double) is a step / 2 every time, "
                + seen.get(NtApiCheckOpMode.HALF).size() + " values");

        boolean sawTrue = false;
        boolean sawFalse = false;
        for (NetworkTableValue v : seen.get(NtApiCheckOpMode.EVEN)) {
            sawTrue |= v.getBoolean();
            sawFalse |= !v.getBoolean();
        }
        expect(sawTrue && sawFalse, NtApiCheckOpMode.EVEN + " (boolean) was both true and false");

        boolean words = !seen.get(NtApiCheckOpMode.WORD).isEmpty();
        for (NetworkTableValue v : seen.get(NtApiCheckOpMode.WORD)) words &= WORD.matcher(v.getString()).matches();
        expect(words, NtApiCheckOpMode.WORD + " (string) is \"step N\" every time, "
                + seen.get(NtApiCheckOpMode.WORD).size() + " values");

        boolean triples = !seen.get(NtApiCheckOpMode.TRIPLE).isEmpty();
        for (NetworkTableValue v : seen.get(NtApiCheckOpMode.TRIPLE)) {
            long[] t = v.getIntegerArray();
            triples &= t.length == 3 && t[0] >= 1 && t[1] == t[0] + 1 && t[2] == t[0] + 2;
        }
        expect(triples, NtApiCheckOpMode.TRIPLE + " (int[]) is {n, n+1, n+2} every time, "
                + seen.get(NtApiCheckOpMode.TRIPLE).size() + " values");

        boolean poses = !seen.get(NtApiCheckOpMode.POSE).isEmpty();
        for (NetworkTableValue v : seen.get(NtApiCheckOpMode.POSE)) {
            ByteBuffer b = ByteBuffer.wrap(v.getRaw()).order(ByteOrder.LITTLE_ENDIAN);
            double x = b.getDouble(0) * 1000;
            poses &= v.getRaw().length == 24 && Math.abs(x - Math.rint(x)) < 1e-6 && x >= 1
                    && b.getDouble(8) == 0 && b.getDouble(16) == 0;
        }
        expect(poses, NtApiCheckOpMode.POSE + " (struct:Pose2d) is at step / 1000 m every time, "
                + seen.get(NtApiCheckOpMode.POSE).size() + " values");
        return first;
    }

    // ------------------------------------------------------------ the log

    private void compareLog(String log, String against) throws IOException {
        Map<String, Set<String>> inLog = new HashMap<>();
        Map<Integer, String[]> entries = new HashMap<>();
        DataLogReader reader = new DataLogReader(log);
        expect(reader.isValid(), log + " is a valid WPILOG");
        for (DataLogRecord r : reader) {
            if (r.isStart()) {
                DataLogRecord.StartRecordData d = r.getStartData();
                String name = d.name.startsWith("/") ? d.name : "/" + d.name;
                entries.put(d.entry, new String[]{name, d.type});
            } else if (!r.isControl() && entries.containsKey(r.getEntry())) {
                String[] e = entries.get(r.getEntry());
                inLog.computeIfAbsent(e[0], k -> new TreeSet<>()).add(canon(r, e[1]));
            }
        }
        int checked = 0;
        List<String> missing = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(against), StandardCharsets.UTF_8)) {
            if (line.isEmpty()) continue;
            String[] f = line.split("\t", 2);
            checked++;
            if (!inLog.getOrDefault(f[0], new TreeSet<>()).contains(f[1])) missing.add(line);
        }
        expect(checked > 0 && missing.isEmpty(), "every one of " + checked
                + " values the client received is in the log" + (missing.isEmpty() ? "" : "; missing " + missing));
    }

    // ------------------------------------------------------------ one spelling for every value

    private static String canon(NetworkTableValue v) {
        switch (v.getType()) {
            case kBoolean: return Boolean.toString(v.getBoolean());
            case kDouble: return Double.toString(v.getDouble());
            case kFloat: return Float.toString(v.getFloat());
            case kInteger: return Long.toString(v.getInteger());
            case kString: return v.getString();
            case kBooleanArray: return Arrays.toString(v.getBooleanArray());
            case kDoubleArray: return Arrays.toString(v.getDoubleArray());
            case kIntegerArray: return Arrays.toString(v.getIntegerArray());
            case kStringArray: return Arrays.toString(v.getStringArray());
            case kRaw: return hex(v.getRaw());
            default: return "unexpected type " + v.getType();
        }
    }

    private static String canon(DataLogRecord r, String type) {
        switch (type) {
            case "boolean": return Boolean.toString(r.getBoolean());
            case "double": return Double.toString(r.getDouble());
            case "float": return Float.toString(r.getFloat());
            case "int64": return Long.toString(r.getInteger());
            case "string": return r.getString();
            case "boolean[]": return Arrays.toString(r.getBooleanArray());
            case "double[]": return Arrays.toString(r.getDoubleArray());
            case "int64[]": return Arrays.toString(r.getIntegerArray());
            case "string[]": return Arrays.toString(r.getStringArray());
            default: return hex(r.getRaw());
        }
    }

    /** A double[] is the value of a double[] entry, and a struct's fields, packed little-endian, otherwise. */
    private static String canon(Entry e) {
        Object x = e.expected;
        if (x instanceof double[] && e.type.startsWith("struct:")) {
            double[] fields = (double[]) x;
            ByteBuffer b = ByteBuffer.allocate(8 * fields.length).order(ByteOrder.LITTLE_ENDIAN);
            for (double f : fields) b.putDouble(f);
            return hex(b.array());
        }
        if (x instanceof double[]) return Arrays.toString((double[]) x);
        if (x instanceof boolean[]) return Arrays.toString((boolean[]) x);
        if (x instanceof long[]) return Arrays.toString((long[]) x);
        if (x instanceof String[]) return Arrays.toString((String[]) x);
        if (x instanceof byte[]) return hex((byte[]) x);
        return String.valueOf(x);
    }

    private static String hex(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (byte b : bytes) s.append(String.format("%02x", b));
        return s.toString();
    }
}
