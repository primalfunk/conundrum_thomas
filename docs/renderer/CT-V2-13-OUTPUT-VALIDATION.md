# CT-V2-13 Output Validation

`DeterministicRenderValidator` checks mechanically enforceable constraints before output acceptance:

- required versus forbidden emptiness and control characters;
- character, sentence, and question ceilings;
- candidate-declared mode and semantic act;
- required, unknown, and missing semantic-unit IDs;
- allowed memory/source IDs and prohibited sources;
- required grounding, attribution, epistemic, tentativeness, and temporal markers;
- allowed introduced entity and temporal literals;
- scripted diagnosis, cause, motive, trait, mode-switch, technique, advice, identity-merge, contradiction-winner, policy-mutation, system-disclosure, and medical-authority signals;
- command-specific prohibited phrases and exact fixed safety wording;
- normalized recent duplicates and repeated openings.

Questions are counted conservatively by unquoted question marks. Straight and curly quoted source spans are ignored; every unquoted question mark, including a rhetorical one, counts. Sentences are bounded through unquoted terminal punctuation with a minimum count of one for nonblank text. This method is deterministic and its edge behavior is qualified, but it is not a general natural-language parser.

Candidate manifests make known adapter behavior inspectable. They do not make an untrusted declaration true: text-level guards and the fixed reference corpus independently constrain known violation families.

## Honest limitation

String rules cannot prove arbitrary natural-language semantic equivalence, detect every possible paraphrased invention, or establish that every future model output preserves meaning. CT-V2-13 proves typed authority separation, minimal visibility, deterministic mechanical limits, the correctness of its fixed reference corpus, rejection of scripted violations, and safe fallback. Every future model/adapter requires its own empirical semantic-fidelity admission corpus.
