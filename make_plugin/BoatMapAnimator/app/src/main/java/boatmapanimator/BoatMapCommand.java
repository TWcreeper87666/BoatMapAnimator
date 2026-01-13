package boatmapanimator;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Color;

public class BoatMapCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public BoatMapCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 2 || !args[0].equalsIgnoreCase("generate")) {
            sender.sendMessage("§c用法: /mapanim generate <framesDir> [x y z] [repeat]");
            return true;
        }

        // Default values
        String dirName = args[1];
        int baseX, baseY, baseZ;
        int repeat = 1;

        Player player = (sender instanceof Player) ? (Player) sender : null;

        // Parse coordinates (optional, args 2, 3, 4)
        if (args.length >= 5) {
            if (player == null && (args[2].startsWith("~") || args[3].startsWith("~") || args[4].startsWith("~"))) {
                sender.sendMessage("§c控制台不能使用 '~' 作為座標。");
                return true;
            }
            try {
                Location playerLoc = (player != null) ? player.getLocation() : null;
                int playerX = (playerLoc != null) ? playerLoc.getBlockX() : 0;
                int playerY = (playerLoc != null) ? playerLoc.getBlockY() - 1 : 0;
                int playerZ = (playerLoc != null) ? playerLoc.getBlockZ() : 0;

                baseX = parseCoordinate(args[2], playerX);
                baseY = parseCoordinate(args[3], playerY);
                baseZ = parseCoordinate(args[4], playerZ);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c座標必須是數字或 '~' 開頭的相對座標！");
                return true;
            }
        } else { // Use player location if coordinates are not provided
            if (player == null) {
                sender.sendMessage("§c控制台必須指定座標。用法: /boatmap generate <framesdir> <x> <y> <z> [repeat]");
                return true;
            }
            Location playerLoc = player.getLocation();
            baseX = playerLoc.getBlockX();
            baseY = playerLoc.getBlockY() - 1; // Block below the player
            baseZ = playerLoc.getBlockZ();
        }

        // Parse repeat (optional, arg 5)
        if (args.length >= 6) {
            try {
                repeat = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c重複次數必須是數字！");
                return true;
            }
        }

        File framesDir = new File(plugin.getDataFolder(), dirName);
        File[] frameFiles = framesDir.listFiles((d, n) -> n.endsWith(".png"));
        if (frameFiles == null || frameFiles.length == 0) {
            sender.sendMessage("§c找不到任何 PNG 圖片！");
            return true;
        }

        // Sort files by name to ensure correct animation order
        Arrays.sort(frameFiles);

        World world = Bukkit.getWorlds().get(0);
        sender.sendMessage("§a正在生成動畫軌道，共 " + frameFiles.length + " 幀...");

        int currentXOffset = 0;

        for (int i = 0; i < frameFiles.length; i++) {
            try {
                BufferedImage img = ImageIO.read(frameFiles[i]);

                MapView view = Bukkit.createMap(world);

                view.getRenderers().clear();
                view.addRenderer(new MapRenderer() {
                    @Override
                    public void render(MapView v, MapCanvas canvas, Player p) {
                        for (int y = 0; y < 128; y++) {
                            for (int x = 0; x < 128; x++) {
                                if (x < img.getWidth() && y < img.getHeight()) {
                                    int rgb = img.getRGB(x, y);
                                    Color color = rgbToMapColor(rgb);
                                    canvas.setPixelColor(x, y, color);
                                }
                            }
                        }
                    }
                });

                // 第一幀重複 150 次，其他幀則使用指令指定的次數
                int frameRepeatCount = (i == 0) ? 150 : repeat;

                for (int j = 0; j < frameRepeatCount; j++) {
                    // 計算 X 座標
                    Location frameLoc = new Location(world, baseX + currentXOffset, baseY, baseZ);
                    currentXOffset++;

                    // 先鋪方塊
                    Location stoneLoc = frameLoc.clone().add(0, 0, -1);
                    world.getBlockAt(stoneLoc).setType(Material.STONE);

                    Location iceLoc = frameLoc.clone().add(0, -2, 1);
                    world.getBlockAt(iceLoc).setType(Material.ICE);

                    world.spawn(frameLoc, ItemFrame.class, frame -> {
                        frame.setFacingDirection(BlockFace.SOUTH, true);

                        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                        MapMeta meta = (MapMeta) mapItem.getItemMeta();
                        meta.setMapView(view); // ✅ Use modern API
                        mapItem.setItemMeta(meta);

                        frame.setItem(mapItem);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("§c第 " + i + " 幀生成失敗: " + e.getMessage());
            }
        }

        sender.sendMessage("§b✔ 動畫軌道生成完成！");

        // Spawn a boat at the start
        Location boatLoc = new Location(world, baseX + 0.5, baseY - 1, baseZ + 1.5);
        world.spawn(boatLoc, OakBoat.class, boat -> {
            boat.setRotation(-90, 0); // Yaw -90 (East, along positive X), Pitch 0
        });
        sender.sendMessage("§a已在起點生成一艘橡木船！");

        return true;
    }

    private Color rgbToMapColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return MapPalette.getNearestColor(new Color(r, g, b));
    }

    private int parseCoordinate(String arg, int base) throws NumberFormatException {
        if (arg.startsWith("~")) {
            if (arg.length() == 1) {
                return base;
            } else {
                // Support for relative coordinates like ~5 or ~-10
                return base + Integer.parseInt(arg.substring(1));
            }
        }
        return Integer.parseInt(arg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        // Suggest "generate" for the first argument
        if (args.length == 1) {
            completions.add("generate");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }

        // Handle completions for "/mapanim generate ..."
        if (args[0].equalsIgnoreCase("generate")) {
            switch (args.length) {
                case 2: // framesDir
                    // Suggest directory names
                    File dataFolder = plugin.getDataFolder();
                    if (dataFolder.exists() && dataFolder.isDirectory()) {
                        File[] directories = dataFolder.listFiles(File::isDirectory);
                        if (directories != null) {
                            for (File dir : directories) {
                                completions.add(dir.getName());
                            }
                        }
                    }
                    break;
                case 3: // x
                case 4: // y
                case 5: // z
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        completions.add("~");
                        Location loc = player.getLocation();
                        if (args.length == 3) {
                            completions.add(String.valueOf(loc.getBlockX()));
                        } else if (args.length == 4) {
                            completions.add(String.valueOf(loc.getBlockY() - 1));
                        } else { // args.length == 5
                            completions.add(String.valueOf(loc.getBlockZ()));
                        }
                    }
                    break;
                case 6: // repeat
                    completions.addAll(Arrays.asList("1", "2"));
                    break;
            }
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}
