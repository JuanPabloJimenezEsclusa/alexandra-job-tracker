# alexandra-job-tracker changelog

Changelog of alexandra-job-tracker.

## Unreleased
### No issue

**ci: add dependabot automerge and queue CI workflows**

 * Add dependabot-automerge.yml workflow that auto-approves and auto-merges
 * Dependabot PRs for minor/patch updates when CI passes green. Major updates
 * remain open for manual review.
 * Change all CI workflows concurrency to queue (cancel-in-progress: false)
 * so runs execute one at a time per group instead of cancelling previous runs.

[cef83f06aa32b20](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/cef83f06aa32b20) juan.pablo.jimenez.esclusa *2026-09-20 18:37:52*

**chore(docs): add codegraph in ignore files, update changelog**


[fc0e69f7c0581dd](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/fc0e69f7c0581dd) juan.pablo.jimenez.esclusa *2026-09-20 18:36:04*

**refactor(architecture): relocate outbound ports out of the domain (#83)**

 * refactor(coverage): tighten architecture contract for relocated outbound ports
 * refactor(adapter): move the cache contract into adapter-cache
 * refactor(application): move JobPostingService into application.service
 * refactor(application): relocate technical outbound ports and event publisher
 * refactor(application): drop the unused matches operation from the password encoder port
 * fix(bootstrap): establish the registration transaction in the composition root
 * fix(adapter): dispatch transactional events without an active transaction
 * chore(docs): add codegraph in ignore files, update changelog
 * refactor(bootstrap): move submit transaction wrapping into a named decorator
 * fix(bootstrap): make the application-status write path atomic
 * fix(bootstrap): wrap analysis deletion in a composition-root transaction
 * fix(bootstrap): wrap tracking creation and leave external calls unwrapped
 * refactor(adapter): remove the unused password encoder matches method

[41dee2949b02d60](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/41dee2949b02d60) Juan Pablo Jimenez Esclusa *2026-09-20 18:27:16*

**build(deps): bump the infrastructure-dependencies group (#81)**

 * Bumps the infrastructure-dependencies group in /deploy/compose with 3 updates: adminer, otel/opentelemetry-collector-contrib and grafana/grafana.
 * Updates &#x60;adminer&#x60; from 5.3.0 to 5.5.0
 * Updates &#x60;otel/opentelemetry-collector-contrib&#x60; from 0.160.0 to 0.161.0
 * Updates &#x60;grafana/grafana&#x60; from 13.2.1 to 13.2.2
 * ---
 * updated-dependencies:
 * - dependency-name: adminer
 * dependency-version: 5.5.0
 * dependency-type: direct:production
 * update-type: version-update:semver-minor
 * dependency-group: infrastructure-dependencies
 * - dependency-name: otel/opentelemetry-collector-contrib
 * dependency-version: 0.161.0
 * dependency-type: direct:production
 * update-type: version-update:semver-minor
 * dependency-group: infrastructure-dependencies
 * - dependency-name: grafana/grafana
 * dependency-version: 13.2.2
 * dependency-type: direct:production
 * update-type: version-update:semver-patch
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[abc24a5ea7e6ec4](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/abc24a5ea7e6ec4) dependabot[bot] *2026-09-20 14:51:55*

**build(deps): update hashicorp/aws requirement from ~> 6.64.0 to ~> 6.65.0 (#80)**

 * Updates the requirements on [hashicorp/aws](https://github.com/hashicorp/terraform-provider-aws) to permit the latest version.
 * Updates &#x60;hashicorp/aws&#x60; to 6.65.0
 * - [Release notes](https://github.com/hashicorp/terraform-provider-aws/releases)
 * - [Changelog](https://github.com/hashicorp/terraform-provider-aws/blob/main/CHANGELOG.md)
 * - [Commits](https://github.com/hashicorp/terraform-provider-aws/compare/v6.64.0...v6.65.0)
 * ---
 * updated-dependencies:
 * - dependency-name: hashicorp/aws
 * dependency-version: 6.65.0
 * dependency-type: direct:production
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[d30c0f4dcfc2be3](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/d30c0f4dcfc2be3) dependabot[bot] *2026-09-20 14:50:56*

**build(deps): bump docker/build-push-action (#82)**

 * Bumps the github-actions-dependencies group with 1 update: [docker/build-push-action](https://github.com/docker/build-push-action).
 * Updates &#x60;docker/build-push-action&#x60; from 7.3.0 to 7.4.0
 * - [Release notes](https://github.com/docker/build-push-action/releases)
 * - [Commits](https://github.com/docker/build-push-action/compare/53b7df96c91f9c12dcc8a07bcb9ccacbed38856a...c3c9e263c25d99ce0380d002d59b67737d91b0dc)
 * ---
 * updated-dependencies:
 * - dependency-name: docker/build-push-action
 * dependency-version: 7.4.0
 * dependency-type: direct:production
 * update-type: version-update:semver-minor
 * dependency-group: github-actions-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[7e1395dcb06ad04](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/7e1395dcb06ad04) dependabot[bot] *2026-09-20 14:50:20*


## tracker
### No issue

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


