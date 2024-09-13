package ru.hh.search.nparray;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import static ru.hh.search.nparray.NpArrays.BYTE_ORDER_TO_STRING;
import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.arrays.CompressedIntArray;
import ru.hh.search.nparray.arrays.FloatArray;
import ru.hh.search.nparray.arrays.HalfArray;
import ru.hh.search.nparray.arrays.IntArray;
import ru.hh.search.nparray.arrays.ShortArray;
import ru.hh.search.nparray.arrays.StringArray;
import ru.hh.search.nparray.serializers.CompressedIntArraySerializer;
import ru.hh.search.nparray.serializers.FloatSerializer;
import ru.hh.search.nparray.serializers.IntSerializer;
import ru.hh.search.nparray.serializers.Serializer;
import ru.hh.search.nparray.serializers.ShortSerializer;
import ru.hh.search.nparray.serializers.StringSerializer;
import ru.hh.search.nparray.util.ByteArrayViews;

public class NpArraySerializer implements AutoCloseable {

  private final RandomAccessFile out;
  private final ByteOrder byteOrder;
  private String version;
  private String lastUsedName;
  private Map<Class<? extends AbstractArray>, Serializer> serializers = new HashMap<>();

  public NpArraySerializer(Path path) throws IOException {
    this(new RandomAccessFile(path.toString(), "rw"), ByteOrder.BIG_ENDIAN);
  }

  public NpArraySerializer(Path path, ByteOrder byteOrder) throws IOException {
    this(new RandomAccessFile(path.toString(), "rw"), byteOrder);
  }

  public NpArraySerializer(RandomAccessFile out) {
    this(out, ByteOrder.BIG_ENDIAN);
  }

  public NpArraySerializer(RandomAccessFile out, ByteOrder byteOrder) {
    this.out = out;
    this.byteOrder = byteOrder;

    if (byteOrder == null) {
      throw new IllegalArgumentException("Invalid byte order");
    }
  }

  public void writeArray(String name, int[][] array) throws IOException {
    writeArray(new IntArray(name, array));
  }

  public void writeArray(String name, float[][] array) throws IOException {
    writeArray(new FloatArray(name, array));
  }

  public void writeArray(String name, String[][] array) throws IOException {
    writeArray(new StringArray(name, array));
  }

  public void writeArray(String name, short[][] array) throws IOException {
    writeArray(new ShortArray(name, array));
  }

  public void writeHalfArray(String name, short[][] array) throws IOException {
    writeArray(new HalfArray(name, array));
  }

  public void writeArray(AbstractArray array) throws IOException {
    prepareWriting(array.getName());
    serializers.computeIfAbsent(array.getClass(), type -> SerializerFactory.create(type, out, byteOrder)).serialize(array);
    lastUsedName = array.getName();
  }

  private void prepareWriting(String name) throws IOException {
    checkName(name);
    writeHeaderIfNecessary();
  }

  private void checkName(String name) {
    if (lastUsedName != null && name.compareTo(lastUsedName) < 0) {
      throw new IllegalArgumentException("Incorrect name order");
    }
  }

  private void writeHeaderIfNecessary() throws IOException {
    if (version != null) {
      return;
    }
    version = NpArrays.BYTE_ORDER_SELECT_VERSION;
    out.write(version.getBytes());
    out.write(BYTE_ORDER_TO_STRING.get(byteOrder).getBytes());
  }

  @Override
  public void close() throws IOException {
    if (out != null) {
      out.close();
    }
  }

  public static class SerializerFactory {

    private SerializerFactory() {

    }

    public static <T extends AbstractArray, R extends Serializer<T>> R create(Class<T> clazz, RandomAccessFile out, ByteOrder byteOrder) {
      Serializer<?> serializer;
      if (clazz == IntArray.class) {
        VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.INT_BE.getView() : ByteArrayViews.INT_LE.getView();
        serializer = new IntSerializer(out, view);
      } else if (clazz == FloatArray.class) {
        VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.FLOAT_BE.getView() : ByteArrayViews.FLOAT_LE.getView();
        serializer = new FloatSerializer(out, view);
      } else if (clazz == StringArray.class) {
        serializer = new StringSerializer(out);
      } else if (clazz == ShortArray.class || clazz == HalfArray.class) {
        VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.SHORT_BE.getView() : ByteArrayViews.SHORT_LE.getView();
        serializer = new ShortSerializer(out, view);
      } else if (clazz == CompressedIntArray.class) {
        VarHandle view = ByteOrder.BIG_ENDIAN.equals(byteOrder) ? ByteArrayViews.INT_BE.getView() : ByteArrayViews.INT_LE.getView();
        serializer = new CompressedIntArraySerializer(out, view);
      } else {
        throw new IllegalArgumentException(String.format("unknown type: %s", clazz));
      }
      @SuppressWarnings("unchecked cast")
      var result = (R) serializer;
      return result;
    }
  }
}
