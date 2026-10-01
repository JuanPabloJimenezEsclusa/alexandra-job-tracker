# alexandra-job-tracker changelog

Changelog of alexandra-job-tracker.

## Unreleased
### No issue

**fix(testdata): run k6 suites sequentially and report honest results**

 * Run each k6 script sequentially, continue after per-suite failures, and aggregate a single non-zero exit. Add a K6_TEST_RESULT marker plus named check, threshold, and metric summaries to stdout and the HTML reports, and forward completed k6 container logs from run-pentest.sh.
 * Guard missing posting ids, make per-run fixtures unique, and query the schema-valid id/jobPostingId/status fields in load, spike, and soak. Extend the harness self-test to cover ordering, overlap, and aggregate-failure semantics.
 * adapter: classify BindException as BAD_REQUEST/VALIDATION and log non-unexpected errors at warn without a stack trace to stop the error-level log flood.

[172de5bf066c557](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/172de5bf066c557) juan.pablo.jimenez.esclusa *2026-09-30 16:51:36*

**docs(testdata): remove sequential k6 design spec**


[72768c558a938e8](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/72768c558a938e8) juan.pablo.jimenez.esclusa *2026-09-30 12:48:58*

**docs(testdata): design sequential k6 test reporting**


[a93a21234075823](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/a93a21234075823) juan.pablo.jimenez.esclusa *2026-09-30 10:40:06*

**fix(testdata): tear down on every error path and stop reporting false greens**

 * The harness reported success while k6 checks failed, and any error before the
 * teardown call left the compose stack running.
 * - run-pentest.sh: bind teardown to EXIT/INT/TERM, guarded so it runs exactly
 * once and so INT/TERM terminate instead of resuming. Aggregate container exit
 * codes and report validation into a non-zero exit.
 * - run-pentest.sh: install the teardown traps only after the profile is
 * accepted. &#x60;docker compose down&#x60; does not filter by profile, so a usage error
 * must not run &#x60;down --volumes&#x60; against the shared compose project used by
 * deploy/compose/start.sh.
 * - run-pentest.sh: __cleanReports passed a quoted glob to rm, which never
 * expands it, so stale reports from another profile survived and were validated
 * as PASS. Expand the glob and report how many files were removed.
 * - run-pentest.sh: __validateReports returned the failure count as an exit
 * status and errexit killed the script before the FAILED message could print.
 * Return a real 0/1 verdict and keep the message reachable.
 * - k6-run.sh: drop &#x60;|| true&#x60; and aggregate every job&#x27;s status, so a k6 threshold
 * breach reaches the container exit code instead of being discarded.
 * - reporter.js: label the passes/fails rows by their own value; the fails row
 * printed FAIL even at 0. State plainly that k6 does not expose per-check names
 * to handleSummary rather than pointing at output that has none either.
 * - harness-selftest.sh: new plain-bash self-test with a stubbed docker,
 * asserting teardown on every failure path including a usage error.
 * The known failing checks (introspection enabled, a valid aliases query expected
 * to be rejected, no 429 under burst load) are untouched: the harness now reports
 * them honestly instead of as PASS.

[85428580aa78d0b](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/85428580aa78d0b) juan.pablo.jimenez.esclusa *2026-09-28 14:07:21*

**refactor(ci): drop vacuous classification field**


[990f6f3197e36fd](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/990f6f3197e36fd) juan.pablo.jimenez.esclusa *2026-09-28 12:39:35*

**build(deps-dev): bump org.instancio:instancio-core (#98)**

 * Bumps the maven-dependencies group with 1 update: [org.instancio:instancio-core](https://github.com/instancio/instancio).
 * Updates &#x60;org.instancio:instancio-core&#x60; from 6.0.0 to 6.0.1
 * - [Release notes](https://github.com/instancio/instancio/releases)
 * - [Commits](https://github.com/instancio/instancio/compare/instancio-parent-6.0.0...instancio-parent-6.0.1)
 * ---
 * updated-dependencies:
 * - dependency-name: org.instancio:instancio-core
 * dependency-version: 6.0.1
 * dependency-type: direct:development
 * update-type: version-update:semver-patch
 * dependency-group: maven-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[44911badcad98bf](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/44911badcad98bf) dependabot[bot] *2026-09-28 03:31:08*

**build(deps): bump the infrastructure-dependencies group (#97)**

 * Bumps the infrastructure-dependencies group in /deploy/compose with 3 updates: postgres, grafana/loki and grafana/k6.
 * Updates &#x60;postgres&#x60; from 17-alpine to 18-alpine
 * Updates &#x60;grafana/loki&#x60; from 3.7.7 to 3.7.8
 * Updates &#x60;grafana/k6&#x60; from 2.2.0 to 2.3.0
 * ---
 * updated-dependencies:
 * - dependency-name: postgres
 * dependency-version: 18-alpine
 * dependency-type: direct:production
 * dependency-group: infrastructure-dependencies
 * - dependency-name: grafana/loki
 * dependency-version: 3.7.8
 * dependency-type: direct:production
 * update-type: version-update:semver-patch
 * dependency-group: infrastructure-dependencies
 * - dependency-name: grafana/k6
 * dependency-version: 2.3.0
 * dependency-type: direct:production
 * update-type: version-update:semver-minor
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[9158dc2fc995e93](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/9158dc2fc995e93) dependabot[bot] *2026-09-27 17:51:26*

**build(deps): update hashicorp/aws requirement from ~> 6.65.0 to ~> 6.66.0 (#96)**

 * Updates the requirements on [hashicorp/aws](https://github.com/hashicorp/terraform-provider-aws) to permit the latest version.
 * Updates &#x60;hashicorp/aws&#x60; to 6.66.0
 * - [Release notes](https://github.com/hashicorp/terraform-provider-aws/releases)
 * - [Changelog](https://github.com/hashicorp/terraform-provider-aws/blob/main/CHANGELOG.md)
 * - [Commits](https://github.com/hashicorp/terraform-provider-aws/compare/v6.65.0...v6.66.0)
 * ---
 * updated-dependencies:
 * - dependency-name: hashicorp/aws
 * dependency-version: 6.66.0
 * dependency-type: direct:production
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[74844e49e7002ae](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/74844e49e7002ae) dependabot[bot] *2026-09-27 03:26:37*

**refactor(adapter): make core error taxonomy declarative and move forbidden to api (#95)**

 * Declare the stable error code and classification on the core error vocabulary so GraphQlExceptionResolver maps code, classification and ErrorType from the declaration. The ErrorType mapping is an exhaustive switch over ErrorCode, so a new core error type forces an adapter edit instead of silently degrading to INTERNAL_ERROR.
 * Move ForbiddenException out of the domain into adapter-api, the only module that raises and maps it, keeping the FORBIDDEN code and DOMAIN classification so responses are unchanged.
 * Add CoreErrorTaxonomyTest, which discovers every core error type and asserts it declares and resolves to its code, and CoreErrorVocabularyPlacementTest, which rejects an error type referenced by a single adapter in the core vocabulary.

[a9dfefbb5ced813](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/a9dfefbb5ced813) Juan Pablo Jimenez Esclusa *2026-09-22 12:08:54*

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


