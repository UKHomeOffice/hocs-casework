# hocs-casework

[![CodeQL](https://github.com/UKHomeOffice/hocs-casework/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/UKHomeOffice/hocs-casework/actions/workflows/codeql-analysis.yml)

This is the Home Office Correspondence Service (HOCS) casework service.

## Getting Started

### Prerequisites

* ```Java 17```
* ```Docker```
* ```Postgres```
* ```LocalStack```

### Submodules

This project contains a 'ci' submodule with a docker-compose and infrastructure scripts in it.
Most modern IDEs will handle pulling this automatically for you, but if not

```console
$ git submodule update --init --recursive
```

## Docker Compose

This repository contains a [Docker Compose](https://docs.docker.com/compose/)
file.

### Start localstack (sqs, sns, s3) and postgres

From the project root run:

```console
$ docker-compose -f ./ci/docker-compose.yml up -d localstack postgres
```

> With Docker using 4 GB of memory, this takes approximately 2 minutes to startup.

### Stop the services

From the project root run:

```console
$ docker-compose -f ./ci/docker-compose.yml stop
```

> This will retain data in the local database and other volumes.

## Running in an IDE

This project contains a `git-blame-ignore-revs` file which can be used to ignore large formatting commits when using git blame. This also works in IntelliJ as it uses the standard `git blame` command to annotate commits.
To see the correct blame information, you need to add the following to your git config:

``` console
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

If you are using an IDE, such as IntelliJ, this service can be started by running the ```HocsCaseServiceApplication```
main class.
The service can then be accessed at ```http://localhost:8082```.

You need to specify appropriate Spring profiles.
Paste `development,local` into the "Active profiles" box of your run configuration.

## Versioning

For versioning this project uses [SemVer](https://semver.org/).

## Authors

This project is authored by the Home Office.

## License

This project is licensed under the MIT license. For details please see [License](LICENSE)
