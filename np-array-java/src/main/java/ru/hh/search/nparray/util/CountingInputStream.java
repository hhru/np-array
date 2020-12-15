package ru.hh.search.nparray.util;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {
  private final InputStream inputStream;
  private long count;
  private long mark;

  public CountingInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public long getCount() {
    return count;
  }

  @Override
  public int read() throws IOException {
    int result = inputStream.read();
    if (result != -1) {
      count++;
    }

    return result;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int result = inputStream.read(b, off, len);
    if (result != -1) {
      count += result;
    }

    return result;
  }

  @Override
  public long skip(long n) throws IOException {
    long result = inputStream.skip(n);
    count += result;
    return result;
  }

  @Override
  public int available() throws IOException {
    return inputStream.available();
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }

  @Override
  public synchronized void mark(int readlimit) {
    inputStream.mark(readlimit);
    mark = count;
  }

  @Override
  public boolean markSupported() {
    return inputStream.markSupported();
  }

  @Override
  public synchronized void reset() throws IOException {
    if (!inputStream.markSupported()) {
      throw new IOException("Mark not supported");
    }

    if (this.mark == -1) {
      throw new IOException("Mark not set");
    }

    inputStream.reset();
    count = mark;
  }
}
