After the past number of months I have needed to build state machines to solve a number of
processing problems.  All of the state machines that I created were hand-rolled and all of
their development followed a similar process:

- Create a plantuml state diagram to describe the state machine and use of the basis for tests,
- Create a set of tests to validate that the transitions are what was expected, and
- Construct the state machine.

Once the state machine was implemented the action behaviour was added with all of the
accompanying tests.

This project is an attempt to create a type-safe DSL that allows me to create state machines
without the need to construct tests to confirm that the state machine works the way I need it to.

At a high-level the DSL to describe the state machine should have the following characteristics:

- The objects that need to be written by the developer should not be infected by the state machine
  implementation.  It is therefore necessary to use generics rather than inheritance when describing
  the state machine.  The benefit of this is that it becomes trivial to create a state machine where
  the states are of type java.lang.Integer and the events are java.lang.Character instances without
  the need to create wrapper classes.
- The state machine needs to be re-entrant.  Re-entrancy will be achieved through using value objects
  rather than mutable objects.

I have noted that there are primarily 2 kinds of state machines with each being one of type flavours:

- The events have a static state: examples of this is where the events can be represented as a
  native type or as an enumeration.  Specifically ```equals``` can be used as a comparator.
- The events are stateful: an example is each event type is represented as a class with the event
  being an instance of that class.  Specifically ```equals``` can not be used on the event instance 
  but rather on confirming that the instance is an instance of a specific class.
  
Each of these can then have a flavour where the state machine has a bespoke runtime state and the
second, a simpler flavour, is where the state machine does not have a bespoke runtime state.

The following names each kind of state machine and provides the name of the builder which is used in
the construction of the builder:

| Name | Description | Builder |
|------|-------------|---------|
| NativeStatelessStateMachine | Events are native types or an enum type with the state machine having no bespoke state. | playing.statemachine.nativestateless.StateMachine.Builder<STATE, EVENT> |
| NativeStatefulStateMachine | Events are native types or an enum type with the state machine supporting a bespoke state. | playing.statemachine.nativestateful.StateMachine.Builder<STATE, EVENT, RS> |
| ClassStatelessStateMachine | Events are stateful with the state machine having no bespoke state. | playing.statemachine.classstateless.StateMachine.Builder<STATE> |
| ClassStatefulStateMachine | Events are stateful with the state machine supporting a bespoke state. | playing.statemachine.classstateful.StateMachine.Builder<STATE, RS> |

