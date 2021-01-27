package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.ShortArray;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;

public class ShortSerializer extends Serializer<ShortArray> {

  public ShortSerializer(OutputStream out, VarHandle view) {
    super(out, view);
  }

  @Override
  protected void writeData(ShortArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        view.set(bytes2, 0, data[i][j]);
        out.write(bytes2);
      }
    }
  }
}
