package ru.hh.search.nparray;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.List;
import ru.hh.search.nparray.arrays.CompressedIntArray;

public class GenerateFileTest {
  public static void main(String... args) throws IOException {

    float[][] floats1 = new float[2][3];
    floats1[0][0] = 10.0001f;
    floats1[0][1] = 0.0000001f;
    floats1[0][2] = 3.88E-40f;
    floats1[1][0] = -110.9999f;
    floats1[1][1] = 1.1E20f;
    floats1[1][2] = Float.NEGATIVE_INFINITY;

    float[][] floats2 = new float[1][4];
    floats2[0][0] = Float.MAX_VALUE;
    floats2[0][1] = Float.MIN_NORMAL;
    floats2[0][2] = Float.MIN_VALUE;
    floats2[0][3] = Float.NaN;

    int[][] ints1 = new int[3][2];
    ints1[0][0] = Integer.MAX_VALUE;
    ints1[0][1] = Integer.MIN_VALUE;
    ints1[1][0] = 0;
    ints1[1][1] = 34343;
    ints1[2][0] = 34343545;
    ints1[2][1] = -23231;

    int[][] ints2 = new int[2][1];
    ints2[0][0] = 455545;
    ints2[1][0] = -34444343;

    short[][] shorts1 = new short[2][1];
    shorts1[0][0] = 3433;
    shorts1[1][0] = -2222;

    short[][] halfs1 = new short[1][3];
    halfs1[0][0] = 15360;
    halfs1[0][1] = -10512;
    halfs1[0][2] = 31744;

    String[][] strings1 = new String[2][1];
    strings1[0][0] = "asdcs";
    strings1[1][0] = "апапа";

    String[][] strings2 = new String[3][2];
    strings2[0][0] = "asdcs";
    strings2[0][1] = "!!!U3gARt7BC9VwlAnxFHQ--";
    strings2[1][0] = "апа\nпа";
    strings2[1][1] = "!-0123456789=ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    strings2[2][0] = "整数";
    strings2[2][1] = "&&()";

    String[][] strings3 = new String[1][2];
    strings3[0][0] = "asdcs";
    strings3[0][1] = "!!!U3gARt7BC9VwlAnxFHQ--";

    try (var serializer = new NpArraySerializer(Paths.get(args[0]), ByteOrder.BIG_ENDIAN)) {
      serializer.writeArray("byte_string2", strings1);
      serializer.writeArray(CompressedIntArray.ofWithCopy(
              "compressed_int_array",
              List.of(new int[] {1, 100, 101, 106}, new int[] {999, 1000, 1010, 1060}))
      );
      serializer.writeArray("fffff1", floats1);
      serializer.writeHalfArray("h", halfs1);
      serializer.writeArray("iiiii1", ints1);
      serializer.writeArray("s", shorts1);
      serializer.writeArray("байт_стринга1", strings2);
      serializer.writeArray("строчечка3", strings3);
      serializer.writeArray("флоат!", floats2);
      serializer.writeArray("整数", ints2);
    }

    try (var serializer = new NpArraySerializer(Paths.get(args[1]), ByteOrder.LITTLE_ENDIAN)) {
      serializer.writeArray("byte_string2", strings1);
      serializer.writeArray(CompressedIntArray.ofWithCopy(
              "compressed_int_array",
              List.of(new int[] {1, 100, 101, 106}, new int[] {999, 1000, 1010, 1060}))
      );
      serializer.writeArray("fffff1", floats1);
      serializer.writeHalfArray("h", halfs1);
      serializer.writeArray("iiiii1", ints1);
      serializer.writeArray("s", shorts1);
      serializer.writeArray("байт_стринга1", strings2);
      serializer.writeArray("строчечка3", strings3);
      serializer.writeArray("флоат!", floats2);
      serializer.writeArray("整数", ints2);
    }
  }
}
