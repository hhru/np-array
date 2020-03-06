package ru.hh.search.nparray.arrays;

import ru.hh.search.nparray.TypeDescriptor;

public class FloatArray extends AbstractArray {

  public static final int FLOAT_SIZE = 4;

  public FloatArray(String name, float[][] data) {
    super(name, data);
    rowCount = data.length;
    columnCount = data.length == 0 ? 0 : data[0].length;
    dataSize = calcDataSize();
  }

  public float[][] getData() {
    return (float[][]) data;
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return TypeDescriptor.FLOAT;
  }

  private long calcDataSize() {
    return (long) rowCount * columnCount * FLOAT_SIZE;
  }

}
