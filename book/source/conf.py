"""Sphinx configuration for the lessons how-to guide.

MyST Markdown and Furo, the same pair corbelsflightlog's docs use, so a page
can move between the two without conversion and a student sees one shape here,
there and on gm0.org.

Built with -W: a broken link or a missing image fails the build rather than
printing a warning nobody reads.
"""

project = "Corbels FTC Programming Lessons in Java"
author = "Mike Stitt"
copyright = "2026, Mike Stitt"

extensions = ["myst_parser"]
myst_enable_extensions = ["attrs_block", "colon_fence", "deflist", "substitution"]


def made_from() -> dict[str, str]:
    """B003's three names for what this build was made from, by git describe.

    tools/build.py builds a ref in a throwaway worktree, with the figures filled
    in, and names its refs in BOOK_LESSONS, BOOK_SOLUTIONS and BOOK_MEDIA; those
    are described as committed, since a filled-in worktree is always changed.
    Without them, as under ninja book, the working tree is described, with
    -dirty for an uncommitted change, and keep.toml's solutions pin; the
    figures are boxes, so no media is read.
    """
    import os
    import subprocess
    import tomllib
    from pathlib import Path

    here = Path(__file__).resolve().parent

    def describe(*args: str, cwd: Path = here) -> str:
        done = subprocess.run(["git", "describe", "--abbrev=7", *args], cwd=cwd,
                              capture_output=True, text=True)
        return done.stdout.strip() or "unknown"

    # The PDF is built from a copy of source/ beside book/, so keep.toml is found from the top.
    top = subprocess.run(["git", "rev-parse", "--show-toplevel"], cwd=here, capture_output=True,
                         text=True, check=True).stdout.strip()
    keep = tomllib.loads((Path(top) / "book" / "keep.toml").read_text())
    lessons = os.environ.get("BOOK_LESSONS")
    solutions = os.environ.get("BOOK_SOLUTIONS") or (keep["answers"]["solutions_commit"]
                                                     or keep["answers"]["solutions"])
    media = os.environ.get("BOOK_MEDIA")
    tagged = ["--match", "v*", "--exclude", "*-solutions", "--always"]
    return {
        "lessons": f"`{describe(*tagged, lessons) if lessons else describe(*tagged, '--dirty')}`",
        "media": f"`{keep['media']['ref']}`" + (
            f" at `{describe('--tags', '--always', '--dirty', cwd=Path(media))}`" if media
            else ", not read, since this build shows the figures as boxes"),
        "solutions": f"`{describe('--tags', '--always', solutions)}`",
    }


myst_substitutions = made_from()


# The tag `mentor` picks the book. With it, the mentor book, rooted at index.md.
# Without it, the student book, rooted at student_index.md and leaving out the
# pages only a mentor reads. B003, the welcome, is the first page of both.
MENTOR_ONLY = ["index.md", "tasks/h093.md", "tasks/h095.md"]

exclude_patterns = ["_build", "Thumbs.db", ".DS_Store"]
if tags.has("mentor"):  # noqa: F821, Sphinx puts `tags` in this file's namespace
    exclude_patterns.append("student_index.md")
else:
    root_doc = "student_index"
    exclude_patterns += MENTOR_ONLY

# The PDF: rinohtype's book template, with source/answers.rts setting the answer pages' diffs
# smaller than other code, so their lines fit the page. The title is the one rinohtype gave the
# book before this was set. The subtitle, beside the title at the top of each page, is the version
# tag the lessons were built from, where rinohtype put "Release" and an empty release.
rinoh_documents = [dict(doc=globals().get("root_doc", "index"), target="book", title=project,
                        subtitle=myst_substitutions["lessons"].strip("`"), author=author,
                        template="book.rtt")]

html_theme = "furo"
html_title = "Corbels FTC Programming Lessons in Java"
html_static_path = ["_static"]
html_css_files = ["pencil.css", "steps.css", "diff.css"]


