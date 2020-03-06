package ru.hh.search.nparray;

import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.arrays.FloatArray;
import ru.hh.search.nparray.arrays.IntArray;
import ru.hh.search.nparray.arrays.StringArray;
import ru.hh.search.nparray.serializers.FloatSerializer;
import ru.hh.search.nparray.serializers.IntSerializer;
import ru.hh.search.nparray.serializers.Serializer;
import ru.hh.search.nparray.serializers.StringSerializer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class NpArraySerializer implements AutoCloseable {

  private static final int BUFFER_SIZE = 100 * 1024 * 1024;
  private OutputStream out;
  private String version;
  private String lastUsedName;
  private Map<Class<? extends AbstractArray>, Serializer> serializers = new HashMap<>();

  public NpArraySerializer(Path path) throws IOException {
    this(new FileOutputStream(path.toString()));
  }

  public NpArraySerializer(OutputStream out) {
    this.out = new BufferedOutputStream(out, BUFFER_SIZE);
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

  private void writeArray(AbstractArray array) throws IOException {
    prepareWriting(array.getName());
    serializers.computeIfAbsent(array.getClass(), type -> SerializerFactory.create(type, out)).serialize(array);
    lastUsedName = array.getName();
  }

  private void prepareWriting(String name) throws IOException {
    checkName(name);
    writeVersionIfNecessary();
  }

  private void checkName(String name) {
    if (lastUsedName != null && name.compareTo(lastUsedName) < 0) {
      throw new IllegalArgumentException("Incorrect name order");
    }
  }

  private void writeVersionIfNecessary() throws IOException {
    if (version != null) {
      return;
    }
    version = NpArraysV2.ACTUAL_VERSION;
    out.write(version.getBytes());
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

    public static <T extends AbstractArray, R extends Serializer<T>> R create(Class<T> clazz, OutputStream out) {
      Serializer<?> serializer;
      if (clazz == IntArray.class) {
        serializer = new IntSerializer(out);
      } else if (clazz == FloatArray.class) {
        serializer = new FloatSerializer(out);
      } else if (clazz == StringArray.class) {
        serializer = new StringSerializer(out);
      } else {
        throw new IllegalArgumentException(String.format("unknown type: %s", clazz));
      }
      @SuppressWarnings("unchecked cast")
      var result = (R) serializer;
      return result;
    }
  }
}
