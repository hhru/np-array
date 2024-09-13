package ru.hh.search.nparray;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static ru.hh.search.nparray.NpArrays.BYTE_ORDER_SELECT_VERSION;
import static ru.hh.search.nparray.NpArrays.STRING_TO_BYTE_ORDER;
import static ru.hh.search.nparray.arrays.FloatArray.FLOAT_SIZE;
import static ru.hh.search.nparray.arrays.IntArray.INT_SIZE;
import static ru.hh.search.nparray.arrays.ShortArray.SHORT_SIZE;
import ru.hh.search.nparray.util.ByteArrayViews;
import ru.hh.search.nparray.util.CountingInputStream;

public class NpArrayDeserializer implements AutoCloseable {

  private static final int BUFFER_SIZE = 4096;
  private static final int MAX_ROW_BUFFER_ELEMENTS = 1024;

  private final CountingInputStream in;
  private String lastUsedName;
  private String version;
  private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

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
    readHeaderIfNecessary();
    Map<String, Object> result = new HashMap<>();
    Metadata metadata;
    while ((metadata = readMetadata()) != null) {
      result.put(metadata.getArrayName(), readData(metadata));
    }
    return result;
  }

  public Map<String, MetaArray> deserializeMetadata(String... array) throws IOException {
    readHeaderIfNecessary();
    Map<String, MetaArray> result = new HashMap<>();
    Set<String> arrays = new HashSet<>(Arrays.asList(array));

    if (arrays.isEmpty()) {
      return result;
    }

    Metadata metadata;
    while ((metadata = readMetadata()) != null) {
      Object data = null;
      if (arrays.contains(metadata.getArrayName())) {
        data = readData(metadata);
      } else {
        skipNBytesOrThrow(metadata.getDataSize());
      }

      result.put(metadata.getArrayName(),
        new MetaArray(metadata.getRows(), metadata.getColumns(), metadata.getDataSize(), metadata.getDataOffset(),
                TypeDescriptor.ofType(metadata.getTypeDescriptor()), data));
    }
    return result;
  }

  public ByteOrder getByteOrder() {
    return byteOrder;
  }

  public int[][] getIntArray(String name) throws IOException {
    prepareReading(name);
    return (int[][]) readData(findTargetArrayMetadata(name, TypeDescriptor.INTEGER));
  }

  private int[][] getIntArraySmallRows(int rows, int columns, VarHandle view) throws IOException {
    int limit = columns * INT_SIZE;
    int[][] data = new int[rows][columns];
    for (int i = 0; i < rows; i++) {
      readNBytesOrThrow(bytes, limit);
      int offset = 0;
      for (int j = 0; j < columns; j++) {
        data[i][j] = (int) view.get(bytes, offset);
        offset += INT_SIZE;
      }
    }
    return data;
  }

  private int[][] getIntArrayLargeRows(int rows, int columns, VarHandle view) throws IOException {
    var data = new int[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        int remainingColumns = columns - j;
        int limit = Math.min(remainingColumns, MAX_ROW_BUFFER_ELEMENTS);
        readNBytesOrThrow(bytes, limit * INT_SIZE);
        int offset = 0;
        for (int k = 0; k < limit; k++) {
          data[i][j] = (int) view.get(bytes, offset);
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

  private short[][] getShortArraySmallRows(int rows, int columns, VarHandle view) throws IOException {
    int limit = columns * SHORT_SIZE;
    short[][] data = new short[rows][columns];
    for (int i = 0; i < rows; i++) {
      readNBytesOrThrow(bytes, limit);
      int offset = 0;
      for (int j = 0; j < columns; j++) {
        data[i][j] = (short) view.get(bytes, offset);
        offset += SHORT_SIZE;
      }
    }
    return data;
  }

  private short[][] getShortArrayLargeRows(int rows, int columns, VarHandle view) throws IOException {
    var data = new short[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        int remainingColumns = columns - j;
        int limit = Math.min(remainingColumns, MAX_ROW_BUFFER_ELEMENTS);
        readNBytesOrThrow(bytes, limit * SHORT_SIZE);
        int offset = 0;
        for (int k = 0; k < limit; k++) {
          data[i][j] = (short) view.get(bytes, offset);
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

  private float[][] getFloatArraySmallRows(int rows, int columns, VarHandle view) throws IOException {
    int limit = columns * FLOAT_SIZE;
    float[][] data = new float[rows][columns];
    for (int i = 0; i < rows; i++) {
      readNBytesOrThrow(bytes, limit);
      int offset = 0;
      for (int j = 0; j < columns; j++) {
        data[i][j] = (float) view.get(bytes, offset);
        offset += FLOAT_SIZE;
      }
    }
    return data;
  }

  private float[][] getFloatArrayLargeRows(int rows, int columns, VarHandle view) throws IOException {
    float[][] data = new float[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        int remainingColumns = columns - j;
        int limit = Math.min(remainingColumns, MAX_ROW_BUFFER_ELEMENTS);
        readNBytesOrThrow(bytes, limit * FLOAT_SIZE);
        int offset = 0;
        for (int k = 0; k < limit; k++) {
          data[i][j] = (float) view.get(bytes, offset);
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

  public List<int[]> getCompressedIntArray(String name) throws IOException {
    prepareReading(name);
    return (List<int[]>) readData(findTargetArrayMetadata(name, TypeDescriptor.COMPRESSED_INTEGER));
  }

  private List<int[]> getCompressedIntArray(int rows, VarHandle view) throws IOException {
    var data = new ArrayList<int[]>(rows);
    for (int i = 0; i < rows; i++) {
      int sizeUncompressed = readIntBE();
      int sizeCompressed = readIntBE();
      int totalToRead = sizeCompressed * INT_SIZE;
      if (totalToRead > 1024) {
        int[] arr = new int[sizeCompressed + 1];
        arr[0] = sizeUncompressed;
        int pos = 1;
        while(totalToRead > 0) {
          int bytesToRead = Math.min(totalToRead, MAX_ROW_BUFFER_ELEMENTS);
          readNBytesOrThrow(bytes, bytesToRead);
          for (int offset = 0; offset < bytesToRead; offset += INT_SIZE) {
            arr[pos++] = (int) view.get(bytes, offset);
          }
          totalToRead -= MAX_ROW_BUFFER_ELEMENTS;
        }
        data.add(arr);
      } else {
        readNBytesOrThrow(bytes, totalToRead);
        int[] arr = new int[sizeCompressed + 1];
        arr[0] = sizeUncompressed;
        int pos = 1;
        for (int offset = 0; offset < totalToRead; offset += INT_SIZE) {
          arr[pos++] = (int) view.get(bytes, offset);
        }
        data.add(arr);
      }
    }
    return data;
  }

  private void prepareReading(String name) throws IOException {
    checkName(name);
    readHeaderIfNecessary();
  }

  private void checkName(String name) {
    if (lastUsedName != null && name.compareTo(lastUsedName) < 0) {
      throw new IllegalArgumentException("Incorrect name order");
    }
  }

  private void readHeaderIfNecessary() throws IOException {
    if (version != null) {
      return;
    }
    version = readString(8);
    if (!NpArrays.SUPPORTED_VERSIONS.contains(version)) {
      throw new RuntimeException(String.format("Version %s isn't supported", version));
    }
    if (BYTE_ORDER_SELECT_VERSION.equals(version)) {
      String order = readString(1);
      byteOrder = STRING_TO_BYTE_ORDER.get(order);
      if (byteOrder == null) {
        throw new RuntimeException(String.format("Invalid byte order %s", order));
      }
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
    int typeDescriptorValue = readIntBE(typeBuffer);
    String arrayName = readString();
    int rows = readIntBE();
    int columns = readIntBE();
    long dataSize = readLongBE();
    long dataOffset = in.getCount();

    return new Metadata(typeDescriptorValue, arrayName, rows, columns, dataSize, dataOffset);
  }

  private Object readData(Metadata metadata) throws IOException {
    int type = metadata.getTypeDescriptor();
    int rows = metadata.getRows();
    int columns = metadata.getColumns();

    if (type == TypeDescriptor.INTEGER.getValue()) {
      VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.INT_BE.getView() : ByteArrayViews.INT_LE.getView();
      return columns <= MAX_ROW_BUFFER_ELEMENTS ? getIntArraySmallRows(rows, columns, view) : getIntArrayLargeRows(rows, columns, view);
    } else if (type == TypeDescriptor.INTEGER16.getValue() || type == TypeDescriptor.FLOAT16.getValue()) {
      VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.SHORT_BE.getView() : ByteArrayViews.SHORT_LE.getView();
      return columns <= MAX_ROW_BUFFER_ELEMENTS ? getShortArraySmallRows(rows, columns, view) : getShortArrayLargeRows(rows, columns, view);
    } else if (type == TypeDescriptor.FLOAT.getValue()) {
      VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.FLOAT_BE.getView() : ByteArrayViews.FLOAT_LE.getView();
      return columns <= MAX_ROW_BUFFER_ELEMENTS ? getFloatArraySmallRows(rows, columns, view) : getFloatArrayLargeRows(rows, columns, view);
    } else if (type == TypeDescriptor.STRING.getValue()) {
      return getStringArray(rows, columns);
    } else if (type == TypeDescriptor.COMPRESSED_INTEGER.getValue()) {
      VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.INT_BE.getView() : ByteArrayViews.INT_LE.getView();
      return getCompressedIntArray(rows, view);
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

  private int readIntBE() throws IOException {
    readFullOrThrow(bytes4);
    return readIntBE(bytes4);
  }

  private int readIntBE(byte[] bytes) {
    return (int) ByteArrayViews.INT_BE.getView().get(bytes, 0);
  }

  private long readLongBE() throws IOException {
    readFullOrThrow(bytes8);
    return (long) ByteArrayViews.LONG_BE.getView().get(bytes8, 0);
  }

  private String readString() throws IOException {
    int len = readIntBE();
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
