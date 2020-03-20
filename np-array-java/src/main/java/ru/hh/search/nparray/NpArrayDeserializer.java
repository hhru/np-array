package ru.hh.search.nparray;

import ru.hh.search.nparray.util.ByteArrayViews;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;

import static ru.hh.search.nparray.arrays.FloatArray.FLOAT_SIZE;
import static ru.hh.search.nparray.arrays.IntArray.INT_SIZE;

public class NpArrayDeserializer implements AutoCloseable {

  private static final int BUFFER_SIZE = 8 * 1024 * 1024;
  private static final int MAX_ROW_BUFFER_ELEMENTS = 1024;

  private static final VarHandle intView = ByteArrayViews.INT.getView();
  private static final VarHandle floatView = ByteArrayViews.FLOAT.getView();
  private static final VarHandle longView = ByteArrayViews.LONG.getView();

  private final InputStream in;
  private String lastUsedName;
  private String version;

  private final byte[] bytes4 = new byte[4];
  private final byte[] bytes8 = new byte[8];
  private byte[] bytes = new byte[MAX_ROW_BUFFER_ELEMENTS * INT_SIZE];

  public NpArrayDeserializer(Path path) throws IOException {
    this(new FileInputStream(path.toString()));
  }

  public NpArrayDeserializer(InputStream in) {
    this.in = new BufferedInputStream(in, BUFFER_SIZE);
  }

  // TODO: удалить после чистки старого кода
  public NpBase deserialize() throws IOException {
    Metadata metadata;
    NpArrays result = new NpArrays();

    while (true) {
      try {
        metadata = readMetadata();
      } catch (IllegalArgumentException e) {
        return result;
      }

      if (metadata.getTypeDescriptor() == TypeDescriptor.INTEGER.getValue()) {
        result.add(getIntArrayLargeRows(metadata.getRows(), metadata.getColumns()), metadata.getArrayName());
      } else if (metadata.getTypeDescriptor() == TypeDescriptor.FLOAT.getValue()) {
        result.add(getFloatArrayLargeRows(metadata.getRows(), metadata.getColumns()), metadata.getArrayName());
      } else if (metadata.getTypeDescriptor() == TypeDescriptor.STRING.getValue()) {
        result.add(getStringArray(metadata.getRows(), metadata.getColumns()), metadata.getArrayName());
      } else {
        throw new IllegalStateException("Incorrect type descriptor: " + metadata.getTypeDescriptor());
      }

    }
  }

  public int[][] getIntArray(String name) throws IOException {
    prepareReading(name);
    var metadata = findTargetArrayMetadata(name, TypeDescriptor.INTEGER);
    int rows = metadata.getRows();
    int columns = metadata.getColumns();
    return columns <= MAX_ROW_BUFFER_ELEMENTS ? getIntArraySmallRows(rows, columns) : getIntArrayLargeRows(rows, columns);
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

  public float[][] getFloatArray(String name) throws IOException {
    prepareReading(name);
    var metadata = findTargetArrayMetadata(name, TypeDescriptor.FLOAT);
    int rows = metadata.getRows();
    int columns = metadata.getColumns();
    return columns <= MAX_ROW_BUFFER_ELEMENTS ? getFloatArraySmallRows(rows, columns) : getFloatArrayLargeRows(rows, columns);
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
    var metadata = findTargetArrayMetadata(name, TypeDescriptor.STRING);
    int rows = metadata.getRows();
    int columns = metadata.getColumns();
    return getStringArray(rows, columns);
  }

  // TODO: перенести в getStringArray(String name), функция как отдельная нужна только для более удобной обратной совместимости
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
    if (!NpArraysV2.SUPPORTED_VERSIONS.contains(version)) {
      throw new RuntimeException(String.format("Version %s isn't supported", version));
    }
  }

  private Metadata findTargetArrayMetadata(String name, TypeDescriptor typeDescriptor) throws IOException {
    Metadata metadata;
    while (!(metadata = readMetadata()).getArrayName().equals(name)) {
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
        throw new IllegalArgumentException("Failed to find array");
      }
      throw new IOException(String.format("read only %s bytes, expected %s", readedBytes, INT_SIZE));
    }
    int typeDescriptorValue = readInt(typeBuffer, 0);
    String arrayName = readString();
    int rows = readInt();
    int columns = readInt();
    long dataSize = readLong();
    return new Metadata(typeDescriptorValue, arrayName, rows, columns, dataSize);
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
