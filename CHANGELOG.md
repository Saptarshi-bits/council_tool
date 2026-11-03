# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Nothing yet.

## [0.1.0] - 2026-11-03

### Added
- Initial public release of **LLM Council**.
- Flask backend (`council_project/server.py`) exposing endpoints to generate,
  save, and evaluate JUnit 5 tests, plus blind-review and vote JSON parsing.
- Browser UI (`council_tool.html`) driving the full council workflow:
  generate → save → metrics → blind review → vote → synthesis → winner.
- Quality metrics via JaCoCo (line/branch coverage) and PIT (mutation score).
- Provider-agnostic configuration (`council_project/config.py`): API key, base
  URL, model names, and endpoints are all read from the environment (BYOK).
- `.env.example` template and a zero-dependency `.env` loader.
- Unit tests for the configuration loader (`council_project/tests/`).
- Open-source project files: Apache-2.0 `LICENSE`, `NOTICE`, `README.md`,
  `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `CITATION.cff`,
  and GitHub issue/PR templates.
- Sample Java classes under `council_project/src/main/java/io/council/` to use
  as targets for test generation.

### Security
- Removed all hardcoded credentials and personal paths from the codebase;
  secrets are now supplied exclusively through a git-ignored `.env` file.

[Unreleased]: https://github.com/your-username/llm-council/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/your-username/llm-council/releases/tag/v0.1.0
