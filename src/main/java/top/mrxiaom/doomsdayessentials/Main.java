package top.mrxiaom.doomsdayessentials;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Lists;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.api.AdvancedAchievementsBukkitAPI;
import com.hm.achievement.db.CacheManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.ranull.graves.Graves;
import com.ranull.graves.manager.GraveManager;
import me.albert.amazingbot.bot.Bot;
import me.clip.placeholderapi.PlaceholderAPI;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import top.mrxiaom.doomsdaycommands.CmdManager;
import top.mrxiaom.doomsdayessentials.bot.BotMsgListener;
import top.mrxiaom.doomsdayessentials.bot.ControlListener;
import top.mrxiaom.doomsdayessentials.chapter.ChapterManager;
import top.mrxiaom.doomsdayessentials.configs.*;
import top.mrxiaom.doomsdayessentials.configs.MarketConfig.MarketData;
import top.mrxiaom.doomsdayessentials.configs.RandomTPConfig.TeleportMode;
import top.mrxiaom.doomsdayessentials.configs.RandomTPConfig.Zone;
import top.mrxiaom.doomsdayessentials.listener.*;
import top.mrxiaom.doomsdayessentials.placeholder.*;
import top.mrxiaom.doomsdayessentials.skills.IAmNoob;
import top.mrxiaom.doomsdayessentials.skills.SakuzyoBeam;
import top.mrxiaom.doomsdayessentials.skills.SelfAttack;
import top.mrxiaom.doomsdayessentials.skills.SpiritualCrystallization;
import top.mrxiaom.doomsdayessentials.utils.*;
import top.mrxiaom.doomsdayessentials.utils.McbbsUtil.Operation;
import top.mrxiaom.doomsdayessentials.utils.McbbsUtil.ThreadOperation;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil.NMSItemStack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class Main extends JavaPlugin {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	private static final List<Material> damager = Lists.newArrayList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
			Material.WATER, Material.LAVA, Material.COBWEB, Material.FIRE, Material.CAMPFIRE,
			Material.ACACIA_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
			Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE,
			Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE,
			Material.STONE_PRESSURE_PLATE, Material.CACTUS, Material.NETHER_PORTAL, Material.END_PORTAL,
			Material.TRIPWIRE, Material.TRIPWIRE_HOOK, Material.END_GATEWAY, Material.MAGMA_BLOCK);

	// 性能相关
	TPSMonitor tps;
	HighfrequencyRedstoneCleaner redstoneCleaner;
	// 变量
	PlaceholderRespawnNeedle papiRespawnNeedle;
	PlaceholderKit papiKit;
	PlaceholderTitle papiTitle;
	PlaceholderPlayerPoints papiPlayerPoints;
	PlaceholderMCMMO papiMcMMO;
	PlaceholderSettings papiSettings;
	PlaceholderMCBBS papiMcbbs;
	// 事件监听器
	PlayerListener playerListener;
	LifeListener lifeListener;
	LoginListener loginListener;
	ChatListener chatListener;
	InventoryListener inventoryListener;
	BlockListener blockListener;
	EntityListener entityListener;
	OpenWorldListener openWorldListener;
	ControlListener controlListener;
	BotMsgListener botMsgListener;
	// 配置文件
	BackConfig backConfig;
	PlayerConfig playerConfig;
	BindConfig bindConfig;
	WarpConfig warpConfig;
	GunConfig gunConfig;
	HomeConfig homeConfig;
	KitConfig kitConfig;
	OpenWorldConfig openWorldConfig;
	ParkourConfig parkoursConfig;
	WhitelistConfig whitelistConfig;
	RandomTPConfig randomTPConfig;
	SkullConfig skullConfig;
	MarketConfig marketConfig;
	McbbsConfig mcbbsConfig;
	// 其他插件 Api
	Economy ecoApi;
	Permission permsApi;
	ResidenceInterface resApi;
	CoreProtectAPI coreProtectApi;
	PlayerPointsAPI playerPointsApi;
	GraveManager graveApi;
	AdvancedAchievementsAPI aachApi;
	MultiverseCore mvApi;
	CacheManager aachCacheApi;
	top.mrxiaom.pluginupdater.Main updaterApi;
	ProtocolManager protocolManager;
	// 其他功能
	CmdManager cmdManager;
	KeyConfig keyManager;
	TagConfig tagConfig;
	PlayerCooldownManager playerCooldownManager;
	AutoMineChecker autoMineChecker;
	GuiManager guiManager;
	ChapterManager chapterManager;
	// 技能
	SakuzyoBeam skillSakuzyoBeam;
	SelfAttack skillSelfAttack;
	SpiritualCrystallization skillSpiritualCrystallization;
	IAmNoob skillIAmNoob;

	public boolean bindingDebug = true;
	private List<String> fireWorldBlackList;
	private final List<Material> pistonBlackList = new ArrayList<Material>();
	private List<String> notice = new ArrayList<String>();
	private List<String> tips = new ArrayList<String>();
	private int fireTime;
	private int noticeCountdown = 300;
	private static final long cleanTimerDefault = 900L;
	private long cleanTimer = cleanTimerDefault;
	private long lastActionTime = System.currentTimeMillis();
	private final Map<UUID, Integer> armorDamageTime = new HashMap<>();
	private int teleportWaitTime = 3;
	private int tpTimeout = 120;
	private int randomTPCacheCount = 0;
	private String threadId;
	boolean disabled = false;
	@Override
	public void onEnable() {
		instance = this;
		this.hookDependPlugins();
		this.initListener();

		this.reloadConfig();
		
		this.initSkill();

		this.initSafeAndTimer();
		System.out.println("末日社团基础插件 初始化完毕");
		System.out.println(this.getServerRoot());
	}
	private void initListener() {
		this.playerListener = new PlayerListener(this);
		this.lifeListener = new LifeListener(this);
		this.loginListener = new LoginListener(this);
		this.tagConfig = new TagConfig(this);
		this.chatListener = new ChatListener(this);
		this.inventoryListener = new InventoryListener(this);
		this.blockListener = new BlockListener(this);
		this.entityListener = new EntityListener(this);
		this.openWorldListener = new OpenWorldListener(this);
		this.controlListener = new ControlListener(this);
		this.botMsgListener = new BotMsgListener(this);
	}
	private void initSkill() {
		this.skillSakuzyoBeam = new SakuzyoBeam(this);
		this.skillSelfAttack = new SelfAttack(this);
		this.skillSpiritualCrystallization = new SpiritualCrystallization(this);
		this.skillIAmNoob = new IAmNoob(this);
	}
	private void initSafeAndTimer() {
		this.cmdManager = new CmdManager(this, "top.mrxiaom.doomsdayessentials.commands").register();
		this.playerCooldownManager = new PlayerCooldownManager(this);
		this.tps = new TPSMonitor();
		this.redstoneCleaner = new HighfrequencyRedstoneCleaner(this);
		this.autoMineChecker = new AutoMineChecker(this);
		this.guiManager = new GuiManager(this);
		this.chapterManager = new ChapterManager(this);
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this::everySecond, 20, 20);
		this.getServer().getScheduler().runTaskTimer(this, this.tps::updateTps, 1L, 1L);
		this.getServer().getScheduler().runTaskTimer(this, Util::checkAndCleanMobFarm, 600 * 20L, 600 * 20L);
		// 20 秒检查一次传送点
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, this::updateRandomTPCache, 400, 400);
		this.getServer().getScheduler().runTaskTimer(this, this::checkMarketOutdate, 7200L, 7200L);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, this::mcbbsChecker, 1200L, 1200L);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, this::onBotSecond, 20, 20);
	}
	private void hookDependPlugins() {
		boolean papiExists = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		if (papiExists) {
			Util.registerPlaceholder(getLogger(), papiMcbbs = new PlaceholderMCBBS(this), "顶贴奖励");
			Util.registerPlaceholder(getLogger(), papiRespawnNeedle = new PlaceholderRespawnNeedle(this), "复活针");
			Util.registerPlaceholder(getLogger(), papiTitle = new PlaceholderTitle(this), "称号");
			Util.registerPlaceholder(getLogger(), papiKit = new PlaceholderKit(this), "工具包");
			Util.registerPlaceholder(getLogger(), papiSettings = new PlaceholderSettings(this), "设置");
		}

		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null){
			protocolManager = ProtocolLibrary.getProtocolManager();
		}
		if (Bukkit.getPluginManager().getPlugin("PluginUpdater") != null) {
			updaterApi = (top.mrxiaom.pluginupdater.Main) Bukkit.getPluginManager().getPlugin("PluginUpdater");
		}
		if (Bukkit.getPluginManager().getPlugin("AdvancedAchievements") != null) {
			aachApi = ((AdvancedAchievements) Bukkit.getPluginManager().getPlugin("AdvancedAchievements"))
					.getAdvancedAchievementsAPI();
			try {
				Field field = AdvancedAchievementsBukkitAPI.class.getDeclaredField("cacheManager");
				field.setAccessible(true);
				this.aachCacheApi = (CacheManager) field.get(aachApi);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (Bukkit.getPluginManager().getPlugin("Graves") != null) {
			this.graveApi = ((Graves) Bukkit.getPluginManager().getPlugin("Graves")).getGraveManager();
		}
		if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
			PlayerPoints pp = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
			this.playerPointsApi = new PlayerPointsAPI(pp);
			if (papiExists)
				Util.registerPlaceholder(getLogger(), papiPlayerPoints = new PlaceholderPlayerPoints(pp.getAPI()),
						"点券");
		}
		if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
			if (papiExists)
				Util.registerPlaceholder(getLogger(), this.papiMcMMO = new PlaceholderMCMMO(this), "mcMMO");
		}
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			permsApi = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
			ecoApi = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		}
		if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
			resApi = ResidenceApi.getResidenceManager();
			FlagPermissions.addPlayerOrGroupOnlyFlag("power");
			FlagPermissions.addFlag("power");
		}
		if (Bukkit.getPluginManager().getPlugin("CoreProtect") != null) {
			coreProtectApi = new CoreProtectAPI();
		}
		if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
			mvApi = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
		}
	}

	public File getFile(String path) {
		return new File(this.getDataFolder(), path);
	}
	public File getServerRoot() { return this.getDataFolder().getAbsoluteFile().getParentFile().getParentFile(); };
	public File getServerFile(String path) { return new File(this.getServerRoot(), path); }

	public void reloadConfig() {
		this.saveDefaultConfig();
		super.reloadConfig();
		String langFileName = Objects.requireNonNullElse(this.getConfig().getString("lang-file", "lang-zh.yml"), "lang-zh.yml");
		// 语言文件释放
		if (!getFile(langFileName).exists()) {
			try {
				this.saveResource(langFileName, false);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		I18n.loadConfig(this, YamlConfiguration.loadConfiguration(getFile(langFileName)));

		// 读配置
		this.fireWorldBlackList = new ArrayList<>();
		for (String s : this.getConfig().getStringList("player-fire.world-blacklist")) {
			this.fireWorldBlackList.add(s.toLowerCase());
		}
		this.fireTime = this.getConfig().getInt("player-fire.fire-time", 60);
		this.threadId = this.getConfig().getString("mcbbs.thread-id");
		this.tpTimeout = this.getConfig().getInt("tp-timeout", 120);
		this.teleportWaitTime = this.getConfig().getInt("teleport-wait-time", 3);
		List<String> pision_blacklist = this.getConfig().getStringList("pision-extend-black-blocks");
			this.pistonBlackList.clear();
			for (String s : pision_blacklist) {
				Material material = Material.getMaterial(s.toUpperCase().replace(" ", "_"));
				if (material != null) {
					this.pistonBlackList.add(material);
				}
			}
		this.notice = new ArrayList<>();
		if(this.getConfig().contains("notice")) {
			for(String s : this.getConfig().getStringList("notice")) {
				this.notice.add(ChatColor.translateAlternateColorCodes('&', s));
			}
		}
		this.tips = new ArrayList<>();
		if(this.getConfig().contains("tips")) {
			for(String s : this.getConfig().getStringList("tips")) {
				this.tips.add(ChatColor.translateAlternateColorCodes('&', s));
			}
		}
		this.tagConfig.reloadConfig();
		if (this.getChatListener() != null) this.getChatListener().reloadConfig();

		if (this.getWhitelistConfig() == null) this.whitelistConfig = new WhitelistConfig(this);
		if (this.getKeyManager() == null) this.keyManager = new KeyConfig(this);
		if (this.getBindConfig() == null) this.bindConfig = new BindConfig(this);
		if (this.getHomeConfig() == null) this.homeConfig = new HomeConfig(this);
		if (this.getPlayerConfig() == null) this.playerConfig = new PlayerConfig(this);
		if (this.getWarpConfig() == null) this.warpConfig = new WarpConfig(this);
		if (this.getGunConfig() == null) this.gunConfig = new GunConfig(this);
		if (this.getKitConfig() == null) this.kitConfig = new KitConfig(this);
		if (this.getOpenWorldConfig() == null) this.openWorldConfig = new OpenWorldConfig(this);
		if (this.getParkoursConfig() == null) this.parkoursConfig = new ParkourConfig(this);
		if (this.getBackConfig() == null) this.backConfig = new BackConfig(this);
		if (this.getRandomTPConfig() == null) this.randomTPConfig = new RandomTPConfig(this);
		if (this.getSkullConfig() == null) this.skullConfig = new SkullConfig(this);
		if (this.getMarketConfig() == null) this.marketConfig = new MarketConfig(this);
		if (this.getMcbbsConfig() == null) this.mcbbsConfig = new McbbsConfig(this);
		if (this.getChapterManager() == null) this.chapterManager = new ChapterManager(this);

		this.getWhitelistConfig().reloadConfig();
		this.getKeyManager().reloadConfig();
		this.getBindConfig().reloadConfig();
		this.getHomeConfig().reloadConfig();
		this.getPlayerConfig().reloadConfig();
		this.getWarpConfig().reloadConfig();
		this.getGunConfig().reloadConfig();
		this.getKitConfig().reloadConfig();
		this.getOpenWorldConfig().reloadConfig();
		this.getParkoursConfig().reloadConfig();
		this.getBackConfig().reloadConfig();
		this.getRandomTPConfig().reloadConfig();
		this.getSkullConfig().reloadConfig();
		this.getMarketConfig().reloadConfig();
		this.getMcbbsConfig().reloadConfig();
		this.getChapterManager().reloadConfig();
		
		this.getLogger().info("配置文件已重载");
	}

	public void onDisable() {
		this.disabled = true;
		instance = null;
		this.getRandomTPConfig().saveCache();
		if(this.protocolManager != null) this.protocolManager.removePacketListeners(this);
		Bukkit.getScheduler().cancelTasks(this);
		this.playerListener.enable = false;
		this.getRandomTPConfig().saveCache();
		if (this.papiRespawnNeedle != null && this.papiRespawnNeedle.isRegistered()) {
			this.papiRespawnNeedle.unregister();
		}
		if (this.papiTitle != null && this.papiTitle.isRegistered()) {
			this.papiTitle.unregister();
		}
		if (this.papiPlayerPoints != null && this.papiPlayerPoints.isRegistered()) {
			this.papiPlayerPoints.unregister();
		}
		if (this.papiMcMMO != null && this.papiMcMMO.isRegistered()) {
			this.papiMcMMO.unregister();
		}
		if (this.papiSettings != null && this.papiSettings.isRegistered()) {
			this.papiSettings.unregister();
		}
		if(this.papiMcbbs != null && this.papiMcbbs.isRegistered()){
			this.papiMcbbs.unregister();
		}
		this.getLogger().info("基础插件 DoomsdayEssentials 已卸载");
		System.gc();
	}
	int botCooldown = 0;
	public void onBotSecond(){
		if(botCooldown > 0) {
			botCooldown--;
			return;
		}
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		if(minute == 0 && second == 0 && (hour >= 7 || hour == 0)) {
			botCooldown = 60;
			if(this.getConfig().contains("chat.group.notice")) {
				List<String> list = this.getConfig().getStringList("chat.group.notice");
				if(list.size() > 0) {
					StringBuilder msg = new StringBuilder();
					for (int i = 0; i < list.size(); i++)
					{
						msg.append(list.get(i)
								.replace("%hour%", (hour < 10 ? "0" : "") + hour)
								.replace("%minute%", (minute < 10 ? "0":"") +minute)
								.replace("%second%", (second < 10 ? "0":"")+second)
						).append(i < list.size() - 1 ? "\n" : "");
					}
					Bot.getApi().getGroup(951534513L).sendMessage(msg.toString());
				}
			}

		}
	}

	public void mcbbsChecker() {
		try {
			List<ThreadOperation> list = McbbsUtil.getThreadOperation(threadId);
			if(!this.mcbbsConfig.isNowMatchedLastDate() && list.size() > this.mcbbsConfig.getLastSize()) {
				// mcbbs 的操作是最新的在最前，所以要倒着取
				for(int i = list.size() - this.mcbbsConfig.getLastSize() - 1; i >= 0; i--) {
					ThreadOperation to = list.get(i);
					// 服务器提升卡
					if(to.getOperation().equals(Operation.UP_SERVER)) {
						String player = this.mcbbsConfig.getUidPlayer();
						if(player != null) {
							this.mcbbsConfig.setLastDateToNow(player);
							Util.sendItemToMail(player, "末日社团", this.mcbbsConfig.genReward());
							Util.alert(I18n.tn("mcbbs.alert-reward").replace("%player%", player));
							break;
						}
					}
				}
			}
			this.mcbbsConfig.setLastSize(list.size());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void checkMarketOutdate() {
		for(MarketData data : this.getMarketConfig().allData()) {
			if(data.isOutdate()) {
				ClaimedResidence res = this.getMarketConfig().getMarketResidence(data.getId());
				if(res != null) {
					res.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), data.getOwner(), "container", "remove", true, false);
					res.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), data.getOwner(), "build", "remove", true, false);
					res.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), data.getOwner(), "destroy", "remove", true, false);
				}
				this.getMarketConfig().onMarketRemoved(data.getId());
				Player player = Bukkit.getPlayer(data.getOwner());
				data.setOwner("");
				data.removeOutdateTime();
				if(player != null) {
					// TODO 给玩家发邮件
					//new EmailManager(player).saveEmail(EmailManager.generateUUID(), "末日社团", I18n.t("market.out-of-date")
					//		.replace("%id%", data.getId())
					//		.replace("%time%", TimeUtil.getChineseTime(data.getOutdateTime())), null, null);
				}
			}
		}
	}

	public void updateRandomTPCache() {
		for (Zone zone : this.getRandomTPConfig().getZonesNeedToCache()) {
			// 只找一次点，成功了就放入缓存
			World world = zone.getWorld();
			int randomX = Util.randomIntegerBetween((int) zone.getX1(), (int) zone.getX2());
			int randomZ = Util.randomIntegerBetween((int) zone.getZ1(), (int) zone.getZ2());
			if (zone.getMode().equals(TeleportMode.TOP)) {
				Block block = world.getHighestBlockAt(randomX, randomZ);
				if (!damager.contains(block.getType())) {
					this.getRandomTPConfig().addToCache(zone, randomX, block.getY() + 1, randomZ);
					continue;
				}
			}
			int i = Util.getIntegerMin((int) zone.getY1(), (int) zone.getY2());
			int j = Util.getIntegerMin(Util.getIntegerMax((int) zone.getY1(), (int) zone.getY2()),
					world.getHighestBlockYAt(randomX, randomZ) - 2);
			if (zone.getMode().equals(TeleportMode.GROUND)) {
				// 从最小值开始遍历
				for (; i < j; i++) {
					Block block = world.getBlockAt(randomX, i, randomZ);
					// 如果该方块不危险且上方两格都是空气则传送
					if (!damager.contains(block.getType()) && block.getRelative(BlockFace.UP, 1).getType().isAir()
								&& block.getRelative(BlockFace.UP, 2).getType().isAir()) {
						this.getRandomTPConfig().addToCache(zone, randomX, i + 1, randomZ);
					}
				}
			}
			if (zone.getMode().equals(TeleportMode.TOP_GROUND)) {
				// 从最小值开始遍历，到这一格的最上面一个方块
				for (i = Util.getIntegerMax(i, 63); i < j; i++) {
					Block block = world.getBlockAt(randomX, i, randomZ);
					// 如果该方块不危险且上方两格都是空气则传送
					if (!damager.contains(block.getType()) && block.getRelative(BlockFace.UP, 1).getType().isAir()
								&& block.getRelative(BlockFace.UP, 2).getType().isAir()) {
						this.getRandomTPConfig().addToCache(zone, randomX, i + 1, randomZ);
					}
				}
			}
		}
		randomTPCacheCount++;
		if (randomTPCacheCount > 10) {
			randomTPCacheCount = 0;
			this.getRandomTPConfig().saveCache();
		}
	}

	public boolean isWorldNotFire(String name) {
		if (this.fireWorldBlackList == null)
			return false;
		return this.fireWorldBlackList.contains(name.toLowerCase());
	}

	public void checkPoints(final Player p) {
		p.sendMessage(I18n.t("points.checking", true));
		int mcrmb_p = Util.look_MCRMB(p);
		if (mcrmb_p > 0) {
			Util.buy_MCRMB(p, mcrmb_p, "将 Mcrmb 点券转换成 PlayerPoints 点券【共转换" + mcrmb_p + "点券】");
			this.playerPointsApi.give(p.getUniqueId(), mcrmb_p);
			p.sendMessage(I18n.t("points.success", true).replace("%points%", String.valueOf(mcrmb_p)));
		} else {
			p.sendMessage(I18n.t("points.no-need", true));
		}
	}
	
	public String getWorldAlias(World world) {
		return this.mvApi.getMVWorldManager().getMVWorld(world).getAlias();
	}

	public String getWorldAlias(String world) {
		return this.mvApi.getMVWorldManager().getMVWorld(world).getAlias();
	}

	public int getTpTimeout() {
		return this.tpTimeout;
	}

	public int getTeleportWaitTime() {
		return this.teleportWaitTime;
	}

	public static void showPlayerBullets(Player player, String displayName, int bullets) {
		NMSUtil.sendActionMsg(player, I18n.t("gun.action-bar").replace("%gun%", displayName).replace("%bullets%",
				(bullets == 0 ? "&c&l" : "") + bullets));
	}

	private void handlePlayerStatus(Player player) {
		RPGItem rpg = ItemManager.toRPGItemByMeta(player.getInventory().getItemInOffHand()).orElse(null);
		// 老B灯
		if(rpg != null && rpg.getName().equalsIgnoreCase("blantern")) {
			if(player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 280, 1, false, false, true), true);
		}
		// 诅咒
		if (player.getHealth() <= 4) {
			if (this.getPlayerConfig().getConfig().getConfigurationSection(player.getName()).getBoolean("curse")) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 30, 4, true));
			}
		}
		if (this.getPlayerConfig().isShowBullets(player.getName())) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.hasItemMeta()) {
				ItemMeta im = item.getItemMeta();
				if (im != null && im.hasLore() && im.getLore() != null) {

					String s = im.getLore().get(im.getLore().size() - 1).toLowerCase();
					NMSItemStack nms = NMSItemStack.fromBukkitItemStack(item);
					if (nms != null && s.toLowerCase().startsWith("§g§u§n")) {
						int bullets = nms.getNBTTagInt("bullets", 0);
						showPlayerBullets(player, im.getDisplayName(), bullets);
					}
				}
			}
		}
	}
	
	public void everySecond() {
		if(Bukkit.getOnlinePlayers().size() > 0) {
			this.onCleanSecond();
			this.onNoticeSecond();
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.handlePlayerStatus(player);
			Location loc = player.getLocation();
			// 不在线不燃烧，死了不燃烧，非生存模式&冒险模式不燃烧，游泳时不燃烧，世界下雨/下雪时不燃烧，黑夜时不燃烧
			if (!player.isOnline() || player.isDead()
					|| !(player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
					|| this.isWorldNotFire(player.getWorld().getName()) || player.isSwimming()
					|| loc.getWorld() == null || loc.getWorld().hasStorm()
					|| loc.getWorld().getTime() > 12500 || loc.getWorld().getTime() < 1000) {
				return;
			}
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			boolean firePlayer = true;
			for (int i = y; i <= loc.getWorld().getHighestBlockYAt(x, z); i++) {
				if (ItemStackUtil.isBlockAntiSun(player.getWorld().getBlockAt(x, i, z).getType())) {
					firePlayer = false;
					break;
				}
			}
			// 别问，写这个的人都不记得代码的意思了
			if (firePlayer) {
				if (player.getInventory().getHelmet() != null
						&& ItemStackUtil.hasLore(player.getInventory().getChestplate(), "§5§7§f§e§a头上戴任意物品不自燃")) {
					return;
				}
				if (ItemStackUtil.hasHelmet(player)) {
					ItemStack itemStack = player.getInventory().getHelmet();
					if (!ItemStackUtil.isDisplayNameContains(itemStack, String.valueOf(ChatColor.COLOR_CHAR))) {
						ItemMeta im = Objects.requireNonNullElse(itemStack.getItemMeta(), ItemStackUtil.getItemMeta(itemStack.getType()));
						if (ItemStackUtil.isHelmetFastBurn(itemStack)) {
							if (((Damageable) im).getDamage() + 1 > itemStack.getType().getMaxDurability()) {
								player.getInventory().setHelmet(null);
								player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
							} else {
								((Damageable) im).setDamage(((Damageable) im).getDamage() + 1);
								itemStack.setItemMeta((ItemMeta) im);
								player.getInventory().setHelmet(itemStack);
							}
						} else {
							int reduceInt = this.armorDamageTime.getOrDefault(player.getUniqueId(), 0);
							if(reduceInt < 4) {
								reduceInt++;
							}
							else {
								if (((Damageable) im).getDamage() + 1 > itemStack.getType().getMaxDurability()) {
									player.getInventory().setHelmet(null);
									player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
								} else {
									((Damageable) im).setDamage(((Damageable) im).getDamage() + 1);
									itemStack.setItemMeta(im);
									player.getInventory().setHelmet(itemStack);
								}
								reduceInt = 0;
							}
							this.armorDamageTime.put(player.getUniqueId(), reduceInt);
						}
					}
					return;
				}
				// 烧多久
				player.setFireTicks(this.fireTime);
			}
		}
	}

	// 公告
	private void onNoticeSecond() {
		if (noticeCountdown > 0) {
			noticeCountdown--;
		} else {
			noticeCountdown = 300;
			if (!notice.isEmpty()) {
				String alert = ChatColor.translateAlternateColorCodes('&', notice.get(new Random().nextInt(notice.size()))
						.replace("\\n", "\n").replace("\r", "").replace("　", " "));
				Util.logger.info(alert);
				for (Player p : Bukkit.getOnlinePlayers()) {
					if(this.playerConfig.getConfig().getBoolean(p.getName() + ".show-alerts", true)) {
						p.sendMessage(alert);
					}
				}
			}
		}
	}

	private void onCleanSecond() {
		if (this.tps.getAverageTPS() < 15 && lastActionTime + 60000 < System.currentTimeMillis()) {
			lastActionTime = System.currentTimeMillis();
			int count = 0;
			for (World world : Bukkit.getWorlds()) {
				for (Entity entity : world.getEntities()) {
					if (Util.checkCustomNpc(entity)) {
						continue;
					}
					if (entity instanceof Monster) {
						if (entity.getCustomName() == null) {
							entity.remove();
							count++;
						}
					}
				}
			}
			if (count > 0) {
				Util.alert(I18n.t("cleaner.clean-monster").replace("%count%", String.valueOf(count)));
			}
			cleanTimer = 0;
			System.runFinalization();
			System.gc();
		}
		cleanTimer--;
		if (cleanTimer == 60 || cleanTimer == 30 || cleanTimer == 10) {
			Util.alert(I18n.t("cleaner.clean-pre").replace("%time%", String.valueOf(cleanTimer)));
		}
		if (cleanTimer <= 0) {
			this.cleanTimer = cleanTimerDefault;
			int count = 0;
			int countM = 0;
			for (World world : Bukkit.getWorlds()) {
				for (Entity e : world.getEntities()) {
					if (e.getType() == EntityType.DROPPED_ITEM) {
						e.remove();
						count++;
					}
					if (e.getType() == EntityType.ARROW && e.isOnGround()) {
						e.remove();
					}
				}
				for (Entity entity : world.getEntities()) {
					if (Util.checkCustomNpc(entity) || Util.isNoClearEntities(entity)) {
						continue;
					}
					if (entity instanceof Monster) {
						if (ResidenceApi.getResidenceManager().getByLoc(entity.getLocation()) == null) {
							if (entity.getCustomName() == null || entity.getCustomName().length() == 0) {
								entity.remove();
								countM++;
							}
						}
					}
				}
				world.save();
			}
			Util.alert(I18n.t("cleaner.clean-done").replace("%items%", String.valueOf(count)).replace("%monsters%",
					String.valueOf(countM)));
		}
	}

	public boolean isPisionExtendBlackList(Material m) {
		return this.pistonBlackList.contains(m);
	}

	public String getRandomTips() {
		if(tips.size() < 1) return "使用 /tips 可以刷新这条消息";
		return tips.get(new Random().nextInt(this.tips.size()));
	}
	
	public BindConfig getBindConfig() {
		return bindConfig;
	}
	
	public WarpConfig getWarpConfig() {
		return warpConfig;
	}

	public GunConfig getGunConfig() {
		return gunConfig;
	}

	public KitConfig getKitConfig() {
		return kitConfig;
	}

	public HomeConfig getHomeConfig() {
		return homeConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return playerConfig;
	}

	public OpenWorldConfig getOpenWorldConfig() {
		return openWorldConfig;
	}

	public BackConfig getBackConfig() {
		return backConfig;
	}

	public Economy getEcoApi() {
		return ecoApi;
	}

	public Permission getPermsApi() {
		return permsApi;
	}

	public ResidenceInterface getResidenceApi() {
		return this.resApi;
	}

	public CoreProtectAPI getCoreProtectApi() {
		return this.coreProtectApi;
	}

	public PlayerCooldownManager getPlayerCooldownManager() {
		return this.playerCooldownManager;
	}

	public HighfrequencyRedstoneCleaner getRedstoneCleaner() {
		return this.redstoneCleaner;
	}

	public TagConfig getTagConfig() {
		return this.tagConfig;
	}

	public KeyConfig getKeyManager() {
		return this.keyManager;
	}

	public PlayerListener getPlayerListener() {
		return this.playerListener;
	}

	public ChatListener getChatListener() {
		return this.chatListener;
	}

	public InventoryListener getInventoryListener() {
		return this.inventoryListener;
	}

	public OpenWorldListener getOpenWorldListener() {
		return this.openWorldListener;
	}


	public BotMsgListener getBotMsgListener() {
		return botMsgListener;
	}
	public ControlListener getControlListener() {
		return controlListener;
	}
	public ParkourConfig getParkoursConfig() {
		return parkoursConfig;
	}

	public WhitelistConfig getWhitelistConfig() {
		return whitelistConfig;
	}

	public LifeListener getLifeListener() {
		return lifeListener;
	}

	public GraveManager getGraveApi() {
		return graveApi;
	}

	public MultiverseCore getMVApi() {
		return mvApi;
	}

	public LoginListener getLoginListener() {
		return loginListener;
	}

	public AdvancedAchievementsAPI getAachApi() {
		return aachApi;
	}

	public CacheManager getAachCacheApi() {
		return aachCacheApi;
	}

	public SakuzyoBeam getSkillSakuzyoBeam() {
		return skillSakuzyoBeam;
	}

	public SelfAttack getSkillSelfAttack() {
		return skillSelfAttack;
	}

	public SpiritualCrystallization getSkillSpiritualCrystallization() {
		return skillSpiritualCrystallization;
	}

	public RandomTPConfig getRandomTPConfig() {
		return randomTPConfig;
	}

	public SkullConfig getSkullConfig() {
		return skullConfig;
	}

	public MarketConfig getMarketConfig() {
		return marketConfig;
	}

	public McbbsConfig getMcbbsConfig() {
		return mcbbsConfig;
	}

	public AutoMineChecker getAutoMineChecker() {
		return autoMineChecker;
	}

	public GuiManager getGuiManager() {
		return guiManager;
	}

	public long getCleanTime() {
		return cleanTimer;
	}

	public IAmNoob getSkillIAmNoob() {
		return skillIAmNoob;
	}
	public top.mrxiaom.pluginupdater.Main getUpdaterApi() {
		return updaterApi;
	}
	public ChapterManager getChapterManager() {
		return chapterManager;
	}
	public ProtocolManager getProtocolManager(){return protocolManager;}
}
