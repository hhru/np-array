package ru.hh.search.nparray;

class Metadata {
  private final int typeDescriptorValue;
  private final String arrayName;
  private final int rows;
  private final int columns;
  private final long dataSize;
  private final long dataOffset;

  Metadata(int typeDescriptorValue, String arrayName, int rows, int columns, long dataSize, long dataOffset) {
    this.typeDescriptorValue = typeDescriptorValue;
    this.arrayName = arrayName;
    this.rows = rows;
    this.columns = columns;
    this.dataSize = dataSize;
    this.dataOffset = dataOffset;
  }

  int getTypeDescriptor() {
    return typeDescriptorValue;
  }

  String getArrayName() {
    return arrayName;
  }

  int getRows() {
    return rows;
  }

  int getColumns() {
    return columns;
  }

  long getDataSize() {
    return dataSize;
  }

  public long getDataOffset() {
    return dataOffset;
  }
}
