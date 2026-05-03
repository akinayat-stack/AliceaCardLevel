package com.wonderland.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/** Represents one card on the board. */
public class Card {
    private final Texture frontTexture;
    private final Texture backTexture;
    private final Rectangle bounds;

    private boolean faceUp;
    private boolean matched;

    public Card(Texture frontTexture, Texture backTexture, Rectangle bounds, boolean faceUp) {
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        this.bounds = bounds;
        this.faceUp = faceUp;
    }

    public Texture getTextureToDraw() {
        return faceUp || matched ? frontTexture : backTexture;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(float x, float y, float width, float height) {
        bounds.set(x, y, width, height);
    }

    public Texture getFrontTexture() {
        return frontTexture;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            this.faceUp = true;
        }
    }
}
