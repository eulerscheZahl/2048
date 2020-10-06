package engine;

import com.codingame.game.Player;
import com.codingame.gameengine.core.SoloGameManager;

import java.util.ArrayList;

public class Board {
    public final int SIZE = 4;
    private long seed;
    private int[][] grid = new int[SIZE][SIZE];
    private int score;
    private BoardModule boardModule;
    private SoloGameManager<Player> gameManager;

    public Board(int seed, BoardModule boardModule, SoloGameManager<Player> gameManager) {
        this.boardModule = boardModule;
        this.gameManager = gameManager;
        this.seed = seed;
        spawnTile();
        spawnTile();
        boardModule.onAfterGameTurn();
    }

    private void spawnTile() {
        ArrayList<Integer> freeCells = new ArrayList<>();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (grid[x][y] == 0) freeCells.add(x + y * SIZE);
            }
        }

        int spawnIndex = freeCells.get((int) seed % freeCells.size());
        int value = (seed & 0x10) == 0 ? 2 : 4;

        grid[spawnIndex % SIZE][spawnIndex / SIZE] = value;
        boardModule.addSpawn(spawnIndex, value);

        seed = seed * seed % 50515093L;
    }

    public int getScore() {
        return score;
    }

    public boolean canMove() {
        for (int i = 0; i < 4; i++) {
            if (canMove(i)) return true;
        }
        return false;
    }

    private boolean canMove(int dir) {
        if (wrongCommands == 10) return false;

        int[][] backup = new int[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) backup[x][y] = grid[x][y];
        }

        applyMove(dir);
        boolean changed = false;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                changed |= grid[x][y] != backup[x][y];
                grid[x][y] = backup[x][y];
            }
        }

        return changed;
    }

    private int applyMove(int dir) {
        int turnScore = 0;
        boolean[][] merged = new boolean[SIZE][SIZE];
        int targetStart = new int[]{0, SIZE - 1, SIZE * (SIZE - 1), 0}[dir];
        int targetStep = new int[]{1, SIZE, 1, SIZE}[dir];
        int sourceStep = new int[]{SIZE, -1, -SIZE, 1}[dir];

        for (int i = 0; i < SIZE; i++) {
            int finalTarget = targetStart + i * targetStep;
            for (int j = 1; j < SIZE; j++) {
                int source = finalTarget + j * sourceStep;
                int sourceX = source % SIZE;
                int sourceY = source / SIZE;
                if (grid[sourceX][sourceY] == 0) continue;
                for (int k = j - 1; k >= 0; k--) {
                    int intermediate = finalTarget + k * sourceStep;

                    int intermediateX = intermediate % SIZE;
                    int intermediateY = intermediate / SIZE;
                    if (grid[intermediateX][intermediateY] == 0) {
                        grid[intermediateX][intermediateY] = grid[sourceX][sourceY];
                        grid[sourceX][sourceY] = 0;
                        source = intermediate;
                        sourceX = source % SIZE;
                        sourceY = source / SIZE;
                    } else {
                        if (!merged[intermediateX][intermediateY] && grid[intermediateX][intermediateY] == grid[sourceX][sourceY]) {
                            grid[sourceX][sourceY] = 0;
                            grid[intermediateX][intermediateY] *= 2;
                            merged[intermediateX][intermediateY] = true;
                            turnScore += grid[intermediateX][intermediateY];
                        }
                        break;
                    }
                }
            }
        }
        return turnScore;
    }

    private final String dirs = "URDL";
    private boolean tooltipShown = false;
    private int wrongCommands = 0;
    private char lastAction = ' ';

    public void playTurn(String action) {
        if (action.length() == 0) gameManager.loseGame("no action given");
        int subFrames = 0;
        for (char c : action.toUpperCase().toCharArray()) {
            int dir = dirs.indexOf(c);
            if (dir == -1) {
                gameManager.loseGame("unknown command: " + c);
                continue;
            }
            if (!canMove(dir)) {
                if (!tooltipShown) {
                    gameManager.addTooltip(gameManager.getPlayer(), "invalid action: " + c);
                    tooltipShown = true;
                }
                if (c == lastAction) wrongCommands++;
                lastAction = c;
                gameManager.addToGameSummary("invalid action: " + c);
                return; // ignore remaining plans
            }
            wrongCommands = 0;
            subFrames++;
            score += applyMove(dir);
            boardModule.addMove(dir);
            spawnTile();
        }
        gameManager.setFrameDuration(500 * Math.max(1, subFrames));
    }

    public ArrayList<String> getInput() {
        ArrayList<String> result = new ArrayList<>();
        result.add(String.valueOf(seed));
        result.add(String.valueOf(score));
        for (int y = 0; y < SIZE; y++) {
            String line = "";
            for (int x = 0; x < SIZE; x++) line += grid[x][y] + " ";
            result.add(line.trim());
        }
        return result;
    }
}
