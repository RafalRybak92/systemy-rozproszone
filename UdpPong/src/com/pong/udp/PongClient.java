package com.pong.udp;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Roophie on 2014-11-03.
 */
public class PongClient extends Thread implements ApplicationListener{
    //Globals
    protected static final int SERVER_SOCKET = 2222;
    protected static final int MAX_BUFF = 1024;
    public static final String VERSION = "0.00000.0000000001";
    private static final float BALL_SPEED = 350f;
    private static final float BALL_ANGLE = 45f;
    private static final float PADDLE_SPEED = 350f;
    private Rectangle field = new Rectangle();
    private Ball ball = new Ball();
    private Paddle paddle1 = new Paddle();
    private Paddle paddle2 = new Paddle();
    private ShapeRenderer shapeRender;
    private float fieldTop, fieldDown, fieldLeft, fieldRight;
    private stateOfGame currentState = stateOfGame.NEW;
    private int score1 = 0, score2 = 0;
    private String yourScore1;
    BitmapFont yourBitmapFont1;
    SpriteBatch spriteBatch;
    private byte[] reciveBuffer = new byte[MAX_BUFF];
    private byte[] sendBuffer = new byte[MAX_BUFF];
    private DatagramPacket recivePacket;
    private DatagramPacket sendPacket;
    private DatagramSocket clientSocket;
    private InetAddress inetAddress;
    private String UP, DOWN, NEW, END, RESET;



     //Networking methods
    private void recive() throws IOException {
        recivePacket = new DatagramPacket(reciveBuffer, reciveBuffer.length);
        clientSocket.receive(recivePacket);
        String receivedMessage;
        receivedMessage = new String(recivePacket.getData(), recivePacket.getOffset(), recivePacket.getLength());

        if(receivedMessage.equals("UP")){
                paddle1.integrate(60f);
                paddle1.updatebouds();
                paddle1.setVelocity(0f, PADDLE_SPEED);
                update(60f);
                if(paddle1.getTop() > fieldTop){
                    paddle1.move(0f, fieldTop-paddle1.getHeight());
                    paddle1.setVelocity(0f, 0f);
                    render();
                }
                render();
        }else if(receivedMessage.equals("DOWN")){
            boolean moveDown;
            moveDown = true;
            paddle1.integrate(60f);
            paddle1.updatebouds();

            if(moveDown){
                paddle1.setVelocity(0f, -PADDLE_SPEED);
            }

            else{
                paddle1.setVelocity(0f, 0f);
            }

            if(paddle1.getBottom() < fieldDown){
                paddle1.move(0f, fieldDown);
                paddle1.setVelocity(0f, 0f);
            }
            render();
         }
    }

