package com.nocompany.bulletexample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;


public class MyGdxGame extends ApplicationAdapter {

	GameRenderer gameRenderer;
	Model model;
	PerspectiveCamera camera;
	WorldPhysics worldPhysics;
	float spawnTime = 3f;
	GameObject hero;
	Vector3 heroImpuls , cameraDir;
	JoystickController joystick;
	
	final static short GROUND_FLAG = 1 << 8;
	final static short OBJECT_FLAG = 1 << 9;
	final static short ALL_FLAG = -1;

	@Override
	public void create() {
		super.create();

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1;
		camera.far = 300;
		camera.position.set(15, 15, 15);
		camera.lookAt(0,0,0);
		camera.update();
		
		model = Model3dLoader.load();
		worldPhysics = new WorldPhysics(model);
		gameRenderer = new GameRenderer(camera, worldPhysics);
			
		worldPhysics.Instantiate(0, 0, 0 , 5, 0,0,5, GROUND_FLAG, ALL_FLAG);
		worldPhysics.Instantiate(0, 0, 0 , 5, 0,0,10, GROUND_FLAG, ALL_FLAG);
		worldPhysics.Instantiate(0, 0, 0 , -5, 0,0,15, GROUND_FLAG, ALL_FLAG);
		worldPhysics.Instantiate(0, 0, 0 , 0, 0,0,20, GROUND_FLAG, ALL_FLAG);
		
		joystick = new JoystickController(100, 100, 50);
		
		hero = worldPhysics.Instantiate(1, 0, 0, 0, 0, 5, 0, OBJECT_FLAG, GROUND_FLAG);
		heroImpuls = new Vector3(0,0,0);
		cameraDir = new Vector3(0,0,0);
	}

	@Override
	public void render() {
		super.render();
		float dt = Gdx.graphics.getDeltaTime();
		gameRenderer.render(dt);
		
		camera.rotateAround(Vector3.Zero, Vector3.Y, -Gdx.input.getDeltaX()/5f);
		
		cameraDir.set(camera.direction);
		Vector3 left = cameraDir.crs(Vector3.Y);
		Vector3 forward = left.rotate(Vector3.Y ,90);
		heroImpuls.set(forward.x * joystick.getDY()/100f,forward.y * joystick.getDY()/100f, forward.z * joystick.getDY()/100f);
		hero.body.applyCentralImpulse(heroImpuls);
		hero.body.activate();
		joystick.update(dt);
		/*
		if((spawnTime-=dt) < 0)
		{
			worldPhysics.Instantiate(3,MathUtils.random(0,360),0,0,MathUtils.random(-2.5f,2.5f),9,MathUtils.random(-2.5f,2.5f));
			spawnTime = 3;
		}*/
	}

	@Override
	public void dispose()
	{
		super.dispose();
		gameRenderer.dispose();
		worldPhysics.dispose();
	}

}


