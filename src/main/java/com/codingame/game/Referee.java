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
            board = new Board(seed, boardModule, gameManager);
        } catch (Exception ex) {
            Random random = new Random();
            int seed = -1;
            while (seed < 0) seed = random.nextInt();
            board = new Board(seed, boardModule, gameManager);
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
        if (playerTurns == 600) {
            gameManager.putMetadata("Points", String.valueOf(board.getScore()));
            gameManager.addToGameSummary("Maximum number of player interactions (600) reached");
            gameManager.winGame("score: " + board.getScore());
            return;
        }
        playerTurns++;
        ArrayList<String> turnInput = board.getInput();
        for (String line : turnInput) player.sendInputLine(line);
        player.execute();
        String action = null;
        try {
            action = player.getOutputs().get(0);
        } catch (TimeoutException e) {
            gameManager.loseGame("timeout");
            return;
        }

        board.playTurn(action);
        gameManager.addToGameSummary("Score: " + board.getScore());
        gameManager.addToGameSummary("Moves: " + board.getMoves());
    }
}
