package net.sorenon.images.api;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.sorenon.images.init.ImagesModClient;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class Print {
    @Nullable
    public URL url;

    @Nullable
    public UUID player;

    public MutableText toText(@Nullable World world) {
        if (url != null) {
            String urlStr = url.toString();
            MutableText text = Texts.bracketed(new LiteralText(urlStr)).styled(style -> style.withClickEvent(
                    new ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            urlStr
                    )
            ).withHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new TranslatableText("images.open_or_copy_link")
                    )
            )).formatted(Formatting.GREEN);

            if (player != null) {
                PlayerEntity printer = null;
                if (world != null) {
                    printer = world.getPlayerByUuid(player);
                }
                String string;
                if (printer == null) {
                    string = player.toString();
                } else {
                    string = printer.getName().asString();
                }
                text.append(new LiteralText("->" + string).styled(style ->
                        style.withClickEvent(
                                new ClickEvent(
                                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                                        player.toString()
                                )
                        ).withHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new TranslatableText("images.copy_player")
                                )
                        )
                ).formatted(Formatting.WHITE));
            }
            return text;
        }
        return null;
    }

    public void serialize(CompoundTag nbt) {
        if (url != null) {
            nbt.putString("url", url.toString());
        }
        if (player != null) {
            nbt.putUuid("player", player);
        }
    }

    public void deserialize(CompoundTag nbt) {
        if (nbt == null) return;
        player = null;
        url = null;
        if (nbt.contains("player")) {
            player = nbt.getUuid("player");
        }
        if (nbt.contains("url", NbtType.STRING)) {
            try {
                url = new URL(nbt.getString("url"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public void appendTooltip(List<Text> tooltip, boolean isSneaking, int maxWidth, boolean waila) {
        if (url == null) return;
        String urlStr = url.toString();
        Style urlStyle = Style.EMPTY.withFormatting(Formatting.GREEN);
        Style playerStyle = Style.EMPTY.withFormatting(Formatting.GRAY);

        PlayerEntity printerEntity = null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (player != null && mc.world != null) {
            printerEntity = mc.world.getPlayerByUuid(player);
        }

        if (!isSneaking) {
            boolean showURL =
                    waila && ImagesModClient.Companion.getCFG_WAILA_URL() ||
                            !waila && ImagesModClient.Companion.getCFG_TOOLTIP_URL();
            boolean showPlayer =
                    waila && ImagesModClient.Companion.getCFG_WAILA_PLAYER() ||
                            !waila && ImagesModClient.Companion.getCFG_TOOLTIP_PLAYER();

            if (showURL) {
                if (urlStr.length() > 24) {
                    urlStr = urlStr.substring(0, 24) + '…';
                }
                tooltip.add(new LiteralText(urlStr).setStyle(urlStyle));
            }
            if (showPlayer && player != null) {
                if (printerEntity != null) {
                    tooltip.add(new TranslatableText("images.printed_by", printerEntity.getName()).setStyle(playerStyle));
                } else {
                    tooltip.add(new TranslatableText("images.printed_by_offline").setStyle(playerStyle));
                }
            }
        } else {
            TextHandler textHandler = mc.textRenderer.getTextHandler();
            while (urlStr.length() > 0) {
                int maxLen = textHandler.getTrimmedLength(urlStr, maxWidth, urlStyle);
                tooltip.add(new LiteralText(urlStr.substring(0, maxLen)).setStyle(urlStyle));
                urlStr = urlStr.substring(Math.min(maxLen, urlStr.length()));
            }

            if (player != null) {
                if (printerEntity == null) {
                    tooltip.add(new TranslatableText("images.printed_by", player.toString()).setStyle(playerStyle));
                } else {
                    tooltip.add(new TranslatableText("images.printed_by", printerEntity.getName()).setStyle(playerStyle));
                }
            }
        }
    }

    public Print copy() {
        Print clone = new Print();
        clone.url = url;
        clone.player = player;
        return clone;
    }
}
