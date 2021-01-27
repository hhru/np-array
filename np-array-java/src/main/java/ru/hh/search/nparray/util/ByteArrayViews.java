package ru.hh.search.nparray.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public enum ByteArrayViews {
  SHORT_BE(MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN)),
  INT_BE(MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN)),
  LONG_BE(MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN)),
  FLOAT_BE(MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.BIG_ENDIAN)),

  SHORT_LE(MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN)),
  INT_LE(MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN)),
  LONG_LE(MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN)),
  FLOAT_LE(MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN));

  private final VarHandle view;

  ByteArrayViews(VarHandle view) {
    this.view = view;
  }

  public VarHandle getView() {
    return view;
  }
}
