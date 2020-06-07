package ru.hh.search.nparray.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public enum ByteArrayViews {

  SHORT(MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN)),
  INT(MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN)),
  LONG(MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN)),
  FLOAT(MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.BIG_ENDIAN));

  private final VarHandle view;

  ByteArrayViews(VarHandle view) {
    this.view = view;
  }

  public VarHandle getView() {
    return view;
  }
}
