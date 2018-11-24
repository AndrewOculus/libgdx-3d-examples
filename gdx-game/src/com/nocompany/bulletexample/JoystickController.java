package com.nocompany.bulletexample;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;

public class JoystickController implements Disposable
{
	private ShapeRenderer shapeRenderer;
	private float x, y, size, x_, y_;
	
	public JoystickController(float x , float y, float size){
		shapeRenderer = new ShapeRenderer();
		this.x = x;
		this.y = y;
		this.size = size;
	}
	
	public void update(float dt){
		shapeRenderer.setColor(Color.GRAY);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.circle(x, y , size);
		x_ =x;
		y_ =y;
		if(Gdx.input.isTouched()){
			shapeRenderer.setColor(Color.WHITE);
			x_ = Gdx.input.getX() ;
			y_ = Gdx.graphics.getHeight() - Gdx.input.getY();
			shapeRenderer.circle(x_, y_ , size/2f);
		}
		shapeRenderer.end();
	}
	
	public float getDX(){
		float dx = x - x_;
		return Math.abs(dx) > 1f? 1:dx;
	}	
	
	public float getDY(){
		float dy = y - y_;
		return Math.abs(dy) > 1f? 1:dy;
	}
	
	@Override
	public void dispose()
	{
		// TODO: Implement this method
		shapeRenderer.dispose();
	}

	
}
