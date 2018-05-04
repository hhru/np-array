# coding=utf-8

import sys

import numpy as np

from nparray import serialize


if __name__ == '__main__':
    floats1 = np.zeros((2, 3), dtype='float32')
    floats1[0][0] = 10.0001
    floats1[0][1] = 0.0000001
    floats1[0][2] = 3.88E-40
    floats1[1][0] = -110.9999
    floats1[1][1] = 1.1E20
    floats1[1][2] = -np.inf

    floats2 = np.zeros((1, 4), dtype='float32')
    floats2[0][0] = np.finfo(np.float32).max
    floats2[0][1] = np.finfo(np.float32).tiny
    floats2[0][2] = 1.4E-45
    floats2[0][3] = np.NaN

    ints1 = np.zeros((3, 2), dtype='int32')
    ints1[0][0] = np.iinfo(np.int32).max
    ints1[0][1] = np.iinfo(np.int32).min
    ints1[1][0] = 0
    ints1[1][1] = 34343
    ints1[2][0] = 34343545
    ints1[2][1] = -23231

    ints2 = np.zeros((2, 1), dtype='int32')
    ints2[0][0] = 455545
    ints2[1][0] = -34444343

    data = {
        'fffff1': floats1,
        'флоат!': floats2,
        'iiiii1': ints1,
        '整数': ints2,
    }

    serialize(sys.argv[1], **data)
