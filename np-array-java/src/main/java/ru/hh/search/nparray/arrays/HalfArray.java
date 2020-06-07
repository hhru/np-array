package ru.hh.search.nparray.arrays;

import ru.hh.search.nparray.TypeDescriptor;

public class HalfArray extends ShortArray {

  public HalfArray(String name, short[][] data) {
    super(name, data);
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return TypeDescriptor.FLOAT16;
  }
}
