package ru.hh.search.nparray;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class NpArraysTest {

  @Test
  public void testSerializer() throws IOException, ClassNotFoundException {
    NpArrays npArrays = new NpArrays();
    float[][] floats1 = generateArrayFloat(2, 2, 2.4f);
    npArrays.add(floats1, "10_10_2.4");
    float[][] floats2 = generateArrayFloat(2, 2, -5.2f);
    npArrays.add(floats2, "20_20_5.2");
    int[][] ints1 = generateArrayInt(2, 2, 5);
    npArrays.add(ints1, "20_20_5");
    int[][] ints2 = generateArrayInt(2, 2, -6);
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

  private void printFloatArray(float[][] floatArray) {
    for (int i = 0; i < floatArray.length; i++) {
      for (int j = 0; j < floatArray[0].length; j++) {
        System.out.print(floatArray[j][i] + " ");
      }
      System.out.println();
    }
    System.out.println();
  }

  @Test
  public void onlyFloatsSmall() throws IOException {
    NpArrays npArrays = new NpArrays();
    float[][] floats = generateArrayFloat(2, 2, 2.4f);
    npArrays.add(floats, "10_10_2.4");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    assertArrayEquals(floats, newNpArrays.getFloatArray("10_10_2.4"));
    Files.delete(Paths.get("123"));
  }

  @Test
  public void onlyFloats() throws IOException {
    NpArrays npArrays = new NpArrays();
    float[][] floats = generateArrayFloat(3, 3, 2.4f);
    npArrays.add(floats, "10_10_2.4");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    assertArrayEquals(floats, newNpArrays.getFloatArray("10_10_2.4"));
    Files.delete(Paths.get("123"));
  }

  @Test
  public void onlyIntegers() throws IOException {
    NpArrays npArrays = new NpArrays();
    int[][] ints = generateArrayInt(100, 100, 2);
    npArrays.add(ints, "10_10_2.4");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    assertArrayEquals(ints, newNpArrays.getIntArray("10_10_2.4"));
    Files.delete(Paths.get("123"));
  }

  @Test
  public void loadTest() throws IOException {
    NpArrays npArrays = new NpArrays();
    float[][] floats = generateArrayFloat(100000, 100, 2.4f);
    npArrays.add(floats, "10_10_2.4");
    long time = System.nanoTime();
    System.out.println("Start serialize");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    System.out.println("Finish serialize: " + ((System.nanoTime() - time)/1000_000));
    time = System.nanoTime();
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    System.out.println("Finish deserialize: " + ((System.nanoTime() - time)/1000_000));
    assertArrayEquals(floats, newNpArrays.getFloatArray("10_10_2.4"));

  }


  @Test
  public void partitionReadTest() throws IOException {
    NpArrays npArrays = new NpArrays();
    Path path = Paths.get("123");
    float[][] floats1 = generateArrayFloat(2, 2, 2.4f);
    npArrays.add(floats1, "10_10_2.4");
    float[][] floats2 = generateArrayFloat(2, 2, -5.2f);
    npArrays.add(floats2, "20_20_5.2");
    int[][] ints1 = generateArrayInt(2, 2, 5);
    npArrays.add(ints1, "20_20_5");
    int[][] ints2 = generateArrayInt(2, 2, -6);
    npArrays.add(ints2, "20_20_6");
    NpArraySerializers.serialize(npArrays, path);
    NpHeaders headers = NpArraySerializers.getOnlyHeaders(path);

    float[][] newFloats2 = NpArraySerializers.getFloatArray(path, headers, "20_20_5.2");
    int[][] newInts2 = NpArraySerializers.getIntArray(path, headers, "20_20_6");

    assertArrayEquals(floats2, newFloats2);
    assertArrayEquals(ints2, newInts2);
  }

  private float[][] generateArrayFloat(int column, int row, float elem) {
    float[][] floats = new float[row][column];
    for (int i = 0; i < column; i++) {
      for (int j = 0; j < row; j++) {
        floats[j][i] = elem + (1 + i) * (j + 1);
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
