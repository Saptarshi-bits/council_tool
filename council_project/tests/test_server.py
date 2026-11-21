"""Endpoint tests for parsing model-generated JSON responses."""

import json
import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import server  # noqa: E402


@pytest.fixture
def client():
    server.app.config.update(TESTING=True)
    with server.app.test_client() as test_client:
        yield test_client


def test_parse_review_json_ignores_unrelated_braces(client):
    review = {
        "assertion_quality": 8,
        "edge_case_handling": 7,
        "semantic_correctness": 9,
        "readability": 6,
        "redundancy_reduction": 8,
        "branch_coverage_potential": 7,
        "overall_comments": 'Handles nested {values} and escaped quotes: "well".',
    }
    raw = (
        'An example is {"shape": {"name": "review"}}; this brace is unmatched: {. '
        "Final review:\n```json\n"
        f"{json.dumps(review)}\n```"
    )

    response = client.post("/parse-review-json", json={"raw": raw})

    assert response.status_code == 200
    parsed = response.get_json()
    assert parsed["assertion_quality"] == 8.0
    assert parsed["semantic_correctness"] == 9.0
    assert parsed["overall_comments"] == review["overall_comments"]
    assert parsed["avg"] == 7.5


def test_parse_vote_json_ignores_unrelated_braces(client):
    vote = {
        "vote": "A",
        "rank_1": "A",
        "rank_2": "B",
        "rank_3": "C",
        "correctness_rationale": 'The sample {"x": "}"} is not the answer.',
        "maintainability_rationale": "Clear structure.",
        "assertion_quality_rationale": "Specific assertions.",
        "edge_case_rationale": "Covers boundaries.",
    }
    raw = (
        "The previous format was {vote: A}; the final vote follows:\n"
        f"{json.dumps(vote)}\n"
    )

    response = client.post("/parse-vote-json", json={"raw": raw})

    assert response.status_code == 200
    parsed = response.get_json()
    assert parsed["vote"] == "A"
    assert parsed["rank_1"] == "A"
    assert parsed["correctness_rationale"] == vote["correctness_rationale"]


@pytest.mark.parametrize("path", ["/parse-review-json", "/parse-vote-json"])
def test_parse_endpoints_keep_no_object_error(client, path):
    response = client.post(path, json={"raw": "There is no structured answer."})

    assert response.status_code == 400
    assert response.get_json() == {"error": "No JSON object found"}


@pytest.mark.parametrize("path", ["/parse-review-json", "/parse-vote-json"])
def test_parse_endpoints_keep_malformed_object_error(client, path):
    response = client.post(path, json={"raw": "The answer is {not valid JSON}."})

    assert response.status_code == 400
    assert response.get_json()["error"]
