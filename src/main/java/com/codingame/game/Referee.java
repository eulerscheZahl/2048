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
    private int playerTurns = 0;

    @Override
    public void init() {
        gameManager.setMaxTurns(100000);
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
            gameManager.winGame("score: " + board.getScore());
            return;
        }
        Player player = gameManager.getPlayer();
        if (board.needsPlayerAction()) {
            if (playerTurns == 600) {
                gameManager.putMetadata("Points", String.valueOf(board.getScore()));
                gameManager.addToGameSummary("Maximum number of player interactions (600) reached");
                gameManager.winGame("score: " + board.getScore());
                return;
            }
            playerTurns++;
            ArrayList<String> turnInput = board.getInput(turn == 1);
            for (String line : turnInput) player.sendInputLine(line);
            player.execute();
            String action = null;
            try {
                action = player.getOutputs().get(0);
            } catch (TimeoutException e) {
                gameManager.loseGame("timeout");
                return;
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
