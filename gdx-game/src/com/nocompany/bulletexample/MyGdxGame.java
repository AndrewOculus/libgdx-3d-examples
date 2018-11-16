package com.nocompany.bulletexample;

import java.util.List;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import java.util.*;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.math.*;

public class MyGdxGame extends ApplicationAdapter {

	GameRenderer gameRenderer;
	List<ModelInstance> models;
	PerspectiveCamera camera;
	WorldPhysics worldPhysics;

	@Override
	public void create() {
		super.create();

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1;
		camera.far = 300;
		camera.position.set(15, 15, 15);
		camera.lookAt(0,0,0);
		camera.update();

		worldPhysics = new WorldPhysics();

		models = Model3dLoader.load();
		gameRenderer = new GameRenderer(camera, worldPhysics);
			
	}

	@Override
	public void render() {
		super.render();
		float dt = Gdx.graphics.getDeltaTime();
		gameRenderer.render(dt, models);
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

}

class GameRenderer implements Disposable
{
	private PerspectiveCamera camera;
	private ModelBatch batch;
	private Environment environment;
	private DebugDrawer debugDrawer;
	private WorldPhysics worldPhysics;

	public GameRenderer(PerspectiveCamera camera, WorldPhysics worldPhysics) {
		this.camera = camera;
		this.batch = new ModelBatch();

		environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		this.debugDrawer = new DebugDrawer();
		this.debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);

		this.worldPhysics = worldPhysics;
	}

	public void render(float dt ,List<ModelInstance> models) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin(camera);
		/*
		for( ModelInstance md : models){
			batch.render(md, environment);
		}
		*/
		worldPhysics.render(dt, batch, environment);
		batch.end();
		
		
		debugDrawer.begin(camera);
		worldPhysics.debugDraw();
		debugDrawer.end();
		
		camera.update();
		

	}

	@Override
	public void dispose()
	{
		batch.dispose();
	}
}

class WorldPhysics{

	class MotionState extends btMotionState {
	    Matrix4 transform;
	    @Override
	    public void getWorldTransform (Matrix4 worldTrans) {
	        worldTrans.set(transform);
	    }
	    @Override
	    public void setWorldTransform (Matrix4 worldTrans) {
	        transform.set(worldTrans);
	    }
	}

