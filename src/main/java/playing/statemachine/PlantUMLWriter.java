package playing.statemachine;

import playing.util.StringUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class PlantUMLWriter {
    public static <STATE, EVENT> void write(StateMachineWriter<STATE, EVENT> stateMachine, String fileName) throws IOException {
        write(stateMachine, fileName, 1000);
    }

    public static <STATE, EVENT> void write(StateMachineWriter<STATE, EVENT> stateMachine, String fileName, int scale) throws IOException {
        String eventNamePrefix = commonEventNamePrefix(stateMachine);
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("@startuml");
            pw.println("scale " + scale + " width");
            pw.println("[*] --> " + stateMachine.initialState());
            for (StateMachineTransition<STATE, EVENT> transition : stateMachine.transitions()) {
                final String eventName = trimPrefix(eventNamePrefix, transition.event().toString());
                pw.println("" + transition.fromState() + " --> " + transition.toState() + (eventName.equals("") ? "" : " : " + eventName));
            }

            pw.println("@enduml");
        }
    }

    private static <STATE, EVENT> String commonEventNamePrefix(StateMachineWriter<STATE, EVENT> stateMachine) {
        Optional<String> commonPrefix = Optional.empty();
        for (StateMachineTransition<STATE, EVENT> transition: stateMachine.transitions()) {
            if (commonPrefix.isPresent()) {
                commonPrefix = Optional.of(StringUtil.prefix(commonPrefix.get(), transition.event().toString()));
            } else {
                commonPrefix = Optional.of(transition.event().toString());
            }
        }
        return commonPrefix.orElse("");
    }

    private static String trimPrefix(String prefix, String value) {
        return value.substring(prefix.length());
    }
}
