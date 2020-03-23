# coding=utf-8

from setuptools import setup
from nparray.version import version


setup(
    name='nparray',
    version=version,
    description='NumPy arrays serializer/deserializer',
    packages=[
        'nparray'
    ],
    test_suite='tests',
    tests_require=[
        'numpy',
        'pycodestyle == 2.5.0'
    ],
    zip_safe=False
)
