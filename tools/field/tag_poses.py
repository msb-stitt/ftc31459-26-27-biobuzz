"""Status: controlling.

Read where the BIOBUZZ AprilTags are, from FIRST's field CAD, in Pedro's field.

    python tools/field/tag_poses.py --cache <dir>

It fetches the field's STEP and the four decal images from FIRST's Onshape document "BIOBUZZ™
Playing Field", without signing in, into --cache, which is read again on the next run. The STEP is
about 75 MB and is not committed. It prints, for each tipped-up cluster in the pre-match state, its
tags' centres, facing and right, and then the Java rows camera/FieldTags.java is typed from.

Each cluster is one sticker part in the STEP, such as `am-5870_red1 Red Goal April Tag (Audience)`.
The sticker's two large faces are its printed face, the one looking down, and its back. Its two long
edge faces give its middle and its across direction: the coloured strip is on the edge the CAD
notches, which has the smaller face. A tag's centre is then the sticker's middle moved along and
across by the layout below, checked against the decal images at their own 150 px per inch.

The CAD's frame is millimetres, +y up, origin at the field's centre on the tiles' top. In Pedro's
field, in inches: x = 72 - z / 25.4, y = 72 - x / 25.4, z = y / 25.4.

It runs on Python 3.14.8 with the packages in requirements.txt beside it.
"""
import argparse
import math
import pathlib
import time
import urllib.error
import urllib.request

DOCUMENT = "a355e772e3d24813de7852ee"
WORKSPACE = "f106353168f1f92100b81259"
STEP_ELEMENT = "291fb08026de5c999171ec78"          # am-5850 BIOBUZZ 8-10-26.STEP

# Each sticker, by the name the STEP gives its part: its decal image's element, and its IDs left
# to right on the decal, as the decal prints them.
STICKERS = {
    "red1": ("947f56153fe4ade3a9b85e97", [34, 35, 36, 37]),      # Red Goal April Tag (Audience)
    "red2": ("ecddd4f175f59efa7e1e9012", [30, 31, 32, 33]),      # Red Goal April Tag (Scoring)
    "blue1": ("ab5d11cfa34a6f056facdb23", [38, 39, 40, 41]),     # Blue Goal April Tag (Audience)
    "blue2": ("afb269408bb496eb10d7dea9", [42, 43, 44, 45]),     # Blue Goal April Tag (Scoring)
}
TIPPED_UP = ["red1", "blue2"]                       # in the pre-match state
JAVA_STICKER = "red1"                               # the cluster the calibration auto uses

# Where the tags sit on a sticker, in inches: along it, as the SDK's cluster offsets have them,
# and across it from its middle, away from the strip.
ALONG_IN = [-6.50, -2.75, 2.75, 6.50]
ACROSS_IN = 0.25
# How far a decal's measured layout may be from the one above, in inches.
LAYOUT_TOLERANCE_IN = 0.05
TAG_SIZE_IN = 3.25

MM_PER_IN = 25.4


def blob_url(element):
    return (f"https://cad.onshape.com/api/v6/blobelements/d/{DOCUMENT}/w/{WORKSPACE}"
            f"/e/{element}")


def fetch(element, path):
    """The element's file at path, downloaded unless it is there; a name lookup that fails is
    tried again, since one did on 2026-11-19."""
    if path.exists():
        return path
    path.parent.mkdir(parents=True, exist_ok=True)
    for attempt in range(5):
        try:
            with urllib.request.urlopen(blob_url(element), timeout=600) as response:
                path.write_bytes(response.read())
            return path
        except urllib.error.URLError as error:
            print(f"fetching {element}: {error}; trying again")
            time.sleep(5 * (attempt + 1))
    raise SystemExit(f"could not fetch {element}")


# --- arithmetic -------------------------------------------------------------------------------

def sub(a, b):
    return tuple(x - y for x, y in zip(a, b))


def add(a, b):
    return tuple(x + y for x, y in zip(a, b))


def mul(a, k):
    return tuple(x * k for x in a)


def dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def unit(a):
    return mul(a, 1 / math.sqrt(dot(a, a)))


def clean(a):
    """a with its components that print as zero made 0, so none prints as -0."""
    return tuple(0.0 if abs(x) < 5e-5 else x for x in a)


def pedro_point(p):
    """A CAD point, mm, to Pedro's field, in."""
    return (72 - p[2] / MM_PER_IN, 72 - p[0] / MM_PER_IN, p[1] / MM_PER_IN)


