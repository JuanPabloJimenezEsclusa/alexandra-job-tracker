# alexandra-job-tracker changelog

Changelog of alexandra-job-tracker.

## Unreleased
### No issue

**chore(config): externalize llm and jwt config via environment variables**


[b31f5bad1865b37](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/b31f5bad1865b37) juan.pablo.jimenez.esclusa *2026-09-05 15:16:05*

**test(adapter): replace thread.sleep with awaitility in client timeout test**


[a9c3cf7dc5adf8e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/a9c3cf7dc5adf8e) juan.pablo.jimenez.esclusa *2026-09-05 11:18:11*

**refactor(adapter): clear spec debt — remove suppress warning, decouple cache test**


[8f7f688565f62a4](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/8f7f688565f62a4) juan.pablo.jimenez.esclusa *2026-09-05 11:18:02*

**fix(ci): homologate terraform sqs events and kms with cloudformation**


[30286e5cb19a7ac](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/30286e5cb19a7ac) juan.pablo.jimenez.esclusa *2026-09-05 11:18:02*

**refactor(domain): rename port packages in/out to inbound/outbound**


[f05368c4d5a3b36](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/f05368c4d5a3b36) juan.pablo.jimenez.esclusa *2026-09-05 11:18:01*

**fix(adapter): add http timeouts to graphql client**


[99613a9e8b134b4](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/99613a9e8b134b4) juan.pablo.jimenez.esclusa *2026-09-05 11:18:01*

**refactor(application): inject analytics calculator into analytics use case**


[0e4e1b7bb213fe1](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/0e4e1b7bb213fe1) juan.pablo.jimenez.esclusa *2026-09-05 11:17:52*

**fix(adapter): upsert job analysis atomically on re-analysis**


[5c5cf5d492340cf](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/5c5cf5d492340cf) juan.pablo.jimenez.esclusa *2026-09-05 11:05:35*

**fix(adapter): reject stale application updates with conflict**


[7a69a479655511c](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/7a69a479655511c) juan.pablo.jimenez.esclusa *2026-09-05 11:05:25*

**fix(adapter): return conflict on duplicate job posting url**


[730893c8faf2e3f](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/730893c8faf2e3f) juan.pablo.jimenez.esclusa *2026-09-05 11:05:17*

**fix(adapter): harden jwt token lifetime, default exposure, and login errors**


[ce1de9724847150](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/ce1de9724847150) juan.pablo.jimenez.esclusa *2026-09-05 10:57:09*

**fix(adapter): require jwt secret, add invalid token exception**


[c6ec9839ca2fa34](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/c6ec9839ca2fa34) juan.pablo.jimenez.esclusa *2026-09-03 14:03:18*

**fix(adapter): scope application and analysis operations to the caller**


[f642b81de6edf68](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/f642b81de6edf68) juan.pablo.jimenez.esclusa *2026-09-03 11:24:54*

**fix(adapter): remove source in application list command**


[ca70fd3ef1e4e38](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/ca70fd3ef1e4e38) juan.pablo.jimenez.esclusa *2026-08-31 15:06:59*

**refactor(testdata): fix smells test codes**


[038aebd4c963549](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/038aebd4c963549) juan.pablo.jimenez.esclusa *2026-08-31 00:26:37*

**docs(architecture): update aws diagram**

 * Signed-off-by: juan.pablo.jimenez.esclusa &lt;juan.pablo.jimenez.esclusa@gmail.com&gt;

[d665ff19ad6e592](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/d665ff19ad6e592) juan.pablo.jimenez.esclusa *2026-08-30 23:08:56*

**refactor(domain): normalize jobposting and job application model**


[e3996c46b93590a](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/e3996c46b93590a) juan.pablo.jimenez.esclusa *2026-08-30 20:05:48*

**feat(adapter): add authz**


[9af2e1bfd1a565e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/9af2e1bfd1a565e) juan.pablo.jimenez.esclusa *2026-08-30 16:10:28*

**refactor(architecture): break adapter dependency between api and auth**


[b8406d15bc4e44d](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/b8406d15bc4e44d) juan.pablo.jimenez.esclusa *2026-08-30 15:03:40*

**fix(ci): set zap sservice user with host id (#67)**


[89625947b4ce600](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/89625947b4ce600) Juan Pablo Jimenez Esclusa *2026-08-30 13:32:39*

**build(deps): update hashicorp/aws requirement from ~> 6.61.0 to ~> 6.62.0 (#68)**

 * Updates the requirements on [hashicorp/aws](https://github.com/hashicorp/terraform-provider-aws) to permit the latest version.
 * Updates &#x60;hashicorp/aws&#x60; to 6.62.0
 * - [Release notes](https://github.com/hashicorp/terraform-provider-aws/releases)
 * - [Changelog](https://github.com/hashicorp/terraform-provider-aws/blob/main/CHANGELOG.md)
 * - [Commits](https://github.com/hashicorp/terraform-provider-aws/compare/v6.61.0...v6.62.0)
 * ---
 * updated-dependencies:
 * - dependency-name: hashicorp/aws
 * dependency-version: 6.62.0
 * dependency-type: direct:production
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[4bbed2cd8942769](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/4bbed2cd8942769) dependabot[bot] *2026-08-30 13:13:12*

**build(deps): bump debian (#69)**

 * Bumps the infrastructure-dependencies group in /deploy/compose with 1 update: debian.
 * Updates &#x60;debian&#x60; from 12-slim to 13-slim
 * ---
 * updated-dependencies:
 * - dependency-name: debian
 * dependency-version: 13-slim
 * dependency-type: direct:production
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[44b7ecd62ea4c68](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/44b7ecd62ea4c68) dependabot[bot] *2026-08-30 13:12:29*

**build(deps): bump actions/setup-java (#71)**

 * Bumps the github-actions-dependencies group with 1 update: [actions/setup-java](https://github.com/actions/setup-java).
 * Updates &#x60;actions/setup-java&#x60; from 5.7.0 to 6.0.0
 * - [Release notes](https://github.com/actions/setup-java/releases)
 * - [Commits](https://github.com/actions/setup-java/compare/v5.7.0...v6.0.0)
 * ---
 * updated-dependencies:
 * - dependency-name: actions/setup-java
 * dependency-version: 6.0.0
 * dependency-type: direct:production
 * update-type: version-update:semver-major
 * dependency-group: github-actions-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[41e4e5306b058ce](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/41e4e5306b058ce) dependabot[bot] *2026-08-30 13:11:43*

**build(deps): bump grafana/loki (#70)**

 * Bumps the infrastructure-dependencies group in /deploy/compose with 1 update: grafana/loki.
 * Updates &#x60;grafana/loki&#x60; from 3.7.6 to 3.7.7
 * ---
 * updated-dependencies:
 * - dependency-name: grafana/loki
 * dependency-version: 3.7.7
 * dependency-type: direct:production
 * update-type: version-update:semver-patch
 * dependency-group: infrastructure-dependencies
 * ...
 * Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
 * Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt;

[613b466c7bcc11a](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/613b466c7bcc11a) dependabot[bot] *2026-08-30 13:10:56*

**feat(domain): implement job tracker app**

 * feat(ai): improve capability adding skills (#62)
 * feat(ai): improve capability adding skills
 * refactor(architecture): add submit job posting events

[58c7ffaeed1085e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/58c7ffaeed1085e) juan.pablo.jimenez.esclusa *2026-08-29 00:42:05*

**feat(architecture): scaffold multi module maven project**


[e3461a1c0bba75e](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker/commit/e3461a1c0bba75e) juan.pablo.jimenez.esclusa *2026-08-08 14:35:53*


