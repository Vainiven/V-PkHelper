import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import simple.api.HeadIcon;
import simple.api.actions.SimpleItemActions;
import simple.api.actions.SimplePlayerActions;
import simple.api.filters.SimplePrayers.Prayers;
import simple.api.filters.SimpleSkills.Skill;
import simple.api.script.Category;
import simple.api.script.LoopingScript;
import simple.api.script.Script;
import simple.api.script.ScriptManifest;
import simple.api.script.interfaces.SimplePaintable;
import simple.api.wrappers.SimplePlayer;

@ScriptManifest(author = "Vainiven", category = Category.OTHER, description = "Pk Helper", name = "V-PkHelper", version = "1.0", discord = "Vainiven#6986", servers = {
		"SpawnPk" })

public class main extends Script implements KeyListener, SimplePaintable, LoopingScript {

	String targetName;
	int saraCount;
	ArrayList<SimplePlayer> enemys = new ArrayList<SimplePlayer>();

//	final String[] mageSet = { "Blood slayer helmet", "Occult necklace (or)", "Eternal bounty cape", "Nightmare staff",
//			"Tome of fire", "Zuriel's robe top", "Zuriel's robe bottom", "Blood slayer gloves", "Blood slayer boots",
//			"Brimstone ring" };
//	final String[] rangeSet = { "Blood slayer helmet", "Necklace of anguish (or)", "Eternal bounty cape", "Zaryte bow",
//			"Morrigan's leather body", "Morrigan's leather chaps", "Blood slayer gloves", "Blood slayer boots",
//			"Brimstone ring" };
//	final String[] specSet = { "Armadyl godsword" };

	final String[] mageSet = { "Ahrim's staff", "Spirit shield", "Mystic robe bottom", "Mystic robe top",
			"Zuriel's staff" };
	final String[] rangeSet = { "Rune c'bow", "Rune platelegs", "Black d'hide body" };
	final String[] specSet = { "Dragon dagger", "Vesta longsword" };

	@Override
	public boolean onExecute() {
		return true;
	}

	@Override
	public void onProcess() {

		enablePrayer(Prayers.PROTECT_ITEM);
		if (hasTarget() && targetClose()) {

			for (SimplePlayer q : ctx.players.populate()) {
				if (q.getInteracting().equals(ctx.players.getLocal())) {
					enemys.add(q);
				} else if (enemys.contains(q)) {
					enemys.remove(q);
				}
			}

			System.out.println(enemys);
			for (SimplePlayer p : enemys) {
				int[] gear = p.getEquipment();
				System.out.println(ctx.definitions.getItemDefinition(gear[3] - 512));
			}

			drinkPrayerPotion();
			setDefensivePrayer();
			changeGearAndPrayer();
			eat();
			whenDead();
			attackTarget();
		} else {
			getTarget();
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
			// FIRE SURGE
			ctx.menuActions.sendAction(315, 4738, 0, 19530);

			// ANCIENT
			ctx.menuActions.sendAction(315, 4151, 4, 13136);
		}
		if (set.equals(specSet)) {
			enablePrayer(Prayers.PIETY);
		}
	}