def pedro_direction(d):
    """A CAD direction to Pedro's field."""
    return (-d[2], -d[0], d[1])


def sticker_tags(planes, ids):
    """Each tag's id, centre, facing and right in Pedro's field, and the sticker's tilt from facing
    straight down in degrees, from the sticker's planar faces as (area, centre, outward normal),
    CAD mm."""
    planes = sorted(planes, reverse=True)
    printed = min(planes[:2], key=lambda f: f[2][1])             # the face looking down
    out = unit(printed[2])
    edges = [f for f in planes[2:]
             if abs(dot(unit(sub(f[1], printed[1])), out)) < 0.2 and math.dist(f[1], printed[1]) > 50]
    notched, plain = sorted(sorted(edges, key=lambda f: f[0])[-2:], key=lambda f: f[0])
    up = unit(sub(plain[1], notched[1]))                         # across, away from the strip
    right = cross(up, out)                                       # the decal's right, from below
    middle = mul(add(notched[1], plain[1]), 0.5)
    middle = sub(middle, mul(out, dot(sub(middle, printed[1]), out)))
    tilt = math.degrees(math.acos(-out[1]))
    tags = []
    for tag_id, along in zip(ids, ALONG_IN):
        centre = add(add(middle, mul(right, along * MM_PER_IN)), mul(up, ACROSS_IN * MM_PER_IN))
        tags.append((tag_id, pedro_point(centre), clean(pedro_direction(out)),
                     clean(pedro_direction(right))))
    return tags, tilt


def decal_layout(width, height, ppi, pixel):
    """Each tag's (along, across) on a decal, in inches from the sticker's middle, across positive
    away from the strip, and each tag's size; pixel(x, y) gives (r, g, b)."""
    xs, ys = [], []
    for y in range(height):
        for x in range(0, width, 3):
            if min(pixel(x, y)) < 200:
                xs.append(x)
                ys.append(y)
    x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)          # the sticker's outline
    mid_x, mid_y = (x0 + x1) / 2, (y0 + y1) / 2

    def black(x, y):
        return max(pixel(x, y)) < 60

    def coloured(y):
        return sum(1 for x in range(x0, x1, 5) if max(pixel(x, y)) - min(pixel(x, y)) > 100)

    strip_below = coloured(y1 - 20) > coloured(y0 + 20)
    inside = range(y0 + 10, y1 - 10)
    columns = [sum(black(x, y) for y in inside) for x in range(width)]
    runs, start = [], None
    for x, count in enumerate(columns + [0]):
        if count > 60 and start is None:
            start = x
        elif count <= 60 and start is not None:
            if (x - start) / ppi > 1:
                runs.append((start, x - 1))
            start = None
    layout = []
    for a, b in runs:
        rows = [y for y in inside if sum(black(x, y) for x in range(a, b + 1, 4)) > (b - a) / 4 * 0.3]
        cx, cy = (a + b) / 2, (min(rows) + max(rows)) / 2
        across = (mid_y - cy) / ppi if strip_below else (cy - mid_y) / ppi
        layout.append(((cx - mid_x) / ppi, across, (b - a + 1) / ppi))
    return layout


def check_layout(name, layout):
    """Stops the run when a decal's tags are not where ALONG_IN and ACROSS_IN put them."""
    print(f"  {name}: " + ", ".join(f"({a:+.3f}, {c:+.3f}) in, {s:.3f} in" for a, c, s in layout))
    if len(layout) != len(ALONG_IN):
        raise SystemExit(f"{name}: {len(layout)} tags found on the decal")
    for (along, across, size), want in zip(layout, ALONG_IN):
        if (abs(along - want) > LAYOUT_TOLERANCE_IN or abs(across - ACROSS_IN) > LAYOUT_TOLERANCE_IN
                or abs(size - TAG_SIZE_IN) > LAYOUT_TOLERANCE_IN):
            raise SystemExit(f"{name}: a tag at ({along:.3f}, {across:.3f}) in, {size:.3f} in")


# --- reading the files ------------------------------------------------------------------------

def read_decal(path):
    from PIL import Image
    image = Image.open(path).convert("RGB")
    pixels = image.load()
    return decal_layout(image.width, image.height, image.info["dpi"][0], lambda x, y: pixels[x, y])


