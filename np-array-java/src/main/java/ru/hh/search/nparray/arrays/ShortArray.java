package ru.hh.search.nparray.arrays;

import ru.hh.search.nparray.TypeDescriptor;

public class ShortArray extends AbstractArray {

  public static final int SHORT_SIZE = 2;

  public ShortArray(String name, short[][] data) {
    super(name, data);
    rowCount = data.length;
    columnCount = data.length == 0 ? 0 : data[0].length;
    dataSize = calcDataSize();
  }

  public short[][] getData() {
    return (short[][]) data;
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return TypeDescriptor.INTEGER16;
  }

  protected long calcDataSize() {
    return (long) rowCount * columnCount * SHORT_SIZE;
  }
}
