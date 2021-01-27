package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.StringArray;

import java.io.IOException;
import java.io.OutputStream;

public class StringSerializer extends Serializer<StringArray> {

  public StringSerializer(OutputStream out) {
    super(out, null);
  }

  @Override
  protected void writeData(StringArray array) throws IOException {
    var data = array.getData();
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        writeString(data[i][j]);
      }
    }
  }
}
