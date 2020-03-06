package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.FloatArray;

import java.io.IOException;
import java.io.OutputStream;

public class FloatSerializer extends Serializer<FloatArray> {

  public FloatSerializer(OutputStream out) {
    super(out);
  }

  @Override
  protected void writeData(FloatArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        writeFloat(data[i][j]);
      }
    }
  }
}
