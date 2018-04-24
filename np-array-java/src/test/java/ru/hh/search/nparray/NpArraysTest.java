package ru.hh.search.nparray;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class NpArraysTest {



  @Test
  public void testSerializer() throws IOException, ClassNotFoundException {
    NpArrays npArrays = new NpArrays();
    float[][] floats1 = generateArrayFloat(11, 10, 2.4f);
    npArrays.add(floats1, "10_10_2.4");
    float[][] floats2 = generateArrayFloat(23, 20, -5.2f);
    npArrays.add(floats2, "20_20_5.2");
    int[][] ints1 = generateArrayInt(22, 21, 5);
    npArrays.add(ints1, "20_20_5");
    int[][] ints2 = generateArrayInt(22, 22, -6);
    npArrays.add(ints2, "20_20_6");

    assertArrayEquals(floats1, npArrays.getFloatArray("10_10_2.4"));
    assertArrayEquals(floats2, npArrays.getFloatArray("20_20_5.2"));
    assertArrayEquals(ints1, npArrays.getIntArray("20_20_5"));
    assertArrayEquals(ints2, npArrays.getIntArray("20_20_6"));


    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));

    assertArrayEquals(newNpArrays.getFloatArray("10_10_2.4"), npArrays.getFloatArray("10_10_2.4"));
    assertArrayEquals(newNpArrays.getFloatArray("20_20_5.2"), npArrays.getFloatArray("20_20_5.2"));
    assertArrayEquals(newNpArrays.getIntArray("20_20_5"), npArrays.getIntArray("20_20_5"));
    assertArrayEquals(newNpArrays.getIntArray("20_20_6"), npArrays.getIntArray("20_20_6"));

    assertArrayEquals(floats1, newNpArrays.getFloatArray("10_10_2.4"));
    assertArrayEquals(floats2, newNpArrays.getFloatArray("20_20_5.2"));
    assertArrayEquals(ints1, newNpArrays.getIntArray("20_20_5"));
    assertArrayEquals(ints2, newNpArrays.getIntArray("20_20_6"));

    Files.delete(Paths.get("123"));
  }

  @Test(expected = NullPointerException.class)
  public void testOnlyHeaders() throws IOException {
    NpArrays npArrays = new NpArrays();
    float[][] floats1 = generateArrayFloat(11, 10, 2.4f);
    npArrays.add(floats1, "10_10_2.4");
    float[][] floats2 = generateArrayFloat(23, 20, -5.2f);
    npArrays.add(floats2, "20_20_5.2");
    int[][] ints1 = generateArrayInt(22, 21, 5);
    npArrays.add(ints1, "20_20_5");
    int[][] ints2 = generateArrayInt(22, 22, -6);
    npArrays.add(ints2, "20_20_6");

    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays headersNpArrays = NpArraySerializers.deserialize(Paths.get("123"), true);

    Files.delete(Paths.get("123"));

    float[] npe = headersNpArrays.floatsArrays[0][0];
  }

  private float[][] generateArrayFloat(int column, int row, float elem) {
    float[][] floats = new float[row][column];
    for (int i = 0; i < column; i++) {
      for (int j = 0; j < row; j++) {
        floats[j][i] = elem;
      }
    }
    return floats;
  }

  private int[][] generateArrayInt(int column, int row, int elem) {
    int[][] ints = new int[row][column];
    for (int i = 0; i < column; i++) {
      for (int j = 0; j < row; j++) {
        ints[j][i] = elem;
      }
    }
    return ints;
  }
}
