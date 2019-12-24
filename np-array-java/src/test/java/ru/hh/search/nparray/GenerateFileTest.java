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

    String[][] strings1 = new String[2][1];
    strings1[0][0] = "asdcs";
    strings1[1][0] = "апапа";
    npArrays.add(strings1, "byte_string2");

    String[][] strings2 = new String[3][2];
    strings2[0][0] = "asdcs";
    strings2[0][1] = "!!!U3gARt7BC9VwlAnxFHQ--";
    strings2[1][0] = "апа\nпа";
    strings2[1][1] = "!-0123456789=ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    strings2[2][0] = "整数";
    strings2[2][1] = "&&()";
    npArrays.add(strings2, "байт_стринга1");

    String[][] strings3 = new String[1][2];
    strings3[0][0] = "asdcs";
    strings3[0][1] = "!!!U3gARt7BC9VwlAnxFHQ--";
    npArrays.add(strings3, "строчечка3");

    NpArraySerializers.serialize(npArrays, Paths.get(args[0]));
  }
}
