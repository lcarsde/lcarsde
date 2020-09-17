# IPC between window manager and application window

* Status: accepted <!-- optional -->
* Deciders: Andreas Tennert <!-- optional -->
* Date: 2020-05-08 <!-- optional -->

## Context and Problem Statement

The application menu needs to know the currently active windows, shall display the name and enable the user to select or close them. Therefor it is necessary to exchange the corresponding information with the window manager.

## Decision Drivers <!-- optional -->

* Fast, the application menu entry information should be updated "instantly" when windows open, close or get renamed
* No message should be lost

## Considered Options

* Shared memory
* Shared files
* Named pipes
* Sockets
* Message queues

## Decision Outcome

Chosen option: "Message queues", because this approach is a faster than shared storage approaches and doesn't need to handle locking. Message queues are asynchronous (the producer doesn't need to wait for the consumer) and messages are queued and not lost.

### Positive Consequences <!-- optional -->

* Easy to use and flexible
* Library support in Python

### Negative Consequences <!-- optional -->

* Manual implementation in Kotlin necessary (but straight forward)

## Pros and Cons of the Options <!-- optional -->

See IPC option descriptions in the links.

## Links <!-- optional -->

* https://opensource.com/article/19/4/interprocess-communication-linux-storage
* https://opensource.com/article/19/4/interprocess-communication-linux-channels
* https://opensource.com/article/19/4/interprocess-communication-linux-networking