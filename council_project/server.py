#!/usr/bin/env python3
"""
LLM Council Research Tool — Local Backend
Run: .venv/bin/python3 server.py
Then open: http://localhost:5001
"""

import json
import subprocess
import threading
import urllib.error
import urllib.request
import uuid
import xml.etree.ElementTree as ET
from pathlib import Path

from flask import Flask, jsonify, request
from flask_cors import CORS

from config import load_config

app = Flask(__name__)
CORS(app)

BASE_DIR      = Path(__file__).parent.resolve()
GENERATED_DIR = BASE_DIR.parent / "generated_tests"
TEST_SRC_DIR  = BASE_DIR / "src" / "test" / "java" / "io" / "council"

# ── Provider-agnostic LLM config (bring your own key — see config.py) ─────────
# Key, base URL, models, and endpoints all come from the environment, so anyone
# can point this at their own OpenAI-/Anthropic-/Gemini-compatible gateway.
# Never hardcode a key here. Copy .env.example to .env and fill in your values.
CFG = load_config()

def _key_missing():
    return CFG.key_missing

def _key_error():
    return jsonify({
        "error": "LLM API key not set — copy .env.example to .env and set "
                 "LLM_API_KEY (and LLM_BASE_URL if your gateway differs), "
                 "then restart the server."
    }), 400

def _post_json(url, payload, headers):
    req = urllib.request.Request(url, data=json.dumps(payload).encode(),
                                 headers={"Content-Type": "application/json", **headers},
                                 method="POST")
    with urllib.request.urlopen(req, timeout=180) as resp:
        return json.loads(resp.read().decode())


def _extract_json_object(raw, expected_keys):
    """Extract a JSON object from free-form model output."""
    decoder = json.JSONDecoder()
    first_object = None
    first_error = None

    for start, char in enumerate(raw):
        if char != "{":
            continue
        try:
            parsed, _ = decoder.raw_decode(raw, start)
        except json.JSONDecodeError as exc:
            if first_error is None:
                first_error = exc
            continue
        if not isinstance(parsed, dict):
            continue
        if any(key in parsed for key in expected_keys):
            return parsed
        if first_object is None:
            first_object = parsed

    if first_object is not None:
        return first_object
    if first_error is not None:
        raise first_error
    return None

# ── Job store ──────────────────────────────────────────────────────────────────
jobs = {}
jobs_lock = threading.Lock()

def new_job():
    jid = str(uuid.uuid4())
    with jobs_lock:
        jobs[jid] = {"lines": [], "done": False, "success": False, "result": None}
    return jid

def job_log(jid, msg):
    with jobs_lock:
        jobs[jid]["lines"].append(msg)

def job_done(jid, success, result=None):
    with jobs_lock:
        jobs[jid]["done"]    = True
        jobs[jid]["success"] = success
        jobs[jid]["result"]  = result

# ── Maven helpers ──────────────────────────────────────────────────────────────
def run_mvn(jid, args):
    """Run mvn <args> in BASE_DIR, stream output to job log, return exit code."""
    cmd = ["mvn", "--no-transfer-progress"] + args
    proc = subprocess.Popen(
        cmd, cwd=str(BASE_DIR),
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, bufsize=1
    )
    for raw in proc.stdout:
        line = raw.rstrip()
        if line:
            job_log(jid, line)
    proc.wait()
    return proc.returncode

# ── Routes ─────────────────────────────────────────────────────────────────────

@app.route("/")
def index():
    return (BASE_DIR.parent / "council_tool.html").read_text()

@app.route("/health")
def health():
    return jsonify({"status": "ok", "project": str(BASE_DIR)})

# ── LLM proxy endpoints (all via Hyperspace, one key) ─────────────────────────
# The browser never sees the API key. Used by the "🤖 Auto" buttons.
_AUTH = lambda: {"Authorization": f"Bearer {CFG.api_key}"}

@app.route("/gpt", methods=["POST"])
def gpt():
    prompt = (request.json or {}).get("prompt", "").strip()
    if not prompt:
        return jsonify({"error": "prompt required"}), 400
    if _key_missing():
        return _key_error()
    try:
        body = _post_json(CFG.gpt_url, {
            "model": CFG.gpt_model,
            "messages": [{"role": "user", "content": prompt}],
        }, _AUTH())
    except urllib.error.HTTPError as e:
        return jsonify({"error": f"GPT API error {e.code}",
                        "detail": e.read().decode(errors="replace")[:500]}), 502
    except Exception as e:
        return jsonify({"error": f"GPT request failed: {e}"}), 502
    try:
        text = body["choices"][0]["message"]["content"]
    except (KeyError, IndexError):
        return jsonify({"error": "Unexpected GPT response", "raw": body}), 502
    return jsonify({"text": text, "model": CFG.gpt_model})

