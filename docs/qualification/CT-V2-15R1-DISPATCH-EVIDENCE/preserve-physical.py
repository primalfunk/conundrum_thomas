"""Preserve completed raw device captures as reviewable text with byte custody.

Run from the qualification workspace. APKs stay in the ignored raw capture;
their identities are already included in fixture-artifacts.json.
"""
import hashlib
import json
from pathlib import Path

root = Path.cwd()
destination = root / "docs/qualification/CT-V2-15R1-DISPATCH-EVIDENCE"
raw = root / "out/ct-v2-15r1-dispatch"


def sha(data):
    return hashlib.sha256(data).hexdigest()


def decode(data):
    if data.startswith((b"\xff\xfe", b"\xfe\xff")):
        return data.decode("utf-16")
    return data.decode("utf-8-sig")


for run in sorted(raw.glob("run-*")):
    if not (run / "final-disposition.txt").exists():
        continue  # Never snapshot a running capture as completed evidence.
    output = destination / run.name
    output.mkdir(exist_ok=True)
    inventory = []
    for source in sorted(run.iterdir()):
        if not source.is_file():
            continue
        data = source.read_bytes()
        entry = {"file": source.name, "rawBytes": len(data), "rawSHA256": sha(data)}
        if source.suffix.lower() != ".apk":
            normalized = ("\n".join(line.rstrip() for line in decode(data).splitlines()).rstrip() + "\n").encode("utf-8")
            (output / source.name).write_bytes(normalized)
            entry.update(evidenceBytes=len(normalized), evidenceSHA256=sha(normalized))
        else:
            entry["retention"] = "APK retained in ignored raw qualification capture"
        inventory.append(entry)
    (output / "capture-manifest.json").write_text(json.dumps(inventory, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(run.name, len(inventory), "raw identities preserved; text normalized to UTF-8/LF")
