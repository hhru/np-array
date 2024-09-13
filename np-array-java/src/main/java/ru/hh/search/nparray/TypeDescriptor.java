package ru.hh.search.nparray;

public enum TypeDescriptor {
  INTEGER(1),
  FLOAT(2),
  STRING(3),
  INTEGER16(4),
  FLOAT16(5),
  COMPRESSED_INTEGER(6)
  ;

  private final int value;

  TypeDescriptor(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static TypeDescriptor ofType(int type) {
    if (type == INTEGER.value) {
      return INTEGER;
    } else if (type == FLOAT.value) {
      return FLOAT;
    } else if (type == STRING.value) {
      return STRING;
    } else if (type == INTEGER16.value) {
      return INTEGER16;
    } else if (type == FLOAT16.value) {
      return FLOAT16;
    } else if (type == COMPRESSED_INTEGER.value) {
      return COMPRESSED_INTEGER;
    }
    return null;
  }
}
