from functools import partial
import os.path
import unittest

import pycodestyle


class CodestyleTestReport(pycodestyle.StandardReport):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.bad_lines = []

    def get_file_results(self):
        if self._deferred_print:
            for line_number, offset, code, text, _ in sorted(self._deferred_print):
                self.bad_lines.append('{0} {1} {2}:{3}:{4}'.format(code, text, self.filename, line_number, offset))
        return self.file_errors


class TestPycodestyle(unittest.TestCase):
    def test_pycodestyle(self):
        join_project_dir = partial(os.path.join, os.path.dirname(os.path.dirname(__file__)))

        style_guide = pycodestyle.StyleGuide(
            show_pep8=False,
            show_source=True,
            max_line_length=120,
            reporter=CodestyleTestReport,
        )

        result = style_guide.check_files(
            [join_project_dir('nparray'), join_project_dir('tests'), join_project_dir('setup.py')]
        )

        self.assertEqual(result.total_errors, 0, '\n\n' + '\n'.join(result.bad_lines))
