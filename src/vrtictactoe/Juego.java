
package vrtictactoe;

import org.joml.*;
import vrtictactoe.engine.*;
import vrtictactoe.engine.graph.*;
import vrtictactoe.engine.scene.*;
import vrtictactoe.engine.scene.lights.*;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.openal.AL11;
import static vrtictactoe.Menu.gameEng;
import static vrtictactoe.VRTicTacToe.ganador;
import vrtictactoe.engine.sound.SoundBuffer;
import vrtictactoe.engine.sound.SoundListener;
import vrtictactoe.engine.sound.SoundManager;
import vrtictactoe.engine.sound.SoundSource;


public class Juego implements IAppLogic {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    private Entity michiEntity1;
    
    private char[][] tablero = {
            {' ', ' ', ' '},
            {' ', ' ', ' '},
            {' ', ' ', ' '}
    };
    
    private char jugadorActual = 'O';    
    
    private int numEsfera = 0;
    private int numcubo = 0;

    private AnimationData animationData;
    private float lightAngle;

    private Model cubeModel;
    private Model sphereModel;
    
    private ArrayList<Entity> esferas = new ArrayList<>();
    private ArrayList<Entity> cubos = new ArrayList<>();

    private SoundSource playerSoundSource;
    private SoundManager soundMgr;
    

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        
        String terrainModelId = "terrain";
        Model terrainModel = ModelLoader.loadModel(terrainModelId, "resources/models/terrain/terrain.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(terrainModel);
        
        Entity terrainEntity = new Entity("terrainEntity", terrainModelId);
        terrainEntity.setScale(100.0f);
        terrainEntity.updateModelMatrix();
        scene.addEntity(terrainEntity);

        cubeModel = ModelLoader.loadModel("cube-model", "resources/models/cube/cube.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(cubeModel);
        
        sphereModel = ModelLoader.loadModel("sphere-model", "resources/models/sphere/sphere.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(sphereModel);
        
        Model michiModel = ModelLoader.loadModel("michi-model", "resources/models/michi/michi.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(michiModel);
        
        
        michiEntity1 = new Entity("michi-entity-1", michiModel.getId());
        michiEntity1.setPosition(3, 2, -1);
        michiEntity1.updateModelMatrix();
        scene.addEntity(michiEntity1);
        
        for(int i = 0; i < 5; i++) {
            esferas.add(new Entity(String.valueOf(i), sphereModel.getId()));
        }
        
        for(int i = 5; i < 9; i++) {
            cubos.add(new Entity(String.valueOf(i), cubeModel.getId()));
        }

        for(Entity entidad : esferas) {
            entidad.setPosition(100, 100, 100);
            entidad.updateModelMatrix();
            scene.addEntity(entidad);
        }
        
        for(Entity entidad : cubos) {
            entidad.setPosition(100, 100, 100);
            entidad.updateModelMatrix();
            scene.addEntity(entidad);
        }
        

        render.setupData(scene);

        SceneLights sceneLights = new SceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        ambientLight.setIntensity(0.5f);
        ambientLight.setColor(0.3f, 0.3f, 0.3f);

        DirLight dirLight = sceneLights.getDirLight();
        dirLight.setPosition(0, 1, 0);
        dirLight.setIntensity(1.0f);
        scene.setSceneLights(sceneLights);

        SkyBox skyBox = new SkyBox("resources/models/skybox/skybox.obj", scene.getTextureCache(),
                scene.getMaterialCache());
        skyBox.getSkyBoxEntity().setScale(100);
        skyBox.getSkyBoxEntity().updateModelMatrix();
        scene.setSkyBox(skyBox);

        scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.02f));

        Camera camera = scene.getCamera();
        camera.setPosition(-1f, 6.5f, 5.5f);
        camera.addRotation((float) Math.toRadians(15.0f), (float) Math.toRadians(390.f));

        lightAngle = 45.001f;
        initSounds(michiEntity1.getPosition(), camera);
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
        
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            lightAngle -= 2.5f;
            if (lightAngle < -90) {
                lightAngle = -90;
            }
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            lightAngle += 2.5f;
            if (lightAngle > 90) {
                lightAngle = 90;
            }
            
            
        }
        
        // Botones para ingresar jugada
        if(window.isKeyPressed(GLFW_KEY_1)) {
            hacerJugada(0, 0, 0, 8, -1);
        } else if(window.isKeyPressed(GLFW_KEY_2)) {
            hacerJugada(0, 1, 3, 8, -1);
        } else if(window.isKeyPressed(GLFW_KEY_3)) {
            hacerJugada(0, 2, 6.5f, 8, -1);
        } else if(window.isKeyPressed(GLFW_KEY_4)) {
            hacerJugada(1, 0, 0, 5, -1);
        } else if(window.isKeyPressed(GLFW_KEY_5)) {
            hacerJugada(1, 1, 3, 5, -1);
        } else if(window.isKeyPressed(GLFW_KEY_6)) {
            hacerJugada(1, 2, 6.5f, 5, -1);
        } else if(window.isKeyPressed(GLFW_KEY_7)) {
            hacerJugada(2, 0, 0, 2, -1);
        } else if(window.isKeyPressed(GLFW_KEY_8)) {
            hacerJugada(2, 1, 3, 2, -1);
        } else if(window.isKeyPressed(GLFW_KEY_9)) {
            hacerJugada(2, 2, 6.5f, 2, -1);
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            camera.addRotation((float) Math.toRadians(-displVec.x * MOUSE_SENSITIVITY), (float) Math.toRadians(-displVec.y * MOUSE_SENSITIVITY));
        }

        SceneLights sceneLights = scene.getSceneLights();
        DirLight dirLight = sceneLights.getDirLight();
        double angRad = Math.toRadians(lightAngle);
        dirLight.getDirection().z = (float) Math.sin(angRad);
        dirLight.getDirection().y = (float) Math.cos(angRad);
        
        soundMgr.updateListenerPosition(camera);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {

            playerSoundSource.play();

    }
    
    private void hacerJugada(int row, int col, float posX, float posY, float posZ) {
         System.out.println("Trying to make a move at: " + row + ", " + col);
        if (esJugadaValida(row, col)) {
            tablero[row][col] = jugadorActual;
            displayBoard();
            if(jugadorActual == 'O') {
                esferas.get(numEsfera).setPosition(posX - 6, posY - 2.5f, posZ + 1);
                esferas.get(numEsfera).updateModelMatrix();
                numEsfera++;
            } else {
                cubos.get(numcubo).setPosition(posX, posY, posZ);
                cubos.get(numcubo).updateModelMatrix();
                numcubo++;
            }

            if(checkTie() || checkWin()) {
                System.out.println("Game over!");
            }
            cambiarJugador();
        } else {
            System.out.println("Invalid move. Try again.");
        }
    }
    
    private boolean esJugadaValida(int row, int col) {
        System.out.println("Checking if move is valid at: " + row + ", " + col);
        boolean isValid = tablero[row][col] == ' ';
        System.out.println("Is valid move: " + isValid);
        return isValid;
    }
    
    private void cambiarJugador() {
        jugadorActual = (jugadorActual == 'O') ? 'X' : 'O';
    }
    
    private void displayBoard() {
        System.out.println("-------------");
        for (int i = 0; i < 3; i++) {
            System.out.print("| ");
            for (int j = 0; j < 3; j++) {
                System.out.print(tablero[i][j] + " | ");
            }
            System.out.println();
            System.out.println("-------------");
        }
    }
    
    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (tablero[i][0] == jugadorActual && tablero[i][1] == jugadorActual && tablero[i][2] == jugadorActual) {
                ganador = Character.toString(jugadorActual);
                gameEng.stop();
                return true;
            }
            if (tablero[0][i] == jugadorActual && tablero[1][i] == jugadorActual && tablero[2][i] == jugadorActual) {
                ganador = Character.toString(jugadorActual);
                gameEng.stop();

                return true;
            }
        }

