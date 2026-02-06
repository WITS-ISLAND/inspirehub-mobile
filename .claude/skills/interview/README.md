# Interview Skill

A structured requirement gathering skill that uses deep, hierarchical questioning to transform vague ideas into detailed specifications.

## What This Skill Does

The Interview skill conducts a systematic Q&A session to help you:

- **Clarify ambiguous requirements** - Turn "I want a login feature" into comprehensive specifications
- **Explore technical decisions** - Understand trade-offs between different approaches
- **Uncover hidden assumptions** - Surface constraints you haven't articulated yet
- **Gather detailed requirements** - Go from high-level ideas to implementation-ready specs

## When to Use It

Use this skill when:
- Starting a new feature and requirements are unclear
- Making architectural decisions with multiple valid approaches
- Refactoring and need to understand implications
- Exploring technical options (libraries, patterns, etc.)
- Converting a rough idea into a detailed specification

## How It Works

1. **Context Gathering**: Optionally searches codebase for related files
2. **Hierarchical Questioning**: 3-level deep-dive (12 questions total)
   - Level 1: High-level (problem, users, goals)
   - Level 2: Mid-level (requirements, technical approach)
   - Level 3: Detailed (edge cases, performance, security)
3. **Flexible Termination**: End early or continue deeper as needed
4. **Output Generation**: Creates integrated specification document

## Usage Examples

```bash
# Invoke manually with a topic
/interview authentication system for InspireHub

# Invoke for architecture decision
/interview state management approach for KMP

# Invoke for feature requirements
/interview idea visualization map feature

# Let Claude auto-invoke when you ask questions like:
"Interview me about the new feature we're planning"
"Help me gather requirements for the API refactoring"
```

## Output

The skill produces:

1. **`.interview_session.md`** - Q&A transcript (intermediate, kept for reference)
2. **Final specification** - Comprehensive markdown document at user-specified location

## Design Philosophy

Based on the approach from [this Zenn article](https://zenn.dev/kenfdev/articles/ba5507f7532418), this skill:

- Asks deep, non-obvious questions that reveal hidden assumptions
- Avoids surface-level or self-evident questions
- Adapts follow-up questions based on your answers
- Balances thoroughness with user patience (12 questions typical)
- Produces actionable, implementation-ready documentation

## Comparison with Other Skills

- **`doc-coauthoring`**: Use for writing/editing documents collaboratively
- **`interview`** (this): Use for requirement gathering through questioning
- **`skill-creator`**: Use for creating new skills

These skills complement each other but serve different purposes.

## Tips for Best Results

- Provide context when invoking (e.g., mention existing files or features)
- Answer thoughtfully - your answers shape follow-up questions
- Use "Other" option to add nuance beyond provided choices
- Don't rush - the goal is comprehensive understanding, not speed
- You can end early if you feel enough information has been gathered

## Example Session Flow

```
You: /interview mobile payment feature

Claude: I'll help you gather requirements for a mobile payment feature.
        Let me search for related files in the codebase...

        Found these potentially relevant files:
        - docs/design/機能一覧.md
        - shared/commonMain/.../Payment.kt
        - ...

        [Confirms file selection with you]

        --- Level 1: High-Level Questions ---
        Q1: What specific problem does this payment feature solve?
        [You answer]

        Q2-4: [More high-level questions]

        [After Level 1, asks if you want to continue to Level 2]

        --- Level 2: Mid-Level Questions ---
        ...

        [Generates final specification at end]
```

## Customization

You can modify this skill by editing `.claude/skills/interview/SKILL.md`:

- Adjust question count per level (currently 4 per level)
- Change level count (currently 3 levels)
- Modify output template
- Add domain-specific question categories

---

**Created**: 2026-01-29
**Inspired by**: [Claude Codeのインタビュー機能による仕様設計](https://zenn.dev/kenfdev/articles/ba5507f7532418)
