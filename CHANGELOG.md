# alexandra-job-tracker changelog

Changelog of alexandra-job-tracker.

## Unreleased
### No issue

**fix(adapter): dispatch transactional events without an active transaction**


[23b74ed42ace0dc](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/23b74ed42ace0dc) juan.pablo.jimenez.esclusa *2026-09-19 19:32:14*

**refactor(application): drop the unused matches operation from the password encoder port**


[2372ad2a83e1e8e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/2372ad2a83e1e8e) juan.pablo.jimenez.esclusa *2026-09-19 19:13:38*

**refactor(application): relocate technical outbound ports and event publisher**


[0009a6b4f3f2235](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/0009a6b4f3f2235) juan.pablo.jimenez.esclusa *2026-09-19 19:11:27*

**refactor(application): move JobPostingService into application.service**


[a692dd34b8773fd](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/a692dd34b8773fd) juan.pablo.jimenez.esclusa *2026-09-19 19:04:27*

**refactor(adapter): move the cache contract into adapter-cache**


[45f8f97042a803e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/45f8f97042a803e) juan.pablo.jimenez.esclusa *2026-09-19 18:53:17*

**refactor(coverage): tighten architecture contract for relocated outbound ports**


[e79d0b87455c1fb](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/e79d0b87455c1fb) juan.pablo.jimenez.esclusa *2026-09-19 18:50:36*

**refactor(adapter): migrate authentication from jjwt to spring security (#79)**

 * refactor(adapter): migrate authentication from jjwt to spring security
 * Replace hand-rolled JWT (jjwt) + bcrypt (jbcrypt) with Spring Security&#x27;s
 * filter chain, method security, Nimbus JWT, and BCryptPasswordEncoder.
 * feat(adapter): reduce token to 10 min, require auth on me, add mdc ids, use forbidden
 * - Reduce JWT expiration from 30 min to 10 min
 * - Require authentication on  query (returns FORBIDDEN instead of null)
 * - Add userId and traceId to MDC logging context
 * - Change unauthenticated error from BAD_REQUEST to FORBIDDEN (Authz, resolvers)
 * - Update specs: identity, graphql-api, observability, architecture

[74495d155dccc5d](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/74495d155dccc5d) Juan Pablo Jimenez Esclusa *2026-09-18 13:50:45*

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

[d954dcb23e29f85](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/d954dcb23e29f85) juan.pablo.jimenez.esclusa *2026-09-17 00:48:27*

**feat(architecture): scaffold multi module maven project**


[e3461a1c0bba75e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/e3461a1c0bba75e) juan.pablo.jimenez.esclusa *2026-08-08 14:35:53*


