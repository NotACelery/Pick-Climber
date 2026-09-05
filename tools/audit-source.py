#!/usr/bin/env python3
from pathlib import Path
import json, re, sys
ROOT = Path(__file__).resolve().parents[1]
violations = []
java_files = list((ROOT / "src").rglob("*.java"))
for path in java_files:
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if "\t" in line:
            violations.append(f"{path.relative_to(ROOT)}:{number}: tab")
        if line.rstrip() != line:
            violations.append(f"{path.relative_to(ROOT)}:{number}: trailing whitespace")
        if len(line) > 120:
            violations.append(f"{path.relative_to(ROOT)}:{number}: {len(line)} chars")
        if any(token in line for token in ("System.out.", "printStackTrace(", "TODO", "FIXME", "HACK")):
            violations.append(f"{path.relative_to(ROOT)}:{number}: temporary/debug marker")
for path in (ROOT / "src/main/resources").rglob("*"):
    if path.suffix not in {".json", ".mcmeta"}:
        continue
    try:
        json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        violations.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
lang = ROOT / "src/main/resources/assets/pickclimber/lang"
canonical = set(json.loads((lang / "en_us.json").read_text(encoding="utf-8")))
for path in sorted(lang.glob("*.json")):
    keys = set(json.loads(path.read_text(encoding="utf-8")))
    if keys != canonical:
        violations.append(f"{path.relative_to(ROOT)}: localization keys differ from en_us")
for stale in (
    "ClimbingRuleBookDuplicateScreen.java", "DuplicateRuleBookPayload.java", "EjectRuleBookPayload.java",
    "ToolWearState.java", "ToolWearMath.java", "climbing_rule_book_backing.png",
):
    if list((ROOT / "src").rglob(stale)):
        violations.append(f"stale artifact still present: {stale}")
version = next((line.split("=", 1)[1] for line in (ROOT / "gradle.properties").read_text().splitlines()
                if line.startswith("mod_version=")), "")
for rel in ("README.md", "docs/DEVELOPMENT.md", "docs/BUILD-STATUS.md", "docs/WAITLIST.md"):
    text = (ROOT / rel).read_text(encoding="utf-8")
    if version not in text:
        violations.append(f"{rel}: does not mention current version {version}")
print(f"Pick Climber audit: {len(java_files)} Java files, version {version}")
if violations:
    print("FAIL")
    for violation in violations:
        print(" -", violation)
    sys.exit(1)
print("PASS")
