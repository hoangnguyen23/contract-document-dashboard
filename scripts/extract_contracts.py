#!/usr/bin/env python3
"""Extract the canonical B blocks; never manufacture a second contract."""
import hashlib
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/FE_BE_Shared_Development_Contract.md"


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def canonical(value):
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8")


def walk(value):
    if isinstance(value, dict):
        if "$ref" in value and not value["$ref"].startswith("#/"):
            raise ValueError(f"External $ref forbidden: {value['$ref']}")
        for item in value.values():
            walk(item)
    elif isinstance(value, list):
        for item in value:
            walk(item)


def main():
    blocks = {}
    expression = r"<!-- contract:block ([a-z0-9-]+) -->\s*```json\s*\n(.*?)\n```"
    specification = ROOT / "docs/Final_Project_Specification.md"
    texts = SOURCE.read_text(encoding="utf-8") + "\n" + specification.read_text(encoding="utf-8")
    for name, contents in re.findall(expression, texts, re.DOTALL):
        if name in blocks:
            raise ValueError(f"Duplicate block: {name}")
        blocks[name] = json.loads(contents, object_pairs_hook=unique_object)
    manifest = blocks["contract-manifest-v1"]
    if hashlib.sha256(specification.read_bytes()).hexdigest() != manifest["specificationFileSha256"]:
        raise ValueError("Specification A does not match the frozen B manifest")
    if set(blocks) != set(manifest["blocks"]) | {"contract-manifest-v1"}:
        raise ValueError("Canonical block inventory differs from manifest")
    target = ROOT / "contracts"
    resources = ROOT / "apps/api/src/main/resources/contracts"
    target.mkdir(exist_ok=True)
    resources.mkdir(parents=True, exist_ok=True)
    for name, value in blocks.items():
        content = canonical(value)
        if name in manifest["blocks"] and hashlib.sha256(content).hexdigest() != manifest["blocks"][name]:
            raise ValueError(f"Canonical hash mismatch: {name}")
        if name == "openapi-v1":
            walk(value)
        (target / f"{name}.json").write_bytes(content)
        (resources / f"{name}.json").write_bytes(content)
    print(f"Extracted {len(blocks)} frozen blocks; A/B version {manifest['documentVersions']}")


if __name__ == "__main__":
    main()
