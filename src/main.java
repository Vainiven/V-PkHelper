import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import simple.api.actions.SimpleItemActions;
import simple.api.actions.SimplePlayerActions;
import simple.api.filters.SimplePrayers.Prayers;
import simple.api.script.Category;
import simple.api.script.Script;
import simple.api.script.ScriptManifest;
import simple.api.wrappers.SimplePlayer;

@ScriptManifest(author = "Vainiven", category = Category.OTHER, description = "Pk Helper", name = "V-PkHelper", version = "1.0", discord = "Vainiven#6986", servers = {
		"SpawnPk" })

public class main extends Script implements KeyListener {

	// GOODLUCK ON PKING :):)

	final String[] meleeSet = { "Blood whip", "Fighter torso", "Torag's platelegs", "Dragonfire shield" };
	final String[] mageSet = { "Ahrim's robetop", "Ahrim's robeskirt", "Ahrim's staff", "Dragonfire shield" };
	final String[] rangeSet = { "Chaotic crossbow", "Karil's leatherskirt", "Karil's leathertop", "Dragonfire shield" };

	final String[] specSet = { "Dragon dagger", "Fighter torso", "Torag's platelegs", "Dragonfire shield" };

	@Override
	public boolean onExecute() {
		return true;
	}

	@Override
	public void onProcess() {
		if (ctx.prayers.prayerPercent() < 40) {
			if (!ctx.inventory.populate().filterContains("Super Restore").isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
		} else if (ctx.combat.healthPercent() < 60 && !ctx.inventory.populate().filter("Rocktail").isEmpty()) {
			ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			ctx.sleep(100);
			if (!ctx.inventory.populate().filterContains("Cooked Karambwan").isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
		}
	}

	@Override
	public void onTerminate() {
	}

	public void equipGear(String[] set) {
		for (final String p : set) {
			if (!ctx.inventory.populate().filterContains(p).isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.WEAR);
			}
		}
	}

	public void enablePrayers(Prayers offensive) {
		if (!ctx.prayers.prayerActive(Prayers.PROTECT_FROM_MISSILES)) {
			ctx.prayers.prayer(Prayers.PROTECT_FROM_MISSILES);
		}
		if (!ctx.prayers.prayerActive(Prayers.PROTECT_ITEM)) {
			ctx.prayers.prayer(Prayers.PROTECT_ITEM);
		}
		if (!ctx.prayers.prayerActive(offensive)) {
			ctx.prayers.prayer(offensive);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		final SimplePlayer interacting = (SimplePlayer) ctx.players.getLocal().getInteracting();
		final int key = e.getKeyCode();
		switch (key) {
		case KeyEvent.VK_1: {
			System.out.println("We have detected 1. Melee Set.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			equipGear(meleeSet);
			enablePrayers(Prayers.PIETY);
			break;
		}
		case KeyEvent.VK_2: {
			System.out.println("We have detected 2. Range Set.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			equipGear(rangeSet);
			enablePrayers(Prayers.RIGOUR);
			break;
		}

//		// CAST SNARE TO OPPONENT
//		case KeyEvent.VK_W: {
//			System.out.println("We have detected W. Casting Snare.");
//			equipGear(mageSet);
//			enablePrayers(Prayers.MYSTIC_MIGHT);
//			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
//			ctx.magic.selectSpell(1592);
//			ctx.sleep(300);
//			if (interacting != null) {
//				interacting.interact(365);
//
//			}
//			break;
//		}

//		// CAST TELEBLOCK ON OPPONENT
//		case KeyEvent.VK_Q: {
//			System.out.println("We have detected Q. Casting Teleblock.");
//			equipGear(mageSet);
//			enablePrayers(Prayers.MYSTIC_MIGHT);
//			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
//			ctx.magic.selectSpell(12445);
//			ctx.sleep(300);
//			if (interacting != null) {
//				interacting.interact(365);
//
//			}
//			break;
//		}

		// SPECIAL ATTACK
		case KeyEvent.VK_3: {
			System.out.println("We have detected 3. Casting Spec.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			equipGear(specSet);
			enablePrayers(Prayers.PIETY);
			if (!ctx.combat.specialAttack()) {
				ctx.combat.toggleSpecialAttack(true);
				if (interacting != null) {
					interacting.interact(SimplePlayerActions.ATTACK);

				}
			}
			break;
		}

		// NURSE
		case KeyEvent.VK_EQUALS: {
			System.out.println("We have detected +. Nursing.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			ctx.keyboard.sendKeys("::nurse");
			ctx.keyboard.pressKey(KeyEvent.VK_ENTER);
			break;
		}

		default:
			break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
