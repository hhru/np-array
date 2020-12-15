package ru.hh.search.nparray;

import ru.hh.search.nparray.util.ByteArrayViews;
import ru.hh.search.nparray.util.CountingInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ru.hh.search.nparray.arrays.FloatArray.FLOAT_SIZE;
import static ru.hh.search.nparray.arrays.IntArray.INT_SIZE;
import static ru.hh.search.nparray.arrays.ShortArray.SHORT_SIZE;

public class NpArrayDeserializer implements AutoCloseable {

  private static final int BUFFER_SIZE = 8 * 1024 * 1024;
  private static final int MAX_ROW_BUFFER_ELEMENTS = 1024;

  private static final VarHandle shortView = ByteArrayViews.SHORT.getView();
  private static final VarHandle intView = ByteArrayViews.INT.getView();
  private static final VarHandle floatView = ByteArrayViews.FLOAT.getView();
  private static final VarHandle longView = ByteArrayViews.LONG.getView();

  private final CountingInputStream in;
  private String lastUsedName;
  private String version;

  private final byte[] bytes4 = new byte[4];
  private final byte[] bytes8 = new byte[8];
  private byte[] bytes = new byte[MAX_ROW_BUFFER_ELEMENTS * INT_SIZE];

  public NpArrayDeserializer(Path path) throws IOException {
    this(new FileInputStream(path.toString()));
  }

  public NpArrayDeserializer(InputStream in) {
    this.in = new CountingInputStream(new BufferedInputStream(in, BUFFER_SIZE));
  }

  public Map<String, Object> deserialize() throws IOException {
    readVersionIfNecessary();
    Map<String, Object> result = new HashMap<>();
    Metadata metadata;
    while ((metadata = readMetadata()) != null) {
      result.put(metadata.getArrayName(), readData(metadata));
    }
    return result;
  }

  public Map<String, MetaArray> deserializeMetadata(String... array) throws IOException {
    readVersionIfNecessary();
    Map<String, MetaArray> result = new HashMap<>();
    Set<String> arrays = new HashSet<>(Arrays.asList(array));
    Metadata metadata;
    while ((metadata = readMetadata()) != null) {
      Object data = null;
      if (arrays.contains(metadata.getArrayName())) {
        data = readData(metadata);
      } else {
        skipNBytesOrThrow(metadata.getDataSize());
      }

      result.put(metadata.getArrayName(),
        new MetaArray(metadata.getRows(), metadata.getColumns(), metadata.getDataSize(), metadata.getDataOffset(), data));
    }
    return result;
  }

  public int[][] getIntArray(String name) throws IOException {
    prepareReading(name);
    return (int[][]) readData(findTargetArrayMetadata(name, TypeDescriptor.INTEGER));
  }

  private int[][] getIntArraySmallRows(int rows, int columns) throws IOException {
    int limit = columns * INT_SIZE;
    int[][] data = new int[rows][columns];
    for (int i = 0; i < rows; i++) {
      readNBytesOrThrow(bytes, limit);
      int offset = 0;
      for (int j = 0; j < columns; j++) {
        data[i][j] = readInt(bytes, offset);
        offset += INT_SIZE;
      }
    }
    return data;
  }

