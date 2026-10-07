# How the guide is written

**Status: controlling.** Written 2026-09-27; the lesson page format added 2026-10-05.

The register and the voice belong to
[`prose-style.md`](../../.claude/rules/parts/prose-style.md) and nothing here repeats it. What is
here is what that file does not say: the shape of a task page, how words are introduced, what a
picture has to be, and what a simplification may do.

## A lesson page is one page for both books

A lesson is named L*nnn*, counting by 10s from L020, and its page is `source/tasks/l020.md`, titled
`# L020: read the sticks`. The pages named `l2a` to `l17` keep the five parts below until the lesson
that takes their content replaces them.

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

Each part is one or two imperative sentences. A part leads with its name in bold, not a heading.

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

## A picture is registered before it is drawn

Every figure has a row in [`FIGURES.md`](FIGURES.md) and an id of the form `fig-<something>`. A
figure that does not exist yet is a pencilled box in the page, so the gap is visible to a reader and
not blocking to a writer. `tools/check_figures.py` checks both directions.

## One source makes two books

- **The student book** is built with no tag, from `source/student_index.md`, into `build/html`.
- **The mentor book** is built with `-t mentor`, from `source/index.md`, into `build/mentor`.

`ninja book` builds both. Both front pages include `source/_intro.md`. `MENTOR_ONLY` in
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

It is a view for reading, not a gate and not part of the guide: `ninja book` does not run it, and
its pages are not committed. The answer pages under `source/answers/` say what each lesson's patch
changes in each file, from applying `solutions/` in order; this says what each lesson asks, and how
much work it is.
