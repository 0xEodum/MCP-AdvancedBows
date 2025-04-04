package com.example.advancedbows.utils;
import org.bukkit.util.Vector;
import java.util.Random;
public class MathUtils {
    private static final Random random = new Random();
    public static Vector getRandomDeviation(Vector baseVector, double maxDeviationDegrees) {
        Vector normalizedBase = baseVector.clone().normalize();
        Vector perpendicular1 = getPerpendicular(normalizedBase);
        Vector perpendicular2 = normalizedBase.clone().crossProduct(perpendicular1).normalize();
        double deviationDegrees = random.nextDouble() * maxDeviationDegrees;
        double deviationRadians = Math.toRadians(deviationDegrees);
        double directionAngle = random.nextDouble() * 2 * Math.PI;
        double xComp = Math.sin(deviationRadians) * Math.cos(directionAngle);
        double yComp = Math.sin(deviationRadians) * Math.sin(directionAngle);
        double zComp = Math.cos(deviationRadians);
        Vector result = normalizedBase.clone().multiply(zComp)
                .add(perpendicular1.clone().multiply(xComp))
                .add(perpendicular2.clone().multiply(yComp));
        return result.normalize().multiply(baseVector.length());
    }
    public static Vector getPerpendicular(Vector vector) {
        if (Math.abs(vector.getX()) < Math.abs(vector.getY())) {
            return new Vector(0, -vector.getZ(), vector.getY()).normalize();
        } else {
            return new Vector(-vector.getZ(), 0, vector.getX()).normalize();
        }
    }
    public static Vector lerp(Vector a, Vector b, double t) {
        t = Math.max(0, Math.min(1, t));
        return a.clone().add(b.clone().subtract(a).multiply(t));
    }
}