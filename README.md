# LLM Council

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Python 3.9+](https://img.shields.io/badge/python-3.9%2B-blue.svg)](https://www.python.org/)
[![Java 11+](https://img.shields.io/badge/java-11%2B-orange.svg)](https://adoptium.net/)

**LLM Council** turns several Large Language Models into a *panel of judges* for
unit-test generation. Instead of trusting a single model, it asks multiple LLMs
(GPT, Claude, Gemini) to each write a JUnit 5 test suite for the same Java class,
runs a **blind peer review and vote** between them, synthesizes a consensus
"winner" suite, and measures the real quality of every suite with **JaCoCo**
(line/branch coverage) and **PIT** (mutation testing).

It runs entirely on your machine: a small Flask backend plus a single-page
browser UI. It is **provider-agnostic** — you bring your own API key and point
it at any OpenAI-/Anthropic-/Gemini-compatible gateway.

> This tool was built in the context of a Software Quality Assurance & Testing
> (SQAT) study of whether an ensemble of LLMs produces higher-quality tests than
> any individual model.

---

## Table of contents

- [Features](#features)
- [How it works](#how-it-works)
- [Repository layout](#repository-layout)
- [Requirements](#requirements)
- [Quick start (Bring Your Own Key)](#quick-start-bring-your-own-key)
- [Using the tool](#using-the-tool)
- [Understanding the metrics](#understanding-the-metrics)
- [Configuration reference](#configuration-reference)
- [Development](#development)
- [Contributing](#contributing)
- [Security](#security)
- [License](#license)
- [Citation](#citation)

---

## Features

- **Multi-model test generation** — one Java class in, three independent JUnit 5
  suites out (one per council member).
- **Blind review & vote** — each model reviews the *anonymized* suites and scores
  them on assertion quality, edge-case handling, correctness, readability, and
  more; then casts a ranked vote.
- **Synthesis** — a chosen model merges the best ideas into a single consensus
  suite ("the Council's answer").
- **Objective metrics** — every suite is compiled and executed; JaCoCo reports
  line/branch coverage and PIT reports mutation score, so claims are measured,
  not asserted.
- **Bring Your Own Key (BYOK)** — no credentials in the source; everything is
  read from the environment, so the tool works with any compatible gateway.

## How it works

```
   Java class under test
            │
            ▼
   ┌──────────────────────────────────────────────┐
   │  1. Generate   GPT ─┐  Claude ─┐  Gemini ─┐    │
   │                     ▼          ▼          ▼    │
   │  2. Metrics    compile + JUnit + JaCoCo + PIT  │
   │                                                │
   │  3. Blind review   each model scores the       │
   │                    anonymized suites           │
   │                                                │
   │  4. Vote           ranked vote → winner        │
   │                                                │
   │  5. Synthesis      consensus suite + metrics   │
   └──────────────────────────────────────────────┘
            │
            ▼
   Comparison table: coverage & mutation score per model vs. the Council
```

The browser UI walks you through each step; the Flask backend handles file I/O,
Maven builds, and calls to your LLM gateway (the browser never sees your key).

## Repository layout

```
.
├── council_tool.html          # single-page UI (served at http://localhost:5001)
├── start_council.sh           # convenience launcher (macOS)
├── .env.example               # copy to .env and add your key (BYOK)
├── LICENSE / NOTICE           # Apache-2.0
├── README.md / CONTRIBUTING.md / CODE_OF_CONDUCT.md / SECURITY.md
├── CHANGELOG.md / CITATION.cff
└── council_project/
    ├── server.py              # Flask backend (provider-agnostic)
    ├── config.py              # reads key/URL/models from the environment
    ├── pom.xml                # Maven build: JUnit 5, JaCoCo, PIT
    ├── requirements.txt       # runtime Python deps (Flask)
    ├── requirements-dev.txt   # dev deps (pytest)
    ├── src/main/java/io/council/   # sample Java classes to test
    ├── src/test/java/io/council/   # active test suite (written during a run)
    └── tests/                 # Python unit tests for config.py
```

> `generated_tests/` is created at runtime to hold each model's output and is
> intentionally git-ignored.

## Requirements

- **Python 3.9+** (backend)
- **Java 11+** and **Apache Maven 3.6+** (to compile/run the generated tests)
- An **API key** for an LLM gateway that exposes OpenAI-, Anthropic-, and
  Gemini-compatible HTTP endpoints under a single base URL

## Quick start (Bring Your Own Key)

```bash
# 1. Clone
git clone https://github.com/your-username/llm-council.git
cd llm-council

# 2. Configure your key (BYOK) — never commit this file
cp .env.example .env
#   then edit .env and set LLM_API_KEY (and LLM_BASE_URL if your gateway differs)

# 3. Create a virtual environment and install dependencies
cd council_project
python3 -m venv .venv
source .venv/bin/activate              # Windows: .venv\Scripts\activate
pip install -r requirements.txt

# 4. Run the backend
python3 server.py
```

Then open <http://localhost:5001> in your browser.

On macOS you can skip steps 3–4 (once `.venv` exists) and just run the launcher
from the repo root:

```bash
./start_council.sh
```

If `LLM_API_KEY` is not set, the LLM buttons return a clear "key not set" error —
the rest of the UI (compile, run, metrics on manually pasted tests) still works.

## Using the tool

1. **Setup** — compiles the Java project and prepares output folders.
2. **Pick a class** — choose one of the sample classes in
   `council_project/src/main/java/io/council/` (or add your own).
3. **Generate** — each model writes a JUnit 5 suite (use the "Auto" buttons, or
   paste a suite in manually).
4. **Metrics** — each suite is compiled and executed; you get test count,
   line/branch coverage (JaCoCo), and mutation score (PIT).
5. **Blind review** — models score the anonymized suites.
6. **Vote** — a ranked vote picks the winner.
7. **Synthesis** — a model merges the best suite; its metrics are measured too.
8. **Compare** — a final table shows every model against the Council consensus.

## Understanding the metrics

| Metric | Tool | What it tells you |
| --- | --- | --- |
| **Line coverage** | JaCoCo | % of executable lines exercised by the tests |
| **Branch coverage** | JaCoCo | % of decision branches (if/else, switch) exercised |
| **Mutation score** | PIT | % of injected faults ("mutants") the tests actually *catch* |

Mutation score is the strongest signal: high line coverage with a low mutation
score means the tests *run* the code but don't really *check* its behavior.

## Configuration reference

All configuration is read from the environment (or a local `.env`). See
[`.env.example`](.env.example).

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `LLM_API_KEY` | **yes** | — | API key/token sent as a Bearer token to your gateway. |
| `LLM_BASE_URL` | no | `http://localhost:6655` | Base URL of your OpenAI-/Anthropic-/Gemini-compatible gateway. |
| `GPT_MODEL` | no | `gpt-5.5` | Model id for the GPT council seat. |
| `CLAUDE_MODEL` | no | `anthropic--claude-4.5-sonnet` | Model id for the Claude seat. |
| `GEMINI_MODEL` | no | `gemini-3.1-flash-lite` | Model id for the Gemini seat. |
| `GPT_URL` | no | derived from base URL | Override the full GPT endpoint. |
| `CLAUDE_URL` | no | derived from base URL | Override the full Claude endpoint. |
| `GEMINI_URL` | no | derived from base URL | Override the full Gemini endpoint. |
| `CLAUDE_MAX_TOKENS` | no | `16384` | Max output tokens for Claude. |

> `HYPERSPACE_API_KEY` / `HYPERSPACE_BASE` are still accepted as aliases for
> `LLM_API_KEY` / `LLM_BASE_URL` for backward compatibility.

## Development

Python unit tests for the configuration loader:

```bash
cd council_project
pip install -r requirements-dev.txt
pytest tests -q
```

Java build and the active test suite:

```bash
cd council_project
mvn test                                   # compile + run tests + JaCoCo
mvn org.pitest:pitest-maven:mutationCoverage   # mutation testing
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for
local setup, the BYOK workflow, coding conventions, and the pull-request
process. By participating you agree to the
[Code of Conduct](CODE_OF_CONDUCT.md).

## Security

Please report vulnerabilities privately as described in [SECURITY.md](SECURITY.md).
Never commit real API keys — the project is built around a git-ignored `.env`.

## License

Licensed under the **Apache License 2.0** — see [LICENSE](LICENSE) and
[NOTICE](NOTICE). You may use, modify, and distribute this software, including
commercially, provided you preserve the license and attribution notices.

## Citation

If you use LLM Council in academic work, please cite it using the metadata in
[CITATION.cff](CITATION.cff).
