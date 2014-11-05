package com.pong.udp;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Roophie on 2014-11-03.
 */
public class Ball extends GameObject{

    protected Ball() {
        super(32, 32);
    }

    public void reflect(boolean x, boolean y){
        Vector2 velocity = getVelocity();
        if(x)
            velocity.x *= -1;

        if(y)
            velocity.y *= -1;

        setVelocity(velocity);
    }
}
