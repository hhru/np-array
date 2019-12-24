package ru.hh.search.nparray;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
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
    String[][] strings1 = generateArrayString(2, 3, "hello");
    npArrays.add(strings1, "30_30_hello");
    String[][] strings2 = generateArrayString(2, 3, "хай");
    npArrays.add(strings2, "40_40_hi");

    assertEquals(NpBase.ACTUAL_VERSION, npArrays.getVersion());

    assertArrayEquals(floats1, npArrays.getFloatArray("10_10_2.4"));
    assertArrayEquals(floats2, npArrays.getFloatArray("20_20_5.2"));
    assertArrayEquals(ints1, npArrays.getIntArray("20_20_5"));
    assertArrayEquals(ints2, npArrays.getIntArray("20_20_6"));
    assertArrayEquals(strings1, npArrays.getStringArray("30_30_hello"));
    assertArrayEquals(strings2, npArrays.getStringArray("40_40_hi"));


    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));

    assertArrayEquals(newNpArrays.getFloatArray("10_10_2.4"), npArrays.getFloatArray("10_10_2.4"));
    assertArrayEquals(newNpArrays.getFloatArray("20_20_5.2"), npArrays.getFloatArray("20_20_5.2"));
    assertArrayEquals(newNpArrays.getIntArray("20_20_5"), npArrays.getIntArray("20_20_5"));
    assertArrayEquals(newNpArrays.getIntArray("20_20_6"), npArrays.getIntArray("20_20_6"));
    assertArrayEquals(newNpArrays.getStringArray("30_30_hello"), npArrays.getStringArray("30_30_hello"));
    assertArrayEquals(newNpArrays.getStringArray("40_40_hi"), npArrays.getStringArray("40_40_hi"));

    assertArrayEquals(floats1, newNpArrays.getFloatArray("10_10_2.4"));
    assertArrayEquals(floats2, newNpArrays.getFloatArray("20_20_5.2"));
    assertArrayEquals(ints1, newNpArrays.getIntArray("20_20_5"));
    assertArrayEquals(ints2, newNpArrays.getIntArray("20_20_6"));
    assertArrayEquals(strings1, newNpArrays.getStringArray("30_30_hello"));
    assertArrayEquals(strings2, newNpArrays.getStringArray("40_40_hi"));

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
  public void onlyStrings() throws IOException {
    NpArrays npArrays = new NpArrays();
    String[][] strings = generateArrayString(100, 100, "test");
    npArrays.add(strings, "10_10_test");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    assertArrayEquals(strings, newNpArrays.getStringArray("10_10_test"));
    Files.delete(Paths.get("123"));
  }

  @Test
  public void stringsWithSpecialSymbols() throws IOException {
    NpArrays npArrays = new NpArrays();
    String[][] strings = generateArrayString(100, 100, "test");
    strings[0][0] += "\ntest";
    strings[0][1] += "\t another test";
    strings[strings.length - 1][strings[0].length - 1] += "\n";
    npArrays.add(strings, "10_10_test");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    assertArrayEquals(strings, newNpArrays.getStringArray("10_10_test"));
    Files.delete(Paths.get("123"));
  }

  @Test
  public void loadTest() throws IOException {
    NpArrays npArrays = new NpArrays();
    float[][] floats = generateArrayFloat(100_000, 100, 2.4f);
    npArrays.add(floats, "10_10_2.4");
    long time = System.nanoTime();
    System.out.println("Start serialize");
    NpArraySerializers.serialize(npArrays, Paths.get("123"));
    System.out.println("Finish serialize: " + ((System.nanoTime() - time) / 1_000_000));
    time = System.nanoTime();
    NpArrays newNpArrays = NpArraySerializers.deserialize(Paths.get("123"));
    System.out.println("Finish deserialize: " + ((System.nanoTime() - time) / 1_000_000));
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
    String[][] strings1 = generateArrayString(2, 3, "привет");
    npArrays.add(strings1, "30_30_hello");
    String[][] strings2 = generateArrayString(2, 3, "hi");
    npArrays.add(strings2, "40_40_hi");
    NpArraySerializers.serialize(npArrays, path);
    NpHeaders headers = NpArraySerializers.getOnlyHeaders(path);

    float[][] newFloats2 = NpArraySerializers.getFloatArray(path, headers, "20_20_5.2");
    int[][] newInts2 = NpArraySerializers.getIntArray(path, headers, "20_20_6");
    String[][] newStrings2 = NpArraySerializers.getStringArray(path, headers, "40_40_hi");
    String[][] newStrings1 = NpArraySerializers.getStringArray(path, headers, "30_30_hello");

    assertArrayEquals(floats2, newFloats2);
    assertArrayEquals(ints2, newInts2);
    assertArrayEquals(strings2, newStrings2);
    assertArrayEquals(strings1, newStrings1);
  }

  @Test
  @Ignore
  public void hugeArrayTestLoadEach() throws IOException {
    NpArrays npArrays = new NpArrays();
    Path path = Paths.get("123");
    float[][] floats1 = generateArrayFloat(580_864_151, 1, 2.4f);
    npArrays.add(floats1, "looongFloat");
    int[][] ints1 = generateArrayInt(580_864_151, 1, 5);
    npArrays.add(ints1, "looongInt1");
    int[][] ints2 = generateArrayInt(580_864_151, 1, 10);
    npArrays.add(ints2, "looongInt2");
    String[][] strings1 = generateArrayString(580_864_151, 1, "test");
    npArrays.add(strings1, "looongString1");
    String[][] strings2 = generateArrayString(580_864_151, 2, "another");
    npArrays.add(strings2, "looongString2");
    NpArraySerializers.serialize(npArrays, path);
    NpHeaders headers = NpArraySerializers.getOnlyHeaders(path);

    assertArrayEquals(floats1, NpArraySerializers.getFloatArray(path, headers, "looongFloat"));
    assertArrayEquals(ints1, NpArraySerializers.getIntArray(path, headers, "looongInt1"));
    assertArrayEquals(ints2, NpArraySerializers.getIntArray(path, headers, "looongInt2"));
    assertArrayEquals(strings1, NpArraySerializers.getStringArray(path, headers, "looongString1"));
    assertArrayEquals(strings2, NpArraySerializers.getStringArray(path, headers, "looongString2"));
  }

  @Test
  @Ignore
  public void hugeArrayTest() throws IOException {
    NpArrays npArrays = new NpArrays();
    Path path = Paths.get("123");
    float[][] floats1 = generateArrayFloat(580_874_151, 1, 2.4f);
    npArrays.add(floats1, "looongFloat");
    int[][] ints1 = generateArrayInt(581_864_151, 1, 5);
    npArrays.add(ints1, "looongInt1");
    int[][] ints2 = generateArrayInt(582_864_151, 2, 11);
    npArrays.add(ints2, "looongInt2");
    String[][] strings1 = generateArrayString(580_864_151, 1, "test");
    npArrays.add(strings1, "looongString1");
    String[][] strings2 = generateArrayString(580_864_151, 2, "another");
    npArrays.add(strings2, "looongString2");
    NpArraySerializers.serialize(npArrays, path);

    NpArrays result = NpArraySerializers.deserialize(path);

    assertArrayEquals(floats1, result.getFloatArray("looongFloat"));
    assertArrayEquals(ints1, result.getIntArray("looongInt1"));
    assertArrayEquals(ints2, result.getIntArray("looongInt2"));
    assertArrayEquals(strings1, result.getStringArray("looongString1"));
    assertArrayEquals(strings2, result.getStringArray("looongString2"));
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

  private String[][] generateArrayString(int column, int row, String elem) {
    String[][] result = new String[row][column];
    for (int i = 0; i < column; i++) {
      for (int j = 0; j < row; j++) {
        result[j][i] = elem;
      }
    }
    return result;
  }
}