@app.route("/claude", methods=["POST"])
def claude():
    prompt = (request.json or {}).get("prompt", "").strip()
    if not prompt:
        return jsonify({"error": "prompt required"}), 400
    if _key_missing():
        return _key_error()
    try:
        body = _post_json(CFG.claude_url, {
            "model": CFG.claude_model,
            "max_tokens": CFG.claude_max_tokens,
            "messages": [{"role": "user", "content": prompt}],
        }, {**_AUTH(), "anthropic-version": "2023-06-01"})
    except urllib.error.HTTPError as e:
        return jsonify({"error": f"Claude API error {e.code}",
                        "detail": e.read().decode(errors="replace")[:500]}), 502
    except Exception as e:
        return jsonify({"error": f"Claude request failed: {e}"}), 502
    try:
        text = "".join(part.get("text", "") for part in body["content"])
    except (KeyError, TypeError):
        return jsonify({"error": "Unexpected Claude response", "raw": body}), 502
    return jsonify({"text": text, "model": CFG.claude_model})

@app.route("/gemini", methods=["POST"])
def gemini():
    prompt = (request.json or {}).get("prompt", "").strip()
    if not prompt:
        return jsonify({"error": "prompt required"}), 400
    if _key_missing():
        return _key_error()
    try:
        body = _post_json(CFG.gemini_url, {
            "contents": [{"role": "user", "parts": [{"text": prompt}]}],
        }, _AUTH())
    except urllib.error.HTTPError as e:
        return jsonify({"error": f"Gemini API error {e.code}",
                        "detail": e.read().decode(errors="replace")[:500]}), 502
    except Exception as e:
        return jsonify({"error": f"Gemini request failed: {e}"}), 502
    try:
        parts = body["candidates"][0]["content"]["parts"]
        text = "".join(p.get("text", "") for p in parts)
    except (KeyError, IndexError):
        return jsonify({"error": "Unexpected Gemini response", "raw": body}), 502
    return jsonify({"text": text, "model": CFG.gemini_model})

# ── Job polling ────────────────────────────────────────────────────────────────
@app.route("/job-status")
def job_status():
    jid = request.args.get("id")
    if not jid:
        return jsonify({"error": "id required"}), 400
    with jobs_lock:
        job = jobs.get(jid)
    if not job:
        return jsonify({"error": "job not found"}), 404
    offset = int(request.args.get("offset", 0))
    with jobs_lock:
        new_lines = jobs[jid]["lines"][offset:]
        done      = jobs[jid]["done"]
        success   = jobs[jid]["success"]
        result    = jobs[jid]["result"]
    return jsonify({
        "lines":   new_lines,
        "offset":  offset + len(new_lines),
        "done":    done,
        "success": success,
        "result":  result,
    })

# ── /setup ─────────────────────────────────────────────────────────────────────
@app.route("/setup", methods=["POST"])
def setup():
    jid = new_job()

    def run():
        job_log(jid, "Creating output directories...")
        for model in ["GPT", "Claude", "Gemini", "Council"]:
            p = GENERATED_DIR / model
            p.mkdir(parents=True, exist_ok=True)
            job_log(jid, f"  ✓ {p}")

        job_log(jid, "Running mvn compile ...")
        rc = run_mvn(jid, ["compile"])
        if rc == 0:
            job_log(jid, "✅ BUILD SUCCESSFUL — ready to generate tests")
            job_done(jid, True)
        else:
            job_log(jid, f"❌ BUILD FAILED (exit code {rc})")
            job_done(jid, False)

    threading.Thread(target=run, daemon=True).start()
    return jsonify({"jobId": jid})

# ── /save-test ─────────────────────────────────────────────────────────────────
@app.route("/save-test", methods=["POST"])
def save_test():
    data       = request.json
    model      = data.get("model")
    class_name = data.get("className")
    code       = data.get("code", "")

    if not model or not class_name or not code.strip():
        return jsonify({"error": "model, className, and code required"}), 400

    test_class = class_name + "Test"
    dest = GENERATED_DIR / model / f"{test_class}_{model}.java"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(code)
    return jsonify({"saved": str(dest), "testClass": test_class})

