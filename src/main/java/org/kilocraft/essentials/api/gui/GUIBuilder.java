package org.kilocraft.essentials.api.gui;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.util.nbt.NBTTypes;
import org.kilocraft.essentials.util.text.Texter;

import java.util.List;
import java.util.Map;

public class GUIBuilder {
    private NamedScreenHandlerFactory factory;
    private Map<Integer, GUIButton> buttons;
    private int rows;
    private Text title;
    private ItemStack background;

    public GUIBuilder() {
        this.rows = 3;
        this.title = Texter.newRawText("GUI");
        this.buttons = Maps.newHashMap();
        this.background = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS);
        this.background.setCustomName(Texter.newText());
    }

    public GUIBuilder addButton(@NotNull final GUIButton button) {
        this.buttons.put(button.settings.slot, button);
        return this;
    }

    public GUIBuilder addButton(@NotNull final GUIButtonSettings settings) {
        return this.addButton(new GUIButton(settings));
    }

    public GUIBuilder setRows(final int rows) {
        if (rows > 6 || rows < 1) {
            throw new IllegalArgumentException("The rows for a GUI cannot be less than 1 and more than 6!");
        }

        this.rows = rows;
        return this;
    }

    public GUIBuilder titled(@NotNull final Text title) {
        Validate.notNull(title, "The title for a GUI cannot be null!");
        this.title = title;
        return this;
    }

    public GUIBuilder setBackground(@Nullable ItemStack background) {
        this.background = background == null ? new ItemStack(Items.AIR) : background;
        return this;
    }

    public NamedScreenHandlerFactory build() {
        this.factory = new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return title;
            }

            @Override
            public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return createMenuScreen(i, playerInventory, buttons);
            }
        };

        return this.factory;
    }

    private GUIScreen createMenuScreen(int sync, PlayerInventory inv, Map<Integer, GUIButton> buttons) {
        ScreenHandlerType<?> handlerType;
        switch (rows) {
            case 1:
                handlerType = ScreenHandlerType.GENERIC_9X1;
                break;
            case 2:
                handlerType = ScreenHandlerType.GENERIC_9X2;
                break;
            case 4:
                handlerType = ScreenHandlerType.GENERIC_9X4;
                break;
            case 5:
                handlerType = ScreenHandlerType.GENERIC_9X5;
                break;
            case 6:
                handlerType = ScreenHandlerType.GENERIC_9X6;
                break;
            default:
                handlerType = ScreenHandlerType.GENERIC_9X3;
                break;
        }

        GUIScreen screen = new GUIScreen(ScreenHandlerType.GENERIC_9X1, sync, inv, new SimpleInventory(rows * 9), rows, buttons);

        for (int i = 0; i < screen.slots.size(); i++) {
            screen.setStackInSlot(i, this.background);
        }

        this.buttons.forEach((slot, btn) -> screen.setStackInSlot(slot, btn.settings.icon));
        return screen;
    }

    public static class Button {
        private final GUIButtonSettings settings;

        public Button(final int slot, @NotNull final ItemStack icon) {
            this.settings = new GUIButtonSettings();
            this.settings.slot = slot;
            this.settings.icon = icon;
            this.settings.name = icon.hasCustomName() ? icon.getName() : Texter.newText();
        }

        public Button titled(@Nullable final String name) {
            return this.titled(Texter.newText(name));
        }

        public Button titled(@Nullable final Text text) {
            this.settings.name = text;
            this.settings.icon.setCustomName(text);
            return this;
        }

        public Button setEventAction(@NotNull final SlotActionType type, Runnable runnable) {
            switch (type) {
                case QUICK_MOVE:
                    this.settings.onQuickMove = runnable;
                    break;
                case PICKUP_ALL:
                    this.settings.onPickUpAll = runnable;
                    break;
                case PICKUP:
                    this.settings.onPickup = runnable;
                    break;
                case THROW:
                    this.settings.onThrow = runnable;
                    break;
                case CLONE:
                    this.settings.onClone = runnable;
                    break;
                case SWAP:
                    this.settings.onSwap = runnable;
                    break;
                default:
                    break;
            }

            return this;
        }

        public GUIButton build() {
            return new GUIButton(this.settings);
        }
    }

    public static class Icon {
        private final ItemStack icon;

        public Icon(@NotNull final ItemStack icon) {
            this.icon = icon;
        }

        public Icon titled(@Nullable final Text title) {
            this.icon.setCustomName(title);
            return this;
        }

        public Icon withLore(@NotNull final Map<Integer, Text> map) {
            map.forEach(this::withLore);
            return this;
        }

        public Icon withLore(@NotNull final List<Text> texts) {
            for (int i = 0; i < texts.size(); i++) {
                this.withLore(i, texts.get(i));
            }

            return this;
        }

        public Icon withLore(final int line, @Nullable final Text text) {
            CompoundTag tag = icon.getTag();
            if (tag == null) {
                tag = new CompoundTag();
            }

            if (!tag.contains("display")) {
                tag.put("display", new CompoundTag());
            }

            ListTag lore = tag.getCompound("display").getList("Lore", NBTTypes.STRING);
            if (lore == null) {
                lore = new ListTag();
            }

            if (line > lore.size() - 1) {
                for (int i = lore.size(); i <= line; i++) {
                    lore.add(StringTag.of(Text.Serializer.toJson(Texter.newText())));
                }
            }

            lore.set(line, StringTag.of(Text.Serializer.toJson(text)));
            tag.getCompound("display").put("Lore", lore);
            this.icon.setTag(tag);

            return this;
        }

        public ItemStack build() {
            return this.icon;
        }
    }
}
