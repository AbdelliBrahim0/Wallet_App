package com.example.be9ik_wallet;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticlesDrawable extends Drawable {
    private final Paint paint = new Paint();
    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final int particleCount = 50;
    private boolean isRunning = false;
    private long lastUpdateTime;
    private float speed = 1.0f;

    public ParticlesDrawable() {
        paint.setAntiAlias(true);
        paint.setColor(0x33FFFFFF); // Default semi-transparent white
        paint.setStyle(Paint.Style.FILL);

        initParticles();
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < particleCount; i++) {
            particles.add(createRandomParticle());
        }
    }

    private Particle createRandomParticle() {
        int width = getBounds().width() > 0 ? getBounds().width() : 1080; // Default if bounds not set
        int height = getBounds().height() > 0 ? getBounds().height() : 1920; // Default if bounds not set

        float x = random.nextFloat() * width;
        float y = random.nextFloat() * height;
        float size = 2f + random.nextFloat() * 4f;
        float speedX = (random.nextFloat() - 0.5f) * 2f;
        float speedY = (random.nextFloat() - 0.5f) * 2f;
        float alpha = 0.1f + random.nextFloat() * 0.3f;

        return new Particle(x, y, size, speedX, speedY, alpha);
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            lastUpdateTime = SystemClock.uptimeMillis();
            scheduleSelf(animationRunnable, SystemClock.uptimeMillis() + 16);
        }
    }

    public void stop() {
        isRunning = false;
        unscheduleSelf(animationRunnable);
    }

    public void setParticleColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    private final Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long currentTime = SystemClock.uptimeMillis();
                float deltaTime = (currentTime - lastUpdateTime) / 1000f;
                lastUpdateTime = currentTime;

                updateParticles(deltaTime);
                invalidateSelf();

                scheduleSelf(this, SystemClock.uptimeMillis() + 16);
            }
        }
    };

    private void updateParticles(float deltaTime) {
        int width = getBounds().width();
        int height = getBounds().height();

        if (width <= 0 || height <= 0) return;

        for (Particle particle : particles) {
            // Update position
            particle.x += particle.speedX * speed * deltaTime * 60;
            particle.y += particle.speedY * speed * deltaTime * 60;

            // Wrap around screen
            if (particle.x < 0) particle.x = width;
            if (particle.x > width) particle.x = 0;
            if (particle.y < 0) particle.y = height;
            if (particle.y > height) particle.y = 0;
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        for (Particle particle : particles) {
            paint.setAlpha((int) (255 * particle.alpha));
            canvas.drawCircle(particle.x, particle.y, particle.size, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // Not used
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private static class Particle {
        float x, y;
        float size;
        float speedX, speedY;
        float alpha;

        Particle(float x, float y, float size, float speedX, float speedY, float alpha) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speedX = speedX;
            this.speedY = speedY;
            this.alpha = alpha;
        }
    }
}
