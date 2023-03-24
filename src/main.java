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
import simple.api.wrappers.SimplePlayer;

@ScriptManifest(author = "Vainiven", category = Category.OTHER, description = "Pk Helper", name = "V-PkHelper", version = "1.0", discord = "Vainiven#6986", servers = {
		"SpawnPk" })

public class main extends Script implements KeyListener, SimplePaintable {

	SimpleActor<?> target;

	final String[] mageSet = { "Blood slayer helmet", "Blood slayer amulet", "Eternal bounty cape",
			"Toxic staff of the dead", "Tome of fire", "Zuriel's robe top", "Zuriel's robe bottom",
			"Blood slayer gloves", "Blood slayer boots", "Brimstone ring" };
	final String[] rangeSet = { "Blood slayer helmet", "Blood slayer amulet", "Eternal bounty cape", "Zaryte bow",
			"Morrigan's leather body", "Morrigan's leather chaps", "Blood slayer gloves", "Blood slayer boots",
			"Brimstone ring" };
	final String[] specSet = { "Armadyl godsword" };

	@Override
	public boolean onExecute() {
		return true;
	}

	@Override
	public void onProcess() {
		if (!drinkPrayerPotion()) {
			if (target == null) {
				getTarget();
			} else {
				if (target.withinRange(ctx.players.getLocal().getLocation(), 15)
						&& ctx.players.getLocal().getInteracting() != null) {
					setDefensivePrayer();
					if (!eat()) {
						changeGearAndPrayer();
					}
				} else {
					target = null;
				}
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
		if (set.equals(mageSet)) {
			ctx.menuActions.sendAction(315, 4738, 0, 19530);
		}
	}

	private void getTarget() {
		if (ctx.players.getLocal().getInteracting() != null) {
			SimpleActor<?> g = ctx.players.getLocal().getInteracting();
			target = g;
		}
	}

	private void changeGearAndPrayer() {
		if (!ctx.players.populate().filter(target).isEmpty()) {
			if (target.getRemainingPath() > 0) {
				snare();
			}
			if (overheadIcon() == HeadIcon.MAGIC || overheadIconInt().getHeadIcon() == 14) {
				equipGear(rangeSet);
				enablePrayer(Prayers.RIGOUR);
			} else if (overheadIcon() == HeadIcon.RANGED || overheadIconInt().getHeadIcon() == 15) {
				equipGear(mageSet);
				enablePrayer(Prayers.AUGURY);
			}
			attackTarget();
		}
	}

	private void snare() {
		equipGear(mageSet);
		ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
		ctx.magic.selectSpell(1592);
		ctx.players.populate().filter(target).next().interact(365);
		ctx.sleep(1300);
	}

	private HeadIcon overheadIcon() {
		return ctx.players.populate().filter(target).next().getOverheadIcon();
	}

	private SimplePlayer overheadIconInt() {
		return ctx.players.populate().filter(target).next();
	}

	private boolean drinkPrayerPotion() {
		if (ctx.prayers.prayerPercent() < 70) {
			if (!ctx.inventory.populate().filterContains("Super Restore", "Sanfew serum flask", "Super restore flask")
					.isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
			return true;
		}
		return false;
	}

	private void spec() {
		enablePrayer(Prayers.PIETY);
		if (!ctx.combat.specialAttack()) {
			ctx.combat.toggleSpecialAttack(true);
			attackTarget();
			ctx.onCondition(() -> !ctx.combat.specialAttack(), 2, 1500);
		}
	}

	private void attackTarget() {
		if (target != null && !ctx.players.getLocal().getInteracting().equals(target)) {
			ctx.players.populate().filter(target).next().interact(SimplePlayerActions.ATTACK);
		}
	}

	private boolean eat() {
		if (ctx.combat.healthPercent() < 60
				&& !ctx.inventory.populate().filter("Manta ray", "Cooked Karambwan").isEmpty()) {
			ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			ctx.sleep(100);
			if (!ctx.inventory.populate().filterContains("Cooked Karambwan").isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
			attackTarget();
			return true;
		}
		return false;
	}

	private void enablePrayer(Prayers prayer) {
		if (!ctx.prayers.prayerActive(prayer)) {
			ctx.prayers.prayer(prayer);
		}
	}

	private void setDefensivePrayer() {
		int gear[] = ctx.players.populate().filter(target).next().getEquipment();
		if (ctx.definitions.getItemDefinition(gear[3] - 512) != null) {
			String equippedWeapon = ctx.definitions.getItemDefinition(gear[3] - 512).getName();

			System.out.println(equippedWeapon);

			String[] rangeItems = { "ballista", "blowpipe", "bow", "cannon", "knife" };
			String[] magicItems = { "korasi", "staff", "trident", "staff", "wand", "sceptre", "bulwark" };
			String[] meleeItems = { "godsword", "sword", "hasta", "axe", "spear", "maul", "mace", "rapier", "dagger",
					"bludgeon", "whip", "tent", "blade", "scythe", "claws", "scimitar", "hammer", "flail" };

			boolean isRange = containsItemName(equippedWeapon, rangeItems);
			boolean isMagic = containsItemName(equippedWeapon, magicItems);
			boolean isMelee = containsItemName(equippedWeapon, meleeItems);

			if (isRange) {
				enablePrayer(Prayers.PROTECT_FROM_MISSILES);
			} else if (isMagic) {
				enablePrayer(Prayers.PROTECT_FROM_MAGIC);
			} else if (isMelee) {
				enablePrayer(Prayers.PROTECT_FROM_MELEE);
			}
			attackTarget();
		}
	}

	private boolean containsItemName(String item, String[] itemNames) {
		for (String i : itemNames) {
			if (item.toLowerCase().contains(i)) {
				return true;
			}
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

		case KeyEvent.VK_2: {
			System.out.println("Speccing because enemy hp is: " + target.getHealthRatio() + "%");
			equipGear(specSet);
			spec();
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
