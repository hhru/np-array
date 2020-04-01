package ru.hh.search.nparray;

import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class NpArraysTest {

  private Path tempFilePath;

  @Before
  public void prepare() throws IOException {
    tempFilePath = Files.createTempFile("nparray", "data").toAbsolutePath();
  }

  @After
  public void tearDown() throws IOException {
    if (Files.exists(tempFilePath)) {
      Files.delete(tempFilePath);
    }
  }

  @Test
  public void testSerialization() throws IOException {

    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
    }

    int[][] deserializedInts;
    float[][] deserializedFloats;
    String[][] deserializedString;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedInts = deserializer.getIntArray("1test");
      deserializedFloats = deserializer.getFloatArray("2test");
      deserializedString = deserializer.getStringArray("3test");
    }

    assertArrayEquals(ints, deserializedInts);
    assertArrayEquals(floats, deserializedFloats);
    assertArrayEquals(strings, deserializedString);

  }

  @Test
  public void testDeserializeAll() throws IOException {

    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
    }

    int[][] deserializedInts;
    float[][] deserializedFloats;
    String[][] deserializedString;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedInts = deserializer.getIntArray("1test");
      deserializedFloats = deserializer.getFloatArray("2test");
      deserializedString = deserializer.getStringArray("3test");
    }

    Map<String, Object> matrices;
    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      matrices = deserializer.deserialize();
    }

    assertArrayEquals(deserializedInts, (int[][])matrices.get("1test"));
    assertArrayEquals(deserializedFloats, (float[][])matrices.get("2test"));
    assertArrayEquals(deserializedString, (String[][])matrices.get("3test"));

  }

  @Test
  public void testSerializationOneByOne() throws IOException {

    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
    }

    int[][] deserializedInts;
    float[][] deserializedFloats;
    String[][] deserializedString;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedInts = deserializer.getIntArray("1test");
      deserializedFloats = deserializer.getFloatArray("2test");
      deserializedString = deserializer.getStringArray("3test");
    }

    assertArrayEquals(ints, deserializedInts);
    assertArrayEquals(floats, deserializedFloats);
    assertArrayEquals(strings, deserializedString);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectOrderSerialize() throws IOException {

    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, (float) Math.PI);

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("2test", floats);
      serializer.writeArray("1test", ints);
    }

  }


  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectOrderAccess() throws IOException {

    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, (float) Math.PI);

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("2test", floats);
      serializer.writeArray("1test", ints);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializer.getFloatArray("2test");
      deserializer.getIntArray("1test");
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonexistentArray() throws IOException {

    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, (float) Math.PI);

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializer.getFloatArray("4test");
    }

  }

  @Test
  public void onlyFloatsSmall() throws IOException {
    float[][] floats = generateArrayFloat(2, 2, 2.4f);
    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("10_10_2.4", floats);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(floats, deserializer.getFloatArray("10_10_2.4"));
    }
  }

  @Test
  public void onlyFloats() throws IOException {
    float[][] floats = generateArrayFloat(3, 3, 2.4f);
    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("10_10_2.4", floats);
    }
    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(floats, deserializer.getFloatArray("10_10_2.4"));
    }
  }

  @Test
  public void onlyIntegers() throws IOException {

    int[][] ints = generateArrayInt(100, 100, 2);
    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("10_10_2.4", ints);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(ints, deserializer.getIntArray("10_10_2.4"));
    }
  }

  @Test
  public void onlyStrings() throws IOException {
    String[][] strings = generateArrayString(100, 100, "test");
    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("10_10_test", strings);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(strings, deserializer.getStringArray("10_10_test"));
    }
  }

  @Test
  public void stringsWithSpecialSymbols() throws IOException {
    String[][] strings = generateArrayString(100, 100, "test");
    strings[0][0] += "\ntest";
    strings[0][1] += "\t another test";
    strings[strings.length - 1][strings[0].length - 1] += "\n";

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("10_10_test", strings);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(strings, deserializer.getStringArray("10_10_test"));
    }
  }

  @Test
  public void loadTest() throws IOException {
    float[][] floats = generateArrayFloat(100_000, 100, 2.4f);

    long time = System.nanoTime();
    System.out.println("Start serialize");
    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("10_10_2.4", floats);
    }
    System.out.println("Finish serialize: " + ((System.nanoTime() - time) / 1_000_000));
    time = System.nanoTime();
    float[][] deserializedFloats;
    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedFloats = deserializer.getFloatArray("10_10_2.4");
    }
    System.out.println("Finish deserialize: " + ((System.nanoTime() - time) / 1_000_000));
    assertArrayEquals(floats, deserializedFloats);
  }

  @Test
  public void partitionReadTest() throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
    }

    String[][] deserializedString;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedString = deserializer.getStringArray("3test");
    }

    assertArrayEquals(strings, deserializedString);
  }

  @Test
  @Ignore
  public void hugeArrayTest() throws IOException {
    float[][] floats1 = generateArrayFloat(580_864_151, 1, 2.4f);
    int[][] ints1 = generateArrayInt(580_864_151, 1, 5);
    int[][] ints2 = generateArrayInt(580_864_151, 1, 10);
    String[][] strings1 = generateArrayString(580_864_151, 1, "test");
    String[][] strings2 = generateArrayString(580_864_151, 2, "another");

    try (var serializer = new NpArraySerializer(tempFilePath)) {
      serializer.writeArray("looongFloat", floats1);
      serializer.writeArray("looongInt1", ints1);
      serializer.writeArray("looongInt2", ints2);
      serializer.writeArray("looongString1", strings1);
      serializer.writeArray("looongString2", strings2);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(floats1, deserializer.getFloatArray("looongFloat"));
      assertArrayEquals(ints1, deserializer.getIntArray("looongInt1"));
      assertArrayEquals(ints2, deserializer.getIntArray("looongInt2"));
      assertArrayEquals(strings1, deserializer.getStringArray("looongString1"));
      assertArrayEquals(strings2, deserializer.getStringArray("looongString2"));
    }
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
