"""Sphinx configuration for the lessons how-to guide.

MyST Markdown and Furo, the same pair corbelsflightlog's docs use, so a page
can move between the two without conversion and a student sees one shape here,
there and on gm0.org.

Built with -W: a broken link or a missing image fails the build rather than
printing a warning nobody reads.
"""

project = "Biobuz lessons"
author = "Catholic Central Spires Robotics"
copyright = "2026, Catholic Central Spires Robotics"

extensions = ["myst_parser"]
myst_enable_extensions = ["colon_fence", "deflist"]

# The tag `mentor` picks the book. With it, the mentor book, rooted at index.md.
# Without it, the student book, rooted at student_index.md and leaving out the
# pages only a mentor reads. _intro.md is the front page both roots include.
MENTOR_ONLY = ["index.md", "tasks/panels.md", "tasks/bench.md"]

exclude_patterns = ["_build", "Thumbs.db", ".DS_Store", "_intro.md"]
if tags.has("mentor"):  # noqa: F821, Sphinx puts `tags` in this file's namespace
    exclude_patterns.append("student_index.md")
else:
    root_doc = "student_index"
    exclude_patterns += MENTOR_ONLY

html_theme = "furo"
html_title = "Biobuz lessons"
html_static_path = ["_static"]
html_css_files = ["pencil.css"]


def setup(app):
    """Drop the other book's `only` blocks before links are resolved, and give
    the student book an index.html.

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
