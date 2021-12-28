package top.mrxiaom.doomsdayessentials.bot;

import me.albert.amazingbot.bot.Bot;
import me.albert.amazingbot.events.GroupMessageEvent;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.contact.file.AbsoluteFolder;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControlListener implements Listener {
    Main plugin;

    public ControlListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    BukkitTask downloadTask = null;
    // 等待从qq群下载到本地的文件
    List<AbsoluteFile> waitToDownload = new ArrayList<>();

    public static void reply(net.mamoe.mirai.event.events.GroupMessageEvent e, String msg) {
        reply(e, new PlainText(msg));
    }

    public static void reply(net.mamoe.mirai.event.events.GroupMessageEvent e, Message msg) {
        e.getGroup().sendMessage(new QuoteReply(e.getSource()).plus(msg));
    }

    public void download() {
        if (waitToDownload.isEmpty()) {
            downloadTask = null;
            return;
        }
        downloadTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AbsoluteFile f = waitToDownload.get(0);
            try {
                URL url = new URL(f.getUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                File path = plugin.getServerFile("upload/" + f.getName());
                path.createNewFile();
                RandomAccessFile file = new RandomAccessFile(path, "rw");
                InputStream stream = conn.getInputStream();
                byte buffer[] = new byte[1024];
                boolean canceled = false;
                while (true) {
                    if (downloadTask == null || downloadTask.isCancelled()) {
                        canceled = true;
                        break;
                    }
                    int len = stream.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    file.write(buffer, 0, len);
                }
                if (file != null) {
                    file.close();
                }
                if (stream != null) {
                    stream.close();
                }
                if (canceled) {
                    path.delete();
                    waitToDownload.clear();
                    downloadTask = null;
                    return;
                } else {
                    f.delete();
                    Bot.getApi().getBot().getGroup(690439164L).sendMessage("文件 " + path.getName() + " 已上传完成");
                }
            } catch (Throwable t) {
                Bot.getApi().getBot().getGroup(690439164L).sendMessage("下载qq群文件时出现一个异常 " + t.getLocalizedMessage());
                t.printStackTrace();
            }
            waitToDownload.remove(0);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                download();
            }, 40);
        });
    }

    @EventHandler
    public void onGroupMessage(GroupMessageEvent event) {
        if (event.getGroupID() == 690439164L && !event.getEvent().getSender().getPermission().equals(MemberPermission.MEMBER)) {
            net.mamoe.mirai.event.events.GroupMessageEvent e = event.getEvent();

            String[] args = event.getMsg().contains(" ") ? event.getMsg().split(" ") : new String[]{event.getMsg()};
            if (args.length == 0) return;

            if (args[0].equalsIgnoreCase("/help")) {
                reply(e, "末日社团 公共后台帮助\n" +
                        "格式: 命令根 <必选参数> [可选参数] (可选项1,可选项2) - 描述\n" +
                        "\n" +
                        " (/folder,/f) [路径] - 浏览文件夹\n" +
                        " (/open,/o) [--encode=编码] <路径> - (不支持文件夹)查看文件内容(默认编码UTF-8)\n" +
                        " (/upload,/u) start - 开始下载群文件upload文件夹里的文件到服务端根目录的upload文件夹中(上传成功了会删除群文件)\n" +
                        " (/upload,/u) stop - 终止下载\n" +
                        " (/download,/d) <路径> - (不支持文件夹)上传指定路径的文件到群文件download文件夹里\n" +
                        " --- [未完成 START] ----\n" +
                        " (/copy,/c) <源路径>|<目标路径> - (不支持文件夹)复制文件，如果目标路径文件存在，将会自动删除再进行复制\n" +
                        " (/delete,/del) <路径> - (不支持文件夹)删除指定文件，可通过换行来指定多个文件\n" +
                        " --- [未完成 END] ----\n" +
                        " sudo <命令> - 使用控制台权限执行命令\n" +
                        "\n" +
                        "常用控制台命令:\n" +
                        " lazycat reload - 重载基础插件\n" +
                        " pu update - 从plugins/PluginUpdater/DoomsdayEssentials-2.3.jar文件热更新基础插件\n" +
                        " rn (add,remove,set,get) <玩家> <数量> - 增加,减少,设置,获取玩家的复活针数量(获取时不需要“数量”参数)\n" +
                        " (broadcast,bc,alert) <消息> - 发布游戏内公告");
                return;
            }

            if (args[0].equalsIgnoreCase("/f") || args[0].equalsIgnoreCase("/folder")) {
                String pathString = args.length < 2 ? "/" : (args[1].startsWith("/") ? args[1] : ("/" + args[1]));
                for (int i = 2; i < args.length; i++) pathString += " " + args[i];
                plugin.getLogger().info("管理员 " + e.getSender().getNameCard() + " (" + e.getSender().getId() + ") 访问文件夹 " + pathString);
                File path = plugin.getServerFile(pathString);
                if (!path.exists()) {
                    reply(e, "【错误】路径指定的文件夹不存在\n" + pathString);
                    return;
                }
                if (!path.isDirectory()) {
                    reply(e, "【错误】该路径指定的不是文件夹\n" + pathString);
                    return;
                }
                final StringBuilder fullPath = new StringBuilder("当前位置: " + pathString + "\n文件列表: ");
                File[] files = path.listFiles();
                if (files.length == 0) {
                    fullPath.append("空");

                } else {
                    fullPath.append("共 " + files.length + " 个文件(夹)");
                    Arrays.stream(files).filter(file -> file.isDirectory()).forEach(file -> {
                        fullPath.append("\n[文件夹] " + file.getName());
                    });
                    Arrays.stream(files).filter(file -> !file.isDirectory()).forEach(file -> {
                        fullPath.append("\n" + file.getName());
                    });
                }
                ForwardMessageBuilder forward = new ForwardMessageBuilder(e.getGroup());
                forward.says(e.getBot(), fullPath.toString());
                e.getGroup().sendMessage(forward.build());
                return;
            }

            if (args[0].equalsIgnoreCase("/o") || args[0].equalsIgnoreCase("/open")) {
                String pathString = args.length < 2 ? "/" : (args.length > 3 && args[1].toLowerCase().startsWith("--encode=") ? args[2] : args[1]);
                String encode = args.length > 3 && args[1].toLowerCase().startsWith("--encode=") ? args[1].substring(9) : "UTF-8";

                for (int i = 2; i < args.length; i++) pathString += " " + args[i];
                plugin.getLogger().info("管理员 " + e.getSender().getNameCard() + " (" + e.getSender().getId() + ") 以 " + encode + " 编码打开文件 " + pathString);

                File path = plugin.getServerFile(pathString);
                if (!path.exists()) {
                    reply(e, "【错误】路径指定的文件不存在\n" + pathString);
                    return;
                }
                if (!path.isFile()) {
                    reply(e, "【错误】该路径指定的不是文件\n" + pathString);
                    return;
                }
                String content = Util.readFile(path, encode);
                ForwardMessageBuilder forward = new ForwardMessageBuilder(e.getGroup());
                forward.says(e.getBot(), "文件内容如下: (" + encode + " 编码)");
                forward.says(e.getBot(), content);
                e.getGroup().sendMessage(forward.build());
                return;
            }
            if (args[0].equalsIgnoreCase("/u") || args[0].equalsIgnoreCase("/upload")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("start")) {
                        if (!this.waitToDownload.isEmpty()) {
                            reply(e, "【错误】现在有正在进行中的上传任务，请先取消再试");
                            return;
                        }
                        AbsoluteFolder folder = e.getGroup().getFiles().getRoot().resolveFolder("upload");
                        folder.filesStream().forEach(file -> {
                            this.waitToDownload.add(file);
                        });
                        File uploadFolder = plugin.getServerFile("upload");
                        uploadFolder.mkdirs();
                        download();
                    }

                    if (args[1].equalsIgnoreCase("stop")) {
                        if (downloadTask == null || downloadTask.isCancelled()) {
                            reply(e, "【错误】没有进行中的上传任务");
                            return;
                        }
                        downloadTask.cancel();
                    }
                }
            }

            if (args[0].equalsIgnoreCase("/d") || args[0].equalsIgnoreCase("/download")) {
                if (args.length == 1) {
                    reply(e, "【错误】请键入文件路径");
                    return;
                }
                String pathString = args[1];
                for (int i = 3; i < args.length; i++) pathString += " " + args[i];
                File file = plugin.getServerFile(pathString);
                if (!file.isFile()) {
                    reply(e, "【错误】你只能上传文件，不能上传文件夹");
                    return;
                }
                reply(e, "正在下载 " + file.getName());
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        ExternalResource res = ExternalResource.create(file);
                        AbsoluteFile groupFile = e.getGroup().getFiles().getRoot().resolveFolder("download").uploadNewFile(file.getName(), res);
                        reply(e, "文件下载成功:\n" +
                                groupFile.getName() + "\n" +
                                "请到群文件中的 download 文件夹里下载，并尽快删除文件");
                    } catch (Throwable t) {
                        reply(e, "上传qq群文件时出现一个异常 " + t.getLocalizedMessage());
                        t.printStackTrace();
                    }
                });
            }
        }
    }
}
