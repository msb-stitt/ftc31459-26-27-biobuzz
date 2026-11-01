"""Status: controlling. tools/release.py's rules, each passing a tag and refusing one.

    python3 tools/test_release.py

Made-up commits and lines; no repository is read.
"""

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from release import Refused, decide_tag  # noqa: E402

# a <- b <- c on dev; main at a; release-0.4 at d, off a.
HEADS = {"dev": "c" * 40, "main": "a" * 40, "release-0.4": "d" * 40}
ON = {"dev": {"a" * 40, "b" * 40, "c" * 40}, "main": {"a" * 40},
      "release-0.4": {"a" * 40, "d" * 40}}
TAGS = ["v0.4.0", "v0.5.0-rc.3", "v0.4.0-solutions"]


def decide(version, commit, heads=HEADS, tags=TAGS, solutions_is_tag=True):
    return decide_tag(version, commit * 40, heads, lambda c, line: c in ON.get(line, ()),
                      tags, solutions_is_tag, "solutions-x")


class PreRelease(unittest.TestCase):
    def test_on_dev(self):
        self.assertEqual(decide("v0.5.0-rc.4", "b"), ("pre", False))

    def test_on_a_release_line(self):
        self.assertEqual(decide("v0.4.1-rc.1", "d"), ("pre", False))

    def test_on_no_release_line(self):
        with self.assertRaisesRegex(Refused, "on no release line"):
            decide("v0.5.0-rc.4", "e")


class Release(unittest.TestCase):
    def test_at_mains_head(self):
        self.assertEqual(decide("v0.4.0", "a"), ("release", True))

    def test_at_its_release_lines_head(self):
        self.assertEqual(decide("v0.4.1", "d"), ("release", True))

    def test_on_dev_but_no_head(self):
        with self.assertRaisesRegex(Refused, "head of neither main nor release-0.5"):
            decide("v0.5.0", "c")

    def test_at_another_release_lines_head(self):
        with self.assertRaisesRegex(Refused, "head of neither main nor release-0.3"):
            decide("v0.3.1", "d")

    def test_no_release_line_named(self):
        with self.assertRaisesRegex(Refused, "there is no release-0.6"):
            decide("v0.6.1", "e", heads={"dev": "c" * 40})

    def test_not_latest_below_a_higher_release(self):
        self.assertEqual(decide("v0.4.1", "d", tags=TAGS + ["v0.5.0"]), ("release", False))

    def test_latest_above_a_higher_pre_release(self):
        self.assertEqual(decide("v0.4.1", "d", tags=TAGS + ["v0.5.0-rc.9"]), ("release", True))


class AnyTag(unittest.TestCase):
    def test_solutions_not_a_tag(self):
        with self.assertRaisesRegex(Refused, "which is not a tag"):
            decide("v0.5.0-rc.4", "b", solutions_is_tag=False)

    def test_not_a_version(self):
        with self.assertRaisesRegex(Refused, "is not vX.Y.Z"):
            decide("v0.5", "b")


if __name__ == "__main__":
    unittest.main()
