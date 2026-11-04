#!/usr/bin/env python3
"""Provider-agnostic configuration for the LLM Council backend.

Every setting is read from the environment so anyone can bring their own key,
endpoint, and model names (BYOK — Bring Your Own Key). Nothing is hardcoded.

Quick start:
    cp .env.example .env     # then edit .env with your values
    # ...or just export the variables in your shell.

The gateway is expected to expose OpenAI-, Anthropic-, and Gemini-compatible
HTTP APIs under a single base URL (for example a local proxy). If your setup
uses different paths, override the full endpoint URLs directly via GPT_URL /
CLAUDE_URL / GEMINI_URL.
"""

import os
from dataclasses import dataclass
from pathlib import Path

# Values the loader treats as "no real key provided yet".
_PLACEHOLDERS = {
    "",
    "PASTE_YOUR_API_KEY_HERE",
    "PASTE_YOUR_HYPERSPACE_API_KEY_HERE",
    "PASTE_YOUR_GEMINI_API_KEY_HERE",
}


def _load_dotenv() -> None:
    """Load KEY=VALUE pairs from the first .env found, without extra deps.

    Existing environment variables always win, so exported shell values are
    never overwritten by the file.
    """
    here = Path(__file__).resolve().parent
    for candidate in (here / ".env", here.parent / ".env", Path.cwd() / ".env"):
        if not candidate.is_file():
            continue
        for line in candidate.read_text().splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, val = line.partition("=")
            val = val.strip().strip('"').strip("'")
            os.environ.setdefault(key.strip(), val)
        break


def _env(name: str, default: str = "") -> str:
    return os.environ.get(name, default).strip()


@dataclass(frozen=True)
class LLMConfig:
    """Resolved configuration for the three LLM providers."""

    api_key: str
    base_url: str
    gpt_model: str
    claude_model: str
    gemini_model: str
    gpt_url: str
    claude_url: str
    gemini_url: str
    claude_max_tokens: int

    @property
    def key_missing(self) -> bool:
        return self.api_key in _PLACEHOLDERS


def load_config() -> LLMConfig:
    """Build an LLMConfig from the environment (loading .env if present)."""
    _load_dotenv()

    # Generic name first; HYPERSPACE_API_KEY kept as a backward-compatible alias.
    api_key = _env("LLM_API_KEY") or _env("HYPERSPACE_API_KEY")
    base_url = (_env("LLM_BASE_URL") or _env("HYPERSPACE_BASE")
                or "http://localhost:6655").rstrip("/")

    gpt_model = _env("GPT_MODEL", "gpt-5.5")
    claude_model = _env("CLAUDE_MODEL", "anthropic--claude-4.5-sonnet")
    gemini_model = _env("GEMINI_MODEL", "gemini-3.1-flash-lite")

    # Full endpoint URLs may be overridden directly; otherwise they are derived
    # from base_url using common OpenAI/Anthropic/Gemini-compatible paths.
    gpt_url = _env("GPT_URL") or f"{base_url}/openai/v1/chat/completions"
    claude_url = _env("CLAUDE_URL") or f"{base_url}/anthropic/v1/messages"
    gemini_url = (_env("GEMINI_URL")
                  or f"{base_url}/gemini/v1beta/models/{gemini_model}:generateContent")

    try:
        max_tokens = int(_env("CLAUDE_MAX_TOKENS", "16384") or "16384")
    except ValueError:
        max_tokens = 16384

    return LLMConfig(
        api_key=api_key,
        base_url=base_url,
        gpt_model=gpt_model,
        claude_model=claude_model,
        gemini_model=gemini_model,
        gpt_url=gpt_url,
        claude_url=claude_url,
        gemini_url=gemini_url,
        claude_max_tokens=max_tokens,
    )