        if (tablero[0][0] == jugadorActual && tablero[1][1] == jugadorActual && tablero[2][2] == jugadorActual) {
            ganador = Character.toString(jugadorActual);
            gameEng.stop();

            return true;
        }   
        if (tablero[0][2] == jugadorActual && tablero[1][1] == jugadorActual && tablero[2][0] == jugadorActual) {
                ganador = Character.toString(jugadorActual);
                gameEng.stop();

            return true;
        }

        return false;
    }

    private boolean checkTie() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tablero[i][j] == ' ') {
                    return false;
                }
            }
        }
        ganador = "empate";
        gameEng.stop();

        return true;
    }
    
    private void initSounds(Vector3f position, Camera camera) {
        soundMgr = new SoundManager();
        soundMgr.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        soundMgr.setListener(new SoundListener(camera.getPosition()));

        SoundBuffer buffer = new SoundBuffer("resources/sounds/creak1.ogg");
        soundMgr.addSoundBuffer(buffer);
        playerSoundSource = new SoundSource(false, false);
        playerSoundSource.setPosition(position);
        playerSoundSource.setBuffer(buffer.getBufferId());
        soundMgr.addSoundSource("CREAK", playerSoundSource);

        buffer = new SoundBuffer("resources/sounds/woo_scary.ogg");
        soundMgr.addSoundBuffer(buffer);
        SoundSource source = new SoundSource(true, true);
        source.setBuffer(buffer.getBufferId());
        soundMgr.addSoundSource("MUSIC", source);
        source.play();
    }
}