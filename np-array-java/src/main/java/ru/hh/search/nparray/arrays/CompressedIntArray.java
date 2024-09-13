package ru.hh.search.nparray.arrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.hh.search.nparray.TypeDescriptor;

/**
 * Compressed int array, array's data should have been sorted in advance of creation {@link CompressedIntArray} object
 */
public class CompressedIntArray extends AbstractArray {
  private CompressedIntArray(String name, List<int[]> data) {
    super(name, data);
    rowCount = data.size();
    columnCount = 0;
    dataSize = 0;
  }

  public static CompressedIntArray of(String name, List<int[]> data) {
    return new CompressedIntArray(name, data);
  }

  public static CompressedIntArray ofWithCopy(String name, List<int[]> data) {
    List<int[]> copy = new ArrayList<>(data.size());
    for (var e : data) {
      copy.add(Arrays.copyOf(e, e.length));
    }
    return new CompressedIntArray(name, copy);
  }

  public List<int[]> getData() {
    return (List<int[]>) data;
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return TypeDescriptor.COMPRESSED_INTEGER;
  }
}