	class MyContactListener extends ContactListener {
        @Override
        public boolean onContactAdded (int userValue0, int partId0, int index0, int userValue1, int partId1, int index1) {
            if (userValue0 != 0)
                ((ColorAttribute)instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
            if (userValue1 != 0)
                ((ColorAttribute)instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
            return true;
        }
    }
	
	private btDynamicsWorld dynamicsWorld;
	private btCollisionConfiguration collisionConfiguration;
	private btDispatcher dispatcher;
	private btBroadphaseInterface broadphaseInterface;
	private btConstraintSolver constraintSolver;
	private MyContactListener contactListener;
	
	private Array<GameObject> instances;
	private ArrayMap<String, GameObject.Constructor> constructors;

	final static short GROUND_FLAG = 1 << 8;
	final static short OBJECT_FLAG = 1 << 9;
	final static short ALL_FLAG = -1;
	
	float spawnTimer= 0;
	
	
	public WorldPhysics() {
		Bullet.init();
		
		collisionConfiguration = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfiguration);
		broadphaseInterface = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphaseInterface, constraintSolver, collisionConfiguration);
		dynamicsWorld.setGravity(new Vector3(0, -10f, 0));
		contactListener = new MyContactListener();
		
		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		mb.node().id = "ground";
		mb.part("ground", GL20.GL_TRIANGLES, VertexAttributes. Usage.Position |VertexAttributes. Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
			.box(5f, 1f, 5f);
		mb.node().id = "sphere";
		mb.part("sphere", GL20.GL_TRIANGLES,VertexAttributes. Usage.Position |VertexAttributes. Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
			.sphere(1f, 1f, 1f, 10, 10);
		mb.node().id = "box";
		mb.part("box", GL20.GL_TRIANGLES,VertexAttributes. Usage.Position |VertexAttributes. Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
			.box(1f, 1f, 1f);
		mb.node().id = "cone";
		mb.part("cone", GL20.GL_TRIANGLES,VertexAttributes. Usage.Position |VertexAttributes. Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
			.cone(1f, 2f, 1f, 10);
		mb.node().id = "capsule";
		mb.part("capsule", GL20.GL_TRIANGLES,VertexAttributes. Usage.Position |VertexAttributes. Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.CYAN)))
			.capsule(0.5f, 2f, 10);
		mb.node().id = "cylinder";
		mb.part("cylinder", GL20.GL_TRIANGLES,VertexAttributes. Usage.Position | VertexAttributes.Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 2f, 1f, 10);
		Model model = mb.end();
		
		constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        constructors.put("ground", new GameObject.Constructor(model, "ground", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
        constructors.put("sphere", new GameObject.Constructor(model, "sphere", new btSphereShape(0.5f), 1f));
        constructors.put("box", new GameObject.Constructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f)), 1f));
        constructors.put("cone", new GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f), 1f));
        constructors.put("capsule", new GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f), 1f));
        constructors.put("cylinder", new GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f)), 1f));
		
		
        instances = new Array<GameObject>();
        GameObject object = constructors.get("ground").construct();
        instances.add(object);
        dynamicsWorld.addRigidBody(object.body, GROUND_FLAG, ALL_FLAG);
		
		spawn();
	}
	
	public void spawn () {
        GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
        obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
        obj.body.setWorldTransform(obj.transform);
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        instances.add(obj);
        dynamicsWorld.addRigidBody(obj.body, OBJECT_FLAG, GROUND_FLAG);
    }
	
	public btRigidBody AddRigidbody(Array<Node> nodes , Matrix4 transform) {

		MotionState state = new MotionState();
		state.setWorldTransform(transform);

		btCollisionShape shape = Bullet.obtainStaticNodeShape(nodes);

		btRigidBody rigidBody = new btRigidBody(0,state,shape);
		dynamicsWorld.addRigidBody(rigidBody);

		return rigidBody;
	}

	public Array<GameObject> getGameobjects(){
		return instances;
	}
	
	public void render(float dt , ModelBatch batch , Environment environment){
		
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        dynamicsWorld.stepSimulation(delta, 5, 1f/60f);
		for (GameObject obj : instances)
			obj.body.getWorldTransform(obj.transform);
		
		batch.render(instances,environment);
		
		if ((spawnTimer -= delta) < 0) {
			spawn();
			spawnTimer = 1.5f;
		}
		
	}
	
	public void debugDraw() {
		dynamicsWorld.debugDrawWorld();
	}
	
}

class Model3dLoader{
	public static List<ModelInstance> load(){
		List<ModelInstance> lst = new ArrayList<ModelInstance>();
		
		ModelBuilder modelBuilder = new ModelBuilder();
		
        Model model = modelBuilder.createBox(5f, 5f, 5f, 
		   new Material(ColorAttribute.createDiffuse(Color.GREEN)),
		   VertexAttributes. Usage.Position | VertexAttributes. Usage.Normal);
		   
		   
        lst.add(new ModelInstance(model));
		
		return lst;
	}
}


class GameObject extends ModelInstance implements Disposable {
	
        public final btRigidBody body;
        public boolean moving;

        public GameObject (Model model, String node, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
            super(model, node);
            body = new btRigidBody(constructionInfo);
        }

        @Override
        public void dispose () {
            body.dispose();
        }

        static class Constructor implements Disposable {
            public final Model model;
            public final String node;
            public final btCollisionShape shape;
            public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
            private static Vector3 localInertia = new Vector3();

            public Constructor (Model model, String node, btCollisionShape shape, float mass) {
                this.model = model;
                this.node = node;
                this.shape = shape;
                if (mass > 0f)
                    shape.calculateLocalInertia(mass, localInertia);
                else
                    localInertia.set(0, 0, 0);
                this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
            }

            public GameObject construct () {
                return new GameObject(model, node, constructionInfo);
            }

            @Override
            public void dispose () {
                shape.dispose();
                constructionInfo.dispose();
            }
        }
}
