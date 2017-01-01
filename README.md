# Xenon Extensions
Unofficial extensions to Xenon framework (the open source micro-services framework)
More details of xenon framework can be found [here] (https://github.com/vmware/xenon)
This extension has following sub-modules

## JAX RS Support
#### Client Side
Acts as a client builder for REST services using Xenon Operation. 
Inspired by Jersey and Resteasy proxy client framework, this module provides JAX-RS annotation processing capability using Xenon constructs.
**JAX-RS** is a large spec and this is not an attempt to implement this completely. 

#### Server Side
Provides JAX-RS annotation processing capability for a stateless service. Honors JAX-RS Annotation on a best effort basis 

More details [here] (https://github.com/tteky/xenon-ext/tree/master/jaxrs-support)

## State machine for Xenon

Create state machine based workflows directly in java code. Its inspired from stateless & Xenon task service

#### Features
Provides ability to 
1. Attach on-entry & on-exit handlers on each state
2. Attach expiry based auto-transition
3. Basic UI to visualize the state machine diagram

More details [here] (https://github.com/tteky/xenon-ext/tree/master/xenon-fsm)