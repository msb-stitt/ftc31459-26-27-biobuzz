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
  - **AdvantageScope**: what to open or set to see the result.
  - **Log**: the same values in the flight log, from the repository's top folder.
  - **Send**: the code sent to the robot, and the OpMode picked and started on the Driver Station.
  - **Robot**: what to do with the robot. A part can instead be named for that one thing, as
    **Push**, **Spin** or **Drive**.
  - **Panels**: what to open in Panels to see the result. AdvantageScope connects only to the
    simulator.
- **Check**: one line after each part that changes what the student sees, saying what they see
  when it worked.

Each part is one or two imperative sentences. A part leads with its name in bold, not a heading.

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
looking for something that never appears, which is worse than saying nothing. Where the failure has
not been reproduced, the line says it comes from reading the code rather than from a robot.

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
changes applied one at a time rebuild what each patch left.

It is a view for reading, not a gate and not part of the guide: `ninja book` does not run it, and
its pages are not committed. The answer pages under `source/answers/` say what each lesson's patch
changes in each file, from applying `solutions/` in order; this says what each lesson asks, and how
much work it is.
