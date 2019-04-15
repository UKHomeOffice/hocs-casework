package uk.gov.digital.ho.hocs.casework.application;

import org.slf4j.MDC;
import org.springframework.core.task.TaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

public class ContextAwareExecutorDecorator implements Executor, TaskExecutor {

    private final Executor executor;

    public ContextAwareExecutorDecorator(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        Runnable ctxAwareCommand = decorateContextAware(command);
        executor.execute(ctxAwareCommand);
    }

    private Runnable decorateContextAware(Runnable command) {
        final Map<String, String> originalContextCopy = MDC.getCopyOfContextMap();
        return () -> {
            // copy the current context
            final Map<String, String> localContextCopy = MDC.getCopyOfContextMap();

            MDC.clear();
            if (originalContextCopy != null) {
                // set the desired context that was present at point of calling execute
                MDC.setContextMap(originalContextCopy);
            }

            // execute the command
            command.run();

            MDC.clear();
            if (localContextCopy != null) {
                // reset the context
                MDC.setContextMap(localContextCopy);
            }
        };
    }
}