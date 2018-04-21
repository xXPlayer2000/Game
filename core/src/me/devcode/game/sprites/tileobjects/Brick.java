package me.devcode.game.sprites.tileobjects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;

import me.devcode.game.Game;
import me.devcode.game.scenes.Hud;
import me.devcode.game.screens.PlayScreen;
import me.devcode.game.sprites.Mario;

public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen screen, MapObject object){
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(Game.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if(mario.isBig()) {
            setCategoryFilter(Game.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(200);
            Game.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
        Game.manager.get("audio/sounds/bump.wav", Sound.class).play();
    }

}
