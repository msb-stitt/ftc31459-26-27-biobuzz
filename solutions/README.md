# Solutions

What a student does to `mytry`, lesson by lesson, as copies and patches that a tool applies and
tests. It lives on `solutions-try` and never on `lessons-try`.

- `order` lists the lesson folders in the order a student meets them.
- Each folder's `steps` file says what that lesson copies, patches and tests, and its patches sit
  beside it. `apply.py`'s opening comment says what a step line holds.
- `apply.py` applies `order` up to a named lesson in a fresh worktree of `solutions-try`, and runs
  that lesson's tests.

```
python3 solutions/apply.py l020
```

`--every` runs every lesson's tests so far after each lesson, which is how a later patch is seen
not to break an earlier lesson. `--unpatched` leaves the last lesson's patches out, to see its
tests fail without them.

A patch is made in a kept worktree: `apply.py LESSON --copies-only` stops after the lesson's copies
and prints where it left the worktree. Write the lesson's solution there, run `git diff` from its
root, and save what it prints as `solutions/LESSON/NAME.patch` in this checkout.

`crosscheck.py` tries one release's answers on another release's code, a lesson at a time, both
ways, and lists each lesson as passing or failing and which earlier lessons it needed. It takes any
ref or commit hash, and writes `report.md` and a `.jsonc` of each lesson's chain for each direction.
Its opening comment says how a lesson is tried, how the earlier lessons are found, and where the
files go.

```
python3 solutions/crosscheck.py v0.4.0 v0.5.0-rc.3
```

A release run against itself, one way, lists the earlier lessons each lesson needs:

```
python3 solutions/crosscheck.py v0.5.0-rc.3 v0.5.0-rc.3 --one-way
```

Nothing here is compiled. It sits outside `TeamCode/src`, so Gradle never reads it.
