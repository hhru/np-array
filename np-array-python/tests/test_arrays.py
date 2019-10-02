# coding=utf-8

import tempfile
import sys

import unittest
import numpy as np

from nparray import serialize, deserialize


class TestNPArrays(unittest.TestCase):
    def test_serialization(self):

        if sys.version_info < (3, 0):
            STRING_TYPE = np.string_
        else:
            STRING_TYPE = np.unicode_

        floats = np.zeros((1, 2), dtype='float32')
        floats[0][0] = 10.344
        floats[0][1] = -10.344

        ints = np.zeros((2, 1), dtype='int32')
        ints[0][0] = 45435444
        ints[1][0] = 1111

        ints2 = np.zeros((2, 3), dtype='int32')
        ints2[0][0] = 45435444
        ints2[0][1] = 1111
        ints2[0][2] = 44
        ints2[1][0] = 4
        ints2[1][1] = 5
        ints2[1][2] = 6

        strings1 = np.array([
            ['asdcs', '!!!U3gARt7BC9VwlAnxFHQ--'],
            ['апапа', '!-0123456789=ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz'],
            ['整数', '&&()']
        ], dtype=STRING_TYPE)

        strings2 = np.array([
            ['asdcs'],
            ['апапа'],
        ], dtype=STRING_TYPE)

        strings3 = np.array([
            ['asdcs', '!!!U3gARt7BC9VwlAnxFHQ--']
        ], dtype=STRING_TYPE)

        data = {
            'super_matrix': ints,
            'data': ints2,
            'апапа': floats,
            'byte_string1': strings1,
            'byte_string2': strings2,
            'byte_string3': strings3
        }

        fp, filename = tempfile.mkstemp()

        serialize(filename, **data)
        result = deserialize(filename)

        self.assertTrue(np.array_equal(ints, result['super_matrix']))
        self.assertTrue(np.array_equal(ints2, result['data']))
        self.assertTrue(np.array_equal(floats, result['апапа']))
        self.assertTrue(np.array_equal(strings1, result['byte_string1']))
        self.assertTrue(np.array_equal(strings2, result['byte_string2']))
        self.assertTrue(np.array_equal(strings3, result['byte_string3']))
