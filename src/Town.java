import java.awt.*;

/**
 * The Town Class is where it all happens.
 * The Town is designed to manage all the things a Hunter can do in town.
 * This code has been adapted from Ivan Turner's original program -- thank you Mr. Turner!
 */

public class Town {
    // instance variables
    private Hunter hunter;
    private Shop shop;
    private Terrain terrain;
    private String printMessage;
    private boolean toughTown;
    private boolean easy;
    private boolean dug;
    private boolean treasureDig;
    private String treasure;
    private int reward;
    private boolean brawled;



    /**
     * The Town Constructor takes in a shop and the surrounding terrain, but leaves the hunter as null until one arrives.
     *
     * @param shop The town's shoppe.
     * @param toughness The surrounding terrain.
     */
    public Town(Shop shop, double toughness, boolean easy) {
        this.easy = easy;
        this.shop = shop;
        this.terrain = getNewTerrain();
        dug = false;
        int x = (int )(Math.random() * 4 + 1);
        if (x == 1) {
            treasure = "a crown";
        } else if (x == 2) {
            treasure = "a trophy";
        } else if (x == 3) {
            treasure = "a gem";
        } else {
            treasure = "dust";
        }

        // the hunter gets set using the hunterArrives method, which
        // gets called from a client class
        hunter = null;
        printMessage = "";

        // higher toughness = more likely to be a tough town
        toughTown = (Math.random() < toughness);
        dug = false;
        treasureDig = false;
        brawled = false;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public String getLatestNews() {
        return printMessage;
    }

    /**
     * Assigns an object to the Hunter in town.
     *
     * @param hunter The arriving Hunter.
     */
    public void hunterArrives(Hunter hunter) {
        this.hunter = hunter;
        printMessage = "Welcome to town, " + hunter.getHunterName() + ".";
        if (toughTown) {
            printMessage += "\nIt's pretty rough around here, so watch yourself.";
        } else {
            printMessage += "\nWe're just a sleepy little town with mild mannered folk.";
        }
    }

    /**
     * Handles the action of the Hunter leaving the town.
     *
     * @return true if the Hunter was able to leave town.
     */
    public boolean leaveTown() {
        boolean canLeaveTown = terrain.canCrossTerrain(hunter);
        if (canLeaveTown) {
            String item = terrain.getNeededItem();
            printMessage = "You used your " + item + " to cross the " + terrain.getTerrainName() + ".";
            if (checkItemBreak() && !easy) {
                hunter.removeItemFromKit(item);
                printMessage += "\nUnfortunately, you lost your " + item + ".";
            }
            return true;
        }

        printMessage = "You can't leave town, " + hunter.getHunterName() + ". You don't have a " + terrain.getNeededItem() + ".";
        return false;
    }

    /**
     * Handles calling the enter method on shop whenever the user wants to access the shop.
     *
     * @param choice If the user wants to buy or sell items at the shop.
     */
    public void enterShop(String choice) {
        printMessage = shop.enter(hunter, choice);
    }

    public void dig() {
        if (!hunter.hasItemInKit("shovel")) {
            TreasureHunter.window.addTextToWindow("You can't dig for gold without a shovel\n", Color.BLACK);
            dug = false;
        }
        else if (dug) {
            TreasureHunter.window.addTextToWindow("You already dug for gold in this town.", Color.RED);
        }
        else if ((Math.random() * 100) > 50.0) {
            reward = (int) (Math.random() * 20) + 1;
            TreasureHunter.window.addTextToWindow("You dug up " + reward + " gold" + "!\n", Color.BLACK);
            TreasureHunter.window.addTextToWindow("You can no longer dig in this town.\n", Color.BLACK);
            hunter.changeGold(reward);
            dug = true;
        }
        else {
            TreasureHunter.window.addTextToWindow("You dug but only found dirt.\n", Color.BLACK);
            TreasureHunter.window.addTextToWindow("You can no longer dig in this town.\n", Color.BLACK);
            dug = true;
        }

    }

    /**
     * Gives the hunter a chance to fight for some gold.<p>
     * The chances of finding a fight and winning the gold are based on the toughness of the town.<p>
     * The tougher the town, the easier it is to find a fight, and the harder it is to win one.
     */
    public void lookForTrouble() {
        double noTroubleChance;
        if (toughTown) {
            noTroubleChance = 0.66;
        } else {
            noTroubleChance = 0.33;
        }
        if (Math.random() > noTroubleChance) {
            printMessage = "You couldn't find any trouble";
        }
        else if (hunter.hasItemInKit("sword")) {
            TreasureHunter.window.addTextToWindow("IS THAT A WHOLE SWORD, PLEASE STRANGER JUST TAKE MY MONEY\n", Color.BLACK);
            int goldDiff = (int) (Math.random() * 10) + 1;
            hunter.changeGold(goldDiff);
        }
        else {
            printMessage = "You want trouble, stranger!  You got it!\nOof! Umph! Ow!\n";
            int goldDiff = (int) (Math.random() * 10) + 1;
            if (Math.random() > noTroubleChance) {
                printMessage += Colors.RED + "Okay, stranger! You proved yer mettle. Here, take my gold." + Colors.RED;
                printMessage += Colors.RED + "\nYou won the brawl and receive " + goldDiff + " gold" + "." + Colors.RED;
                printMessage = "You won a brawl";
                hunter.changeGold(goldDiff);
            } else {
                printMessage += Colors.RED + "That'll teach you to go lookin' fer trouble in MY town! Now pay up!" + Colors.RED;
                printMessage += Colors.RED + "\nYou lost the brawl and pay " + goldDiff +  " gold" + "." + Colors.RED;
                printMessage = "You lost a brawl";
                if (hunter.gold < goldDiff) {
                    hunter.lose = true;
                    hunter.setGold(hunter.gold - goldDiff);
                } else {
                    hunter.changeGold(-goldDiff);
                }
            }
        }
    }

    public String huntTreasure() {
        treasureDig = true;
        return treasure;
    }

    public boolean getDug() {
        return dug;
    }

    public boolean getTreasureDig() {
        return treasureDig;
    }

    public String infoString() {
        return "This nice little town is surrounded by " + terrain.getTerrainName() + ".";
    }

    /**
     * Determines the surrounding terrain for a town, and the item needed in order to cross that terrain.
     *
     * @return A Terrain object.
     */
    private Terrain getNewTerrain() {
        double rnd = Math.random();
        if (rnd < 1.0/6) {
            return new Terrain(Colors.CYAN + "Marsh" + Colors.RESET, "Boots");
        } else if (rnd < 2.0/6) {
            return new Terrain(Colors.CYAN + "Mountains" + Colors.RESET, "Rope");
        } else if (rnd < 3.0/6) {
            return new Terrain(Colors.CYAN + "Ocean" + Colors.RESET, "Boat");
        } else if (rnd < 4.0/6) {
            return new Terrain(Colors.CYAN + "Plains" + Colors.RESET, "Horse");
        } else if (rnd < 5.0/6) {
            return new Terrain(Colors.CYAN + "Desert" + Colors.RESET, "Water");
        } else {
            return new Terrain(Colors.CYAN + "Jungle" + Colors.RESET, "Machete");
        }
    }

    /**
     * Determines whether a used item has broken.
     *
     * @return true if the item broke.
     */
    private boolean checkItemBreak() {
        double rand = Math.random();
        return (rand < 0.5);
    }
}