    public void send(String MESSAGE) throws IOException {
        sendBuffer = MESSAGE.getBytes();
        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress, SERVER_SOCKET);
        clientSocket.send(sendPacket);
    }




    private enum stateOfGame{
        NEW,
        RESET,
        PLAY;
    }


    public PongClient(int SOCKET) throws SocketException {
        clientSocket = new DatagramSocket(SOCKET);
    }


    @Override
    public void create() {
        field.set(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        fieldLeft = field.x;
        fieldRight = field.x + field.width;
        fieldDown = field.y;
        fieldTop = field.y + field.height;
        score1 = 0 ;
        score2 = 0;
        yourScore1 = "P1: " + score1 + " P2: " + score2;
        yourBitmapFont1 = new BitmapFont(Gdx.files.internal("C:\\Users\\Roophie\\IdeaProjects\\Pong\\src\\com\\pong\\Calibri.fnt"),Gdx.files.internal("C:\\Users\\Roophie\\IdeaProjects\\Pong\\src\\com\\pong\\Calibri.png"), false);
        spriteBatch = new SpriteBatch();
        shapeRender = new ShapeRenderer();
        newGame();
        reset();
    }

    private void reset() {
        ball.move(field.x + (field.width - ball.getWidth())/2, field.y + (field.height - ball.getHeight())/2);
        Vector2 velocity = ball.getVelocity();
        velocity.set(BALL_SPEED,0f);
        velocity.setAngle(360f - BALL_ANGLE);
        ball.setVelocity(velocity);

        //reset paddle
        paddle1.move(field.x , field.y + (field.height - paddle1.getHeight())/2);
        paddle2.move(field.x + field.width - paddle2.getWidth(), field.y +(field.height -paddle2.getHeight())/2);

    }

    @Override
    public void resize(int width, int height) {

    }

    public void render(){

        float dt = Gdx.graphics.getRawDeltaTime();
        update(dt);
        draw(dt);
        draw(dt);

        spriteBatch.begin();

        yourBitmapFont1.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        yourBitmapFont1.setScale(1.0f, 1.0f);

        yourBitmapFont1.draw(spriteBatch, yourScore1, 25, 160);
        spriteBatch.end();


    }
    public void run() {
        boolean firstConnect = true;
        while (true) {
            try {
                if (firstConnect) {
                    inetAddress = InetAddress.getLocalHost();
                    send("QUdhgavvsjjhjkhk");
                    firstConnect = false;
                }
                else{
                    if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                        UP = "UP";
                        sendBuffer = UP.getBytes();
                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress.getLocalHost(), SERVER_SOCKET);
                        clientSocket.send(sendPacket);
                        System.err.println("Wysyłamn" + sendPacket.getData());
                        render();
                    } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                        DOWN = "DOWN";
                        sendBuffer = DOWN.getBytes();
                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress.getLocalHost(), SERVER_SOCKET);
                        clientSocket.send(sendPacket);

                        render();
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    private void update(float dt) {
        switch(currentState){
            case NEW:
                newGame();
                reset();
                currentState = stateOfGame.PLAY;
                break;
            case RESET:
                reset();
                currentState = stateOfGame.PLAY;
                break;
            case PLAY:
                handlerInput();
                updateBall(dt);
                updatePaddle1(dt);
                updatePaddle2(dt);
                break;
        }
    }

    private void updatePaddle2(float dt) {
        boolean moveDown = false, moveUp = false;
        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            moveUp = true;
            moveDown = false;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            moveDown = true;
            moveUp= false;

        }

        paddle2.integrate(dt);
        paddle2.updatebouds();

        if(moveUp){
            paddle2.setVelocity(0f, PADDLE_SPEED);
        }
        else if (moveDown){
            paddle2.setVelocity(0f, -PADDLE_SPEED);
        }
        else{
            paddle2.setVelocity(0f, 0f);
        }



        if(paddle2.getTop() > fieldTop){
            paddle2.move(paddle2.getX(), fieldTop-paddle2.getHeight());
            paddle2.setVelocity(0f, 0f);
        }
        if(paddle2.getBottom() < fieldDown){
            paddle2.move(paddle2.getX(), fieldDown);
            paddle2.setVelocity(0f, 0f);
        }




    }

    private void updatePaddle1(float dt) {
        boolean moveDown = false, moveUp = false;
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            moveUp = true;
            moveDown = false;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            moveDown = true;
            moveUp= false;

        }

        paddle1.integrate(dt);
        paddle1.updatebouds();

        if(moveUp){
            paddle1.setVelocity(0f, PADDLE_SPEED);
        }
        else if (moveDown){
            paddle1.setVelocity(0f, -PADDLE_SPEED);
        }
        else{
            paddle1.setVelocity(0f, 0f);
        }



        if(paddle1.getTop() > fieldTop){
            paddle1.move(0f, fieldTop-paddle1.getHeight());
            paddle1.setVelocity(0f, 0f);
        }
        if(paddle1.getBottom() < fieldDown){
            paddle1.move(0f, fieldDown);
            paddle1.setVelocity(0f, 0f);
        }



    }

    private void updateBall(float dt) {
        ball.integrate(dt);
        ball.updatebouds();

        if(ball.getLeft() < fieldLeft){
            score2++;
            yourScore1 = "P1: " + score1 + " P2: " + score2;
            ball.move(fieldLeft, ball.getY());
            ball.reflect(true, false);
        }
        if(ball.getRight() > fieldRight){
            score1++;
            yourScore1 = "P1: " + score1 + " P2: " + score2;
            ball.move(fieldRight - ball.getWidth(), ball.getY());
            ball.reflect(true, false);
        }
        if(ball.getTop() > fieldTop){
            ball.move(ball.getX(), fieldTop- ball.getHeight());
            ball.reflect(false, true);
        }
        if(ball.getBottom() < fieldDown){
            ball.move(ball.getX(), fieldDown );
            ball.reflect(false, true);
        }

        if(ball.getBounds().overlaps(paddle1.getBounds())){
            if(ball.getLeft() < paddle1.getRight() && ball.getRight() > paddle1.getRight()) {
                ball.move(paddle1.getRight(), ball.getY());
                ball.reflect(true, false);
            }
        }
        else if(ball.getBounds().overlaps(paddle2.getBounds())){
            if(ball.getRight() > paddle2.getLeft() && ball.getLeft() < paddle2.getRight()) {
                ball.move(paddle2.getLeft() - ball.getWidth(), ball.getY());
                ball.reflect(true, false);
            }
        }

    }

    private void handlerInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.N)){
            currentState = stateOfGame.NEW;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }
    }

    private void newGame() {
        score1 = 0;
        score2 = 0;
    }

    private void draw(float dt) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);




        shapeRender.begin(ShapeRenderer.ShapeType.Filled);
        drawBall(dt);
        drawPaddles(dt);
        shapeRender.end();

    }

    private void drawPaddles(float dt) {
        shapeRender.rect(paddle1.getX(), paddle1.getY(), paddle1.getWidth(), paddle1.getHeight());
        shapeRender.rect(paddle2.getX(), paddle2.getY(), paddle2.getWidth(), paddle2.getHeight());
    }

    private void drawBall(float dt) {
        shapeRender.rect(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
    }

    @Override
    public void pause() {

    }

    @Override
    public void dispose() {

    }
    public static void main(String[] args) throws IOException {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.resizable = false;
        cfg.width = 1280;
        cfg.height = 720;
        cfg.title = "Pong" + PongClient.VERSION;
        PongClient pC = new PongClient(1264);
        new LwjglApplication(pC, cfg);
        pC.start();

        while(true){
            pC.recive();
            pC.render();
        }


    }
}
