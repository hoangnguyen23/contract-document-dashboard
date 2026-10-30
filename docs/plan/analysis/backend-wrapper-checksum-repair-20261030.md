# TC02 developer evidence — 30 October 2026

Fetched exact official artifact `https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.10.0/apache-maven-3.10.0-bin.zip` and separately published same URL + `.sha512`. Download retained at ignored `D:/contract-document-dashboard/var/toolchains/apache-maven-3.10.0-bin.zip`.

Published and actual SHA512 both `22d31676d5b92ed53308c19ecded3245d0efd0cddfec51ec7bab62f4f13ad70ddddceff8e62940e0e8e2421f3cde8106737f4becec0aa034ffeff2a754e88337`. After comparison succeeded, derived SHA256 `1f6d9909266510f039f59aa0e13dcd2c66da85f043e56276e41f2918f8bddaff` and added it as distributionSha256Sum in existing wrapper properties. Maven version/distribution URL/scripts unchanged. No unauthenticated locally invented checksum was treated as upstream proof.

No Maven execution, fresh bootstrap, tests, HTTP business request, runtime restart, migration or commit/push performed. Artifact HTTP retrieval is toolchain provisioning, not an application smoke scenario. Independent source/config review pending. Fresh-bootstrap behavior remains separate acceptance; existing cached Maven does not prove it.
