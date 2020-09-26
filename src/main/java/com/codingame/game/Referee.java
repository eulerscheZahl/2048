package com.codingame.game;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.google.inject.Inject;
import engine.Board;
import engine.BoardModule;

public class Referee extends AbstractReferee {
    @Inject
    private SoloGameManager<Player> gameManager;
    @Inject
    BoardModule boardModule;
    private Board board;

    @Override
    public void init() {
        try {
        int seed = Integer.parseInt(gameManager.getTestCaseInput().get(0));
        board = new Board(seed, boardModule);}
        catch (Exception ex) {
            Random random = new Random();
            board = new Board(random.nextInt(), boardModule);
        }
    }

    @Override
    public void gameTurn(int turn) {
        if (!board.canMove()) {
            gameManager.putMetadata("Points", String.valueOf(board.getScore()));
            gameManager.winGame();
        }
        Player player = gameManager.getPlayer();
        if (board.needsPlayerAction()) {
            ArrayList<String> turnInput = board.getInput(turn == 1);
            for (String line : turnInput) player.sendInputLine(line);
            player.execute();
            String action = null;
            try {
                action = player.getOutputs().get(0);
            } catch (TimeoutException e) {
                gameManager.loseGame("timeout");
            }
            board.cache(action);
        }

        try {
            board.playTurn();
        } catch (Exception e) {
            gameManager.loseGame("invalid action");
        }
    }
}
