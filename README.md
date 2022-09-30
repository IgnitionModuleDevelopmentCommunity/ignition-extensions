# Ignition Extensions

A (hopefully) community driven Ignition module project to house utilities that are often useful, but just too niche (or
potentially risky) to go into Ignition itself.

# Usage

Simply download the .modl file from
the [latest release](https://github.com/IgnitionModuleDevelopmentCommunity/ignition-extensions/releases) and install it
to your gateway.

# Contribution

Contributions are welcome. This project is polyglot and set up for both Kotlin and Java. There are example utilities
written in both Kotlin and Java to extend from. Ideas for new features should start as issues for broader discussion.

# Building

This project uses Gradle, and the Gradle Module Plugin. Use `./gradlew build` to assemble artifacts,
and `./gradlew zipModule` to build an unsigned module file for installation into a development gateway.

# Testing

The easiest way to test is a local Docker installation. Simple run `docker compose up` in the root of this repository to
stand up a local development gateway. Use `./gradlew deployModl` to install the locally built module on that test
gateway.
