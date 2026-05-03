package com.wonderland.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** Memory matching game implementation shared by all platforms. */
public class СфСCardGame extends ApplicationAdapter {
    private static final int GRID_SIZE = 4;
    private static final int TOTAL_PAIRS = 8;
    private static final float PREVIEW_SECONDS = 5f;
    private static final float MISMATCH_DELAY_SECONDS = 1.2f;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
    private BitmapFont font;
    private Texture backgroundTexture;
    private Texture backCardTexture;
    private Texture[] frontTextures;
    private final Vector2 touchPoint = new Vector2();

    private Array<Card> cards;
    private Card firstSelected;
    private Card secondSelected;

    private float previewTimer;
    private float mismatchTimer;
    private int matchedPairs;

    private boolean previewPhase;
    private boolean inputLocked;
    private boolean win;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.setColor(Color.GOLD);

        backgroundTexture = new Texture("background.png");
        backCardTexture = new Texture("back_card.png");
        frontTextures = new Texture[TOTAL_PAIRS];
        for (int i = 0; i < TOTAL_PAIRS; i++) {
            frontTextures[i] = new Texture("card_" + (i + 1) + ".png");
        }

        buildShuffledCards();
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        previewPhase = true;
        inputLocked = true;
        previewTimer = PREVIEW_SECONDS;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (inputLocked || win) {
                    return false;
                }

                touchPoint.set(screenX, screenY);
                viewport.unproject(touchPoint);
                float worldX = touchPoint.x;
                float worldY = touchPoint.y;

                for (Card card : cards) {
                    if (card.contains(worldX, worldY) && !card.isFaceUp() && !card.isMatched()) {
                        revealCard(card);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void buildShuffledCards() {
        Array<Texture> pairTextures = new Array<>();
        for (Texture texture : frontTextures) {
            pairTextures.add(texture);
            pairTextures.add(texture);
        }
        pairTextures.shuffle();

        cards = new Array<>();

        int textureIndex = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cards.add(new Card(pairTextures.get(textureIndex++), backCardTexture, new Rectangle(), true));
            }
        }

        layoutCards(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void layoutCards(float width, float height) {
        float padding = Math.max(12f, Math.min(width, height) * 0.02f);
        float availableWidth = width - (padding * (GRID_SIZE + 1));
        float availableHeight = height - (padding * (GRID_SIZE + 1));
        float cardWidth = availableWidth / GRID_SIZE;
        float cardHeight = availableHeight / GRID_SIZE;

        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                float x = padding + col * (cardWidth + padding);
                float y = height - padding - (row + 1) * cardHeight - row * padding;
                cards.get(index++).setBounds(x, y, cardWidth, cardHeight);
            }
        }
    }

    private void revealCard(Card card) {
        card.setFaceUp(true);

        if (firstSelected == null) {
            firstSelected = card;
            return;
        }

        secondSelected = card;
        inputLocked = true;

        if (firstSelected.getFrontTexture() == secondSelected.getFrontTexture()) {
            firstSelected.setMatched(true);
            secondSelected.setMatched(true);
            matchedPairs++;
            resetSelection();
            inputLocked = false;
            if (matchedPairs == TOTAL_PAIRS) {
                win = true;
            }
        } else {
            mismatchTimer = MISMATCH_DELAY_SECONDS;
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        updateTimers(delta);

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (Card card : cards) {
            Rectangle bounds = card.getBounds();
            batch.draw(card.getTextureToDraw(), bounds.x, bounds.y, bounds.width, bounds.height);
        }

        if (previewPhase) {
            font.draw(batch, "Memorize the cards: " + Math.max(0, (int) Math.ceil(previewTimer)) + "s", 20, 35);
        } else if (win) {
            font.draw(batch, "You Win!", viewport.getWorldWidth() / 2f - 60f, viewport.getWorldHeight() / 2f);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (cards != null) {
            layoutCards(width, height);
        }
    }

    private void updateTimers(float delta) {
        if (previewPhase) {
            previewTimer -= delta;
            if (previewTimer <= 0f) {
                previewPhase = false;
                inputLocked = false;
                for (Card card : cards) {
                    if (!card.isMatched()) {
                        card.setFaceUp(false);
                    }
                }
            }
            return;
        }

        if (secondSelected != null && mismatchTimer > 0f) {
            mismatchTimer -= delta;
            if (mismatchTimer <= 0f) {
                firstSelected.setFaceUp(false);
                secondSelected.setFaceUp(false);
                resetSelection();
                inputLocked = false;
            }
        }
    }

    private void resetSelection() {
        firstSelected = null;
        secondSelected = null;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        backgroundTexture.dispose();
        backCardTexture.dispose();
        for (Texture texture : frontTextures) {
            texture.dispose();
        }
    }
}
