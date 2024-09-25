package ru.hh.search.nparray;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.arrays.FloatArray;
import ru.hh.search.nparray.arrays.IntArray;
import ru.hh.search.nparray.arrays.StringArray;

public class NpArrays {
  public static final String ACTUAL_VERSION = "U20PJBYW";
  public static final String BYTE_ORDER_SELECT_VERSION = "BRSV1ISK";
  public static final Set<String> SUPPORTED_VERSIONS = Set.of(ACTUAL_VERSION, BYTE_ORDER_SELECT_VERSION);
  public static final Map<String, ByteOrder> STRING_TO_BYTE_ORDER = Map.of(">", ByteOrder.BIG_ENDIAN, "<", ByteOrder.LITTLE_ENDIAN);
  public static final Map<ByteOrder, String> BYTE_ORDER_TO_STRING = STRING_TO_BYTE_ORDER
          .entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

  private List<AbstractArray> arrays;

  public NpArrays() {
    arrays = new ArrayList<>();
  }

  public void add(String name, int[][] array) {
    arrays.add(new IntArray(name, array));
  }

  public void add(String name, float[][] array) {
    arrays.add(new FloatArray(name, array));
  }

  public void add(String name, String[][] array) {
    arrays.add(new StringArray(name, array));
  }

  public void clear() {
    arrays.clear();
  }

  public List<AbstractArray> getArrays() {
    return arrays;
  }

}