# ── /run-metrics ───────────────────────────────────────────────────────────────
@app.route("/run-metrics", methods=["POST"])
def run_metrics():
    data       = request.json
    model      = data.get("model")
    class_name = data.get("className")

    if not model or not class_name:
        return jsonify({"error": "model and className required"}), 400

    jid = new_job()

    def run():
        test_class = class_name + "Test"
        src_file   = GENERATED_DIR / model / f"{test_class}_{model}.java"
        dest_file  = TEST_SRC_DIR / f"{test_class}.java"

        if not src_file.exists():
            job_log(jid, f"❌ File not found: {src_file}")
            job_done(jid, False)
            return

        TEST_SRC_DIR.mkdir(parents=True, exist_ok=True)
        for f in TEST_SRC_DIR.glob("*Test.java"):
            if f != dest_file:
                f.unlink()
        dest_file.write_text(src_file.read_text())
        test_count = src_file.read_text().count("@Test")
        job_log(jid, f"Found {test_count} @Test methods")

        # Compile test
        job_log(jid, "Compiling test class ...")
        rc = run_mvn(jid, ["test-compile"])
        compile_ok = rc == 0
        job_log(jid, f"Compile: {'✓ OK' if compile_ok else '✗ FAILED'}")

        if not compile_ok:
            job_done(jid, False, {
                "model": model, "compile": "NO", "pass": "NO",
                "testCount": test_count, "lineCoverage": 0,
                "branchCoverage": 0, "mutationScore": 0, "failures": 0,
            })
            return

        # Tests + JaCoCo
        job_log(jid, "Running tests + JaCoCo ...")
        rc2 = run_mvn(jid, ["test"])
        tests_pass = rc2 == 0
        with jobs_lock:
            failures = sum(1 for l in jobs[jid]["lines"] if "FAILED" in l or "Tests run:" in l and "Failures: 0" not in l)
        job_log(jid, f"Tests: {'✓ ALL PASSED' if tests_pass else '✗ failures detected'}")

        line_cov, branch_cov = parse_jacoco(class_name)
        job_log(jid, f"JaCoCo → Line: {line_cov}%  Branch: {branch_cov}%")

        # PIT
        job_log(jid, "Running PIT mutation testing (1–3 min) ...")
        # Delete stale PIT report so we never read a previous run's results
        pit_xml = BASE_DIR / "target" / "pit-reports" / "mutations.xml"
        if pit_xml.exists():
            pit_xml.unlink()
        rc3 = run_mvn(jid, ["org.pitest:pitest-maven:mutationCoverage"])
        if rc3 == 0:
            mutation_score = parse_pitest(class_name)
            job_log(jid, f"PIT → Mutation Score: {mutation_score}%")
        else:
            mutation_score = None
            job_log(jid, "PIT → Mutation Score: N/A (suite has test failures — fix failing tests first)")
        job_log(jid, "✅ Done")

        job_done(jid, True, {
            "model":          model,
            "compile":        "YES",
            "pass":           "YES" if tests_pass else "NO",
            "testCount":      test_count,
            "lineCoverage":   line_cov,
            "branchCoverage": branch_cov,
            "mutationScore":  mutation_score,
            "failures":       failures,
        })

    threading.Thread(target=run, daemon=True).start()
    return jsonify({"jobId": jid})

