package ru.hh.search.nparray.serializers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.VarHandle;
import ru.hh.search.nparray.arrays.FloatArray;

public class FloatSerializer extends Serializer<FloatArray> {

  public FloatSerializer(RandomAccessFile out, VarHandle view) {
    super(out, view);
  }

  @Override
  protected long writeData(FloatArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        view.set(bytes4, 0, data[i][j]);
        out.write(bytes4);
      }
    }
    return (long) Float.BYTES * array.getRowCount() * array.getColumnCount();
  }
}
