package me.lauriichan.minecraft.pluginbase.command;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Action;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentParser;
import me.lauriichan.minecraft.pluginbase.util.BukkitColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class BukkitActor<P extends CommandSender> extends Actor<P> {

    public BukkitActor(final P handle, final MessageManager messageManager) {
        super(handle, messageManager);
    }

    @Override
    public UUID getId() {
        final Actor<Entity> actor = as(Entity.class);
        if (actor.isValid()) {
            return actor.getHandle().getUniqueId();
        }
        return IMPL_ID;
    }

    @Override
    public String getName() {
        final Actor<Entity> actor = as(Entity.class);
        if (actor.isValid()) {
            final Entity entity = actor.getHandle();
            if (entity.getCustomName() == null) {
                return entity.getName();
            }
            return entity.getCustomName();
        }
        return handle.getName();
    }

    @Override
    public void sendMessage(final String message) {
        handle.sendMessage(BukkitColor.apply(message));
    }
    
    public void sendBarMessage(final String message) {
        if (!(handle instanceof Player player)) {
            return;
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentParser.parse(message));
    }

    public void sendBarMessage(IMessage message, Key... placeholders) {
        sendBarMessage(messageManager.format(message, placeholders));
    }

    public void sendTranslatedBarMessage(MessageProvider provider, Key... placeholders) {
        sendBarMessage(messageManager.translate(provider, getLanguage(), placeholders));
    }

    public void sendTranslatedBarMessage(String messageId, Key... placeholders) {
        sendBarMessage(messageManager.translate(messageId, getLanguage(), placeholders));
    }

    @Override
    public void sendActionMessage(final ActionMessage message) {
        if (message == null) {
            return;
        }
        final String content = message.message();
        if (content == null || content.isBlank()) {
            handle.sendMessage("");
            return;
        }
        ClickEvent click = null;
        HoverEvent hover = null;
        if (message.clickAction() != null) {
            final Action clickAction = message.clickAction();
            switch (clickAction.getType()) {
            case CLICK_COPY:
                try {
                    click = new ClickEvent(ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD"), clickAction.getValueAsString());
                } catch (final IllegalArgumentException exp) {
                }
                break;
            case CLICK_FILE:
                click = new ClickEvent(ClickEvent.Action.OPEN_FILE, clickAction.getValueAsString());
                break;
            case CLICK_RUN:
                click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickAction.getValueAsString());
                break;
            case CLICK_SUGGEST:
                click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickAction.getValueAsString());
                break;
            case CLICK_URL:
                click = new ClickEvent(ClickEvent.Action.OPEN_URL, clickAction.getValueAsString());
                break;
            default:
                break;
            }
        }
        if (message.hoverAction() != null) {
            final Action hoverAction = message.hoverAction();
            switch (hoverAction.getType()) {
            case HOVER_TEXT:
                hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ComponentParser.parse(hoverAction.getValueAsString())));
                break;
            default:
                break;
            }
        }
        handle.spigot().sendMessage(ComponentParser.parse(message.message(), click, hover));
    }

    @Override
    public boolean hasPermission(final String permission) {
        return handle.hasPermission(permission);
    }

}