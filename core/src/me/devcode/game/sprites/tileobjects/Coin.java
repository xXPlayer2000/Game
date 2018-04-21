package me.devcode.game.sprites.tileobjects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;

import me.devcode.game.Game;
import me.devcode.game.scenes.Hud;
import me.devcode.game.screens.PlayScreen;
import me.devcode.game.sprites.Mario;
import me.devcode.game.sprites.items.ItemDef;
import me.devcode.game.sprites.items.Mushroom;

public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28;

    public Coin(PlayScreen screen, MapObject object){
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(Game.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if(getCell().getTile().getId() == BLANK_COIN)
            Game.manager.get("audio/sounds/bump.wav", Sound.class).play();
        else {
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / Game.PPM),
                        Mushroom.class));
                Game.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else
                Game.manager.get("audio/sounds/coin.wav", Sound.class).play();
            getCell().setTile(tileSet.getTile(BLANK_COIN));
            Hud.addScore(100);
        }
    }
}
