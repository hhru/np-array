# coding=utf-8

from struct import pack, unpack
from operator import itemgetter
from itertools import chain

import numpy as np


NUMBER_SIZE = 4
COUNT_OFFSET = NUMBER_SIZE + NUMBER_SIZE
HEADER_SIZE = NUMBER_SIZE + NUMBER_SIZE + 2 * NUMBER_SIZE + 2 * NUMBER_SIZE
DIVIDERS_SIZE = 2

COUNT_STRUCT = '>ii'
HEADER_STRUCT = '>iiqq'


def _write_headers(fp, headers):
    for _, arr, name_offset, array_offset in headers:
        fp.write(pack(HEADER_STRUCT, arr.shape[0], arr.shape[1], name_offset, array_offset))
    fp.write('\n')


def _read_headers(fp, count):
    headers = [unpack(HEADER_STRUCT, fp.read(HEADER_SIZE)) for i in range(count)]
    fp.read(1)
    return headers


def _read_array(fp, header, type_str):
    return np.fromfile(fp, np.dtype('>({},{}){}'.format(header[0], header[1], type_str)), 1)[0]


def serialize(filename, **kwargs):
    int_arrays = []
    float_arrays = []
    int_headers = []
    float_headers = []

    for name, arr in kwargs.iteritems():
        if type(arr) != np.ndarray:
            raise ValueError('not np array: ' + name)
        if arr.ndim != 2:
            raise ValueError('invalid shape: ' + name)

        if arr.dtype == np.int32:
            int_arrays.append((name, arr))
        elif arr.dtype == np.float32:
            float_arrays.append((name, arr))
        else:
            raise ValueError('invalid type: ' + name)

    headers_offset = COUNT_OFFSET + HEADER_SIZE * (len(int_arrays) + len(float_arrays)) + DIVIDERS_SIZE
    data_offset = headers_offset + sum(map(lambda h: len(h[0]), chain(int_arrays, float_arrays)))

    for source, destination in zip((int_arrays, float_arrays), (int_headers, float_headers)):
        for name, arr in source:
            destination.append((name, arr, headers_offset, data_offset))
            headers_offset += len(name)
            data_offset += arr.size * NUMBER_SIZE

    with open(filename, 'w') as fp:
        fp.write(pack(COUNT_STRUCT, len(int_headers), len(float_headers)))

        _write_headers(fp, int_headers)
        _write_headers(fp, float_headers)

        for name, _, _, _ in chain(int_headers, float_headers):
            fp.write(name)

        for _, arr, _, _ in int_headers:
            arr.astype('>i4').tofile(fp)

        for _, arr, _, _ in float_headers:
            arr.astype('>f4').tofile(fp)


def deserialize(filename):
    with open(filename, 'r') as fp:
        int_arrays_count, float_arrays_count = unpack(COUNT_STRUCT, fp.read(COUNT_OFFSET))

        int_headers = _read_headers(fp, int_arrays_count)
        float_headers = _read_headers(fp, float_arrays_count)

        name_offsets = map(itemgetter(2), chain(int_headers, float_headers))
        name_offsets.append(min(map(itemgetter(3), chain(int_headers, float_headers))))

        names = []

        start = name_offsets[0]
        for offset in name_offsets[1:]:
            names.append(fp.read(offset - start))
            start = offset

        int_arrays = [_read_array(fp, header, 'i4') for header in int_headers]
        float_arrays = [_read_array(fp, header, 'f4') for header in float_headers]

        return dict(zip(names, chain(int_arrays, float_arrays)))
