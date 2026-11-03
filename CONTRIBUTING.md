# Contributing to LLM Council

First off, thank you for considering a contribution! This document explains how
to set up the project locally, the **Bring Your Own Key (BYOK)** workflow, and
how to submit changes.

By participating in this project you agree to abide by our
[Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to contribute

- Report bugs or request features via [GitHub Issues](../../issues).
- Improve documentation.
- Add or improve sample Java classes to test.
- Improve the backend, UI, or metrics pipeline.
- Add tests.

For anything beyond a small fix, please open an issue first so we can agree on
the approach before you invest time.

## Bring Your Own Key (BYOK)

This project **never ships or stores API keys**. Every contributor uses their
own key and gateway. The source contains no credentials — all configuration is
read from the environment.

1. Copy the template and fill in **your own** values:
   ```bash
   cp .env.example .env
   ```
2. Edit `.env`:
   - `LLM_API_KEY` — your key/token (**required**).
   - `LLM_BASE_URL` — your gateway's base URL (defaults to a local proxy).
   - `GPT_MODEL`, `CLAUDE_MODEL`, `GEMINI_MODEL` — model ids your gateway serves.
   - Optionally `GPT_URL` / `CLAUDE_URL` / `GEMINI_URL` to override full endpoints.
3. **Never commit `.env`.** It is already in `.gitignore`. Only `.env.example`
   (with placeholder values) is tracked.

If you add a new configuration option, add it to **both** `config.py` and
`.env.example`, and document it in the README's configuration table.

## Local setup

**Prerequisites:** Python 3.9+, Java 11+, Apache Maven 3.6+.

```bash
# From the repository root
cp .env.example .env            # add your key

cd council_project
python3 -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements-dev.txt

python3 server.py                # serves http://localhost:5001
```

## Running the checks

Please make sure these pass before opening a pull request.

```bash
# Python unit tests (config loader) — no API key required
cd council_project
pytest tests -q

# Java: compile, run the active test suite, and mutation testing
mvn test
mvn org.pitest:pitest-maven:mutationCoverage
```

## Coding conventions

- **Python**: follow [PEP 8](https://peps.python.org/pep-0008/). Keep functions
  small and readable; prefer standard library over new dependencies. If you must
  add a dependency, justify it in the PR and add it to `requirements.txt`.
- **Java**: standard Java conventions; tests use **JUnit 5** (`org.junit.jupiter`).
- **Comments**: explain *why*, not *what*. Avoid narrating obvious code.
- Keep changes focused — one logical change per pull request.

## Commit messages

Use clear, imperative commit subjects, ideally following
[Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add mutation-score column to the comparison table
fix: handle empty JaCoCo report gracefully
docs: clarify BYOK setup in README
```

## Pull request process

1. Fork the repository and create a feature branch from `main`
   (e.g. `feat/my-change`).
2. Make your change and add/adjust tests where it makes sense.
3. Ensure all checks above pass and that **no secret is staged**:
   ```bash
   git diff --cached | grep -iE "api[_-]?key|secret|token|bearer" || echo "clean"
   ```
4. Update `README.md`, `.env.example`, and `CHANGELOG.md` if your change affects
   usage, configuration, or behavior.
5. Open a pull request against `main`, fill in the PR template, and link any
   related issue.

A maintainer will review your PR. Please be responsive to feedback — see
[Code of Conduct](CODE_OF_CONDUCT.md) for the kind of collaboration we aim for.

## Reporting security issues

Do **not** open a public issue for security vulnerabilities. Follow the process
in [SECURITY.md](SECURITY.md).

## License of contributions

By contributing, you agree that your contributions will be licensed under the
[Apache License 2.0](LICENSE), consistent with the rest of the project
(see Section 5 of the license regarding contributions).
