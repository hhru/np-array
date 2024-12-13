package ru.hh.search.nparray.serializers;

import java.io.IOException;
import java.io.OutputStream;
import ru.hh.search.nparray.arrays.StringArray;

public class StringSerializer extends Serializer<StringArray> {

  public StringSerializer(OutputStream out) {
    super(out, null);
  }

  @Override
  protected long writeData(StringArray array) throws IOException {
    var data = array.getData();
    long dataSize = 0;
    for (int i = 0; i < array.getRowCount(); i++) {
      for (int j = 0; j < array.getColumnCount(); j++) {
        dataSize += writeString(data[i][j]);
      }
    }
    return dataSize;
  }
}