  private int[][] getIntArrayLargeRows(int rows, int columns) throws IOException {
    var data = new int[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        int remainingColumns = columns - j;
        int limit = Math.min(remainingColumns, MAX_ROW_BUFFER_ELEMENTS);
        readNBytesOrThrow(bytes, limit * INT_SIZE);
        int offset = 0;
        for (int k = 0; k < limit; k++) {
          data[i][j] = readInt(bytes, offset);
          offset += INT_SIZE;
          j++;
        }
      }
    }
    return data;
  }

  public short[][] getHalfArray(String name) throws IOException {
    prepareReading(name);
    return (short[][]) readData(findTargetArrayMetadata(name, TypeDescriptor.FLOAT16));
  }

  public short[][] getShortArray(String name) throws IOException {
    prepareReading(name);
    return (short[][]) readData(findTargetArrayMetadata(name, TypeDescriptor.INTEGER16));
  }

  private short[][] getShortArraySmallRows(int rows, int columns) throws IOException {
    int limit = columns * SHORT_SIZE;
    short[][] data = new short[rows][columns];
    for (int i = 0; i < rows; i++) {
      readNBytesOrThrow(bytes, limit);
      int offset = 0;
      for (int j = 0; j < columns; j++) {
        data[i][j] = readShort(bytes, offset);
        offset += SHORT_SIZE;
      }
    }
    return data;
  }

  private short[][] getShortArrayLargeRows(int rows, int columns) throws IOException {
    var data = new short[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        int remainingColumns = columns - j;
        int limit = Math.min(remainingColumns, MAX_ROW_BUFFER_ELEMENTS);
        readNBytesOrThrow(bytes, limit * SHORT_SIZE);
        int offset = 0;
        for (int k = 0; k < limit; k++) {
          data[i][j] = readShort(bytes, offset);
          offset += SHORT_SIZE;
          j++;
        }
      }
    }
    return data;
  }

  public float[][] getFloatArray(String name) throws IOException {
    prepareReading(name);
    return (float[][]) readData(findTargetArrayMetadata(name, TypeDescriptor.FLOAT));
  }

  private float[][] getFloatArraySmallRows(int rows, int columns) throws IOException {
    int limit = columns * FLOAT_SIZE;
    float[][] data = new float[rows][columns];
    for (int i = 0; i < rows; i++) {
      readNBytesOrThrow(bytes, limit);
      int offset = 0;
      for (int j = 0; j < columns; j++) {
        data[i][j] = readFloat(bytes, offset);
        offset += FLOAT_SIZE;
      }
    }
    return data;
  }

  private float[][] getFloatArrayLargeRows(int rows, int columns) throws IOException {
    float[][] data = new float[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        int remainingColumns = columns - j;
        int limit = Math.min(remainingColumns, MAX_ROW_BUFFER_ELEMENTS);
        readNBytesOrThrow(bytes, limit * FLOAT_SIZE);
        int offset = 0;
        for (int k = 0; k < limit; k++) {
          data[i][j] = readFloat(bytes, offset);
          offset += FLOAT_SIZE;
          j++;
        }
      }
    }
    return data;
  }

  public String[][] getStringArray(String name) throws IOException {
    prepareReading(name);
    return (String[][]) readData(findTargetArrayMetadata(name, TypeDescriptor.STRING));
  }

  private String[][] getStringArray(int rows, int columns) throws IOException {
    var data = new String[rows][columns];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        data[i][j] = readString();
      }
    }
    return data;
  }

  private void prepareReading(String name) throws IOException {
    checkName(name);
    readVersionIfNecessary();
  }

  private void checkName(String name) {
    if (lastUsedName != null && name.compareTo(lastUsedName) < 0) {
      throw new IllegalArgumentException("Incorrect name order");
    }
  }

  private void readVersionIfNecessary() throws IOException {
    if (version != null) {
      return;
    }
    version = readString(8);
    if (!NpArrays.SUPPORTED_VERSIONS.contains(version)) {
      throw new RuntimeException(String.format("Version %s isn't supported", version));
    }
  }

  private Metadata findTargetArrayMetadata(String name, TypeDescriptor typeDescriptor) throws IOException {
    Metadata metadata;
    while (true) {
      metadata = readMetadata();
      if (metadata == null) {
        throw new IllegalArgumentException("Failed to find array");
      }
      if (metadata.getArrayName().equals(name)) {
        break;
      }
      skipNBytesOrThrow(metadata.getDataSize());
    }

    if (metadata.getTypeDescriptor() != typeDescriptor.getValue()) {
      throw new IllegalArgumentException("Incorrect type");
    }
    lastUsedName = name;
    return metadata;
  }

  private Metadata readMetadata() throws IOException {
    byte[] typeBuffer = new byte[INT_SIZE];
    int readedBytes = in.readNBytes(typeBuffer, 0, INT_SIZE);
    if (readedBytes != INT_SIZE) {
      if (readedBytes <= 0) {
        return null;
      }
      throw new IOException(String.format("read only %s bytes, expected %s", readedBytes, INT_SIZE));
    }
    int typeDescriptorValue = readInt(typeBuffer, 0);
    String arrayName = readString();
    int rows = readInt();
    int columns = readInt();
    long dataSize = readLong();
    long dataOffset = in.getCount();

    return new Metadata(typeDescriptorValue, arrayName, rows, columns, dataSize, dataOffset);
  }

  private Object readData(Metadata metadata) throws IOException {
    int type = metadata.getTypeDescriptor();
    int rows = metadata.getRows();
    int columns = metadata.getColumns();

    if (type == TypeDescriptor.INTEGER.getValue()) {
      return columns <= MAX_ROW_BUFFER_ELEMENTS ? getIntArraySmallRows(rows, columns) : getIntArrayLargeRows(rows, columns);
    } else if (type == TypeDescriptor.INTEGER16.getValue() || type == TypeDescriptor.FLOAT16.getValue()) {
      return columns <= MAX_ROW_BUFFER_ELEMENTS ? getShortArraySmallRows(rows, columns) : getShortArrayLargeRows(rows, columns);
    } else if (type == TypeDescriptor.FLOAT.getValue()) {
      return columns <= MAX_ROW_BUFFER_ELEMENTS ? getFloatArraySmallRows(rows, columns) : getFloatArrayLargeRows(rows, columns);
    } else if (type == TypeDescriptor.STRING.getValue()) {
      return getStringArray(rows, columns);
    } else {
      throw new IllegalStateException("Incorrect type descriptor: " + type);
    }
  }

  private void readFullOrThrow(byte[] buff) throws IOException {
    readNBytesOrThrow(buff, buff.length);
  }

  private void readNBytesOrThrow(byte[] buff, int len) throws IOException {
    int read = in.readNBytes(buff, 0, len);
    if (read != len) {
      throw new IOException("Read only " + read + " bytes, expected " + len);
    }
  }

  private void skipNBytesOrThrow(long n) throws IOException {
    long skipped = 0;
    while (skipped < n) {
      skipped += in.skip(n - skipped);
    }
    if (skipped != n) {
      throw new IOException(String.format("Skipped %s bytes, expected %s", skipped, n));
    }
  }

  private int readInt() throws IOException {
    readFullOrThrow(bytes4);
    return readInt(bytes4, 0);
  }

  private int readInt(byte[] bytes, int offset) {
    return (int) intView.get(bytes, offset);
  }

  private short readShort(byte[] bytes, int offset) {
    return (short) shortView.get(bytes, offset);
  }

  private long readLong() throws IOException {
    readFullOrThrow(bytes8);
    return (long) longView.get(bytes8, 0);
  }

  private float readFloat(byte[] bytes, int offset) {
    return (float) floatView.get(bytes, offset);
  }

  private String readString() throws IOException {
    int len = readInt();
    return readString(len);
  }

  private String readString(int len) throws IOException {
    if (len > bytes.length) {
      bytes = new byte[len];
    }
    readNBytesOrThrow(bytes, len);
    return new String(bytes, 0, len);
  }

  @Override
  public void close() throws IOException {
    if (in != null) {
      in.close();
    }
  }
}
