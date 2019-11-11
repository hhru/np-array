package ru.hh.search.nparray;


import static java.lang.Math.min;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.List;

public class NpArraySerializers {

  private static final byte BLOCK_DELIMITER = '\n';
  private static final byte STRING_DELIMITER = '\n';
  private static final int BYTES_4 = 4;
  private static final int BYTES_8 = 8;
  private static final int BUFFER_SIZE = 8192 * 2;
  private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

  /*
    HEADER                24 byte
    rows          int32   4byte
    columns       int32   4byte
    offset name   int64   8byte
    offset array  int64   8byte
  */
  private static final int HEADER_SIZE = BYTES_4 + BYTES_4 + BYTES_8 + BYTES_8;


  /*
      VERSION               8byte
      INT ARRAYS COUNT      4byte
      FLOAT ARRAYS COUNT    4byte
      STRING ARRAYS COUNT    4byte
      -----
      INT HEADERS
      FLOAT HEADERS
      STRING HEADERS
      -----
      INT NAMES
      FLOAT NAMES
      STRING NAMES
      -----
      INT ARRAYS        elems * 4byte
      FLOAT ARRAYS      elems * 4byte
      STRING ARRAYS     variable
   */

  private NpArraySerializers() {

  }

  public static void serialize(NpArrays arrays, Path path) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(path.toString())) {

      fos.write(NpBase.ACTUAL_VERSION.getBytes());
      fos.write(intToBytes(arrays.intPosition));
      fos.write(intToBytes(arrays.floatPosition));
      fos.write(intToBytes(arrays.stringPosition));

      long preArraysOffset = 20;

      preArraysOffset += arrays.intPosition * HEADER_SIZE;
      preArraysOffset += arrays.floatPosition * HEADER_SIZE;
      preArraysOffset += arrays.stringPosition * HEADER_SIZE;
      preArraysOffset += 3; // delimiters

      long preNameOffset = preArraysOffset;

      preArraysOffset = getPreArraysOffset(preArraysOffset, arrays.intPosition, arrays.nameIntArrays);
      preArraysOffset = getPreArraysOffset(preArraysOffset, arrays.floatPosition, arrays.nameFloatArrays);
      preArraysOffset = getPreArraysOffset(preArraysOffset, arrays.stringPosition, arrays.nameStringArrays);

      WriteHeadersStat writtenStat = intHeadersWrite(fos, arrays, preNameOffset, preArraysOffset);
      preNameOffset += writtenStat.nameSize;
      preArraysOffset += writtenStat.arraySize;

      writtenStat = floatHeadersWrite(fos, arrays, preNameOffset, preArraysOffset);
      preNameOffset += writtenStat.nameSize;
      preArraysOffset += writtenStat.arraySize;

      stringHeadersWrite(fos, arrays, preNameOffset, preArraysOffset);

      namesWrite(fos, arrays.intPosition, arrays.nameIntArrays);
      namesWrite(fos, arrays.floatPosition, arrays.nameFloatArrays);
      namesWrite(fos, arrays.stringPosition, arrays.nameStringArrays);

      intArrayWrite(arrays, fos);
      floatArrayWrite(arrays, fos);
      stringArrayWrite(arrays, fos);
    }
  }

  private static WriteHeadersStat intHeadersWrite(FileOutputStream fos, NpArrays arrays,
                                                  long preNameOffset, long preArraysOffset) throws IOException {
    long arrayOffset = preArraysOffset;
    long nameOffset = preNameOffset;
    for (int i = 0; i < arrays.intPosition; i++) {
      int row = arrays.intsArrays[i].length;
      int column = arrays.intsArrays[i][0].length;
      headersDataWrite(fos, row, column, nameOffset, arrayOffset);
      nameOffset += arrays.nameIntArrays[i].getBytes().length;
      arrayOffset += ((long) row * (long) column * BYTES_4);
    }
    fos.write(BLOCK_DELIMITER);
    return new WriteHeadersStat(nameOffset - preNameOffset, arrayOffset - preArraysOffset);
  }

  private static WriteHeadersStat floatHeadersWrite(FileOutputStream fos, NpArrays arrays,
                                                  long preNameOffset, long preArraysOffset) throws IOException {
    long arrayOffset = preArraysOffset;
    long nameOffset = preNameOffset;
    for (int i = 0; i < arrays.floatPosition; i++) {
      int row = arrays.floatsArrays[i].length;
      int column = arrays.floatsArrays[i][0].length;
      headersDataWrite(fos, row, column, nameOffset, arrayOffset);
      nameOffset += arrays.nameFloatArrays[i].getBytes().length;
      arrayOffset += ((long) row * (long) column * BYTES_4);
    }
    fos.write(BLOCK_DELIMITER);
    return new WriteHeadersStat(nameOffset - preNameOffset, arrayOffset - preArraysOffset);
  }

  private static WriteHeadersStat stringHeadersWrite(FileOutputStream fos, NpArrays arrays,
                                                    long preNameOffset, long preArraysOffset) throws IOException {
    long arrayOffset = preArraysOffset;
    long nameOffset = preNameOffset;
    for (int k = 0; k < arrays.stringPosition; k++) {
      int rows = arrays.stringsArrays[k].length;
      int columns = arrays.stringsArrays[k][0].length;
      headersDataWrite(fos, rows, columns, nameOffset, arrayOffset);
      nameOffset += arrays.nameStringArrays[k].getBytes().length;
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < columns; j++) {
          arrayOffset += arrays.stringsArrays[k][i][j].getBytes().length + 1;
        }
      }
    }
    fos.write(BLOCK_DELIMITER);
    return new WriteHeadersStat(nameOffset - preNameOffset, arrayOffset - preArraysOffset);
  }

  private static void headersDataWrite(FileOutputStream fos, int row, int column,
                                       long nameOffset, long arrayOffset) throws IOException {
    fos.write(intToBytes(row));
    fos.write(intToBytes(column));
    fos.write(longToBytes(nameOffset));
    fos.write(longToBytes(arrayOffset));
  }

  public static NpArrays deserialize(InputStream input) throws IOException {
    return (NpArrays) deserialize(input, false);
  }

  public static NpArrays deserialize(Path path) throws IOException {
    try (InputStream input = new FileInputStream(path.toFile())) {
      return (NpArrays) deserialize(input, false);
    }
  }

  public static NpHeaders getOnlyHeaders(Path path) throws IOException {
    try (InputStream input = new FileInputStream(path.toFile())) {
      return (NpHeaders) deserialize(input, true);
    }
  }

  public static float[][] getFloatArray(Path path, NpHeaders headers, String key) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byte[] bytes = new byte[BUFFER_SIZE];
    int n = getKey(headers.nameFloatArrays, key);
    float[][] result;

    try (InputStream input = new FileInputStream(path.toString())) {
      long positionStart = headers.offsetArrayFloat[n];
      input.skip(positionStart);
      result = readFloatArray(input, bytes, byteBuffer, headers.rowsFloat[n], headers.columnFloat[n]);
    }
    return result;
  }

  public static int[][] getIntArray(Path path, NpHeaders headers, String key) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byte[] bytes = new byte[BUFFER_SIZE];
    int n = getKey(headers.nameIntArrays, key);
    int[][] result;

    try (InputStream input = new FileInputStream(path.toString())) {
      long positionStart = headers.offsetArrayInt[n];
      input.skip(positionStart);
      result = readIntArray(input, bytes, byteBuffer, headers.rowsInt[n], headers.columnInt[n]);
    }
    return result;
  }

  public static String[][] getStringArray(Path path, NpHeaders headers, String key) throws IOException {

    byte[] bytes = new byte[BUFFER_SIZE];
    byte[] tailBytes = null;

    int bytesRead;
    int currentMatrixRow = 0;
    int currentMatrixCol = 0;
    String delimiter = new String(new byte[]{STRING_DELIMITER});
    int currentMatrix = getKey(headers.nameStringArrays, key);

    String[][] result = new String[headers.rowsString[currentMatrix]][headers.columnString[currentMatrix]];

    try (InputStream input = new FileInputStream(path.toString())) {
      long positionStart = headers.offsetArrayString[currentMatrix];
      input.skip(positionStart);

      while ((bytesRead = input.read(bytes)) > 0) {
        int lastDelimiterIndex = bytesRead == BUFFER_SIZE ? lastIndexOf(bytes, STRING_DELIMITER) : bytesRead - 1;
        byte[] effectiveBytes = getEffectiveBytes(bytes, tailBytes, lastDelimiterIndex);
        tailBytes = getTailBytes(bytes, lastDelimiterIndex, bytesRead);
        String allStrings = new String(effectiveBytes);
        for (String elem : allStrings.split(delimiter)) {
          result[currentMatrixRow][currentMatrixCol] = elem;
          currentMatrixCol++;
          if (currentMatrixCol == headers.columnString[currentMatrix]) {
            currentMatrixCol = 0;
            currentMatrixRow++;
          }
          if (currentMatrixRow == headers.rowsString[currentMatrix]) {
            return result;
          }
        }
      }
    }
    return result;
  }

  private static byte[] getEffectiveBytes(byte[] bytes, byte[] tailBytes, int lastDelimiterIndex) {
    byte[] effectiveBytes;
    if (tailBytes != null) {
      effectiveBytes = new byte[lastDelimiterIndex + tailBytes.length];
      System.arraycopy(tailBytes, 0, effectiveBytes, 0, tailBytes.length);
      System.arraycopy(bytes, 0, effectiveBytes, tailBytes.length, lastDelimiterIndex);
    } else {
      effectiveBytes = new byte[lastDelimiterIndex];
      System.arraycopy(bytes, 0, effectiveBytes, 0, lastDelimiterIndex);
    }
    return effectiveBytes;
  }

  private static byte[] getTailBytes(byte[] bytes, int lastDelimiterIndex, int bytesRead) {
    byte[] tailBytes;
    if (lastDelimiterIndex < BUFFER_SIZE - 1 && bytesRead == BUFFER_SIZE) {
      tailBytes = new byte[BUFFER_SIZE - lastDelimiterIndex - 1];
      System.arraycopy(bytes, lastDelimiterIndex + 1, tailBytes, 0, BUFFER_SIZE - lastDelimiterIndex - 1);
    } else {
      tailBytes = null;
    }
    return tailBytes;
  }

  private static int getKey(String[] names, String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key is null");
    }
    int n = -1;
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(key)) {
        n = i;
        break;
      }
    }
    if (n == -1) {
      throw new IllegalArgumentException("Key not found");
    }
    return n;
  }

  private static NpBase deserialize(InputStream fis, boolean onlyHeaders) throws IOException {
    byte[] bytes = new byte[HEADER_SIZE - 1];
    byte[] bytesAll = new byte[HEADER_SIZE];

    byte[] bytes4 = new byte[BYTES_4];
    byte[] bytes8 = new byte[BYTES_8];

    NpBase npBase;

    String oldVersion = "OLD";

    String version;
    int intSize;
    int floatSize;
    int stringSize;
    fis.read(bytes8);
    if (bytes8[0] == 0) {
      version = oldVersion;
      System.arraycopy(bytes8, 0, bytes4, 0, 4);
      intSize = bytesToInt(bytes4);
      System.arraycopy(bytes8, 4, bytes4, 0, 4);
      floatSize = bytesToInt(bytes4);
      stringSize = 0;
    } else {
      version = new String(bytes8);
      fis.read(bytes4);
      intSize = bytesToInt(bytes4);
      fis.read(bytes4);
      floatSize = bytesToInt(bytes4);
      fis.read(bytes4);
      stringSize = bytesToInt(bytes4);
    }

    if (!onlyHeaders) {
      npBase = new NpArrays(version, intSize, floatSize, stringSize);
    } else {
      npBase = new NpHeaders(version, intSize, floatSize, stringSize);
    }

    int[] rowsInt = new int[intSize];
    int[] columnInt = new int[intSize];
    long[] offsetNameInt = new long[intSize];
    long[] offsetArrayInt = new long[intSize];

    int[] rowsFloat = new int[floatSize];
    int[] columnFloat = new int[floatSize];
    long[] offsetNameFloat = new long[floatSize];
    long[] offsetArrayFloat = new long[floatSize];

    int[] rowsString = new int[stringSize];
    int[] columnString = new int[stringSize];
    long[] offsetNameString = new long[stringSize];
    long[] offsetArrayString = new long[stringSize];

    // INT META READ
    int counter = 0;
    readMetadata(bytes, bytesAll, bytes4, bytes8, fis, rowsInt, columnInt, offsetNameInt, offsetArrayInt, counter);
    counter = 0;
    // FLOAT META READ
    readMetadata(bytes, bytesAll, bytes4, bytes8, fis, rowsFloat, columnFloat, offsetNameFloat, offsetArrayFloat, counter);
    counter = 0;
    if (!version.equals(oldVersion)) {
      // STRING META READ
      readMetadata(bytes, bytesAll, bytes4, bytes8, fis, rowsString, columnString, offsetNameString, offsetArrayString, counter);
    }

    intNamesRead(fis, intSize, npBase, offsetNameInt, offsetNameFloat, offsetNameString, offsetArrayInt);
    floatNamesRead(fis, floatSize, npBase, offsetNameString, offsetNameFloat, offsetArrayInt, offsetArrayFloat);
    if (!version.equals(oldVersion)) {
      stringNamesRead(fis, stringSize, npBase, offsetNameString, offsetArrayInt, offsetArrayFloat, offsetArrayString);
    }

    if (onlyHeaders) {
      NpHeaders npHeaders = (NpHeaders) npBase;
      npHeaders.setColumnString(columnString);
      npHeaders.setColumnFloat(columnFloat);
      npHeaders.setColumnInt(columnInt);
      npHeaders.setRowsString(rowsString);
      npHeaders.setRowsFloat(rowsFloat);
      npHeaders.setRowsInt(rowsInt);
      npHeaders.setOffsetArrayString(offsetArrayString);
      npHeaders.setOffsetArrayFloat(offsetArrayFloat);
      npHeaders.setOffsetArrayInt(offsetArrayInt);
      return npHeaders;
    }

    NpArrays npArrays = (NpArrays) npBase;
    readArraysInt(fis, intSize, npArrays, rowsInt, columnInt);
    readArraysFloat(fis, floatSize, npArrays, rowsFloat, columnFloat);
    if (!version.equals(oldVersion)) {
      readArraysString(fis, stringSize, npArrays, rowsString, columnString);
    }
    return npArrays;
  }

  private static void namesWrite(FileOutputStream fos, int position, String[] names) throws IOException {
    for (int i = 0; i < position; i++) {
      fos.write(names[i].getBytes());
    }
  }

  private static void floatArrayWrite(NpArrays arrays, FileOutputStream fos) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byteBuffer.order(BYTE_ORDER);
    for (int i = 0; i < arrays.floatPosition; i++) {
      for (int j = 0; j < arrays.floatsArrays[i].length; j++) {
        for (int n = 0; n < arrays.floatsArrays[i][j].length; n++) {
          writeFloats(arrays, fos, byteBuffer, i, j, n);
        }
      }
    }
  }

  private static void writeFloats(NpArrays arrays, FileOutputStream fos,
                                  ByteBuffer byteBuffer, int i, int j, int n) throws IOException {
    float elem = arrays.floatsArrays[i][j][n];
    writeBufferIfItFull(fos, byteBuffer);
    byteBuffer.putFloat(elem);
    if (isLastElementOfArray(i, j, n, arrays.floatPosition, arrays.floatsArrays[i].length, arrays.floatsArrays[i][j].length)) {
      writeBytesToPosition(fos, byteBuffer);
    }
  }

  private static void intArrayWrite(NpArrays arrays, FileOutputStream fos) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byteBuffer.order(BYTE_ORDER);
    for (int i = 0; i < arrays.intPosition; i++) {
      for (int j = 0; j < arrays.intsArrays[i].length; j++) {
        for (int n = 0; n < arrays.intsArrays[i][j].length; n++) {
          writeInts(arrays, fos, byteBuffer, i, j, n);
        }
      }
    }
  }

  private static void writeInts(NpArrays arrays, FileOutputStream fos,
                                ByteBuffer byteBuffer, int i, int j, int n) throws IOException {
    int elem = arrays.intsArrays[i][j][n];
    writeBufferIfItFull(fos, byteBuffer);
    byteBuffer.putInt(elem);
    if (isLastElementOfArray(i, j, n, arrays.intPosition, arrays.intsArrays[i].length, arrays.intsArrays[i][j].length)) {
      writeBytesToPosition(fos, byteBuffer);
    }
  }

  private static void writeBufferIfItFull(FileOutputStream fos, ByteBuffer byteBuffer) throws IOException {
    if (!byteBuffer.hasRemaining()) {
      fos.write(byteBuffer.array());
      byteBuffer.clear();
    }
  }

  private static boolean isLastElementOfArray(int arrayIndex, int row, int col,
                                              int totalArrays, int totalRows, int totalColumns) {
    return arrayIndex + 1 == totalArrays && row + 1 == totalRows && col + 1 == totalColumns;
  }

  private static void writeBytesToPosition(FileOutputStream fos, ByteBuffer byteBuffer) throws IOException {
    byte[] bytes = new byte[byteBuffer.position()];
    byteBuffer.rewind();
    byteBuffer.get(bytes);
    fos.write(bytes);
  }

  private static void stringArrayWrite(NpArrays arrays, FileOutputStream fos) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byteBuffer.order(BYTE_ORDER);
    for (int i = 0; i < arrays.stringPosition; i++) {
      for (int j = 0; j < arrays.stringsArrays[i].length; j++) {
        for (int n = 0; n < arrays.stringsArrays[i][j].length; n++) {
          writeStrings(arrays, fos, byteBuffer, i, j, n);
        }
      }
    }
  }

  private static void writeStrings(NpArrays arrays, FileOutputStream fos,
                                   ByteBuffer byteBuffer, int i, int j, int n) throws IOException {
    String elem = arrays.stringsArrays[i][j][n] + "\n";
    int elemLength = elem.getBytes().length;
    if (isLastElementOfArray(i, j, n, arrays.stringPosition, arrays.stringsArrays[i].length, arrays.stringsArrays[i][j].length)) {
      if (elemLength > byteBuffer.remaining()) {
        fos.write(byteBuffer.array());
        byteBuffer.clear();
      }
      byteBuffer.put(elem.getBytes());
      writeBytesToPosition(fos, byteBuffer);
    } else if (elemLength <= byteBuffer.remaining()) {
      byteBuffer.put(elem.getBytes());
    } else {
      writeBytesToPosition(fos, byteBuffer);
      byteBuffer.clear();
      byteBuffer.put(elem.getBytes());
    }
  }

  private static byte[] namesRead(InputStream input, int index, int arraysSize,
                                  long[] offsetNameCurrent, List<long[]> nextOffsets) throws IOException {
    long size = 0;
    if (index + 1 == arraysSize) {
      for (long[] nextOffset : nextOffsets) {
        if (nextOffset.length != 0) {
          size = nextOffset[0] - offsetNameCurrent[index];
          break;
        }
      }
    } else {
      size = offsetNameCurrent[index + 1] - offsetNameCurrent[index];
    }
    byte[] bytesName = new byte[(int) size];
    input.read(bytesName);
    return bytesName;
  }

  private static void floatNamesRead(InputStream input, int floatSize, NpBase npBase, long[] offsetNameString,
                                     long[] offsetNameFloat, long[] offsetArrayInt, long[] offsetArrayFloat) throws IOException {
    var nextOffsets = List.of(offsetNameString, offsetArrayInt, offsetArrayFloat);
    for (int i = 0; i < floatSize; i++) {
      byte[] bytesName = namesRead(input, i, floatSize, offsetNameFloat, nextOffsets);
      npBase.nameFloatArrays[i] = new String(bytesName);
    }
  }

  private static void intNamesRead(InputStream input, int intSize, NpBase npBase, long[] offsetNameInt,
                                   long[] offsetNameFloat, long[] offsetNameString, long[] offsetArrayInt) throws IOException {
    var nextOffsets = List.of(offsetNameFloat, offsetNameString, offsetArrayInt);
    for (int i = 0; i < intSize; i++) {
      byte[] bytesName = namesRead(input, i, intSize, offsetNameInt, nextOffsets);
      npBase.nameIntArrays[i] = new String(bytesName);
    }
  }

  private static void stringNamesRead(InputStream input, int stringSize, NpBase npBase, long[] offsetNameString,
                                      long[] offsetArrayInt, long[] offsetArrayFloat, long[] offsetArrayString) throws IOException {
    var nextOffsets = List.of(offsetArrayInt, offsetArrayFloat, offsetArrayString);
    for (int i = 0; i < stringSize; i++) {
      byte[] bytesName = namesRead(input, i, stringSize, offsetNameString, nextOffsets);
      npBase.nameStringArrays[i] = new String(bytesName);
    }
  }

  private static void readArraysFloat(InputStream input, int floatSize, NpArrays npArrays,
                                      int[] rowsFloat, int[] columnFloat) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byte[] bytes = new byte[BUFFER_SIZE];
    byteBuffer.order(BYTE_ORDER);

    for (int i = 0; i < floatSize; i++) {
      npArrays.floatsArrays[i] = readFloatArray(input, bytes, byteBuffer, rowsFloat[i], columnFloat[i]);
    }
  }

  private static float[][] readFloatArray(InputStream input, byte[] bytes,
                                          ByteBuffer byteBuffer, int rows, int columns) throws IOException {
    float[][] result = new float[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        long remainingBytes = ((long) columns - j) * 4;
        int limit = (int) min(remainingBytes, BUFFER_SIZE);
        readPartArray(input, byteBuffer, bytes, limit);
        while (byteBuffer.hasRemaining()) {
          result[i][j] = byteBuffer.getFloat();
          j++;
        }
        byteBuffer.clear();
      }
      byteBuffer.clear();
    }
    return result;
  }

  private static void readPartArray(InputStream input, ByteBuffer byteBuffer, byte[] bytes, int limit) throws IOException {
    input.read(bytes, 0, limit);
    byteBuffer.limit(limit);
    byteBuffer.put(bytes, 0, limit);
    byteBuffer.rewind();
  }

  private static void readArraysInt(InputStream input, int intSize, NpArrays npArrays,
                                    int[] rowsInt, int[] columnInt) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    byte[] bytes = new byte[BUFFER_SIZE];
    byteBuffer.order(BYTE_ORDER);

    for (int i = 0; i < intSize; i++) {
      npArrays.intsArrays[i] = readIntArray(input, bytes, byteBuffer, rowsInt[i], columnInt[i]);
    }
  }

  private static int[][] readIntArray(InputStream input, byte[] bytes,
                                      ByteBuffer byteBuffer, int rows, int columns) throws IOException {
    int[][] result = new int[rows][columns];
    for (int i = 0; i < rows; i++) {
      int j = 0;
      while (j < columns) {
        long remainingBytes = ((long) columns - j) * 4;
        int limit = (int) min(remainingBytes, BUFFER_SIZE);
        readPartArray(input, byteBuffer, bytes, limit);
        while (byteBuffer.hasRemaining()) {
          result[i][j] = byteBuffer.getInt();
          j++;
        }
        byteBuffer.clear();
      }
      byteBuffer.clear();
    }
    return result;
  }

  private static void readArraysString(InputStream input, int stringSize, NpArrays npArrays,
                                       int[] rowsString, int[] columnString) throws IOException {
    if (stringSize == 0) {
      return;
    }
    for (int i = 0; i < stringSize; i++) {
      npArrays.stringsArrays[i] = new String[rowsString[i]][columnString[i]];
    }

    byte[] bytes = new byte[BUFFER_SIZE];
    byte[] tailBytes = null;

    int bytesRead;
    int currentMatrix = 0;
    int currentMatrixRow = 0;
    int currentMatrixCol = 0;
    String delimiter = new String(new byte[]{STRING_DELIMITER});
    while ((bytesRead = input.read(bytes)) > 0) {
      int lastDelimiterIndex = bytesRead == BUFFER_SIZE ? lastIndexOf(bytes, STRING_DELIMITER) : bytesRead - 1;
      byte[] effectiveBytes = getEffectiveBytes(bytes, tailBytes, lastDelimiterIndex);
      tailBytes = getTailBytes(bytes, lastDelimiterIndex, bytesRead);
      String allStrings = new String(effectiveBytes);
      for (String elem : allStrings.split(delimiter)) {
        npArrays.stringsArrays[currentMatrix][currentMatrixRow][currentMatrixCol] = elem;
        currentMatrixCol++;
        if (currentMatrixCol == columnString[currentMatrix]) {
          currentMatrixCol = 0;
          currentMatrixRow++;
        }
        if (currentMatrixRow == rowsString[currentMatrix]) {
          currentMatrixRow = 0;
          currentMatrix++;
        }
      }
    }
  }

  private static int lastIndexOf(byte[] bytes, byte value) {
    for (int i = bytes.length - 1; i > 0; i--) {
      if (bytes[i] == value) {
        return i;
      }
    }
    return -1;
  }

  private static void readMetadata(byte[] bytes, byte[] bytesAll, byte[] bytes4, byte[] bytes8,
                                   InputStream input, int[] rows, int[] columns,
                                   long[] offsetName, long[] offsetArray, int counter) throws IOException {
    while (true) {
      byte start = (byte) input.read();
      if (BLOCK_DELIMITER == start) {
        break;
      }
      input.read(bytes);

      System.arraycopy(bytes, 0, bytesAll, 1, bytes.length);
      bytesAll[0] = start;

      System.arraycopy(bytesAll, 0, bytes4, 0, 4);
      rows[counter] = bytesToInt(bytes4);
      System.arraycopy(bytesAll, 4, bytes4, 0, 4);
      columns[counter] = bytesToInt(bytes4);
      System.arraycopy(bytesAll, 8, bytes8, 0, 8);
      offsetName[counter] = bytesToLong(bytes8);
      System.arraycopy(bytesAll, 16, bytes8, 0, 8);
      offsetArray[counter] = bytesToLong(bytes8);
      counter++;
    }
  }

  private static long getPreArraysOffset(long preArraysOffset, int position, String[] names) {
    for (int i = 0; i < position; i++) {
      int length = names[i].getBytes().length;
      preArraysOffset += length;
    }
    return preArraysOffset;
  }

  private static byte[] intToBytes(int x) {
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.putInt(x);
    return buffer.array();
  }

  private static int bytesToInt(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.put(bytes);
    buffer.flip();
    return buffer.getInt();
  }

  private static byte[] longToBytes(long x) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.putLong(x);
    return buffer.array();
  }

  private static long bytesToLong(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.put(bytes);
    buffer.flip();
    return buffer.getLong();
  }

  private static class WriteHeadersStat {
    long nameSize;
    long arraySize;
    WriteHeadersStat(long nameSize, long arraySize) {
      this.nameSize = nameSize;
      this.arraySize = arraySize;
    }
  }
}
