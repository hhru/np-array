# coding=utf-8

import sys
from struct import pack, unpack
from operator import itemgetter
from itertools import chain

import numpy as np

VERSION = 'swkFx7VJ'
VERSION_SIZE = 8

NUMBER_SIZE = 4
COUNT_SIZE = NUMBER_SIZE + NUMBER_SIZE + NUMBER_SIZE
HEADER_SIZE = NUMBER_SIZE + NUMBER_SIZE + 2 * NUMBER_SIZE + 2 * NUMBER_SIZE

HEADER_DELIMITER = '\n'
HEADER_DELIMITER_SIZE = 1

COUNT_STRUCT = '>iii'
HEADER_STRUCT = '>iiqq'

if sys.version_info < (3, 0):
    def to_bytes(data):
        return bytes(data)


    def from_bytes(data):
        return data


    STRING_TYPE = np.string_
else:
    def to_bytes(data):
        return bytes(data, 'utf-8')


    def from_bytes(data):
        return data.decode('utf-8')


    STRING_TYPE = np.unicode_


def _write_headers(fp, headers):
    for _, arr, name_offset, array_offset in headers:
        fp.write(pack(HEADER_STRUCT, arr.shape[0], arr.shape[1], name_offset, array_offset))
    fp.write(to_bytes(HEADER_DELIMITER))


def _read_headers(fp, count):
    headers = [unpack(HEADER_STRUCT, fp.read(HEADER_SIZE)) for _ in range(count)]
    fp.read(HEADER_DELIMITER_SIZE)
    return headers


def _read_array(fp, header, type_str):
    rows, columns = header[0], header[1]
    return (np.fromfile(fp, np.dtype('>{}'.format(type_str)), count=rows * columns)
            .reshape((rows, columns))
            .astype(type_str))


def _is_string_array(arr):
    return arr.dtype.type == STRING_TYPE


def _write_string_array(fp, arr):
    for string in arr.ravel():
        string_bytes = to_bytes(string)
        fp.write((len(string_bytes)).to_bytes(4, byteorder='big'))
        fp.write(string_bytes)


def _read_string_array(fp, header, bytes_to_read):
    rows, columns = header[0], header[1]
    buffer = fp.read(bytes_to_read)
    offset = 0
    arr = []
    for i in range(rows):
        row = []
        for j in range(columns):
            string_length = int.from_bytes(buffer[offset: offset + NUMBER_SIZE], byteorder='big')
            offset += NUMBER_SIZE
            row.append(from_bytes(buffer[offset: offset + string_length]))
            offset += string_length
        arr.append(row)
    return np.asarray(arr, dtype=STRING_TYPE).reshape((rows, columns))


def serialize(filename, **kwargs):
    int_arrays = []
    float_arrays = []
    int_headers = []
    float_headers = []
    str_arrays = []
    str_headers = []

    for name, arr in kwargs.items():
        if type(arr) != np.ndarray:
            raise ValueError('not np array: ' + name)
        if arr.ndim != 2:
            raise ValueError('invalid shape: ' + name)

        if arr.dtype == np.int32:
            int_arrays.append((name, arr))
        elif arr.dtype == np.float32:
            float_arrays.append((name, arr))
        elif _is_string_array(arr):
            str_arrays.append((name, arr))
        else:
            raise ValueError('invalid type: ' + name)

    headers_offset = (VERSION_SIZE + COUNT_SIZE
                      + HEADER_SIZE * (len(int_arrays) + len(float_arrays) + len(str_arrays))
                      + 3 * HEADER_DELIMITER_SIZE)
    data_offset = headers_offset + sum(map(lambda h: len(to_bytes(h[0])), chain(int_arrays, float_arrays, str_arrays)))

    for source, destination in zip((int_arrays, float_arrays, str_arrays), (int_headers, float_headers, str_headers)):
        for name, arr in source:
            destination.append((name, arr, headers_offset, data_offset))
            headers_offset += len(to_bytes(name))
            if _is_string_array(arr):
                data_offset += sum(map(lambda h: len(to_bytes(h)), arr.ravel())) + arr.size * NUMBER_SIZE
            else:
                data_offset += arr.size * NUMBER_SIZE

    with open(filename, 'wb') as fp:
        fp.write(to_bytes(VERSION))
        fp.write(pack(COUNT_STRUCT, len(int_headers), len(float_headers), len(str_headers)))

        _write_headers(fp, int_headers)
        _write_headers(fp, float_headers)
        _write_headers(fp, str_headers)

        for name, _, _, _ in chain(int_headers, float_headers, str_headers):
            fp.write(to_bytes(name))

        for _, arr, _, _ in int_headers:
            arr.astype('>i4').tofile(fp)

        for _, arr, _, _ in float_headers:
            arr.astype('>f4').tofile(fp)

        for _, arr, _, _ in str_headers:
            _write_string_array(fp, arr.astype(STRING_TYPE))


def deserialize(filename):
    with open(filename, 'rb') as fp:
        version = from_bytes(fp.read(VERSION_SIZE))
        int_arrays_count, float_arrays_count, str_arrays_count = unpack(COUNT_STRUCT, fp.read(COUNT_SIZE))

        int_headers = _read_headers(fp, int_arrays_count)
        float_headers = _read_headers(fp, float_arrays_count)
        str_headers = _read_headers(fp, str_arrays_count)

        name_offsets = list(map(itemgetter(2), chain(int_headers, float_headers, str_headers)))
        name_offsets.append(min(map(itemgetter(3), chain(int_headers, float_headers, str_headers))))

        names = []

        start = name_offsets[0]
        for offset in name_offsets[1:]:
            names.append(from_bytes(fp.read(offset - start)))
            start = offset

        int_arrays = [_read_array(fp, header, 'i4') for header in int_headers]
        float_arrays = [_read_array(fp, header, 'f4') for header in float_headers]

        str_offsets = list(map(itemgetter(3), str_headers))
        str_bytes_to_read = list(np.diff(str_offsets))
        str_bytes_to_read.append(-1)  # read till the end
        str_arrays = [_read_string_array(fp, header, bytes_to_read)
                      for header, bytes_to_read in zip(str_headers, str_bytes_to_read)]

    return dict(zip(names, chain(int_arrays, float_arrays, str_arrays)))
