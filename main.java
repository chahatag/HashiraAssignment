import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class main {

    static class Point {
        BigInteger x;
        BigInteger y;
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Parse JSON and extract all points
    static List<Point> readPointsFromJson(String filePath, int[] kOut) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(new FileReader(filePath));

        JSONObject keys = (JSONObject) json.get("keys");
        int n = Integer.parseInt(keys.get("n").toString());
        int k = Integer.parseInt(keys.get("k").toString());
        kOut[0] = k;

        List<Point> points = new ArrayList<>();

        for (Object keyObj : json.keySet()) {
            String key = keyObj.toString();
            if (key.equals("keys")) continue;

            JSONObject pointObj = (JSONObject) json.get(key);
            int base = Integer.parseInt(pointObj.get("base").toString());
            String value = pointObj.get("value").toString();

            BigInteger x = new BigInteger(key);
            BigInteger y = new BigInteger(value, base);

            points.add(new Point(x, y));
        }
        return points;
    }

    // Generate all combinations of size k from given list
    static List<List<Point>> getAllCombinations(List<Point> points, int k) {
        List<List<Point>> result = new ArrayList<>();
        generateCombos(points, new ArrayList<>(), 0, k, result);
        return result;
    }

    static void generateCombos(List<Point> points, List<Point> current, int index, int k, List<List<Point>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = index; i < points.size(); i++) {
            current.add(points.get(i));
            generateCombos(points, current, i + 1, k, result);
            current.remove(current.size() - 1);
        }
    }

    // Apply Lagrange interpolation to get f(0) = constant term
    static BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i == j) continue;

                BigInteger xj = points.get(j).x;

                numerator = numerator.multiply(xj.negate());
                denominator = denominator.multiply(xi.subtract(xj));
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    // Main handler per file
    static void processFile(String filePath) throws Exception {
        int[] kOut = new int[1];
        List<Point> allPoints = readPointsFromJson(filePath, kOut);
        int k = kOut[0];

        Set<BigInteger> secrets = new HashSet<>();

        List<List<Point>> combinations = getAllCombinations(allPoints, k);

        for (List<Point> combo : combinations) {
            BigInteger c = lagrangeInterpolation(combo);
            secrets.add(c);
        }

        if (secrets.size() == 1) {
            System.out.println("Secret from " + filePath + ": " + secrets.iterator().next());
        } else {
            System.out.println("Inconsistent secrets in " + filePath + ": " + secrets);
        }
    }

    public static void main(String[] args) throws Exception {
        processFile("input1.json");
        processFile("input2.json");
    }
}
