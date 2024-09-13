package ru.hh.search.nparray;

public class MetaArray {
  private final int rows;
  private final int columns;
  private final long size;
  private final long offset;
  private final TypeDescriptor typeDescriptor;
  private final Object data;

  public MetaArray(int rows, int columns, long size, long offset, TypeDescriptor typeDescriptor, Object data) {
    this.rows = rows;
    this.columns = columns;
    this.size = size;
    this.offset = offset;
    this.typeDescriptor = typeDescriptor;
    this.data = data;
  }

  public int getRows() {
    return rows;
  }

  public int getColumns() {
    return columns;
  }

  public long getSize() {
    return size;
  }

  public long getOffset() {
    return offset;
  }

  public Object getData() {
    return data;
  }

  public TypeDescriptor getTypeDescriptor() {
    return typeDescriptor;
  }
}
