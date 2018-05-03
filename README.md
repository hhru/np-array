# np-array

File format:

```
INT ARRAYS COUNT      4byte
FLOAT ARRAYS COUNT    4byte
-----
INT HEADERS
FLOAT HEADERS
-----
INT NAMES
FLOAT NAMES
-----
INT ARRAYS        elems * 4byte
FLOAT ARRAYS      elems * 4byte
```

Header format:

```
HEADER                24 byte
  rows          int32   4byte
  columns       int32   4byte
  offset name   int64   8byte
  offset array  int64   8byte
```
