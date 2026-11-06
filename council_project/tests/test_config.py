"""Unit tests for the provider-agnostic config loader (config.py)."""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import config  # noqa: E402

_ALL_VARS = (
    "LLM_API_KEY", "HYPERSPACE_API_KEY", "LLM_BASE_URL", "HYPERSPACE_BASE",
    "GPT_URL", "CLAUDE_URL", "GEMINI_URL",
    "GPT_MODEL", "CLAUDE_MODEL", "GEMINI_MODEL", "CLAUDE_MAX_TOKENS",
)


def _clean(monkeypatch):
    """Deterministic environment: ignore any real .env and clear all vars."""
    monkeypatch.setattr(config, "_load_dotenv", lambda: None)
    for var in _ALL_VARS:
        monkeypatch.delenv(var, raising=False)


def test_defaults_when_only_key_set(monkeypatch):
    _clean(monkeypatch)
    monkeypatch.setenv("LLM_API_KEY", "test-key-123")
    cfg = config.load_config()
    assert cfg.api_key == "test-key-123"
    assert cfg.base_url == "http://localhost:6655"
    assert cfg.gpt_url.endswith("/openai/v1/chat/completions")
    assert cfg.claude_url.endswith("/anthropic/v1/messages")
    assert "generateContent" in cfg.gemini_url
    assert cfg.key_missing is False


def test_key_missing_when_unset(monkeypatch):
    _clean(monkeypatch)
    cfg = config.load_config()
    assert cfg.key_missing is True


def test_placeholder_counts_as_missing(monkeypatch):
    _clean(monkeypatch)
    monkeypatch.setenv("LLM_API_KEY", "PASTE_YOUR_API_KEY_HERE")
    assert config.load_config().key_missing is True


def test_hyperspace_alias_still_works(monkeypatch):
    _clean(monkeypatch)
    monkeypatch.setenv("HYPERSPACE_API_KEY", "legacy-key")
    cfg = config.load_config()
    assert cfg.api_key == "legacy-key"
    assert cfg.key_missing is False


def test_custom_base_url_and_models(monkeypatch):
    _clean(monkeypatch)
    monkeypatch.setenv("LLM_API_KEY", "k")
    monkeypatch.setenv("LLM_BASE_URL", "https://gw.example.com/")  # trailing slash
    monkeypatch.setenv("GEMINI_MODEL", "my-gemini")
    cfg = config.load_config()
    assert cfg.base_url == "https://gw.example.com"
    assert cfg.gpt_url == "https://gw.example.com/openai/v1/chat/completions"
    assert "my-gemini" in cfg.gemini_url


def test_explicit_url_override_wins(monkeypatch):
    _clean(monkeypatch)
    monkeypatch.setenv("LLM_API_KEY", "k")
    monkeypatch.setenv("GPT_URL", "https://custom.example/openai")
    assert config.load_config().gpt_url == "https://custom.example/openai"
