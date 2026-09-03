# Provenance schema

Migration `0001_governed_source_provenance.sql` defines:

- distinct authority domains, including a structurally separate engineering-governance domain;
- canonical documents and multiple immutable versions;
- many-to-many source classes with one primary class;
- official landing, artifact, web, rights, and surveillance locators;
- immutable acquired-artifact SHA-256 and byte-size identity;
- precise section/recommendation/tool/annex addresses without source excerpts;
- per-version rights, applicability, freshness, and review requirements;
- directed version relationships, open conflicts, and scope differences;
- constrained review states and allowed transitions;
- unopened candidate subjects that explicitly confer no rule authority.

Migration `0002_source_discovery_needs.sql` adds governed source gaps. These rows explicitly carry neither source authority nor rule authority.

Generated databases record the SHA-256 of every applied migration and seed. They are disposable build products, not the editable source of truth.
