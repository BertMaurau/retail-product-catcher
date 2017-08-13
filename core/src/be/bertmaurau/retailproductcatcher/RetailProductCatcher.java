package be.bertmaurau.retailproductcatcher;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class RetailProductCatcher extends ApplicationAdapter {
    // Define the Textures
	private Texture textureProduct;
	private Texture textureBag;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle bag;

	private Array<Rectangle> products;
	private long lastProductTime;

	@Override
	public void create() {
		// load the images for the bag and the product
		textureProduct = new Texture(Gdx.files.internal("product.png"));
		textureBag = new Texture(Gdx.files.internal("bag.png"));

		// load the catch sound effect and the background music
		// soundCatch = Gdx.audio.newSound(Gdx.files.internal("catch.wav"));
		// musicBackground = Gdx.audio.newMusic(Gdx.files.internal("bg.mp3"));

		// start the playback of the background music immediately
		// musicBackground.setLooping(true);
		// musicBackground.play();

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		// create a Rectangle to logically represent the bag
		bag = new Rectangle();
		bag.x = 800 / 2 - 64 / 2; // center the bag horizontally
		bag.y = 20; // bottom left corner of the bag is 20 pixels above the bottom screen edge
		bag.width = 64;
		bag.height = 64;

		// create the products array and spawn the first product
		products = new Array<Rectangle>();
		spawnProduct();
	}

	private void spawnProduct() {
		Rectangle product = new Rectangle();
		product.x = MathUtils.random(0, 800-64);
		product.y = 480;
		product.width = 64;
		product.height = 64;
		products.add(product);
		lastProductTime = TimeUtils.nanoTime();
	}

	@Override
	public void render() {
		// Set the background color
		Gdx.gl.glClearColor(135/255f, 206/255f, 235/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bag and all products
		batch.begin();
		batch.draw(textureBag, bag.x, bag.y);
		for(Rectangle product: products) {
			batch.draw(textureProduct, product.x, product.y);
		}
		batch.end();

		// process user input
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bag.x = touchPos.x - 64 / 2;
		}
		if(Gdx.input.isKeyPressed(Keys.LEFT)) bag.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) bag.x += 200 * Gdx.graphics.getDeltaTime();

		// make sure the bag stays within the screen bounds
		if(bag.x < 0) bag.x = 0;
		if(bag.x > 800 - 64) bag.x = 800 - 64;

		// check if we need to create a new product
		if(TimeUtils.nanoTime() - lastProductTime > 1000000000) spawnProduct();

		// move the products, remove any that are beneath the bottom edge of the screen or that hit the bag
		Iterator<Rectangle> iter = products.iterator();
		while(iter.hasNext()) {
			Rectangle product = iter.next();
			product.y -= 200 * Gdx.graphics.getDeltaTime();
			if(product.y + 64 < 0) iter.remove();
			if(product.overlaps(bag)) {
				// Play sound catch.play();
				iter.remove();
			}
		}
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		textureProduct.dispose();
		textureBag.dispose();

		batch.dispose();
	}
}