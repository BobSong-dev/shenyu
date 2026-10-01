# Storage e2e upstream

The Compose runners start a private HTTPBin service on the `shenyu` Docker
network and pass `-Dshenyu.e2e.storage.upstream=shenyu-httpbin:80` to Maven.
Both selector handles and discovery bindings use that address. The existing
eight Divide URI/method scenarios, assertions, readiness checks and timeouts
are unchanged. No host port is exposed for HTTPBin.

The image is pinned by digest from the HTTPBin project's documented
`kennethreitz/httpbin` image (Linux amd64). Its `/anything` healthcheck must
pass before the gateway starts. Compose `up --wait` fails if the service
cannot start; there is no fallback to the public endpoint. Each runner dumps
HTTPBin logs on failure and removes the service in its existing cleanup.

For example, from the repository root:

```sh
bash shenyu-e2e/shenyu-e2e-case/shenyu-e2e-case-storage/compose/script/e2e-mysql-compose.sh
```

The H2, PostgreSQL, OpenGauss and all-storage Compose runners use the same
private service. Existing Kubernetes runners retain their `httpbin.org`
default because these Compose services are not available in Kubernetes.
A different deployment can set `shenyu.e2e.storage.upstream` to a reachable
`host:port` (without a scheme); discovery uses HTTP.

The offline provider regression test checks all eight scenarios for matching
selector and discovery upstreams and preserves the Kubernetes default:

```sh
./mvnw -B -f shenyu-e2e/pom.xml -pl shenyu-e2e-case/shenyu-e2e-case-storage -am test -Dtest=DividePluginCasesTest -Dsurefire.failIfNoSpecifiedTests=false
```

These provider tests do not replace running the Docker storage suites.
