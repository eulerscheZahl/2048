import com.codingame.gameengine.runner.SoloGameRunner;

public class SkeletonMain {
    public static void main(String[] args) {
        SoloGameRunner gameRunner = new SoloGameRunner();
        gameRunner.setAgent(Agent1.class);
        gameRunner.setAgent("mono /home/eulerschezahl/Documents/Programming/C#/2048/bin/Debug/2048.exe");
        gameRunner.setTestCase("test2.json");
        gameRunner.start();
    }
}
