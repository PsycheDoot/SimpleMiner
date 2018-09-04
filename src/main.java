import java.util.Arrays;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.script.Category;

@ScriptManifest(author = "Sean Rice", name = "Simple Miner", version = 1.0, description = "Mine tin and copper automagically.", category = Category.MINING)

public class main extends AbstractScript {
	
	// Declarations
	private enum State {
		waiting,
		searching,
		walking,
		mining,
		noOre,
		invFull,
		none,
		stop
	};
	private enum OreType {
		copper,
		tin,
		none
	}
	
	private short[] Copper = {4645};
	private short[] Tin = {53};
	
	private State currentState = State.waiting;
	private GameObject target = null;
	private OreType targetOre = OreType.copper;
	
	// Global script methods
	public void onStart() {
		log("Running Simple Miner script.");
	}

	public void onExit() {
		log("Stopping Simple Miner script.");
	}
	
	@Override
	public int onLoop() {
		log("STATE: " + getState().toString().toUpperCase());

		switch (currentState) {
			case waiting:
				if (hasEnoughOre()) {
					log("Inventory is full.");
					currentState = State.stop;
				} else {					
					currentState = State.searching;
				}
				break;
			case searching:
				if (getNearestOre(targetOre) != null) {
					log("Ore located.");
					target = getNearestOre(targetOre);
					currentState = State.walking;
				} else {					
					currentState = State.stop;
				}
				break;
			case walking:
				if (target != null && target.distance() > 4) {
					getWalking().walk(target.getTile());
					log("Moving to ore.");
				}
				if (target != null && target.distance() <= 4) {
					if (target.interact("Mine")) {
						currentState = State.mining;
					} else {
						currentState = State.waiting;
					}
				}
				break;
			case mining:
				log(Arrays.toString((target.getModelColors())));
				log("Mining ore.");
				if (getLocalPlayer().getAnimation() == -1 && target.interact("Mine")) {
					while (getLocalPlayer().getAnimation() != -1) {
						sleep(1000);
					}
				}
				log("Mining finished.");
				currentState = State.waiting;
				break;
			case stop:
				stop();
				break;
			default:
				break;
		}
		
		return Calculations.random(500, 5000);
	}
	
	// Local script methods
	public State getState() {
		return currentState;
	}
	
	private GameObject getNearestOre(OreType type) {
		
		GameObject ore = getGameObjects().closest(gameObject -> gameObject != null
													&& gameObject.getName().equals("Rocks")
													&& gameObject.getModelColors() != null
													&& gameObject.getModelColors()[0] == getOreColor(type)[0]
													&& gameObject.hasAction("Mine"));
		
		//log(ore.toString());
		return ore;
	}
	
	private boolean hasEnoughOre() {
		return getInventory().isFull();
	}
	
	private short[] getOreColor(OreType type) {
		switch(type) {
			case copper:
				return Copper;
			case tin:
				return Tin;
			default:
				return Copper;
		}
	}
}
