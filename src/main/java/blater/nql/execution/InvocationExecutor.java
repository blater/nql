package blater.nql.execution;

import blater.nql.cli.NqlInvocation;

/** Executes typed CLI invocations without rediscovering commands from map keys. */
public final class InvocationExecutor {
  private final InvocationCommandHandlers handlers;

  public InvocationExecutor() {
    this(new InputMaterializer(), new ExecutionTargetResolver());
  }

  InvocationExecutor(
      InputMaterializer inputMaterializer,
      ExecutionTargetResolver targetResolver) {
    this.handlers = new InvocationCommandHandlers(inputMaterializer, targetResolver);
  }

  public void execute(NqlInvocation invocation, InputEnvironment environment) throws Exception {
    handlers.execute(invocation, environment);
  }
}
