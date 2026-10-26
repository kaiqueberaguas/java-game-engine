package br.com.beraguasstudio.engine.core;

import java.util.function.DoubleSupplier;

/**
 * Game loop com passo fixo e acumulador. Lógica/física avançam em {@code fixedStep};
 * o render roda a cada iteração com o fator {@code alpha} para interpolação.
 * Não depende de GLFW/OpenGL: tudo que toca a plataforma entra por {@link Callbacks}.
 */
public final class GameLoop {

    public static final double DEFAULT_FIXED_STEP = 1.0 / 60.0;
    public static final double DEFAULT_MAX_FRAME_TIME = 0.25;

    public interface Callbacks {
        boolean shouldClose();

        void pollEvents();

        void fixedUpdate(double dt);

        void render(float alpha);
    }

    private final Callbacks callbacks;
    private final DoubleSupplier clock;
    private final double fixedStep;
    private final double maxFrameTime;

    private double lastTime;
    private double accumulator;
    private boolean stopped;

    public GameLoop(Callbacks callbacks) {
        this(callbacks, GameLoop::systemSeconds, DEFAULT_FIXED_STEP, DEFAULT_MAX_FRAME_TIME);
    }

    public GameLoop(Callbacks callbacks, DoubleSupplier clock, double fixedStep, double maxFrameTime) {
        this.callbacks = callbacks;
        this.clock = clock;
        this.fixedStep = fixedStep;
        this.maxFrameTime = maxFrameTime;
        this.lastTime = clock.getAsDouble();
    }

    public void run() {
        while (tick()) {
            // cada tick é um frame
        }
    }

    public void stop() {
        stopped = true;
    }

    /** Executa um frame. Retorna {@code false} quando o loop deve terminar. */
    public boolean tick() {
        if (stopped || callbacks.shouldClose()) {
            return false;
        }

        double now = clock.getAsDouble();
        double frameTime = Math.min(now - lastTime, maxFrameTime);
        lastTime = now;
        accumulator += frameTime;

        callbacks.pollEvents();

        while (accumulator >= fixedStep) {
            callbacks.fixedUpdate(fixedStep);
            accumulator -= fixedStep;
        }

        callbacks.render((float) (accumulator / fixedStep));
        return true;
    }

    private static double systemSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
