package club.mindtech.mindbot.util;

import java.util.stream.Stream;

public class BotHelper {
	public static <T> T notNull(T obj) {
		if (obj == null) {
			String caller = StackWalker.getInstance().walk(BotHelper::findCaller);
			throw new NullPointerException("Object is null at " + caller);
		}
		return obj;
	}

	private static String findCaller(Stream<StackWalker.StackFrame> frame) {
		return frame.skip(1) // skip this method
					.findFirst()
					.map(f -> f.getClassName() + "::" + f.getMethodName() + "#" + f.getLineNumber())
					.orElse("Unknown");
	}
}
