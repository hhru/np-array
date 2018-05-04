package ru.hh.search.nparray;

import java.io.IOException;
import java.nio.file.Paths;

public class GenerateFileTest {
  public static void main(String... args) throws IOException {
    NpArrays npArrays = new NpArrays();

    float[][] floats1 = new float[2][3];
    floats1[0][0] = 10.0001f;
    floats1[0][1] = 0.0000001f;
    floats1[0][2] = 3.88E-40f;
    floats1[1][0] = -110.9999f;
    floats1[1][1] = 1.1E20f;
    floats1[1][2] = Float.NEGATIVE_INFINITY;
    npArrays.add(floats1, "fffff1");

    float[][] floats2 = new float[1][4];
    floats2[0][0] = Float.MAX_VALUE;
    floats2[0][1] = Float.MIN_NORMAL;
    floats2[0][2] = Float.MIN_VALUE;
    floats2[0][3] = Float.NaN;
    npArrays.add(floats2, "флоат!");

    int[][] ints1 = new int[3][2];
    ints1[0][0] = Integer.MAX_VALUE;
    ints1[0][1] = Integer.MIN_VALUE;
    ints1[1][0] = 0;
    ints1[1][1] = 34343;
    ints1[2][0] = 34343545;
    ints1[2][1] = -23231;
    npArrays.add(ints1, "iiiii1");

    int[][] ints2 = new int[2][1];
    ints2[0][0] = 455545;
    ints2[1][0] = -34444343;
    npArrays.add(ints2, "整数");

    NpArraySerializers.serialize(npArrays, Paths.get(args[0]));
  }
}
