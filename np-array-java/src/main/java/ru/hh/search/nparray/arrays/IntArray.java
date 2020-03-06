package ru.hh.search.nparray.arrays;

import ru.hh.search.nparray.TypeDescriptor;

public class IntArray extends AbstractArray {

  public static final int INT_SIZE = 4;

  public IntArray(String name, int[][] data) {
    super(name, data);
    rowCount = data.length;
    columnCount = data.length == 0 ? 0 : data[0].length;
    dataSize = calcDataSize();
  }

  public int[][] getData() {
    return (int[][]) data;
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return TypeDescriptor.INTEGER;
  }

  private long calcDataSize() {
    return (long) rowCount * columnCount * INT_SIZE;
  }

}
