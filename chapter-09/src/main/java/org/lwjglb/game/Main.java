package org.lwjglb.game;

import org.joml.Vector2f;
import org.lwjglb.engine.*;
import org.lwjglb.engine.graph.*;
import org.lwjglb.engine.scene.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    private int NrOfCubesToGenerate=10000;
    private int frameCount = 0;
    private long lastTime;  // Declare lastTime here

    private Entity cubeEntity;
    private Entity cubeEntity2;

    private Entity cubeEntity3;
    private Model cubeModel;

    private static List<Entity> cubes = new ArrayList<>();

    private static int NrOfCubes=0;

    private long lastCubeGenerationTime;
    private final long cubeGenerationInterval = 1000; // 2000 milliseconds = 2 second

    private float rotation;

    public static void main(String[] args) {
        Main main = new Main();
        main.runMain();


    }


    public void runMain()
    {
        Main main=new Main();
        int totalRuns = 10;
        int batchSize = 5;
        List<Double> allFpsValues = new ArrayList<>();


        for (int i = 0; i < totalRuns; i++) {
            Engine gameEng = new Engine("GPU-Benchmark:"+(i+1)+"/10", new Window.WindowOptions(), main, i + 1);
            gameEng.start();
            allFpsValues.addAll(gameEng.getFpsList());
            gameEng.stop();
            cubes.clear();
            NrOfCubes=0;

            System.out.println();

            // Compute the average FPS after every batch of batchSize runs
            if ((i + 1) % batchSize == 0) {
                List<Double> fpsBatch = allFpsValues.subList(i + 1 - batchSize, i + 1);
                double totalFps = 0;
                for (double fps : fpsBatch) {
                    totalFps += fps;
                }
                double averageFps = totalFps / fpsBatch.size();
                System.out.println("Average FPS after " + (i + 1) + " runs: " + averageFps + "\n");
            }
        }

        System.out.println("Final score:"+calculateFinalSCore(allFpsValues));
    }

    public void setNrOfCubesToGenerate(int nr)
    {
        this.NrOfCubesToGenerate=nr;
    }
    private static double calculateFinalSCore(List<Double> allFpsValues)
    {
        double avgFPS = calculateAverage(allFpsValues);
        double minFPS= Collections.min(allFpsValues);
        double maxFPS= Collections.max(allFpsValues);

        double normalizedAvg = normalize(avgFPS, allFpsValues);

        double weightAvg = 0.6;
        double weightMin = 0.3;
        double weightMax = 0.1;

        double FinalScore=weightAvg * normalizedAvg + weightMin * minFPS + weightMax * maxFPS;
        return FinalScore;
    }
    private static double calculateAverage(List<Double> values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    private static double normalize(double value, List<Double> values) {
        double min = Collections.min(values);
        double max = Collections.max(values);
        return 100 * (value - min) / (max - min);
    }
    @Override
    public void cleanup() {
        // Nothing to be done yet
    }
    @Override
    public void init(Window window, Scene scene, Render render) {
         cubeModel = ModelLoader.loadModel("cube-model", "resources/models/cube/cube.obj",
                scene.getTextureCache());
        scene.addModel(cubeModel);



        cubeEntity2 = new Entity("cube-entity2", cubeModel.getId());
        cubeEntity2.setPosition(0, 0, -10);
        scene.addEntity(cubeEntity2);
        lastCubeGenerationTime = System.currentTimeMillis();
        NrOfCubes++;



        /*
               cubeEntity = new Entity("cube-entity", cubeModel.getId());
        cubeEntity.setPosition(10, -5, -10);
        scene.addEntity(cubeEntity);
        lastCubeGenerationTime = System.currentTimeMillis();

        cubeEntity3 = new Entity("cube-entity3", cubeModel.getId());
        cubeEntity3.setPosition(0, 0, -10);
        scene.addEntity(cubeEntity3);
        lastCubeGenerationTime = System.currentTimeMillis();
*/
        //cubes.add(cubeEntity);
        cubes.add(cubeEntity2);
        //cubes.add(cubeEntity3);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {
  /*      float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            camera.moveDown(move);
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            camera.addRotation((float) Math.toRadians(-displVec.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(-displVec.y * MOUSE_SENSITIVITY));
        }
    */
    }



    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        rotation += 1.5;
        if (rotation > 360) {
            rotation = 0;
        }

        for (Entity cube : cubes) {
            cube.setRotation(5, 10, 12, (float) Math.toRadians(rotation));
            cube.updateModelMatrix();
        }


        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCubeGenerationTime >= cubeGenerationInterval){

            for(int i=0;i<NrOfCubesToGenerate;i++) {
                NrOfCubes++;
                Entity cubeEntity4;
                float x = (float) Math.random() * (20 - (-11)) + (-11);  // Random between -10 to 10
                float y = (float) Math.random() * (6 - (-6)) + (-6);  // Random between -5 to 5
                float z = (float) Math.random() * (20) - 30; // Random in interval -10 ,-30

                cubeEntity4 = new Entity("cube-entity4", cubeModel.getId());
                cubeEntity4.setPosition(x, y, z);

                lastCubeGenerationTime = System.currentTimeMillis();

                cubeEntity4.updateModelMatrix();
                scene.addEntity(cubeEntity4);
                cubes.add(cubeEntity4);
            }
        }
    }
}
