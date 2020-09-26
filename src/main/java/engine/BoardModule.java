package engine;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;

@Singleton
public class BoardModule implements Module {
    private GameManager gameManager;

    @Inject
    BoardModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
    }

    @Override
    public void onGameInit() {
        // we are not using the GraphicsEntityModule directly but we have to set the world dimensions
        HashMap<String, Integer> world = new HashMap<>();
        world.put("width", 1920);
        world.put("height", 1080);
        gameManager.setViewGlobalData("entitymodule",  world);
    }

    private String message = "";

    public void addSpawn(int cellIndex, int value) {
        if (value == 2) message += (char) ('a' + cellIndex);
        else message += (char) ('A' + cellIndex);
    }

    public void addMove(int dir) {
        message += "^>v<".charAt(dir);
    }

    @Override
    public void onAfterGameTurn() {
        gameManager.setViewData("x", message);
        message = "";
    }

    @Override
    public void onAfterOnEnd() {

    }
}
