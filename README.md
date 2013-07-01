protobuf_maven
==============

A maven proof-of-concept for building and working with protobufs.

This provides an example of generating java from protobufs via the Maven ant plugin and has a test suite verifying
basic assumptions about schema enforcement and schema evolution.

1. If you haven't already installed protoc then:
  1. [Download](https://code.google.com/p/protobuf/downloads) & extract protoc
  2. ./configure ; make ; make check ; make install
2. In this projects root run "mvn clean test" to ensure everything is working

That's it!

