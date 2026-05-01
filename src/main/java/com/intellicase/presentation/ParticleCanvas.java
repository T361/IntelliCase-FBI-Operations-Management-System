package com.intellicase.presentation;

import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Hardware-accelerated particle engine rendered on a JavaFX Canvas.
 * Draws 150 drifting particles simulating a data-stream/starfield background.
 * Uses AnimationTimer for continuous 60fps rendering.
 */
public class ParticleCanvas extends Canvas {
    private static final int PARTICLE_COUNT = 150;
    private final double[] particleX = new double[PARTICLE_COUNT];
    private final double[] particleY = new double[PARTICLE_COUNT];
    private final double[] velocityX = new double[PARTICLE_COUNT];
    private final double[] velocityY = new double[PARTICLE_COUNT];
    private final double[] sizes = new double[PARTICLE_COUNT];
    private final double[] opacities = new double[PARTICLE_COUNT];
    private final Random random = new Random();
    private AnimationTimer timer;

    public ParticleCanvas() {
        initializeParticles();
        startAnimation();
    }

    private void initializeParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particleX[i] = random.nextDouble() * 1400;
            particleY[i] = random.nextDouble() * 900;
            velocityX[i] = (random.nextDouble() - 0.5) * 0.6;
            velocityY[i] = (random.nextDouble() - 0.5) * 0.4;
            sizes[i] = 1.0 + random.nextDouble() * 2.5;
            opacities[i] = 0.15 + random.nextDouble() * 0.45;
        }
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = getWidth();
                double h = getHeight();
                if (w <= 0 || h <= 0) {
                    return;
                }
                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, w, h);
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    particleX[i] += velocityX[i];
                    particleY[i] += velocityY[i];
                    if (particleX[i] < 0) {
                        particleX[i] = w;
                    }
                    if (particleX[i] > w) {
                        particleX[i] = 0;
                    }
                    if (particleY[i] < 0) {
                        particleY[i] = h;
                    }
                    if (particleY[i] > h) {
                        particleY[i] = 0;
                    }
                    Color color;
                    if (i % 3 == 0) {
                        color = Color.rgb(201, 169, 77, opacities[i]); // gold
                    } else if (i % 3 == 1) {
                        color = Color.rgb(31, 111, 235, opacities[i]); // blue
                    } else {
                        color = Color.rgb(255, 255, 255, opacities[i] * 0.4); // white dim
                    }
                    gc.setFill(color);
                    if (i % 3 == 0) {
                        gc.fillRect(particleX[i], particleY[i], sizes[i], sizes[i]);
                    } else {
                        gc.fillOval(particleX[i], particleY[i], sizes[i], sizes[i]);
                    }
                }
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }
}
