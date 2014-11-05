package com.pong.udp;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Roophie on 2014-11-03.
 */
public abstract class GameObject {
    private Vector2 position = new Vector2();
    private Vector2 velocity = new Vector2();
    private Rectangle bounds = new Rectangle();


    //Constructor
    protected GameObject(int width, int height){
        bounds.setWidth(width);
        bounds.setHeight(height);

    }

    public Rectangle getBounds(){
        updatebouds();
        return bounds;
    }

    public void updatebouds(){
        bounds.set(position.x, position.y, bounds.width, bounds.height);
    }


    //Getters/setters
    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public void setVelocity(float x, float y){
        velocity.set(x, y);
    }

    public float getHeight(){
        return bounds.height;
    }

    public float getWidth(){
        return bounds.width;
    }

    public float getBottom(){
        return bounds.y;
    }

    public float getLeft(){
        return bounds.x;
    }

    public float getRight(){
        return bounds.x + bounds.width;
    }

    public float getTop(){
        return bounds.y + bounds.height;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getVelocityX(){
        return velocity.x;
    }

    public float getVelocityY(){
        return velocity.y;
    }

    public float getX(){
        return position.x;
    }

    public float getY(){
        return position.y;
    }

    public void move(float x, float y){
        position.set(x, y);
    }

    public void translate(float x, float y){
        position.add(x, y);
    }

    public void integrate(float dt){
        position.add(velocity.x * dt, velocity.y * dt);
    }
}
