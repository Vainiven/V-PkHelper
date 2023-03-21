import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import simple.api.HeadIcon;
import simple.api.actions.SimpleItemActions;
import simple.api.actions.SimplePlayerActions;
import simple.api.filters.SimplePrayers.Prayers;
import simple.api.script.Category;
import simple.api.script.Script;
import simple.api.script.ScriptManifest;
import simple.api.script.interfaces.SimplePaintable;
import simple.api.wrappers.SimpleActor;

@ScriptManifest(author = "Vainiven", category = Category.OTHER, description = "Pk Helper", name = "V-PkHelper", version = "1.0", discord = "Vainiven#6986", servers = {
		"SpawnPk" })

public class main extends Script implements KeyListener, SimplePaintable {

	SimpleActor<?> target;

	final String[] meleeSet = { "Helm of neitiznot", "Blood slayer amulet", "Fire cape", "Blood whip",
			"Dragon defender", "Bandos tassets", "Bandos chestplate", "Blood slayer gloves", "Blood slayer boots",
			"Berserker ring (i)" };
	final String[] mageSet = { "Helm of neitiznot", "Blood slayer amulet", "Fire cape", "Trident of the seas",
			"Dragon defender", "Ahrim's robetop", "Ahrim's robeskirt", "Blood slayer gloves", "Blood slayer boots",
			"Berserker ring (i)" };
	final String[] rangeSet = { "Helm of neitiznot", "Blood slayer amulet", "Fire cape", "Rune knife(p++)",
			"Dragon defender", "Zamorak d'hide", "Zamorak chaps", "Blood slayer gloves", "Blood slayer boots",
			"Berserker ring (i)" };
	final String[] specSet = { "Helm of neitiznot", "Blood slayer amulet", "Fire cape", "Dragon claws",
			"Bandos tassets", "Bandos chestplate", "Blood slayer gloves", "Blood slayer boots", "Berserker ring (i)" };

	@Override
	public boolean onExecute() {
		return true;
	}

	@Override
	public void onProcess() {
		getTarget();
		if (!eat() && !drinkPrayerPotion()) {
			changeGearAndPrayer();
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

	private void getTarget() {
		if (ctx.players.getLocal().getInteracting() != null) {
			SimpleActor<?> g = ctx.players.getLocal().getInteracting();
			System.out.println(g);
			target = g;
		}
	}

	private SimpleActor<?> target() {
		return ctx.players.populate().filter(target).next();
	}

	private void changeGearAndPrayer() {
		if (target != null) {
			if (!ctx.players.populate().filter(target).isEmpty()) {
				if (target.getRemainingPath() > 0) {
					snare();
				}

				if (target().getHealthRatio() < 30 && ctx.combat.getSpecialAttackPercentage() > 49
						&& overheadIcon() != HeadIcon.MELEE) {
					equipGear(specSet);
					spec();
				} else {
					System.out.println(overheadIcon());
					if (overheadIcon() == HeadIcon.MAGIC) {
						equipGear(rangeSet);
						setOffensivePrayer("ranged");
					} else if (overheadIcon() == HeadIcon.RANGED && target().distanceTo(ctx.players.getLocal()) <= 2) {
						equipGear(meleeSet);
						setOffensivePrayer("melee");
					} else if ((overheadIcon() == HeadIcon.MELEE || overheadIcon() == HeadIcon.RANGED)
							&& target( .distanceTo(ctx.players.getLocal()) > 2) {
						equipGear(rangeSet);
						setOffensivePrayer("ranged");
					}
				}
			}
		}
	}

	private void snare() {
		equipGear(mageSet);
		ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
		ctx.magic.selectSpell(1592);
		ctx.sleep(300);
		ctx.players.populate().filter(target).next().interact(365);
	}

	private void setOffensivePrayer(String form) {
		if (form.equals("magic")) {
			enablePrayer(Prayers.AUGURY);
		} else if (form.equals("ranged")) {
			enablePrayer(Prayers.RIGOUR);
		} else if (form.equals("melee")) {
			enablePrayer(Prayers.PIETY);
		}
		enablePrayer(Prayers.PROTECT_ITEM);
	}

	private void enablePrayer(Prayers prayer) {
		if (ctx.prayers.prayerActive(prayer)) {
			ctx.prayers.prayer(prayer);
		}
	}

	private HeadIcon overheadIcon() {
		return ctx.players.populate().filter(target).next().getOverheadIcon();
	}

	private boolean drinkPrayerPotion() {
		if (ctx.prayers.prayerPercent() < 40) {
			if (!ctx.inventory.populate().filterContains("Super Restore").isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
			return true;
		}
		return false;
	}

	private void spec() {
		if (!ctx.combat.specialAttack()) {
			ctx.combat.toggleSpecialAttack(true);
			enablePrayer(Prayers.PIETY);
			attackTarget();
			ctx.onCondition(() -> !ctx.combat.specialAttack(), 2, 1500);
		}
	}

	private void attackTarget() {
		ctx.players.populate().filter(target).next().interact(SimplePlayerActions.ATTACK);
	}

	private boolean eat() {
		if (ctx.combat.healthPercent() < 60
				&& !ctx.inventory.populate().filter("Manta ray", "Cooked Karambwan").isEmpty()) {
			ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			ctx.sleep(100);
			if (!ctx.inventory.populate().filterContains("Cooked Karambwan").isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
			return true;
		}
		return false;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		final int key = e.getKeyCode();
		switch (key) {
		case KeyEvent.VK_1: {
			System.out.println("We have detected 1. Melee Set.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			equipGear(mageSet);
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
//		case KeyEvent.VK_3: {
//			System.out.println("We have detected 3. Casting Spec.");
//			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
//			equipGear(specSet);
//			enablePrayers(Prayers.PIETY);
//			if (!ctx.combat.specialAttack()) {
//				ctx.combat.toggleSpecialAttack(true);
//				if (interacting != null) {
//					interacting.interact(SimplePlayerActions.ATTACK);
//
//				}
//			}
//			break;
//		}
//
//		// NURSE
//		case KeyEvent.VK_EQUALS: {
//			System.out.println("We have detected +. Nursing.");
//			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
//			ctx.keyboard.sendKeys("::nurse");
//			ctx.keyboard.pressKey(KeyEvent.VK_ENTER);
//			break;
//		}
//
		default:
			break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPaint(Graphics2D g) {
		if (target != null) {
			g.drawString(target.toString(), 100, 100);
		}

	}

}
