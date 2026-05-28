# alexandra-job-tracker changelog

Changelog of alexandra-job-tracker.

## Unreleased
### No issue

**feat(domain): implement job tracker app**

 * feat(ai): improve capability adding skills (#62)
 * feat(ai): improve capability adding skills
 * refactor(architecture): add submit job posting events
 * refactor(architecture): break adapter dependency between api and auth
 * feat(adapter): add authz
 * refactor(domain): normalize jobposting and job application model
 * docs(architecture): update aws diagram
 * refactor(testdata): fix smells test codes
 * fix(adapter): remove source in application list command
 * fix(adapter): scope application and analysis operations to the caller
 * fix(adapter): require jwt secret, add invalid token exception
 * fix(adapter): harden jwt token lifetime, default exposure, and login errors
 * fix(adapter): return conflict on duplicate job posting url
 * fix(adapter): reject stale application updates with conflict
 * fix(adapter): upsert job analysis atomically on re-analysis
 * refactor(application): inject analytics calculator into analytics use case
 * fix(adapter): add http timeouts to graphql client
 * refactor(domain): rename port packages in/out to inbound/outbound
 * fix(ci): homologate terraform sqs events and kms with cloudformation
 * refactor(adapter): clear spec debt — remove suppress warning, decouple cache test
 * test(adapter): replace thread.sleep with awaitility in client timeout test
 * chore(config): externalize llm and jwt config via environment variables
 * fix(config): allow chrome extension origin in cors allowlist
 * docs(docs): humanize project readmes
 * chore(config): require type scope and subject in commits
 * docs(docs): improve bruno api collection
 * chore(bootstrap): improve log traces
 * refactor(architecture): remove sqs polling
 * refactor(adapter): extract event handling into adapter-events module
 * feat(adapter): apply logical delete to applications and analyses
 * refactor(adapter): relocate native hints to owning adapters and reorganize packages
 * feat(docker): run dev on postgres and add adminer to compose
 * fix(config): capture indeed jobs on all hosts and query-string urls

[b2023b954ecaf8c](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/b2023b954ecaf8c) juan.pablo.jimenez.esclusa *2026-09-17 00:39:27*

**feat(architecture): scaffold multi module maven project**


[e3461a1c0bba75e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/e3461a1c0bba75e) juan.pablo.jimenez.esclusa *2026-08-08 14:35:53*