# ── /run-winner-metrics ────────────────────────────────────────────────────────
@app.route("/run-winner-metrics", methods=["POST"])
def run_winner_metrics():
    data       = request.json
    model      = data.get("model")
    class_name = data.get("className")

    if not model or not class_name:
        return jsonify({"error": "model and className required"}), 400

    jid = new_job()

    def run():
        test_class   = class_name + "Test"
        src_file     = GENERATED_DIR / model / f"{test_class}_{model}.java"
        dest_file    = TEST_SRC_DIR / f"{test_class}.java"
        council_file = GENERATED_DIR / "Council" / f"{test_class}_Council_{class_name}.java"

        if not src_file.exists():
            job_log(jid, f"❌ File not found: {src_file}")
            job_done(jid, False)
            return

        council_file.parent.mkdir(parents=True, exist_ok=True)
        TEST_SRC_DIR.mkdir(parents=True, exist_ok=True)
        for f in TEST_SRC_DIR.glob("*Test.java"):
            if f != dest_file:
                f.unlink()
        dest_file.write_text(src_file.read_text())
        council_file.write_text(src_file.read_text())

        test_count = src_file.read_text().count("@Test")
        job_log(jid, f"[Council Winner: {model}] Running final validation ...")

        rc = run_mvn(jid, ["test"])
        line_cov, branch_cov = parse_jacoco(class_name)
        job_log(jid, f"JaCoCo → Line: {line_cov}%  Branch: {branch_cov}%")

        pit_xml = BASE_DIR / "target" / "pit-reports" / "mutations.xml"
        if pit_xml.exists():
            pit_xml.unlink()
        rc2 = run_mvn(jid, ["org.pitest:pitest-maven:mutationCoverage"])
        if rc2 == 0:
            mutation_score = parse_pitest(class_name)
            job_log(jid, f"PIT → Mutation Score: {mutation_score}%")
        else:
            mutation_score = None
            job_log(jid, "PIT → Mutation Score: N/A (suite has test failures — fix failing tests first)")
        job_log(jid, "✅ Done")

        job_done(jid, True, {
            "model":          model,
            "compile":        "YES",
            "pass":           "YES" if rc == 0 else "NO",
            "testCount":      test_count,
            "lineCoverage":   line_cov,
            "branchCoverage": branch_cov,
            "mutationScore":  mutation_score,
            "failures":       0,
        })

    threading.Thread(target=run, daemon=True).start()
    return jsonify({"jobId": jid})

# ── /save-synthesis-test ──────────────────────────────────────────────────────
@app.route("/save-synthesis-test", methods=["POST"])
def save_synthesis_test():
    data       = request.json
    model      = data.get("model")          # which synthesizer: GPT | Claude | Gemini
    class_name = data.get("className")
    code       = data.get("code", "")

    if not model or not class_name or not code.strip():
        return jsonify({"error": "model, className, and code required"}), 400

    test_class = class_name + "Test"
    dest = GENERATED_DIR / "Council" / f"{test_class}_Synthesis_{model}.java"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(code)
    return jsonify({"saved": str(dest), "testClass": test_class})

# ── /run-synthesis-metrics ────────────────────────────────────────────────────
@app.route("/run-synthesis-metrics", methods=["POST"])
def run_synthesis_metrics():
    data       = request.json
    model      = data.get("model")
    class_name = data.get("className")

    if not model or not class_name:
        return jsonify({"error": "model and className required"}), 400

    jid = new_job()

    def run():
        test_class = class_name + "Test"
        src_file   = GENERATED_DIR / "Council" / f"{test_class}_Synthesis_{model}.java"
        dest_file  = TEST_SRC_DIR / f"{test_class}.java"

        if not src_file.exists():
            job_log(jid, f"❌ File not found: {src_file}")
            job_done(jid, False)
            return

        TEST_SRC_DIR.mkdir(parents=True, exist_ok=True)
        for f in TEST_SRC_DIR.glob("*Test.java"):
            if f != dest_file:
                f.unlink()
        dest_file.write_text(src_file.read_text())
        test_count = src_file.read_text().count("@Test")
        job_log(jid, f"[Synthesis by {model}] Compiling ...")

        rc = run_mvn(jid, ["test-compile"])
        if rc != 0:
            job_log(jid, "❌ Compile failed")
            job_done(jid, False, {
                "model": model, "compile": "NO", "pass": "NO",
                "testCount": test_count, "lineCoverage": 0,
                "branchCoverage": 0, "mutationScore": 0, "failures": 0,
            })
            return

        rc2 = run_mvn(jid, ["test"])
        tests_pass = rc2 == 0
        line_cov, branch_cov = parse_jacoco(class_name)
        job_log(jid, f"JaCoCo → Line: {line_cov}%  Branch: {branch_cov}%")

        pit_xml = BASE_DIR / "target" / "pit-reports" / "mutations.xml"
        if pit_xml.exists():
            pit_xml.unlink()
        rc3 = run_mvn(jid, ["org.pitest:pitest-maven:mutationCoverage"])
        if rc3 == 0:
            mutation_score = parse_pitest(class_name)
            job_log(jid, f"PIT → Mutation Score: {mutation_score}%")
        else:
            mutation_score = None
            job_log(jid, "PIT → Mutation Score: N/A (suite has test failures)")
        job_log(jid, "✅ Done")

        job_done(jid, True, {
            "model":          model,
            "compile":        "YES",
            "pass":           "YES" if tests_pass else "NO",
            "testCount":      test_count,
            "lineCoverage":   line_cov,
            "branchCoverage": branch_cov,
            "mutationScore":  mutation_score,
            "failures":       0,
        })

    threading.Thread(target=run, daemon=True).start()
    return jsonify({"jobId": jid})

