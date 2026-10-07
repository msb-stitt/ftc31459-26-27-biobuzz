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
- `site.py` copies the newest release's folder to the top level, where the code's links point,
  and writes each version folder's `index.html`, `versions.json` and the front page, all from the
  version folders.

```
python src/draw.py
python src/build.py
```

`build.py --out DIR` writes into another folder in place of this branch's root. It fails if a
pencilled box has no picture here, the PDF leaves out an image, or a built page shows an image that
is not there. The lessons branch's `ninja publish-check` runs it into a scratch folder, and its
`.github/workflows/release.yml` runs that, then `site.py`, to publish a release or `dev/`.

`build.py` needs a Python with the book's `requirements.txt` and `requirements-publish.txt`;
`draw.py` needs `matplotlib`.