def setup(app):
    """Drop the other book's `only` blocks before links are resolved, give the
    student book an index.html, and give each lesson step a short anchor.

    Sphinx drops them at priority 50 and resolves links at 9 and 10, so a link
    to a mentor page inside `{only} mentor` would fail the student build. A
    link to one outside `only` still fails it.
    """
    from sphinx.transforms.post_transforms import SphinxPostTransform
    from sphinx.util.nodes import process_only_nodes

    class OnlyBeforeLinks(SphinxPostTransform):
        default_priority = 5

        def run(self, **kwargs):
            process_only_nodes(self.document, self.env._tags)

    app.add_post_transform(OnlyBeforeLinks)

    def front_page_as_index(app, exception):
        """Give the student book an index.html, so its folder's URL opens it."""
        if exception is None and app.builder.format == "html" and app.config.root_doc != "index":
            import shutil
            from pathlib import Path

            out = Path(app.outdir)
            shutil.copyfile(out / f"{app.config.root_doc}.html", out / "index.html")

    app.connect("build-finished", front_page_as_index)

    def step_anchors(app, doctree):
        """Give a step `### L020S010: read the left stick` the id `l020s010`, so
        the code a student copies can link to it. A how-to's step, `H011S010`,
        gets one the same way."""
        import re

        from docutils import nodes

        for section in doctree.findall(nodes.section):
            step = re.match(r"[LH]\d{3}S\d{3}\b", section[0].astext())
            if step and step.group().lower() not in section["ids"]:
                section["ids"].insert(0, step.group().lower())

    app.connect("doctree-read", step_anchors)

    import re
    import subprocess
    import sys
    from pathlib import Path

    top = Path(subprocess.run(["git", "rev-parse", "--show-toplevel"], cwd=app.confdir,
                              capture_output=True, text=True, check=True).stdout.strip())
    sys.path.insert(0, str(top / "book" / "tools"))
    from check_figures import descriptions

    shows = descriptions(top / "book")

    def describe_pencilled(app, docname, source):
        """Put each pencilled box's row from FIGURES.md, what the figure must show, under its
        own words, so the description has one home. tools/check_figures.py finds a box with
        no row."""
        def box(m):
            what = shows.get(m.group(1))
            return f"{m.group(0)[:-4]}\n\n{what}\n:::" if what else m.group(0)

        source[0] = re.sub(r"^:::\{admonition\} (fig-[\w-]+)\n:class: pencil\n.*?\n:::$", box,
                           source[0], flags=re.M | re.S)

    app.connect("source-read", describe_pencilled)

    def video(name, rawtext, text, lineno, inliner, options=None, content=None):
        """{video}`l020`: a link to l020.mp4 in this version's videos/.

        A page links its own version's copy, beside its book: relative, so dev/ plays dev's
        video and the release plays its own. A PDF has no folder to be relative to, so it links
        a release's or pre-release's GitHub Release, and dev's videos/ on the site. BOOK_AS and
        BOOK_VERSION, set by tools/build.py, name the version; without them, as under ninja
        book, the release at the top level.
        """
        import os

        from docutils import nodes

        site = "https://msb-stitt.github.io/ftc31459-26-27-biobuzz/"
        kind, version = os.environ.get("BOOK_AS"), os.environ.get("BOOK_VERSION")
        root = {"dev": site + "dev/", "pre": f"{site}{version}/"}.get(kind, site)
        mp4 = f"{text}.mp4"
        env = inliner.document.settings.env
        if env.app.builder.name == "rinoh":
            release = "https://github.com/msb-stitt/ftc31459-26-27-biobuzz/releases/download/"
            uri = f"{release}{version}/{mp4}" if kind in ("release", "pre") else \
                f"{root}videos/{mp4}"
        else:
            uri = "../" * (env.docname.count("/") + 1) + f"videos/{mp4}"
        return [nodes.reference(rawtext, f"{root}videos/{mp4}", refuri=uri)], []

    app.add_role("video", video)
