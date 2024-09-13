package ru.hh.search.nparray.arrays;

import ru.hh.search.nparray.TypeDescriptor;

public abstract class AbstractArray {
  private final String name;
  protected final Object data;
  protected int rowCount;
  protected int columnCount;
  protected long dataSize;

  AbstractArray(String name, Object data) {
    this.name = name;
    this.data = data;
  }

  public String getName() {
    return name;
  }

  public abstract TypeDescriptor getTypeDescriptor();

  public int getRowCount() {
    return rowCount;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public long getDataSize() {
    return dataSize;
  }

  public void setDataSize(long dataSize) {
    this.dataSize = dataSize;
  }
}
