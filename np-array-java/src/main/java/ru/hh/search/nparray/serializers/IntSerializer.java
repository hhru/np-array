package ru.hh.search.nparray.serializers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import ru.hh.search.nparray.arrays.IntArray;

public class IntSerializer extends Serializer<IntArray> {

  public IntSerializer(OutputStream out, VarHandle view) {
    super(out, view);
  }

  @Override
  protected long writeData(IntArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        view.set(bytes4, 0, data[i][j]);
        out.write(bytes4);
      }
    }
    return (long) Integer.BYTES * array.getRowCount() * array.getColumnCount();
  }
}