# ── /parse-review-json ─────────────────────────────────────────────────────────
@app.route("/parse-review-json", methods=["POST"])
def parse_review_json():
    raw = request.json.get("raw", "")
    try:
        criteria = ["assertion_quality","edge_case_handling","semantic_correctness",
                    "readability","redundancy_reduction","branch_coverage_potential"]
        parsed = _extract_json_object(raw, criteria + ["overall_comments"])
        if parsed is None:
            return jsonify({"error": "No JSON object found"}), 400
        scores = {}
        for c in criteria:
            try:
                scores[c] = float(parsed[c])
            except (KeyError, TypeError, ValueError):
                scores[c] = None
        scores["overall_comments"] = parsed.get("overall_comments", "")
        nums = [v for v in scores.values() if isinstance(v, float)]
        scores["avg"] = round(sum(nums) / len(nums), 2) if nums else 0
        return jsonify(scores)
    except json.JSONDecodeError as e:
        return jsonify({"error": str(e)}), 400

# ── /parse-vote-json ───────────────────────────────────────────────────────────
@app.route("/parse-vote-json", methods=["POST"])
def parse_vote_json():
    raw = request.json.get("raw", "")
    try:
        fields = (
            "vote", "rank_1", "rank_2", "rank_3",
            "correctness_rationale", "maintainability_rationale",
            "assertion_quality_rationale", "edge_case_rationale",
        )
        parsed = _extract_json_object(raw, fields)
        if parsed is None:
            return jsonify({"error": "No JSON object found"}), 400
        vote = parsed.get("vote", "").strip()
        # Accept blind labels (A/B/C) or real model names — resolution done client-side
        return jsonify({
            "vote":                          vote,
            "rank_1":                        parsed.get("rank_1", ""),
            "rank_2":                        parsed.get("rank_2", ""),
            "rank_3":                        parsed.get("rank_3", ""),
            "correctness_rationale":         parsed.get("correctness_rationale", ""),
            "maintainability_rationale":     parsed.get("maintainability_rationale", ""),
            "assertion_quality_rationale":   parsed.get("assertion_quality_rationale", ""),
            "edge_case_rationale":           parsed.get("edge_case_rationale", ""),
        })
    except json.JSONDecodeError as e:
        return jsonify({"error": str(e)}), 400

# ── XML parsers ────────────────────────────────────────────────────────────────
def parse_jacoco(class_name):
    # Maven puts JaCoCo report at target/site/jacoco/jacoco.xml
    xml = BASE_DIR / "target" / "site" / "jacoco" / "jacoco.xml"
    if not xml.exists():
        return 0, 0
    try:
        root = ET.parse(str(xml)).getroot()
        for cls in root.findall(".//class"):
            if class_name in cls.get("name", "").split("/")[-1]:
                return _counter(cls, "LINE"), _counter(cls, "BRANCH")
        return _counter(root, "LINE"), _counter(root, "BRANCH")
    except Exception:
        return 0, 0

def _counter(el, ctype):
    for c in el.findall("counter"):
        if c.get("type") == ctype:
            covered = int(c.get("covered", 0))
            total   = covered + int(c.get("missed", 0))
            return round(covered / total * 100, 1) if total else 0
    return 0

def parse_pitest(class_name):
    # Maven PIT puts report at target/pit-reports/mutations.xml
    xml = BASE_DIR / "target" / "pit-reports" / "mutations.xml"
    if not xml.exists():
        return 0
    try:
        root   = ET.parse(str(xml)).getroot()
        muts   = root.findall(".//mutation")
        subset = [m for m in muts if class_name in m.findtext("mutatedClass", "")]
        if not subset:
            subset = muts
        if not subset:
            return 0
        killed = sum(1 for m in subset if m.get("detected", "false").lower() == "true")
        return round(killed / len(subset) * 100, 1)
    except Exception:
        return 0

# ── Main ───────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    print("=" * 55)
    print("  LLM Council Research Tool — Local Backend")
    print("=" * 55)
    print(f"  Project: {BASE_DIR}")
    print(f"  Tests:   {GENERATED_DIR}")
    print()
    print("  Open http://localhost:5001")
    print("=" * 55)
    app.run(host="127.0.0.1", port=5001, debug=False, threaded=True)
