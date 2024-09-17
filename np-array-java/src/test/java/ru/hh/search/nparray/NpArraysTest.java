package ru.hh.search.nparray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static ru.hh.search.nparray.NpArrays.STRING_TO_BYTE_ORDER;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hh.search.nparray.arrays.CompressedIntArray;
import ru.hh.search.nparray.deserializers.CompressedIntArrayDeserializer;
import ru.hh.search.nparray.util.ByteArrayViews;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class NpArraysTest {

  private Path tempFilePath;

  @BeforeEach
  public void prepare() throws IOException {
    tempFilePath = Files.createTempFile("nparray", "data").toAbsolutePath();
  }

  @AfterEach
  public void tearDown() throws IOException {
    if (Files.exists(tempFilePath)) {
      Files.delete(tempFilePath);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"<", ">"})
  public void testSerialization(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    short[][] shorts = generateArrayShort(15, 75, (short) 634);
    short[][] halfs = generateArrayShort(50, 50, (short) -224);
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
      serializer.writeArray("4test", shorts);
      serializer.writeHalfArray("5test", halfs);
    }

    int[][] deserializedInts;
    float[][] deserializedFloats;
    String[][] deserializedString;
    short[][] deserializedShorts;
    short[][] deserializedHalfs;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedInts = deserializer.getIntArray("1test");
      deserializedFloats = deserializer.getFloatArray("2test");
      deserializedString = deserializer.getStringArray("3test");
      deserializedShorts = deserializer.getShortArray("4test");
      deserializedHalfs = deserializer.getHalfArray("5test");
    }

    assertArrayEquals(ints, deserializedInts);
    assertArrayEquals(floats, deserializedFloats);
    assertArrayEquals(strings, deserializedString);
    assertArrayEquals(shorts, deserializedShorts);
    assertArrayEquals(halfs, deserializedHalfs);
  }

  @ParameterizedTest
  @ValueSource(strings = {"<", ">"})
  public void testDeserializeAll(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    short[][] shorts = generateArrayShort(15, 75, (short) 634);
    short[][] halfs = generateArrayShort(50, 50, (short) -224);
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
      serializer.writeArray("4test", shorts);
      serializer.writeHalfArray("5test", halfs);
    }

    int[][] deserializedInts;
    float[][] deserializedFloats;
    String[][] deserializedString;
    short[][] deserializedShorts;
    short[][] deserializedHalfs;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializedInts = deserializer.getIntArray("1test");
      deserializedFloats = deserializer.getFloatArray("2test");
      deserializedString = deserializer.getStringArray("3test");
      deserializedShorts = deserializer.getShortArray("4test");
      deserializedHalfs = deserializer.getHalfArray("5test");
    }

    Map<String, Object> matrices;
    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      matrices = deserializer.deserialize();
    }

    assertArrayEquals(deserializedInts, (int[][]) matrices.get("1test"));
    assertArrayEquals(deserializedFloats, (float[][]) matrices.get("2test"));
    assertArrayEquals(deserializedString, (String[][]) matrices.get("3test"));
    assertArrayEquals(deserializedShorts, (short[][]) matrices.get("4test"));
    assertArrayEquals(deserializedHalfs, (short[][]) matrices.get("5test"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"<", ">"})
  public void testDeserializeMetadata(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    short[][] shorts = generateArrayShort(15, 75, (short) 634);
    short[][] halfs = generateArrayShort(1, 1, (short) -224);
    strings[1][1] = "привет!!!!";
    ByteOrder byteOrder = STRING_TO_BYTE_ORDER.get(order);

    try (var serializer = new NpArraySerializer(tempFilePath, byteOrder)) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
      serializer.writeArray("3test", strings);
      serializer.writeArray("4test", shorts);
      serializer.writeHalfArray("5test", halfs);
    }

    Map<String, MetaArray> meta;

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      meta = deserializer.deserializeMetadata("2test", "5test");
    }

    Map<String, Object> matrices;
    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      matrices = deserializer.deserialize();
    }

    assertEquals(38, meta.get("1test").getOffset());
    assertEquals(83, meta.get("2test").getOffset());
    assertEquals(1912, meta.get("3test").getOffset());
    assertEquals(12867, meta.get("4test").getOffset());
    assertEquals(15146, meta.get("5test").getOffset());

    byte[] bytes = Files.readAllBytes(tempFilePath);
    VarHandle floatView = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.FLOAT_BE.getView() : ByteArrayViews.FLOAT_LE.getView();
    VarHandle shortView = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.SHORT_BE.getView() : ByteArrayViews.SHORT_LE.getView();
    assertEquals(765.67f, (float) floatView.get(bytes, (int) meta.get("2test").getOffset()), 0.00001f);
    assertEquals((short) -224, (short) shortView.get(bytes, (int) meta.get("5test").getOffset()));

    assertNull(meta.get("1test").getData());
    assertArrayEquals((float[][]) meta.get("2test").getData(), (float[][]) matrices.get("2test"));
    assertNull(meta.get("3test").getData());
    assertNull(meta.get("4test").getData());
    assertArrayEquals((short[][]) meta.get("5test").getData(), (short[][]) matrices.get("5test"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"<", ">"})
  public void testSerializationOneByOne(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
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

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void testIncorrectOrderSerialize(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, (float) Math.PI);

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("2test", floats);
      Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.writeArray("1test", ints));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void testIncorrectOrderAccess(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, (float) Math.PI);

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("1test", floats);
      serializer.writeArray("2test", ints);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      deserializer.getIntArray("2test");
      Assertions.assertThrows(IllegalArgumentException.class, () -> deserializer.getFloatArray("1test"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void testGetNonexistentArray(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, (float) Math.PI);

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("1test", ints);
      serializer.writeArray("2test", floats);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> deserializer.getFloatArray("4test"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void onlyFloatsSmall(String order) throws IOException {
    float[][] floats = generateArrayFloat(2, 2, 2.4f);
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("10_10_2.4", floats);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(floats, deserializer.getFloatArray("10_10_2.4"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void onlyFloats(String order) throws IOException {
    float[][] floats = generateArrayFloat(3, 3, 2.4f);
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("10_10_2.4", floats);
    }
    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(floats, deserializer.getFloatArray("10_10_2.4"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void onlyIntegers(String order) throws IOException {

    int[][] ints = generateArrayInt(100, 100, 2);
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("10_10_2.4", ints);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(ints, deserializer.getIntArray("10_10_2.4"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void onlyStrings(String order) throws IOException {
    String[][] strings = generateArrayString(100, 100, "test");
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("10_10_test", strings);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(strings, deserializer.getStringArray("10_10_test"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void onlyShorts(String order) throws IOException {
    short[][] shorts = generateArrayShort(100, 100, (short) 50);
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("10_10_test", shorts);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(shorts, deserializer.getShortArray("10_10_test"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void onlyHalfs(String order) throws IOException {
    short[][] shorts = generateArrayShort(100, 100, (short) 200);
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeHalfArray("10_10_test", shorts);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(shorts, deserializer.getHalfArray("10_10_test"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void stringsWithSpecialSymbols(String order) throws IOException {
    String[][] strings = generateArrayString(100, 100, "test");
    strings[0][0] += "\ntest";
    strings[0][1] += "\t another test";
    strings[strings.length - 1][strings[0].length - 1] += "\n";

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("10_10_test", strings);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(strings, deserializer.getStringArray("10_10_test"));
    }
  }

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void loadTest(String order) throws IOException {
    float[][] floats = generateArrayFloat(100_000, 100, 2.4f);

    long time = System.nanoTime();
    System.out.println("Start serialize");
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
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

  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void partitionReadTest(String order) throws IOException {
    int[][] ints = generateArrayInt(2, 2, 5);
    float[][] floats = generateArrayFloat(10, 45, 764.67f);
    String[][] strings = generateArrayString(12, 65, "dr12ЯЯЯ");
    strings[1][1] = "привет!!!!";

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
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

  @Disabled
  @ParameterizedTest()
  @ValueSource(strings = {"<", ">"})
  public void hugeArrayTest(String order) throws IOException {
    float[][] floats = generateArrayFloat(580_864_151, 1, 2.4f);
    int[][] ints = generateArrayInt(580_864_151, 1, 5);
    String[][] strings = generateArrayString(580_864_151, 1, "test");
    short[][] shorts = generateArrayShort(580_864_151, 1, (short) 88);
    short[][] halfs = generateArrayShort(580_864_151, 2, (short) 15);

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray("looongFloat", floats);
      serializer.writeHalfArray("looongHalf", halfs);
      serializer.writeArray("looongInt", ints);
      serializer.writeArray("looongShorts", shorts);
      serializer.writeArray("looongString", strings);
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      assertArrayEquals(floats, deserializer.getFloatArray("looongFloat"));
      assertArrayEquals(halfs, deserializer.getHalfArray("looongHalf"));
      assertArrayEquals(ints, deserializer.getIntArray("looongInt"));
      assertArrayEquals(shorts, deserializer.getShortArray("looongShorts"));
      assertArrayEquals(strings, deserializer.getStringArray("looongString"));
    }
  }

  @ParameterizedTest()
  @MethodSource("compressedDataArguments")
  public void compressedIntegerArrayTest(String order, List<int[]> ints) throws IOException {
    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(order))) {
      serializer.writeArray(CompressedIntArray.ofWithCopy("compressed_int_array", ints));
    }

    try (var deserializer = new NpArrayDeserializer(tempFilePath)) {
      var data = deserializer.getCompressedIntArray("compressed_int_array");
      for (int i = 0; i < data.size(); i++) {
        int[] expectedIntArray = ints.get(i);
        int[] compressedData = data.get(i);
        int uncompressedArraySize = compressedData[0];
        int compressedArraySize = compressedData.length - 1;
        int[] compressedArray = Arrays.copyOfRange(compressedData, 1, compressedData.length);
        var actualIntArray = CompressedIntArrayDeserializer.deserialize(uncompressedArraySize, compressedArray);
        assertArrayEquals(expectedIntArray, actualIntArray);
      }
    }
  }

  private static Stream<Arguments> compressedDataArguments() {
    return Stream.of(
            Arguments.of("<", generateListOfSortedIntArrays(500, 200)),
            Arguments.of(">", generateListOfSortedIntArrays(500, 200)),
            Arguments.of(">", List.of(new int[0])),
            Arguments.of("<", List.of(new int[0])),
            Arguments.of(">", List.of(new int[] {0, 0, 0, 0})),
            Arguments.of("<", List.of(new int[] {0, 0, 0, 0})),
            Arguments.of("<", List.of(new int[] {-2, -1, 0})),
            Arguments.of(">", List.of(new int[] {-2, -1, 0})),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(1))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(2))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(3))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(4))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(5))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(10))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(100))),
            Arguments.of(">", List.of(arrayWithMaxRangeBetweenNumbers(300)))
    );
  }

  private static int[] arrayWithMaxRangeBetweenNumbers(int arraySize) {
    if (arraySize == 1) {
      return new int[] {Integer.MIN_VALUE};
    } else if (arraySize == 2) {
      return new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE};
    } else {
      int step = (int) ((Math.abs((long) Integer.MAX_VALUE) + Math.abs((long) Integer.MIN_VALUE)) / (arraySize - 1));
      int[] result = new int[arraySize];
      result[0] = Integer.MIN_VALUE;
      int current = Integer.MIN_VALUE;
      for (int i = 1; i < arraySize - 1; i++) {
        current += step;
        result[i] = current;
      }
      result[arraySize - 1] = Integer.MAX_VALUE;
      return result;
    }
  }

  @Test
  public void unsortedCompressedIntegerArrayTestWhenError() throws IOException {
    int size = 10_000_000;
    int[] arr = new int[size];
    for (int i = 0; i < size; i++) {
      arr[i] = ThreadLocalRandom.current().nextInt();
    }
    var ints = List.of(arr);

    try (var serializer = new NpArraySerializer(tempFilePath, STRING_TO_BYTE_ORDER.get(">"))) {
      Assertions.assertThrows(
              IndexOutOfBoundsException.class,
              () -> serializer.writeArray(CompressedIntArray.ofWithCopy("compressed_int_array", ints)),
              "As incoming array is not sorted compressed array can't be compressed"
      );
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

  private static List<int[]> generateListOfSortedIntArrays(int column, int row) {
    List<int[]> list = new ArrayList<>();
    for (int i = 0; i < row; i++) {
      int[] rowArray = new int[column];
      for (int j = 0; j < column; j++) {
        rowArray[j] = ThreadLocalRandom.current().nextInt(-1000000, 10000000);
      }
      Arrays.sort(rowArray);
      list.add(rowArray);
    }
    return list;
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

  private short[][] generateArrayShort(int column, int row, short elem) {
    short[][] result = new short[row][column];
    for (int i = 0; i < column; i++) {
      for (int j = 0; j < row; j++) {
        result[j][i] = elem;
      }
    }
    return result;
  }
}
