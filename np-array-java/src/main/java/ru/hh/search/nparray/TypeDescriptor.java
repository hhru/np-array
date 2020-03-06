package ru.hh.search.nparray;

public enum TypeDescriptor {
  INTEGER(1),
  FLOAT(2),
  STRING(3);

  private final int value;

  TypeDescriptor(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
