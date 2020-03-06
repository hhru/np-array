# coding=utf-8

from setuptools import setup


setup(
    name='nparray',
    version='1.0.7',
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
