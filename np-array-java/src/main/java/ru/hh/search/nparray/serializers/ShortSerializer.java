package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.ShortArray;

import java.io.IOException;
import java.io.OutputStream;

public class ShortSerializer extends Serializer<ShortArray> {

  public ShortSerializer(OutputStream out) {
    super(out);
  }

  @Override
  protected void writeData(ShortArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        writeShort(data[i][j]);
      }
    }
  }
}
