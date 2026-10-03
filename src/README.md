# What builds this branch

**Status: controlling.**

This branch is the published book, built from a lessons branch and its solutions branch. Nothing
here is the source of a lesson: the pages come from `book/` on the lessons branch, and only the
pictures live here, so the lessons branch carries no image.

- `figures/` holds one PNG per figure id in `book/FIGURES.md`. The L2a screenshots and the
  AdvantageScope one were taken in Android Studio and AdvantageScope on 2026-10-02 and 2026-10-03;
  the rest come from `draw.py`.
- `draw.py` draws the drawings, and the stand-ins for figures meant to be photos, from the numbers
  in the code.
- `build.py` checks the lessons branch out, puts each figure in place of its pencilled box with the
  box's words as its caption, and builds `guide/`, `review/` and `ftc31459-book.pdf`.

```
python src/draw.py
python src/build.py
```

Both need a Python with the book's `requirements.txt`, `rinohtype` and `matplotlib`. The PDF step
renders the route diagrams with macOS's `qlmanage`, so it runs on a Mac only.
