package sschr15.tools.qblo.agent;

import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {
	public static Instrumentation instrumentation;

	private static void main(String className, Instrumentation inst) {
		instrumentation = inst;
		if (className == null || className.isEmpty()) {
			return;
		}

		try {
			Class<?> clazz = Class.forName(className);
			var method = clazz.getDeclaredMethod("onInstrumentationAgentLoaded");
			method.invoke(null, inst);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		main(agentArgs, inst);
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		main(agentArgs, inst);
	}
}
