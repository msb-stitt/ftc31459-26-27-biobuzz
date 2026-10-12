# How the guide is written

**Status: controlling.** Written 2026-09-27; the lesson page format added 2026-10-05; page types
added 2026-10-10.

The register and the voice belong to
[`prose-style.md`](../../.claude/rules/parts/prose-style.md) and nothing here repeats it. What is
here is what that file does not say: the shape of a task page, how words are introduced, what a
picture has to be, and what a simplification may do.

## Every page has a type and a number

A page is one of the four types [Diátaxis](https://diataxis.fr/) names, or a body page. Each type
has a letter:

- **L, a lesson**: the student builds something, one step at a time. Diátaxis calls it a tutorial.
- **H, a how-to**: the steps for one job, such as installing the tools or working Android Studio's
  panes.
- **R, a reference**: something to look up, such as the glossary or the keyboard shortcuts.
- **E, an explanation**: what is happening inside the computer when the code a lesson wrote runs.
- **B, a body page**: the words that join the book together, such as the welcome.

Every page has one number, from 000 to 999, and the numbers are the book's order, whatever the
type. A page's id is its letter and its number, `L020` or `E021`, and its file is the id in lower
case, `source/tasks/l020.md`. The answers, which `tools/answers.py` writes, are a folder,
`source/r950/`.

**A page's title is its number, then its type in words**: `# 020 lesson: read the sticks`,
`# 011 how-to: get started on Windows`, `# 900 reference: Pedro routes` and
`# 021 what's happening: assignments`. A body page has a title of its own after its number:
`# 003 welcome`.

**Write every title and heading in lower case**, except a proper noun: `## the steps`,
`## Android Studio's window`, and a contents caption such as `get started`. The book's own name,
Corbels FTC Programming Lessons in Java, is a name. A part's bold label inside a step, **Copy.** or
**Check.**, starts a sentence and keeps its capital.

**An E page opens with what the computer does with a program**, in these words: "When the
computer runs your program, it reads one line of code at a time. It moves words and numbers around
in its memory. It is like moving sticky notes around on your desk." Then it takes the lesson's
lines one at a time. It comes right after its lesson, which links to it at the end.

`tools/check_order.py`, in `ninja book`, checks each file's name and title, and that both front
pages list the pages in number order.

## A lesson page is one page for both books

A lesson's steps are named L*nnn*S*nnn*, counting by 10s, and a how-to's H*nnn*S*nnn*. B010 points
to one how-to per computer, H011 to H013.

**What the student book shows**, the shape backed by the research in `ftc-claude`'s
`.docs/2026-10-03-two-books.md`:

- **A picture of what you will have**, first, with one sentence about the robot, not the code.
- **The steps**, each a `### L020S010: read the left stick` heading, counting by 10s. A step is one
  change to the code, then tried everywhere it runs, in this order, leaving out a part the step
  does not need:
  - **Copy**: a file copied into `mytry`, or within it.
  - **Change**: one edit or a few small ones in `mytry`, with the changed lines highlighted.
  - **Simulator**: `simRun`, watched live in AdvantageScope.
  - **AdvantageScope**: what to open or set to see the result, connected to the simulator or, on
    the robot's Wi-Fi, to the robot.
  - **Log**: the same values in the flight log, from the repository's top folder.
  - **Send**: the code sent to the robot, and the OpMode picked and started on the Driver Station.
  - **Robot**: what to do with the robot. A part can instead be named for that one thing, as
    **Push**, **Spin** or **Drive**.
- **Check**: after each part that changes what the student sees, a bulleted list of what they see
  when it worked, one thing to look at or do per item.
- **What's happening**: not on the lesson page. What the code just did, drawn as sticky notes, or
  why it works, is an E page after the lesson, and the lesson's last line links to it. The student
  does nothing in it.

Each part is one or two imperative sentences. A part leads with its name in bold, not a heading.

H017, *navigate Android Studio's panes*, comes before L020. It changes no code, so it has no step
names, and its parts are named for what the student does: **Click**, **Drag**, **Theme**, **View**,
**Find**, **Sync**, **Target** and **Build**.

**Break a skill into its parts the first times it is taught, then trust the student with it.** A
skill is what the pages ask for again and again: copying a starter, switching a copy on, running
the simulator, connecting AdvantageScope, sending the code and running it on the robot, importing a
class. The first time, each part of it has its own name and its own **Check**. It is broken down
once more if the next lesson to use it is close, and twice more if that lesson is many lessons
later. After that, one part names the whole skill, as **Robot.** Send the code, run `L070 Buttons`
from the Driver Station, and watch it in AdvantageScope. `ftc-claude`'s
`.docs/2026-10-05-lessons-restructure.md` lists where each skill is broken down.

**Write a Check as a bulleted list, even of one item.** `**Check.**` stands alone, then a blank
line, then the list, with no blank line between items. An explanation goes after the list, not in
it. The Microsoft Style Guide's
[step-by-step
instructions](https://learn.microsoft.com/en-us/style-guide/procedures-instructions/writing-step-by-step-instructions)
uses a bullet for a single step, to match the lists around it.

**Set apart each action a student does for the first time.** Where a part breaks a skill down,
its name stands alone, then `{.steps}` on the line above a bulleted list, one action per item,
with a blank line between items. `_static/steps.css` puts a line of space between them in the HTML.
The PDF does not show the space. Every other list stays close. The sources:

- USWDS [Typography](https://designsystem.digital.gov/components/typography/): "Use less
  whitespace to group elements and more whitespace to distinguish them from each other."
- digital.gov [Lists](https://digital.gov/guides/plain-language/design/lists/): "Add white space
  for easy reading."
- The Microsoft Style Guide, above: "Use a separate step for each instruction."


**What only the mentor book shows**, each in a `{only} mentor` block that leads with bold text:

- **Before you start**: the lessons this one needs finished, and the state the robot has to be in.
  A mentor reads it to bring back a student who missed a session.
- **If it didn't**: under a check, only where a failure was seen, written as *Write the check
  from a failure somebody saw* says.
- **What you just did**: one paragraph of theory.
- **Where next**: the lessons this one unlocks.

The code a student copies carries a comment where each step changes it, linking to the step.
`source/conf.py` gives each step heading its name as its anchor:

```java
// When on L020S010, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s010
// for what to do here.
```

## The old pages have five parts

- **What you will have when this is done.** One or two sentences, about the robot and not about the
  code. A student deciding what to do next reads only this.
- **Before you start.** The tasks this one needs finished, and the state the robot has to be in: a
  battery in, a particular OpMode already working, a number already measured. A student who missed a
  session starts here, so this part is what makes the guide self-paced.
- **The steps.** One thing to do per step, each carrying **you'll know it worked when** and **if it
  didn't**.
- **What you just did.** One paragraph of theory, written after the robot has already done the
  thing.
- **Where next.** The task or tasks this one unlocks, so the guide can be walked without the
  contents page.

## Write the check from a failure somebody saw

An **if it didn't** line names the symptom a student will actually see. A guessed symptom sends them
looking for something that never appears, which is worse than saying nothing.

## Say nothing about what has been run, tried or seen

A page, a README or a comment in this repository says what to do and what you will see. It does
not say whether a step has been performed, when something was tried or seen, or what is still
unknown. That record is kept in the `ftc-claude` repository, and only there. Naming a source, such
as Pedro's tuning page, is not a record and stays. Neither is a dated measurement in a code comment,
such as "Measured on 2026-09-28: …", which says why the code is the way it is.

## Put the explanation in the step that needs it

A term is introduced where a student first has to use it, named, and reused after that. No page of
theory ahead of the task that needs it. `task.cheatsheet` collects every term with the task that
introduced it.

## A simplification is allowed, and is marked

"The motor goes twice as fast at twice the power" earns its place in L2. The rule is that the guide
never says something a student will later have to unlearn silently: where a simplification gets
corrected later, the page says so in one line and names the task that corrects it.

These simplifications are made everywhere, and are not marked:

- **A field is called a variable.** `gamepad1` and `hardware` are fields of the OpMode.
- **A class used through its static methods is called an object.** `Tracker` is a class.
- **A variable that refers to an object is not called a reference.**
- **What a variable holds before its first assignment is not said.**

## Write the code a student types one idea at a time

- **One idea per statement.** A value worked out, named and tested is three statements, and each
  result has a name: `double alwaysPositiveMagnitude = Math.abs(value);`, then
  `boolean isCloseToZero = alwaysPositiveMagnitude < band;`, then `if (isCloseToZero) {`.
- **No call inside another call's arguments.** Each result gets its own variable first:
  `double forward = deadband(-leftStickY, band);` then `forward = signedSquared(forward);`. A call
  that reads a setting or builds a path stays inside, as
  `hardware.mecanumConfig.frontLeftDirection.get()` and `line(start, end).constant(start)` do.
- **`Math.max` takes two numbers**, so the largest of four is a chain of calls, each on its own
  line.
- **A whole-number `double` is written with `.0`**, in the code and on the page: `1.0`, `0.0`,
  `-1.0`.

## The code a student copies is plain Java

- **Package-private by default.** A modifier is written only where the code will not work without
  it, and then it is `public`. No lesson teaches `protected`.
- **Every modifier has a comment on the line before it**, saying what forced it and what it allows.
  The phrases are in `ftc-claude`'s `.docs/modifiers-explanation.md`.
- **The code we give the students has Javadoc**, and no lesson asks a student to write any.
- **`mytry` holds no `package-info.java`.**
- **A student page uses AdvantageScope, not Panels.** Panels stays in the code for driving the robot
  without a Driver Station, and its page, H093, is in the mentor book only.

## Say nothing about what the lessons do not teach

What just works for a student and no lesson teaches, such as `.gitignore`, is not named on a page.

## Write American English

Tire, color, labeled, center.

## A picture is registered before it is drawn

Every figure has a row in [`FIGURES.md`](FIGURES.md) and an id of the form `fig-<something>`. A
figure that does not exist yet is a pencilled box in the page, so the gap is visible to a reader and
not blocking to a writer. `tools/check_figures.py` checks both directions.

## Draw in the marker-sketch style

A drawing, as against a screenshot or a plot, is drawn by `tools/draw.py` in the look of
an industrial designer's marker sketch, made flat: ink lines that run past their corners, faint
construction lines, slanted marker fills, a red or orange accent, white highlights and soft
shadows, words in Marker Felt and code in Menlo. A variable is a white label with an arrow to a
sticky note that curls off the page and is colored by its type: yellow for `double` and `float`,
green for `boolean`, blue for an object. A plot of numbers, such as `fig-stick-shaping`, stays
plain. A drawing is the guide's picture, not a stand-in for a photo. `draw.py`'s docstring holds the
details.

**Draw a variable as a sticky note**, the picture Ned Batchelder's
[*Facts and myths about Python names and values*](https://nedbatchelder.com/text/names.html) uses
for names and values:

- **A variable or a field is a white label** holding its name in Menlo, with an arrow from a dot at
  its end to a sticky note.
- **The sticky note is labeled with its type** along its glue strip: a primitive type such as
  `double` or `boolean`, or a class name.
- **A primitive's sticky note holds its value**, large, such as `0.7`.
- **An object's sticky note holds a label for each of its fields**, each with its own arrow to a
  smaller sticky note on the big one.
- **Each name sits close to its sticky note**, with a short arrow straight down or straight to the
  right.
- **An assignment is an orange arrow** carrying the value from where it is copied to where it goes.

`=` is an action, assignment: put this value in this variable. The test for equality is `==`, which
gives `true` or `false`.

## One source makes two books

- **The student book** is built with no tag, from `source/student_index.md`, into `build/html`.
- **The mentor book** is built with `-t mentor`, from `source/index.md`, into `build/mentor`.

`ninja book` builds both. B003, the welcome, is the first page of both. `MENTOR_ONLY` in
`source/conf.py` lists the pages only the mentor book has.

A mentor paragraph inside a shared page goes in a `{only} mentor` block, and so does any link to a
mentor-only page; outside one, that link fails the student build. A mentor part leads with bold
text, not a heading: a heading inside `only` brings the whole block into the student book.

## Running the gate

```
python3 -m venv book/.venv
book/.venv/bin/pip install -r book/requirements.txt
cd book && PATH="$PWD/.venv/bin:$PATH" ninja book
```

`ninja -k 0 book` runs every check even when the first fails, which is what you want when fixing
rather than gating. What each check does is in its own docstring.

## Pinning the solutions and releasing are in the README

How `keep.toml` names the solutions the answer pages come from, a round of lesson work, and
building and making a release: [Lesson work and releases](../README.md#lesson-work-and-releases).

## Seeing what a lesson asks a student to do

```
cd book && PATH="$PWD/.venv/bin:$PATH" ninja review
```

`ninja review` writes a separate Sphinx site under `review/` and prints where the HTML landed. One
page per lesson: the files it copies, then one section per file its patches change, and each change
is the diff that making it does, starting from the file as the copies left it and applying one
change at a time, so the context carries the changes already made. Changes made only of comments
are shown and labelled, because a page that says where code goes has to account for them.

It comes from applying `solutions/` in order, the same as the answer pages, and checks that the
changes applied one at a time rebuild what each patch left. `ninja book` builds it too.

It is a view for reading, not part of the guide: its pages are not committed. The answer pages
under `source/r950/` say what each lesson's patch changes in each file, from applying
`solutions/` in order; this says what each lesson asks, and how much work it is.
