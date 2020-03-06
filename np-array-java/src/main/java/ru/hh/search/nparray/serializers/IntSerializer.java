package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.IntArray;

import java.io.IOException;
import java.io.OutputStream;

public class IntSerializer extends Serializer<IntArray> {

  public IntSerializer(OutputStream out) {
    super(out);
  }

  @Override
  protected void writeData(IntArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        writeInt(data[i][j]);
      }
    }
  }
}
