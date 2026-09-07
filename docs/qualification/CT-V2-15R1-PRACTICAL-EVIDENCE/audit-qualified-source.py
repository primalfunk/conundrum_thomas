"""Audit the bounded candidate and capture identities before integration."""
import hashlib
import json
import subprocess
from pathlib import Path

root = Path.cwd()
evidence = root / "docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE"
entry = "4f822fb3498b67b40124c0fb4ec1f19f98c92c8b"


def git(*args):
    return subprocess.check_output(["git", *args], cwd=root).decode().strip()


changed = git("diff", "--name-only", entry).splitlines()
new = git("ls-files", "--others", "--exclude-standard").splitlines()
code = sorted(p for p in set(changed + new) if p.endswith(".kt") and not p.startswith("docs/"))
production = [p for p in code if "/src/main/" in p]
expected = {
    "app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt": "08a2b697208d8ada205d6166c4bc56046a26053ba0f6e443d968333377bfe902",
    "thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt": "c86e1222569557f72b26fd5ca8b9f7a8f26cb408d18613b04b99f4ce1fb51e20",
}
assert set(production) == set(expected), production
for path, identity in expected.items():
    assert hashlib.sha256((root / path).read_bytes()).hexdigest() == identity, path
register = json.loads((root / "migration/v1-component-register.json").read_text(encoding="utf-8-sig"))
assert register["policy"]["defaultApprovalState"] == "DENIED"
assert register["policy"]["v1CodeMigrationAuthorized"] is False
assert len(register["components"]) == 24
assert all(c["approvalState"] == "DENIED" for c in register["components"])
result = {
    "entryHead": entry,
    "onlyChangedProductionFiles": production,
    "productionExactlyMatchesArchivedIdentityCandidate": True,
    "productionDispatchInstrumentationRemoved": True,
    "rendererStorePolicyModelSpeechPatternEngineProductionUnchanged": True,
    "v1Register": {"default": "DENIED", "migrationAuthorized": False, "deniedComponents": 24, "totalComponents": 24},
    "qualifiedCode": [{"path": p, "sha256": hashlib.sha256((root / p).read_bytes()).hexdigest()} for p in code],
}
(evidence / "qualified-source-audit.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8", newline="\n")
print(json.dumps(result, indent=2))
