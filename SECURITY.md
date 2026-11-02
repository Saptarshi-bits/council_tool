# Security Policy

## Supported versions

This project is in early development. Security fixes are applied to the latest
released version and the `main` branch.

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |
| < 0.1   | :x:                |

## Reporting a vulnerability

**Please do not open a public GitHub issue for security problems.**

Instead, report privately using one of these:

- GitHub's [private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability)
  (Security tab → "Report a vulnerability"), or
- email the maintainers at **saptarshibasu124@gmail.com**.

Please include:

- a description of the issue and its impact,
- steps to reproduce (a proof of concept if possible),
- affected version(s) or commit.

You can expect an acknowledgement within **5 business days**. We will keep you
informed of progress and coordinate a disclosure timeline with you once the
issue is confirmed.

## Handling of secrets (API keys)

This project follows a **Bring Your Own Key (BYOK)** model. No credentials are
ever stored in the repository:

- API keys, endpoints, and model names are read only from environment variables
  or a local `.env` file.
- `.env` (and any `*.env` except `.env.example`) is listed in `.gitignore` and
  must **never** be committed.
- If you discover a committed secret, treat it as compromised: rotate the key
  immediately and report it via the channel above.

Before pushing, verify no secret is staged, e.g.:

```bash
git diff --cached | grep -iE "api[_-]?key|secret|token|bearer" || echo "clean"
```
