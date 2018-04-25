package ru.hh.search.nparray;


import static java.lang.Math.min;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

public class NpArraySerializers {

  private final static byte BLOCK_DELIMITER = '\n';
  private final static int BYTES_4 = 4;
  private final static int BYTES_8 = 8;
  private final static int BUFFER_SIZE = 8192 * 2;

  /*
    HEADER                24 byte
    rows          int32   4byte
    columns       int32   4byte
    offset name   int64   8byte
    offset array  int64   8byte
  */
  private final static int HEADER_SIZE = BYTES_4 + BYTES_4 + BYTES_8 + BYTES_8;


  /*
      INT ARRAYS COUNT      4byte
      FLOAT ARRAYS COUNT    4byte
      -----
      INT HEADERS
      FLOAT HEADERS
      -----
      INT NAMES
      FLOAT NAMES
      -----
      INT ARRAYS        elems * 4byte
      FLOAT ARRAYS      elems * 4byte
   */

  public static void serialize(NpArrays arrays, Path path) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(path.toString())) {

      //

      fos.write(intToBytes(arrays.intPosition));
      fos.write(intToBytes(arrays.floatPosition));

      int preArraysOffset = 8;

      preArraysOffset += arrays.intPosition * HEADER_SIZE;
      preArraysOffset += arrays.floatPosition * HEADER_SIZE;
      preArraysOffset += 2; // delimiters

      int preNameOffset = preArraysOffset;

      preArraysOffset = getPreArraysOffset(preArraysOffset, arrays.intPosition, arrays.nameIntArrays);
      preArraysOffset = getPreArraysOffset(preArraysOffset, arrays.floatPosition, arrays.nameFloatArrays);

      // INT HEADERS WRITE
      for (int i = 0; i < arrays.intPosition; i++) {
        int row = arrays.intsArrays[i].length;
        int column = arrays.intsArrays[i][0].length;
        int byteSize = (row * column * BYTES_4);
        fos.write(intToBytes(row));
        fos.write(intToBytes(column));
        fos.write(longToBytes(preNameOffset));
        fos.write(longToBytes(preArraysOffset));

        preNameOffset += arrays.nameIntArrays[i].getBytes().length;
        preArraysOffset += byteSize;
      }
      fos.write(BLOCK_DELIMITER);

      // FLOAT HEADERS WRITE
      for (int i = 0; i < arrays.floatPosition; i++) {
        int row = arrays.floatsArrays[i].length;
        int column = arrays.floatsArrays[i][0].length;
        int byteSize = (row * column * BYTES_4);
        fos.write(intToBytes(row));
        fos.write(intToBytes(column));
        fos.write(longToBytes(preNameOffset));
        fos.write(longToBytes(preArraysOffset));

        preNameOffset += arrays.nameFloatArrays[i].getBytes().length;
        preArraysOffset += byteSize;
      }

      fos.write(BLOCK_DELIMITER);

      namesWrite(fos, arrays.intPosition, arrays.nameIntArrays);
      namesWrite(fos, arrays.floatPosition, arrays.nameFloatArrays);

      intArrayWrite(arrays, fos);
      floatArrayWrite(arrays, fos);
    }
  }

  public static NpArrays deserialize(Path path) throws IOException {
    return deserialize(path, false);
  }

  public static NpArrays deserialize(Path path, boolean onlyHeaders) throws IOException {
    byte[] bytes = new byte[HEADER_SIZE - 1];
    byte[] bytesAll = new byte[HEADER_SIZE];

    byte[] bytes4 = new byte[BYTES_4];
    byte[] bytes8 = new byte[BYTES_8];

    try (FileInputStream fis = new FileInputStream(path.toString())) {
      fis.read(bytes4);
      int intSize = bytesToInt(bytes4);
      fis.read(bytes4);
      int floatSize = bytesToInt(bytes4);


      NpArrays npArrays = new NpArrays();

      int[] rowsInt = new int[intSize];
      int[] columnInt = new int[intSize];
      long[] offsetNameInt = new long[intSize];
      long[] offsetArrayInt = new long[intSize];

      int[] rowsFloat = new int[floatSize];
      int[] columnFloat = new int[floatSize];
      long[] offsetNameFloat = new long[floatSize];
      long[] offsetArrayFloat = new long[floatSize];

      // INT META READ
      int counter = 0;
      readMetadata(bytes, bytesAll, bytes4, bytes8, fis, rowsInt, columnInt, offsetNameInt, offsetArrayInt, counter);
      counter = 0;
      // FLOAT META READ
      readMetadata(bytes, bytesAll, bytes4, bytes8, fis, rowsFloat, columnFloat, offsetNameFloat, offsetArrayFloat, counter);

      intNamesRead(fis, intSize, npArrays, offsetNameInt, offsetNameFloat, offsetArrayInt);
      floatNamesRead(fis, floatSize, npArrays, offsetArrayInt, offsetNameFloat, offsetArrayFloat);

      if (!onlyHeaders) {
        readArrayInt(fis, intSize, npArrays, rowsInt, columnInt);
        readArrayFloat(fis, floatSize, npArrays, rowsFloat, columnFloat);
      }

      return npArrays;
    }
  }

  private static void namesWrite(FileOutputStream fos, int position, String[] names) throws IOException {
    for (int i = 0; i < position; i++) {
      fos.write(names[i].getBytes());
    }
  }


  private static void floatArrayWrite(NpArrays arrays, FileOutputStream fos) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    for (int i = 0; i < arrays.floatPosition; i++) {
      for (int j = 0; j < arrays.floatsArrays[i].length; j++) {
        for (int n = 0; n < arrays.floatsArrays[i][j].length; n++) {
          float elem = arrays.floatsArrays[i][j][n];
          if (i + 1 == arrays.floatPosition
                  && j + 1 == arrays.floatsArrays[i].length
                  && n + 1 == arrays.floatsArrays[i][j].length) {
            if (!byteBuffer.hasRemaining()){
              fos.write(byteBuffer.array());
              byteBuffer.clear();
            }
            byteBuffer.putFloat(elem);
            byte[] bytes = new byte[byteBuffer.position()];
            System.arraycopy(byteBuffer.array(), 0, bytes, 0, byteBuffer.position());
            fos.write(bytes);
          } else if (byteBuffer.hasRemaining()) {
            byteBuffer.putFloat(elem);
          } else {
            fos.write(byteBuffer.array());
            byteBuffer.clear();
            byteBuffer.putFloat(elem);
          }
        }
      }
    }
  }


  private static void intArrayWrite(NpArrays arrays, FileOutputStream fos) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    for (int i = 0; i < arrays.intPosition; i++) {
      for (int j = 0; j < arrays.intsArrays[i].length; j++) {
        for (int n = 0; n < arrays.intsArrays[i][j].length; n++) {
          int elem = arrays.intsArrays[i][j][n];
          if (i + 1 == arrays.intPosition
                  && j + 1 == arrays.intsArrays[i].length
                  && n + 1 == arrays.intsArrays[i][j].length) {
            if (!byteBuffer.hasRemaining()){
              fos.write(byteBuffer.array());
              byteBuffer.clear();
            }
            byteBuffer.putInt(elem);
            byte[] bytes = new byte[byteBuffer.position()];
            System.arraycopy(byteBuffer.array(), 0, bytes, 0, byteBuffer.position());
            fos.write(bytes);
          } else if (byteBuffer.hasRemaining()) {
            byteBuffer.putInt(elem);
          } else {
            fos.write(byteBuffer.array());
            byteBuffer.clear();
            byteBuffer.putInt(elem);
          }
        }
      }
    }
  }

  private static void floatNamesRead(FileInputStream fis, int floatSize, NpArrays npArrays, long[] offsetArrayInt, long[] offsetNameFloat, long[] offsetArrayFloat) throws IOException {
    for (int i = 0; i < floatSize; i++) {
      long size;
      if (i + 1 == floatSize) {
        size = (offsetArrayInt.length != 0 ? offsetArrayInt[0] : offsetArrayFloat[0]) - offsetNameFloat[i];
      } else {
        size = offsetNameFloat[i + 1] - offsetNameFloat[i];
      }
      byte[] bytesName = new byte[(int) size];
      fis.read(bytesName);
      npArrays.nameFloatArrays[i] = new String(bytesName);
    }
  }

  private static void intNamesRead(FileInputStream fis, int intSize, NpArrays npArrays, long[] offsetNameInt, long[] offsetNameFloat, long[] offsetArrayInt) throws IOException {
    for (int i = 0; i < intSize; i++) {
      long size;
      if (i + 1 == intSize) {
        size = (offsetNameFloat.length != 0 ? offsetNameFloat[0] : offsetArrayInt[0]) - offsetNameInt[i];
      } else {
        size = offsetNameInt[i + 1] - offsetNameInt[i];
      }
      byte[] bytesName = new byte[(int) size];
      fis.read(bytesName);
      npArrays.nameIntArrays[i] = new String(bytesName);
    }
  }

  private static void readArrayFloat(FileInputStream fis, int floatSize, NpArrays npArrays, int[] rowsFloat, int[] columnFloat) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    for (int i = 0; i < floatSize; i++) {
      npArrays.floatsArrays[i] = new float[rowsFloat[i]][columnFloat[i]];
      for (int j = 0; j < rowsFloat[i]; j++) {
        int n = 0;
        while (n < columnFloat[i]) {
          int remaining = columnFloat[i] - n;
          byte[] bytes = new byte[min(remaining * 4, BUFFER_SIZE)];
          fis.read(bytes);
          byteBuffer.limit(min(remaining * 4, BUFFER_SIZE));
          byteBuffer.put(bytes);
          byteBuffer.position(0);
          while (byteBuffer.hasRemaining()) {
            npArrays.floatsArrays[i][j][n] = byteBuffer.getFloat();
            n++;
          }
          byteBuffer.clear();
        }
        byteBuffer.clear();
      }
    }
  }

  private static void readArrayInt(FileInputStream fis, int intSize, NpArrays npArrays, int[] rowsInt, int[] columnInt) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    for (int i = 0; i < intSize; i++) {
      npArrays.intsArrays[i] = new int[rowsInt[i]][columnInt[i]];
      for (int j = 0; j < rowsInt[i]; j++) {
        int n = 0;
        while (n < columnInt[i]) {
          int remaining = columnInt[i] - n;
          byte[] bytes = new byte[min(remaining * 4, BUFFER_SIZE)];
          fis.read(bytes);
          byteBuffer.limit(min(remaining * 4, BUFFER_SIZE));
          byteBuffer.put(bytes);
          byteBuffer.position(0);
          while (byteBuffer.hasRemaining()) {
            npArrays.intsArrays[i][j][n] = byteBuffer.getInt();
            n++;
          }
          byteBuffer.clear();
        }
        byteBuffer.clear();
      }
    }
  }

  private static void readMetadata(byte[] bytes, byte[] bytesAll, byte[] bytes4, byte[] bytes8, FileInputStream fis, int[] rowsFloat, int[] columnFloat, long[] offsetNameFloat, long[] offsetArrayFloat, int counter) throws IOException {
    while (true) {
      byte start = (byte) fis.read();
      if (BLOCK_DELIMITER == start) {
        break;
      }
      fis.read(bytes);

      System.arraycopy(bytes, 0, bytesAll, 1, bytes.length);
      bytesAll[0] = start;

      System.arraycopy(bytesAll, 0, bytes4, 0, 4);
      rowsFloat[counter] = bytesToInt(bytes4);
      System.arraycopy(bytesAll, 4, bytes4, 0, 4);
      columnFloat[counter] = bytesToInt(bytes4);
      System.arraycopy(bytesAll, 8, bytes8, 0, 8);
      offsetNameFloat[counter] = bytesToLong(bytes8);
      System.arraycopy(bytesAll, 16, bytes8, 0, 8);
      offsetArrayFloat[counter] = bytesToLong(bytes8);
      counter++;
    }
  }

  private static int getPreArraysOffset(int preArraysOffset, int position, String[] names) {
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
}
