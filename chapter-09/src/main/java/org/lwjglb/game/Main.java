package org.lwjglb.game;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjglb.engine.*;
import org.lwjglb.engine.graph.*;
import org.lwjglb.engine.scene.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import benchmark.*;
import org.lwjglb.engine.scene.lights.PointLight;
import org.lwjglb.engine.scene.lights.SceneLights;
import org.lwjglb.engine.scene.lights.SpotLight;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;
    private static final long CUBE_GENERATION_INTERVAL = 1000;

    private int nrOfCubesToGenerate = 10000;
    private int frameCount = 0;
    private long lastTime;
    private long lastCubeGenerationTime;

    private Entity cubeEntity;
    private Model cubeModel;
    private LightControls lightControls;

    private static List<Entity> cubes = new ArrayList<>();
    private static int nrOfCubes = 0;

    private float rotation;

    public static void main(String[] args) {
        Main main = new Main();
        main.runMain();
    }

    public BenchmarkInfo runMain() {
        Main main = new Main();
        int totalRuns = 2;
        int batchSize = 1;
        List<Double> allFpsValues = new ArrayList<>();

        for (int i = 0; i < totalRuns; i++) {
            Engine gameEng = new Engine("GPU-Benchmark:" + (i + 1) + "/" + totalRuns, new Window.WindowOptions(), main, i + 1);
            gameEng.start();
            allFpsValues.addAll(gameEng.getFpsList());
            gameEng.stop();
            cubes.clear();
            nrOfCubes = 0;

            if ((i + 1) % batchSize == 0) {
                List<Double> fpsBatch = allFpsValues.subList(i + 1 - batchSize, i + 1);
                double averageFps = calculateAverage(fpsBatch);
                System.out.println("Average FPS after " + (i + 1) + " runs: " + averageFps + "\n");
            }
        }

        double finalScore = calculateFinalScore(allFpsValues);
        System.out.println("Final score: " + finalScore);
        return new BenchmarkInfo("GPU benchmark", finalScore, nrOfCubesToGenerate);
    }

    public void setNrOfCubesToGenerate(int nr) {
        this.nrOfCubesToGenerate = nr;
    }

    private static double calculateFinalScore(List<Double> allFpsValues) {
        double avgFPS = calculateAverage(allFpsValues);
        double minFPS = Collections.min(allFpsValues);
        double maxFPS = Collections.max(allFpsValues);

        double normalizedAvg = normalize(avgFPS, allFpsValues);

        double weightAvg = 0.6;
        double weightMin = 0.3;
        double weightMax = 0.1;

        return weightAvg * normalizedAvg + weightMin * minFPS + weightMax * maxFPS;
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
        cubeModel = ModelLoader.loadModel("cube-model", "resources/models/cube/cube.obj", scene.getTextureCache());
        scene.addModel(cubeModel);

        cubeEntity = new Entity("cube-entity", cubeModel.getId());
        cubeEntity.setPosition(0, 0f, -10);
        cubeEntity.updateModelMatrix();
        scene.addEntity(cubeEntity);

        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbientLight().setIntensity(0.3f);
        scene.setSceneLights(sceneLights);

        sceneLights.getPointLights().add(new PointLight(new Vector3f(1, 1, 1), new Vector3f(0, 0, -1.4f), 1.0f));

        Vector3f coneDir = new Vector3f(0, 0, -1);
        sceneLights.getSpotLights().add(new SpotLight(new PointLight(new Vector3f(1, 1, 1), new Vector3f(0, 0, -1.4f), 0.0f), coneDir, 140.0f));

        lightControls = new LightControls(scene);
        scene.setGuiInstance(lightControls);

        lastCubeGenerationTime = System.currentTimeMillis();
        nrOfCubes++;
        cubes.add(cubeEntity);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) {
            return;
        }
        float move = diffTimeMillis * MOVEMENT_SPEED;
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
            camera.addRotation((float) Math.toRadians(-displVec.x * MOUSE_SENSITIVITY), (float) Math.toRadians(-displVec.y * MOUSE_SENSITIVITY));
        }
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
        if (currentTime - lastCubeGenerationTime >= CUBE_GENERATION_INTERVAL) {
            generateCubes(scene);
            lastCubeGenerationTime = currentTime;
        }
    }

    private void generateCubes(Scene scene) {
        for (int i = 0; i < nrOfCubesToGenerate; i++) {
            nrOfCubes++;
            float x = (float) Math.random() * 21 - 11;  // Random between -11 to 10
            float y = (float) Math.random() * 12 - 6;  // Random between -6 to 5
            float z = (float) Math.random() * 20 - 30; // Random between -30 to -10

            Entity cubeEntity = new Entity("cube-entity" + nrOfCubes, cubeModel.getId());
            cubeEntity.setPosition(x, y, z);
            cubeEntity.updateModelMatrix();
            scene.addEntity(cubeEntity);
            cubes.add(cubeEntity);
        }
    }
}
