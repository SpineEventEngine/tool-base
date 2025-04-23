# `gradle-plugin-api-test-fixtures` module

This module contains test fixtures for tests of the `gradle-plugin-api` module.

As of time of writing, it is not possible to apply a Gradle plugin to a separate source set
like `testFixtures`. That's why we need a separate module with stub plugins for our tests.
