# StoreOps Harness Orchestrator

## Entry Format

To start a harness run, invoke the Planner with:

```
@planner <feature description>
```

Example:
```
@planner Add shift handover bulk update to activities
```

## Agent Files

| Agent | File | Responsibility |
|---|---|---|
| Planner | .harness/agents/planner.agent.md | Decomposes feature into sprint contracts |
| Generator | .harness/agents/generator.agent.md | Implements sprint contracts as code |
| Evaluator | .harness/agents/evaluator.agent.md | Reviews and verdicts Generator output |
| Monitor | .harness/agents/monitor.agent.md | Records sprint run outcomes |

## Orchestration Sequence

```
1. Developer invokes: @planner <feature description>
2. Planner reads skill files → writes .harness/output/spec.md
   spec.md contains STATUS: AWAITING APPROVAL
3. Developer reviews spec.md → types APPROVED to continue
4. Generator/Evaluator loop begins (autonomous):
   a. Generator reads sprint-N-contract.md → writes code in src/ + generator-summary.md
   b. Evaluator reads generator-summary.md → runs checks → writes evaluator-feedback.md
   c. Orchestrator reads VERDICT in evaluator-feedback.md:
      - VERDICT: PASS → Monitor runs → advance to next sprint
      - VERDICT: CONDITIONAL PASS → Monitor runs → advance with noted items
      - VERDICT: FAIL → increment iteration counter → retry Generator with feedback
      - 3 consecutive FAILs → write escalation.md → stop and notify developer
5. Monitor runs after every sprint verdict → archives run-log.md to .harness/reviews/
```

## Routing Logic

After each Evaluator run, read `.harness/output/evaluator-feedback.md`:

- If `VERDICT: PASS` → run Monitor → move to next sprint contract
- If `VERDICT: CONDITIONAL PASS` → run Monitor → move to next sprint with noted gaps
- If `VERDICT: FAIL` AND iteration < 3 → pass evaluator-feedback.md back to Generator → retry
- If `VERDICT: FAIL` AND iteration = 3 → write `.harness/output/escalation.md` → stop

## Escalation Output

When iteration limit is reached, write `.harness/output/escalation.md`:

```
ESCALATION NOTICE
Sprint: sprint-N
Iterations used: 3
Blocking issue: <copy the hard gate failure reason from evaluator-feedback.md>
Action required: Developer must resolve the blocking issue manually before resuming.
```

## Maximum Iterations Per Sprint

**3 iterations maximum.** After 3 consecutive FAIL verdicts on the same sprint, escalate.

## Context Scoping Strategy

Each agent invocation is scoped to prevent context window degradation:

- **Planner**: reads app-context + architecture-principles skill files + developer prompt only
- **Generator**: reads its skill files + current sprint-N-contract.md only. Does NOT read previous sprint outputs.
- **Evaluator**: reads its skill files + generator-summary.md + evaluator-feedback.md from current sprint only
- **Monitor**: reads evaluator-feedback.md + generator-summary.md from current sprint only
- Context is reset between sprints — no accumulated context across sprint boundaries

## CI/CD Relationship

The harness **precedes** the CI pipeline gate:

1. Harness Evaluator runs `mvn compile`, `mvn test`, `mvn checkstyle:check` locally
2. Only code that passes the Evaluator hard gates is committed
3. CI pipeline (`mvn test`) then runs as a second confirmation gate
4. The harness does not replace CI — it ensures AI-generated code reaches CI clean

## Skill Files Reference

Shared (all agents):
- `.harness/skills/app-context/SKILL.md`
- `.harness/skills/architecture-principles/SKILL.md`

Generator-specific:
- `.harness/skills/coding-conventions/SKILL.md`
- `.harness/skills/api-integration/SKILL.md`
- `.harness/skills/how-to-test/SKILL.md`

Evaluator-specific:
- `.harness/skills/how-to-review/SKILL.md`
- `.harness/skills/evaluation-criteria/SKILL.md`