def read_stickers(step_path):
    """Each sticker's planar faces, by its key in STICKERS, from the STEP."""
    from OCP.BRepAdaptor import BRepAdaptor_Surface
    from OCP.BRepGProp import BRepGProp
    from OCP.GeomAbs import GeomAbs_Plane
    from OCP.GProp import GProp_GProps
    from OCP.OCP.collections import Sequence_TDF_Label
    from OCP.STEPCAFControl import STEPCAFControl_Reader
    from OCP.TCollection import TCollection_ExtendedString
    from OCP.TDataStd import TDataStd_Name
    from OCP.TDF import TDF_Label
    from OCP.TDocStd import TDocStd_Document
    from OCP.TopAbs import TopAbs_FACE, TopAbs_REVERSED
    from OCP.TopExp import TopExp_Explorer
    from OCP.TopLoc import TopLoc_Location
    from OCP.TopoDS import TopoDS
    from OCP.XCAFDoc import XCAFDoc_DocumentTool

    doc = TDocStd_Document(TCollection_ExtendedString("doc"))
    reader = STEPCAFControl_Reader()
    reader.SetNameMode(True)
    reader.ReadFile(str(step_path))
    reader.Transfer(doc)
    shapes = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())

    def name(label):
        attr = TDataStd_Name()
        return attr.Get().ToExtString() if label.FindAttribute(TDataStd_Name.GetID_s(), attr) else ""

    def planes(shape):
        found = []
        explorer = TopExp_Explorer(shape, TopAbs_FACE)
        while explorer.More():
            face = TopoDS.Face(explorer.Current())
            surface = BRepAdaptor_Surface(face)
            if surface.GetType() == GeomAbs_Plane:
                props = GProp_GProps()
                BRepGProp.SurfaceProperties_s(face, props)
                n = surface.Plane().Axis().Direction()
                if face.Orientation() == TopAbs_REVERSED:
                    n = n.Reversed()
                c = props.CentreOfMass()
                found.append((props.Mass(), (c.X(), c.Y(), c.Z()), (n.X(), n.Y(), n.Z())))
            explorer.Next()
        return found

    stickers = {}

    def walk(label, location, depth):
        target = label
        if shapes.IsReference_s(label):
            target = TDF_Label()
            shapes.GetReferredShape_s(label, target)
        here = location.Multiplied(shapes.GetLocation_s(label)) if depth > 0 else location
        for key in STICKERS:
            if f"am-5870_{key} " in name(target) + " ":
                stickers[key] = planes(shapes.GetShape_s(target).Moved(here))
        if shapes.IsAssembly_s(target):
            kids = Sequence_TDF_Label()
            shapes.GetComponents_s(target, kids, False)
            for i in range(1, kids.Length() + 1):
                walk(kids.Value(i), here, depth + 1)

    roots = Sequence_TDF_Label()
    shapes.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1):
        walk(roots.Value(i), TopLoc_Location(), 0)
    return stickers


# --- the run ----------------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[2])
    parser.add_argument("--cache", type=pathlib.Path, required=True,
                        help="where the STEP and decals are kept between runs")
    args = parser.parse_args()

    print("The decals' tags, (along, across) and size:")
    for key, (element, _) in STICKERS.items():
        check_layout(key, read_decal(fetch(element, args.cache / f"{key}.png")))

    stickers = read_stickers(fetch(STEP_ELEMENT, args.cache / "field.step"))
    print()
    print("| IDs, in order | Tag centres' x (in) | y (in) | z (in) | Facing | Right |")
    print("| --- | --- | --- | --- | --- | --- |")
    rows = {}
    for key in TIPPED_UP:
        tags, tilt = sticker_tags(stickers[key], STICKERS[key][1])
        rows[key] = tags
        _, (_, y, z), facing, right = tags[0]
        print(f"| {', '.join(str(t[0]) for t in tags)} | "
              f"{', '.join(f'{t[1][0]:.3f}' for t in tags)} | {y:.3f} | {z:.3f} | "
              f"({facing[0]:.0f}, {facing[1]:.4f}, {facing[2]:.4f}) | "
              f"({right[0]:.0f}, {right[1]:.0f}, {right[2]:.0f}) |")
        print(f"  {key}: {tilt:.2f} degrees from facing straight down")
    print()
    for tag_id, c, f, r in rows[JAVA_STICKER]:
        print(f"            new Tag({tag_id}, {c[0]:.3f}, {c[1]:.3f}, {c[2]:.3f}, "
              f"{f[0]:.4f}, {f[1]:.4f}, {f[2]:.4f}, {r[0]:.0f}, {r[1]:.0f}, {r[2]:.0f}),")


if __name__ == "__main__":
    main()
