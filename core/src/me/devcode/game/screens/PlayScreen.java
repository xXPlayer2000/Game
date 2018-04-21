package me.devcode.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

import me.devcode.game.Game;
import me.devcode.game.scenes.Hud;
import me.devcode.game.sprites.Mario;
import me.devcode.game.sprites.enemies.Enemy;
import me.devcode.game.sprites.enemies.Goomba;
import me.devcode.game.sprites.enemies.Turtle;
import me.devcode.game.sprites.items.Item;
import me.devcode.game.sprites.items.ItemDef;
import me.devcode.game.sprites.items.Mushroom;
import me.devcode.game.tools.B2WorldCreator;
import me.devcode.game.tools.WorldContactListener;


public class PlayScreen implements Screen {
    //Reference to our Game, used to set Screens
    private Game game;
    private TextureAtlas atlas;
    public static boolean alreadyDestroyed = false;

    //basic playscreen variables
    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private Hud hud;

    //Tiled map variables
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    //Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    private Button rightMoving, leftMoving;
    private int action = -1;
    private boolean isOver;

    //sprites
    private Mario player;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;
    private Stage stage;

    public PlayScreen(Game game){
        atlas = new TextureAtlas("Mario_and_Enemies.pack");
        stage = new Stage(new FitViewport(Game.V_WIDTH, Game.V_HEIGHT, new OrthographicCamera()));
        {
            ImageButton button;
            Texture myTexture = new Texture(Gdx.files.internal("arrow_right.png"));
            TextureRegion myTextureRegion = new TextureRegion(myTexture);
            TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
            button = new ImageButton(myTexRegionDrawable); //Set the button up
            button.setSize(50, 20);
            button.setPosition(20, 1);
            rightMoving = button;

            stage.addActor(rightMoving); //Add the button to the stage to perform rendering and take input.
            Gdx.input.setInputProcessor(stage); //Start taking input from the ui
            rightMoving.addListener(new InputListener() {
                /*@Override
                public void clicked(InputEvent event, float x, float y) {

                    update2(Gdx.graphics.getDeltaTime(),2);

                };*/
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    isOver = true;
                    action = -1;
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    action = 1;
                    isOver = false;
                    return true;
                }
            });
        }

        {
            ImageButton button;
            Texture myTexture = new Texture(Gdx.files.internal("arrow_left.png"));
            TextureRegion myTextureRegion = new TextureRegion(myTexture);
            TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);
            button = new ImageButton(myTexRegionDrawable); //Set the button up
            button.setSize(20, 20);
            button.setPosition(10, 1);
            leftMoving = button;

            stage.addActor(leftMoving); //Add the button to the stage to perform rendering and take input.
            Gdx.input.setInputProcessor(stage); //Start taking input from the ui
            leftMoving.addListener(new InputListener() {
                /*@Override
                public void clicked(InputEvent event, float x, float y) {

                    update2(Gdx.graphics.getDeltaTime(),2);

                };*/
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    isOver = true;
                    action = -1;
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    action = 2;
                    isOver = false;
                    return true;
                }
            });
        }

        this.game = game;
        //create cam used to follow mario through cam world
        gamecam = new OrthographicCamera();

        //create a FitViewport to maintain virtual aspect ratio despite screen size
        gamePort = new FillViewport(Game.V_WIDTH / Game.PPM, Game.V_HEIGHT / Game.PPM, gamecam);

        //create our game HUD for scores/timers/level info
        hud = new Hud(game.batch);

        //Load our map and setup our map renderer
        maploader = new TmxMapLoader();
        map = maploader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1  / Game.PPM);

        //initially set our gamcam to be centered correctly at the start of of map
        gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        //create our Box2D world, setting no gravity in X, -10 gravity in Y, and allow bodies to sleep
        world = new World(new Vector2(0, -10), true);
        //allows for debug lines of our box2d world.
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        //create mario in our game world
        player = new Mario(this);
        gamecam.position.x = player.b2body.getPosition().x+1.4f;
        world.setContactListener(new WorldContactListener());

        music = Game.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();
    }

    public void spawnItem(ItemDef idef){
        itemsToSpawn.add(idef);
    }


    public void handleSpawningItems(){
        if(!itemsToSpawn.isEmpty()){
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class){
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }


    public TextureAtlas getAtlas(){
        return atlas;
    }

    @Override
    public void show() {


    }

    public void handleInput(float dt){
        //control our player using immediate impulses
        if(player.currentState != Mario.State.DEAD && !isOver) {
            switch(action) {
                case 0:
                    player.jump();
                    break;
                case 1:
                    System.out.println("Zesaaqaata");
                    player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
                    break;
                case 2:
                    System.out.println("Zesta");
                    if(player.getX() < 0.3) {
                        System.out.println("Zest");
                        break;
                    }
                    player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
                    break;
                case 3:
                    player.fire();
                    break;
                default:
                    break;
            }

        }
if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
                player.jump();
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2)
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2)
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                player.fire();


    }

    public void update(float dt){
        //handle user input first
        handleInput(dt);
        handleSpawningItems();

        //takes 1 step in the physics simulation(60 times per second)
        world.step(1 / 60f, 6, 2);

        player.update(dt);
        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if(enemy.getY() <= -1) {
                if(enemy instanceof Goomba) {
                    Goomba goomba = (Goomba)enemy;
                    goomba.setSetToDestroy();
                }else if(enemy instanceof Turtle) {
                    Turtle turtle = (Turtle)enemy;
                    turtle.setSetToDestroy();
                }
            }
            if(enemy.getX() < player.getX() + 250 / Game.PPM) {
                enemy.b2body.setActive(true);
            }
        }
        if(player.getY()<=-1) {
            player.die();
        }

        for(Item item : items)
            item.update(dt);

        hud.update(dt);

        //attach our gamecam to our players.x coordinate
        if(player.currentState != Mario.State.DEAD) {
            gamecam.position.x = player.b2body.getPosition().x+1.4f;
        }

        //update our gamecam with correct coordinates after changes
        gamecam.update();
        //tell our renderer to draw only what our camera can see in our game world.
        renderer.setView(gamecam);

    }



    @Override
    public void render(float delta) {
        //separate our update logic from render
        update(delta);

        //Clear the game screen with Black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render our game map
        renderer.render();

        //renderer our Box2DDebugLines
        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for (Enemy enemy : creator.getEnemies())
            enemy.draw(game.batch);
        for (Item item : items)
            item.draw(game.batch);
        game.batch.end();

        //Set our batch to now draw what the Hud camera sees.
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()){
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
        stage.act();
        stage.draw();

    }

    public boolean gameOver(){
        if(player.currentState == Mario.State.DEAD && player.getStateTimer() > 3){
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        //updated our game viewport
        gamePort.update(width,height);

    }

    public TiledMap getMap(){
        return map;
    }
    public World getWorld(){
        return world;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //dispose of all our opened resources
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public Hud getHud(){ return hud; }
}
