package ru.hh.search.nparray;

import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.arrays.IntArray;
import ru.hh.search.nparray.arrays.FloatArray;
import ru.hh.search.nparray.arrays.StringArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NpArrays {
  public static final String ACTUAL_VERSION = "U20PJBYW";
  public static final Set<String> SUPPORTED_VERSIONS = Set.of(ACTUAL_VERSION);

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
