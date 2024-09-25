package ru.hh.search.nparray.arrays;

import ru.hh.search.nparray.TypeDescriptor;
import static ru.hh.search.nparray.arrays.IntArray.INT_SIZE;

public class StringArray extends AbstractArray {

  public StringArray(String name, String[][] data) {
    super(name, data);
    rowCount = data.length;
    columnCount = data.length == 0 ? 0 : data[0].length;
    dataSize = calcDataSize();
  }

  public String[][] getData() {
    return (String[][]) data;
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return TypeDescriptor.STRING;
  }

  private long calcDataSize() {
    long size = 0;
    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < columnCount; j++) {
        size += (long) getData()[i][j].getBytes().length + INT_SIZE;
      }
    }
    return size;
  }

}
