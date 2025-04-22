package dk.itu.mario.level.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Global {

    public static Map<String, Double> Parameters = new HashMap<String, Double>() {{
        put("Population", 30.0);
        put("MaxIterations", 10.0);
        put("ParticleWidth", 30.0);
        put("ParticleReferenceWidth", 10.0);
        put("W", 0.01);
        put("Cmin", 0.05);
        put("Cmax", 0.35);
        put("Rmin", 0.0);
        put("Rmax", 0.2);
    }};

    public static boolean IsDebug = false;
    public static float Gravity = 1000F;
    public static float Friction = 1000F;
    public static long TilePixelWidth = 64;
    public static float PlayerMaxXSpeed = 500F;
    public static float PlayerJumpForce = -Gravity * 0.59F;
    public static long RenderWidth = 60;
    public static boolean IsRightGenBusy = true;
    public static boolean IsLeftGenBusy = true;

    public static Random random = new Random();

    public static long getPopulation() {
        return Parameters.get("Population").longValue();
    }

    public static void setPopulation(long value) {
        Parameters.put("Population", (double) value);
    }

    public static long getParticleWidth() {
        return Parameters.get("ParticleWidth").longValue();
    }

    public static void setParticleWidth(long value) {
        Parameters.put("ParticleWidth", (double) value);
    }

    public static long getMaxIterations() {
        return Parameters.get("MaxIterations").longValue();
    }

    public static void setMaxIterations(long value) {
        Parameters.put("MaxIterations", (double) value);
    }
}
