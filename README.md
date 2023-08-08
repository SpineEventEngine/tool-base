# tool-base

[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![codecov][codecov-badge]][codecov] &nbsp;
[![license][license-badge]][license]


Common code for development tools.

## Structure

* `tool-base` provides common components for building build-time tools, including file manipulations,
Protobuf reflection, simple code generation, etc.

* `plugin-base` provides abstractions for building Gradle plugins.

* `plugin-testlib` provides test fixtures for Gradle plugins.ture

## Java

The modules of this repository are built with Java 11.

[gh-actions]: https://github.com/SpineEventEngine/tool-base/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/tool-base/actions/workflows/build-on-ubuntu.yml/badge.svg
[codecov-badge]: https://codecov.io/gh/SpineEventEngine/tool-base/branch/master/graph/badge.svg
[license-badge]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
[codecov]: https://codecov.io/gh/SpineEventEngine/tool-base
