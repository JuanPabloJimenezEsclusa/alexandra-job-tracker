# alexandra-job-tracker changelog

Changelog of alexandra-job-tracker.

## Unreleased
### No issue

**refactor(adapter): make core error taxonomy declarative and move forbidden to api**

 * Declare the stable error code and classification on the core error vocabulary so GraphQlExceptionResolver maps code, classification and ErrorType from the declaration. The ErrorType mapping is an exhaustive switch over ErrorCode, so a new core error type forces an adapter edit instead of silently degrading to INTERNAL_ERROR.
 * Move ForbiddenException out of the domain into adapter-api, the only module that raises and maps it, keeping the FORBIDDEN code and DOMAIN classification so responses are unchanged.
 * Add CoreErrorTaxonomyTest, which discovers every core error type and asserts it declares and resolves to its code, and CoreErrorVocabularyPlacementTest, which rejects an error type referenced by a single adapter in the core vocabulary.

[4fb783beb242a12](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/4fb783beb242a12) juan.pablo.jimenez.esclusa *2026-09-22 11:22:07*

**chore(adapter): rename adapter-cli module to cli-client (#94)**

 * Rename the adapter-cli Maven module to cli-client, a name that states the
 * module&#x27;s role (a CLI client of the server) instead of claiming an adapter
 * identity it does not implement.
 * Update every tracked referrer: the root pom modules list, the bootstrap-cli
 * and coverage-jacoco dependencies, the ArchUnit per-module whitelist and
 * isolation rules, the deploy Dockerfile, and the README module table,
 * dependency graph, and prose. Remove the stale infrastructure-observability
 * and adapter-observability IDE module registrations from .idea so the
 * reactor, module directories, and IDE registrations agree.
 * Verified with ./mvnw clean verify (all 14 reactor modules SUCCESS).

[d1e04d9800c6904](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/d1e04d9800c6904) Juan Pablo Jimenez Esclusa *2026-09-22 11:14:12*

**docs(architecture): state the read/write model policy over a single model (#93)**


[261c27d7bf1c6e9](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/261c27d7bf1c6e9) Juan Pablo Jimenez Esclusa *2026-09-22 10:47:32*

**fix(ci): report the real automerge outcome in the summary comment (#92)**


[a972b0461d7459e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/a972b0461d7459e) Juan Pablo Jimenez Esclusa *2026-09-21 22:46:58*

**fix(bootstrap): source mdc traceid from the otel span instead of a random uuid (#91)**

 * TracingFilter now publishes the current span&#x27;s trace id into the MDC before invoking the chain and removes it in finally, so a reused servlet thread cannot leak a stale value.
 * MdcLoggingInterceptor no fabricates a traceId, preserving value sourced from OpenTelemetry context.

[80214a98dd1b8ff](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/80214a98dd1b8ff) Juan Pablo Jimenez Esclusa *2026-09-21 19:39:11*

**build(deps): bump the maven-dependencies group with 2 updates (#90)**

 * Bumps the maven-dependencies group with 2 updates: [se.bjurr.gitchangelog:git-changelog-maven-plugin](https://github.com/tomasbjerre/git-changelog-maven-plugin) and [org.codehaus.mojo:exec-maven-plugin](https://github.com/mojohaus/exec-maven-plugin).
 * Updates &#x60;se.bjurr.gitchangelog:git-changelog-maven-plugin&#x60; from 2.2.11 to 2.4.0
 * - [Release notes](https://github.com/tomasbjerre/git-changelog-maven-plugin/releases)
 * - [Changelog](https://github.com/tomasbjerre/git-changelog-maven-plugin/blob/master/CHANGELOG.md)
 * - [Commits](https://github.com/tomasbjerre/git-changelog-maven-plugin/compare/2.2.11...2.4.0)
 * Updates &#x60;org.codehaus.mojo:exec-maven-plugin&#x60; from 3.6.3 to 3.6.4
 * - [Release notes](https://github.com/mojohaus/exec-maven-plugin/releases)
 * - [Commits](https://github.com/mojohaus/exec-maven-plugin/compare/3.6.3...3.6.4)
 * ---
 * updated-dependencies:
 * - dependency-name: se.bjurr.gitchangelog:git-changelog-maven-plugin
 * dependency-version: 2.4.0
 * dependency-type: direct:production
 * update-type: version-update:semver-minor
 * dependency-group: maven-dependencies
 * - dependency-name: org.codehaus.mojo:exec-maven-plugin
 * dependency-version: 3.6.4
 * dependency-type: direct:production
 * update-type: version-update:semver-patch
 * dependency-group: maven-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[89927544797137d](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/89927544797137d) dependabot[bot] *2026-09-21 09:49:20*

**chore(ci): add dependabot automerge and queue ci workflows (#89)**

 * Add dependabot-automerge.yml workflow that auto-approves and auto-merges
 * Dependabot PRs for minor/patch updates when CI passes green. Major updates
 * remain open for manual review.
 * Change all CI workflows concurrency to queue (cancel-in-progress: false)
 * so runs execute one at a time per group instead of cancelling previous runs.

[353bc6fcb2a5849](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/353bc6fcb2a5849) Juan Pablo Jimenez Esclusa *2026-09-20 22:21:36*

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