	private void whenDead() {
		if (target().isDead() || target().getHealthRatio() == 0) {
			if (!ctx.inventory.populate().filter(12111).isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.DROP);
				ctx.sleep(2500);
				if (!ctx.inventory.populate().filter(3241).isEmpty()) {
					ctx.inventory.next().interact(SimpleItemActions.DROP);
				}
			}
		}
	}

	private SimplePlayer target() {
		return ctx.players.populate().filter(targetName).next();
	}

	private boolean targetClose() {
		if (ctx.players.getLocal().getLocation().getRegionID() == 12342) {
			targetName = "";
			return false;
		}
		if (targetName.equals("")) {
			return false;
		}
		if (!ctx.players.populate().filter(targetName).filterWithin(17).isEmpty()) {
			return true;
		} else {
			targetName = "";
			return false;
		}
	}

	private boolean hasTarget() {
		return targetName != "";
	}

	private void getTarget() {
		if (ctx.players.populate().filter(targetName).isEmpty() && ctx.players.getLocal().getInteracting() != null) {
			targetName = ctx.players.getLocal().getInteracting().getName();
			System.out.println("We set our target to: " + targetName);
		}

	}

	private void changeGearAndPrayer() {
		if (target().getRemainingPath() > 0) {
			snare();
		}
		if (overheadIcon() == HeadIcon.MAGIC || overheadIconInt().getHeadIcon() == 14) {
			equipGear(rangeSet);
			enablePrayer(Prayers.RIGOUR);
		} else if (overheadIcon() == HeadIcon.RANGED || overheadIconInt().getHeadIcon() == 15) {
			equipGear(mageSet);
			enablePrayer(Prayers.AUGURY);
		} else {
			equipGear(rangeSet);
		}
		attackTarget();
	}

	private void snare() {
		equipGear(mageSet);
		ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
		ctx.magic.selectSpell(1592);
		target().interact(365);
		ctx.sleep(1500);
		attackTarget();
	}

	private void tb() {
		equipGear(mageSet);
		ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
		ctx.magic.selectSpell(12445);
		target().interact(365);
		ctx.sleep(2500);
	}

	private void teleportTarget() {
		equipGear(mageSet);
		ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
		if (getWildernessLevel() > 30) {
			if (!ctx.inventory.populate().filter(15000).isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.DROP);
			}
		}
		ctx.menuActions.sendAction(315, 23111, 27, 7455);
		ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != 8939, 20, 100);
		if (!ctx.inventory.populate().filter(3241).isEmpty()) {
			ctx.inventory.next().interact(SimpleItemActions.DROP);
		}
	}

	private HeadIcon overheadIcon() {
		return target().getOverheadIcon();
	}

	private int getWildernessLevel() {
		if (!ctx.widgets.populate().filter(199).isEmpty()) {
			return Integer.parseInt(ctx.widgets.populate().filter(199).next().getText().replace("Level: ", ""));
		}
		return 0;
	}

	private SimplePlayer overheadIconInt() {
		return ctx.players.populate().filter(targetName).next();
	}

	private boolean drinkPrayerPotion() {
		if (ctx.prayers.prayerPercent() < 70 || ctx.skills.getLevel(Skill.MAGIC) < 95) {
			if (!ctx.inventory.populate()
					.filterContains("Super Restore", "Sanfew serum flask", "Super restore flask", "Sanfew serum")
					.isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
			return true;
		}
		return false;
	}

	private void spec() {
		if (target().getHealthRatio() < 40 && target().getOverheadIcon() != HeadIcon.MELEE
				&& overheadIconInt().getHeadIcon() != 13 && ctx.combat.getSpecialAttackPercentage() >= 50) {
			equipGear(specSet);
			if (!ctx.combat.specialAttack()) {
				ctx.combat.toggleSpecialAttack(true);
				System.out.println("Speccing because enemy hp is: " + target().getHealthRatio() + "%");
				attackTarget();
				ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == 7074, 20, 150);
			}
		}
	}

	public void specOverride() {
		if (target().getOverheadIcon() != HeadIcon.MELEE && overheadIconInt().getHeadIcon() != 13
				&& ctx.combat.getSpecialAttackPercentage() >= 50) {
			if (!ctx.combat.specialAttack()) {
				ctx.combat.toggleSpecialAttack(true);
				System.out.println("Speccing because enemy hp is: " + target().getHealthRatio() + "%");
				attackTarget();
				ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == 7074, 20, 150);
			}
		}
	}

	private void attackTarget() {
		if (ctx.players.getLocal().getInteracting() == null) {
			target().interact(SimplePlayerActions.ATTACK);
		}
	}

	private boolean eat() {
		if (saraCount == 3) {
			drinkRestore();
		}
		if (ctx.combat.healthPercent() < 60 && !ctx.inventory.populate()
				.filterContains("Manta ray", "Cooked Karambwan", "Saradomin brew", "Shark").isEmpty()) {
			ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			ctx.sleep(250);
			if (!ctx.inventory.populate().filterContains("Cooked Karambwan").isEmpty()) {
				ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			}
			attackTarget();
			return true;
		}
		return false;
	}

	private void drinkRestore() {
		if (!ctx.inventory.populate().filterContains("Super restore").isEmpty()) {
			ctx.inventory.next().interact(SimpleItemActions.CONSUME);
			saraCount = 0;
			ctx.sleep(250);
		}
	}

	private void enablePrayer(Prayers prayer) {
		if (!ctx.prayers.prayerActive(prayer)) {
			ctx.prayers.prayer(prayer);
		}
	}

	private void setDefensivePrayer() {
		if (target().getEquipment() != null) {
			int gear[] = target().getEquipment();
			if (ctx.definitions.getItemDefinition(gear[3] - 512) != null) {
				String equippedWeapon = ctx.definitions.getItemDefinition(gear[3] - 512).getName();

				String[] rangeItems = { "ballista", "blowpipe", "bow", "cannon", "knife" };
				String[] magicItems = { "korasi", "staff", "trident", "staff", "wand", "sceptre", "bulwark" };
				String[] meleeItems = { "balmiung", "godsword", "sword", "hasta", "axe", "spear", "maul", "mace",
						"rapier", "dagger", "bludgeon", "whip", "tent", "blade", "scythe", "claws", "scimitar",
						"hammer", "flail" };

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
		case KeyEvent.VK_Q: {
			System.out.println("We have detected Q. Mage Set.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			enablePrayer(Prayers.AUGURY);
			equipGear(mageSet);
			break;
		}

		case KeyEvent.VK_W: {
			System.out.println("We have detected W. Range Set.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			enablePrayer(Prayers.RIGOUR);
			equipGear(rangeSet);
			break;
		}

		case KeyEvent.VK_E: {
			System.out.println("We have detected E. Spec Set.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			enablePrayer(Prayers.PIETY);
			equipGear(specSet);
			specOverride();
			break;
		}

		case KeyEvent.VK_1: {
			System.out.println("We have detected 1. Tbing target.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			tb();
			break;
		}

		case KeyEvent.VK_2: {
			System.out.println("We have detected 2. Teleporting to target.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			teleportTarget();
			break;
		}

		case KeyEvent.VK_3: {
			System.out.println("We have detected 3. Teleporting home.");
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			if (ctx.players.getLocal().getLocation().getRegionID() != 12342) {
				if (!ctx.inventory.populate().filter(15000).isEmpty()) {
					ctx.inventory.next().interact(SimpleItemActions.DROP);
				}
				ctx.magic.castHomeTeleport();
				ctx.inventory.populate().filter(3241).next().interact(SimpleItemActions.DROP);
			}
			break;
		}

		case KeyEvent.VK_4: {
			ctx.keyboard.pressKey(KeyEvent.VK_BACK_SPACE);
			System.out.println("Resetted target.");
			targetName = "";
			break;
		}

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
		if (target() != null) {
			g.drawString(target().getName(), 100, 100);
		}

	}

	@Override
	public int loopDuration() {
		return 200;
	}

}
