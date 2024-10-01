# coding=utf-8
import os

from setuptools import setup
from version import version

path_to_my_project = "/home/ou/work/np-array/np-array-python/libs/pyfastpfor"

setup(
    name='nparray',
    version=version,
    description='NumPy arrays serializer/deserializer',
    url='https://github.com/hhru/np-array/',
    packages=[
        'nparray'
    ],
    install_requires=[
        f"pyfastpfor @ file://localhost/{os.getcwd()}/libs/pyfastpfor-1.4.1-cp39-cp39-linux_x86_64.whl"
    ],
    test_suite='tests',
    tests_require=[
        'numpy == 1.26.4',
        'pycodestyle == 2.5.0'
    ],
    zip_safe=False
)
