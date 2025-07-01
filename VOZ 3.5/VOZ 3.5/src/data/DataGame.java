package data;

import encrypt.IconEncrypt;
import encrypt.ImageUtil;
import static encrypt.ImageUtil.encryptImage;
import static encrypt.ImageUtil.encryptString;
import static encrypt.ImageUtil.generateRandomKey;
import models.Template.HeadAvatar;
import models.Template.MapTemplate;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.FileIO;
import services.Service;
import skill.NClass;
import skill.Skill;
import models.Template.MobTemplate;
import models.Template.NpcTemplate;
import models.Template.SkillTemplate;
import network.inetwork.ISession;
import network.Message;
import java.io.IOException;
import server.Manager;
import server.io.MySession;
import utils.Logger;
import models.Template.BgItem;
import power.Caption;
import power.CaptionManager;
import utils.Util;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataGame {
    public static byte vsData = 9;
    public static byte vsMap = 2;
    public static byte vsSkill = 1;
    public static byte vsItem = 4;
    public static int vsRes = 4;
    public static short maxSmallVersion = 32767;
    
    public static String LINK_IP_PORT = "LOCAL:voz.dragon7super.io.vn:8686:0,0,0";
    public static Map MAP_MOUNT_NUM = new HashMap();
    private static final Map<Integer, byte[]> ICON_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, byte[]> BG_ITEM_CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    public static void sendVersionGame(MySession session) {
        Message msg = null;
        try {
            msg = Service.gI().messageNotMap((byte) 4);
            msg.writer().writeByte(vsData);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(vsItem);
            msg.writer().writeByte(0);
            
            List<Caption> captions = CaptionManager.getInstance().getCaptions();
            msg.writer().writeByte(captions.size());
            for (Caption caption : captions) {
                msg.writer().writeLong(caption.getPower());
            }
            session.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    // vData
    public static void updateData(MySession session) {
        byte[] dart = FileIO.readFile("data/update_data/dart");
        byte[] arrow = FileIO.readFile("data/update_data/arrow");
        byte[] effect = FileIO.readFile("data/update_data/effect");
        byte[] image = FileIO.readFile("data/update_data/image");
        byte[] part = FileIO.readFile("data/update_data/part");
        byte[] skill = FileIO.readFile("data/update_data/skill");
        
        Message msg = null;
        try {
            msg = new Message(-87);
            msg.writer().writeByte(vsData);
            
            msg.writer().writeInt(dart.length);
            msg.writer().write(dart);
            
            msg.writer().writeInt(arrow.length);
            msg.writer().write(arrow);
            
            msg.writer().writeInt(effect.length);
            msg.writer().write(effect);
            
            msg.writer().writeInt(image.length);
            msg.writer().write(image);
            
            msg.writer().writeInt(part.length);
            msg.writer().write(part);
            
            msg.writer().writeInt(skill.length);
            msg.writer().write(skill);
            
            session.doSendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    // vMap
    public static void updateMap(MySession session) {
        Message msg = null;
        try {
            msg = Service.gI().messageNotMap((byte) 6);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(Manager.MAP_TEMPLATES.length);
            for (MapTemplate temp : Manager.MAP_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
            }
            msg.writer().writeByte(Manager.NPC_TEMPLATES.size());
            for (NpcTemplate temp : Manager.NPC_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
                msg.writer().writeShort(temp.head);
                msg.writer().writeShort(temp.body);
                msg.writer().writeShort(temp.leg);
                msg.writer().writeByte(0);
            }
            msg.writer().writeByte(Manager.MOB_TEMPLATES.size());
            for (MobTemplate temp : Manager.MOB_TEMPLATES) {
                msg.writer().writeByte(temp.type);
                msg.writer().writeUTF(temp.name);
                msg.writer().writeInt(temp.hp);
                msg.writer().writeByte(temp.rangeMove);
                msg.writer().writeByte(temp.speed);
                msg.writer().writeByte(temp.dartType);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    // vSkill
    public static void updateSkill(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(7);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(0); // count skill option
            
            msg.writer().writeByte(Manager.NCLASS.size());
            for (NClass nClass : Manager.NCLASS) {
                msg.writer().writeUTF(nClass.name);
                msg.writer().writeByte(nClass.skillTemplatess.size());
                for (SkillTemplate skillTemp : nClass.skillTemplatess) {
                    msg.writer().writeByte(skillTemp.id);
                    msg.writer().writeUTF(skillTemp.name);
                    msg.writer().writeByte(skillTemp.maxPoint);
                    msg.writer().writeByte(skillTemp.manaUseType);
                    msg.writer().writeByte(skillTemp.type);
                    msg.writer().writeShort(skillTemp.iconId);
                    msg.writer().writeUTF(skillTemp.damInfo);
                    msg.writer().writeUTF("SoulMatee");
                    
                    if (skillTemp.id != 0) {
                        msg.writer().writeByte(skillTemp.skillss.size());
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                    } else {
                        // Thêm 2 skill trống 105, 106
                        msg.writer().writeByte(skillTemp.skillss.size() + 2);
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                        for (int i = 105; i <= 106; i++) {
                            msg.writer().writeShort(i);
                            msg.writer().writeByte(0);
                            msg.writer().writeLong(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeInt(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeByte(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeUTF("");
                        }
                    }
                }
            }
            session.doSendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendDataImageVersion(MySession session) {
        Message msg = null;
        try {
            // Phần code gửi dữ liệu image version hiện đang bị comment, nếu cần có thể mở lại
            // msg = new Message(-111);
            // msg.writer().writeShort(0);
            // msg.writer().writeUTF("NguyenDucVuEntertainment");
            // msg.writer().writeByte(0);
            // msg.writer().writeUTF("SoulMatee");
            // msg.writer().writeByte(1);
            // msg.writer().writeUTF("VuDangCapVaiLonRaMaBanDeoBietThoiDitMeBan");
            // msg.writer().writeByte(2);
            // session.doSendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendEffectTemplate(MySession session, int id, int... idtemp) {
        int idT = id;
        if (idtemp.length > 0 && idtemp[0] != 0) {
            idT = idtemp[0];
        }
        Message msg = null;
        try {
            final byte[] effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
            final byte[] effImg = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
            if (effData == null || effImg == null) {
                return;
            }
            msg = new Message(-66);
            msg.writer().writeShort(id);
            msg.writer().writeInt(effData.length);
            msg.writer().write(effData);
            if (session.version > 216) {
                msg.writer().write(idT == 60 ? 2 : 0);
            }
            msg.writer().writeInt(effImg.length);
            msg.writer().write(effImg);
            session.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendBgItemVersion(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-93);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeByte(bgItem.id);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendItemBGTemplate(MySession session, int id) {
        Message msg = null;
        try {
            final byte[] bg_temp = FileIO.readFile("data/item_bg_temp/x" + session.zoomLevel + "/" + id + ".png");
            if (bg_temp == null) {
                return;
            }
            msg = new Message(-32);
            msg.writer().writeShort(id);
            msg.writer().writeInt(bg_temp.length);
            msg.writer().write(bg_temp);
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendDataItemBG(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-31);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeShort(bgItem.idImage);
                msg.writer().writeByte(bgItem.layer);
                msg.writer().writeShort(bgItem.dx);
                msg.writer().writeShort(bgItem.dy);
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            // Không làm gì khi có lỗi
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendIcon(MySession session, int id) {
        Message msg = null;
        try {
            // Sử dụng cache để tránh đọc file nhiều lần
            byte[] icon = ICON_CACHE.computeIfAbsent(id, key -> FileIO.readFile("data/icon/x" + session.zoomLevel + "/" + id + ".png"));
            if (icon == null) return;
            msg = new Message(-67);
            msg.writer().writeInt(id);
            msg.writer().writeInt(icon.length);
            msg.writer().write(icon);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Có thể log lỗi nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendVersionRes(ISession session) {
        Message msg = null;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(0);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendSmallVersion(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-77);
            msg.writer().writeShort(maxSmallVersion);
            for (int i = 0; i < maxSmallVersion; i++) {
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void requestMobTemplate(MySession session, int id) {
        Message msg = null;
        try {
            final byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + id);
            msg = new Message(11);
            msg.writer().writeByte(id);
            msg.writer().write(mob);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendTileSetInfo(MySession session) {
        Message msg = null;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_set_info");
            msg = new Message(-82);
            msg.writer().write(data);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    // data vẽ map
    public static void sendMapTemp(MySession session, int id) {
        Message msg = null;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_map_data/" + id);
            if (data == null) {
                return;
            }
            msg = new Message(-28);
            msg.writer().writeByte(10);
            msg.writer().write(data);
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    // head-avatar
    public static void sendHeadAvatar(Message msg) {
        try {
            msg.writer().writeShort(Manager.HEAD_AVATARS.size());
            for (HeadAvatar ha : Manager.HEAD_AVATARS) {
                msg.writer().writeShort(ha.headId);
                msg.writer().writeShort(ha.avatarId);
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        }
    }
    
    public static void sendImageByName(MySession session, String imgName) {
        Message msg = null;
        try {
            msg = new Message(66);
            msg.writer().writeUTF(imgName);
            msg.writer().writeByte(Manager.getNFrameImageByName(imgName));
            final byte[] data = FileIO.readFile("data/img_by_name/x" + session.zoomLevel + "/" + imgName + ".png");
            msg.writer().writeInt(data.length);
            msg.writer().write(data);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendSizeRes(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(1);
            final File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                msg.writer().writeShort(files.length);
            } else {
                msg.writer().writeShort(0);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendRes(MySession session) {
        Message msg = null;
        try {
            File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                for (File fileEntry : files) {
                    String original = fileEntry.getName();
                    byte[] res = FileIO.readFile(fileEntry.getAbsolutePath());
                    msg = new Message(-74);
                    msg.writer().writeByte(2);
                    msg.writer().writeUTF(original);
                    msg.writer().writeInt(res.length);
                    msg.writer().write(res);
                    session.sendMessage(msg);
                    msg.cleanup();
                }
            }
            msg = new Message(-74);
            msg.writer().writeByte(3);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public static void sendLinkIP(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-29);
            msg.writer().writeByte(2);
            msg.writer().writeUTF(LINK_IP_PORT + ",0,0");
            msg.writer().writeByte(1);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
