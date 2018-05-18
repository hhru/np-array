# coding=utf-8

import os

import unittest
import numpy as np

from nparray import serialize, deserialize


class TestNPArrays(unittest.TestCase):
    def test_serialization(self):

        floats = np.zeros((1, 2), dtype='float32')
        floats[0][0] = 10.344
        floats[0][1] = -10.344

        ints = np.zeros((2, 1), dtype='int32')
        ints[0][0] = 45435444
        ints[1][0] = 1111

        data = {
            'super_matrix': ints,
            'апапа': floats,
        }

        filename = os.tmpnam()

        serialize(filename, **data)
        result = deserialize(filename)

        self.assertTrue(np.array_equal(ints, result['super_matrix']))
        self.assertTrue(np.array_equal(floats, result['апапа']))
