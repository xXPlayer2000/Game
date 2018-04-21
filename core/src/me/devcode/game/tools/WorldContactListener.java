package me.devcode.game.tools;


import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import me.devcode.game.Game;
import me.devcode.game.sprites.Mario;
import me.devcode.game.sprites.enemies.Enemy;
import me.devcode.game.sprites.items.Item;
import me.devcode.game.sprites.other.FireBall;
import me.devcode.game.sprites.tileobjects.InteractiveTileObject;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef){
            case Game.MARIO_HEAD_BIT | Game.BRICK_BIT:
            case Game.MARIO_HEAD_BIT | Game.COIN_BIT:
                if(fixA.getFilterData().categoryBits == Game.MARIO_HEAD_BIT)
                    ((InteractiveTileObject) fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
                else
                    ((InteractiveTileObject) fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
                break;
            case Game.ENEMY_HEAD_BIT | Game.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == Game.ENEMY_HEAD_BIT)
                    ((Enemy)fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
                else
                    ((Enemy)fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
                break;
            case Game.ENEMY_BIT | Game.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == Game.ENEMY_BIT)
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                else
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                break;
            case Game.MARIO_BIT | Game.ENEMY_BIT:
                if(fixA.getFilterData().categoryBits == Game.MARIO_BIT)
                    ((Mario) fixA.getUserData()).hit((Enemy)fixB.getUserData());
                else
                    ((Mario) fixB.getUserData()).hit((Enemy)fixA.getUserData());
                break;
            case Game.ENEMY_BIT | Game.ENEMY_BIT:
                ((Enemy)fixA.getUserData()).hitByEnemy((Enemy)fixB.getUserData());
                ((Enemy)fixB.getUserData()).hitByEnemy((Enemy)fixA.getUserData());
                break;
            case Game.ITEM_BIT | Game.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == Game.ITEM_BIT)
                    ((Item)fixA.getUserData()).reverseVelocity(true, false);
                else
                    ((Item)fixB.getUserData()).reverseVelocity(true, false);
                break;
            case Game.ITEM_BIT | Game.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == Game.ITEM_BIT)
                    ((Item)fixA.getUserData()).use((Mario) fixB.getUserData());
                else
                    ((Item)fixB.getUserData()).use((Mario) fixA.getUserData());
                break;
            case Game.FIREBALL_BIT | Game.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == Game.FIREBALL_BIT)
                    ((FireBall)fixA.getUserData()).setToDestroy();
                else
                    ((FireBall)fixB.getUserData()).setToDestroy();
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
