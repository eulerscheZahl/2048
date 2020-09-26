package engine;


import java.util.ArrayList;

public class Board {
    public final int SIZE = 4;
    private long seed;
    private int[][] grid = new int[SIZE][SIZE];
    private int score;
    private BoardModule boardModule;

    public Board(int seed, BoardModule boardModule) {
        this.boardModule = boardModule;
        this.seed = seed;
        spawnTile();
        spawnTile();
        boardModule.onAfterGameTurn();
    }

    public int getScore() {
        return score;
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

    public boolean canMove() {
        for (int i = 0; i < 4; i++) {
            if (canMove(i)) return true;
        }
        return false;
    }

    private boolean canMove(int dir) {
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

    private String moveCache = "";
    private final String dirs = "URDL";

    public boolean needsPlayerAction() {
        return moveCache.length() == 0;
    }

    public void playTurn() throws Exception {
        char c = moveCache.charAt(0);
        moveCache = moveCache.substring(1);

        for (int i = 0; i < 4; i++) {
            if (canMove(i)) c = dirs.charAt(i);
        }

        if (!canMove(dirs.indexOf(c))) throw new Exception("Invalid action");
        score += applyMove(dirs.indexOf(c));
        boardModule.addMove(dirs.indexOf(c));
        spawnTile();
    }

    public void cache(String move) {
        moveCache = move.toUpperCase();
    }

    public ArrayList<String> getInput(boolean firstTurn) {
        ArrayList<String> result = new ArrayList<>();
        if (firstTurn) result.add(String.valueOf(seed));
        result.add(String.valueOf(score));
        for (int y = 0; y < SIZE; y++) {
            String line = "";
            for (int x = 0; x < SIZE; x++) line += grid[x][y] + " ";
            result.add(line.trim());
        }
        return result;
    }
}
