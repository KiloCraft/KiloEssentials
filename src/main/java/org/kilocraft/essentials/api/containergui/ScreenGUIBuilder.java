/*
package org.kilocraft.essentials.api.containergui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.containergui.buttons.GUIButton;
import org.kilocraft.essentials.api.containergui.buttons.GUIButtonSettings;
import org.kilocraft.essentials.util.nbt.NBTTypes;
import org.kilocraft.essentials.util.text.Texter;

import java.util.List;
import java.util.Map;

public class ScreenGUIBuilder {
    private final Map<Integer, GUIButton> buttons;
    private int rows;
    private Text title;
    private ItemStack background;

    public ScreenGUIBuilder() {
        this.rows = 0;
        this.title = Texter.newRawText("Screen");
        this.buttons = Maps.newHashMap();
        this.setBackground(new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS).setCustomName(Texter.newText()));
    }

    public ScreenGUIBuilder addButtons(@NotNull final List<GUIButton> buttons) {
        for (GUIButton button : buttons) {
            this.addButton(button);
        }

        return this;
    }

    public ScreenGUIBuilder addButton(@NotNull final GUIButton button) {
        this.buttons.put(button.forcedSlot == -1 ? this.buttons.size() : button.forcedSlot, button);
        return this;
    }

    public ScreenGUIBuilder addButton(@NotNull final GUIButtonSettings settings) {
        return this.addButton(new GUIButton(settings));
    }

    public ScreenGUIBuilder fillRow(final int row, @Nullable final ItemStack stack) {
        validateRows(row);
        switch (row) {
            case 1:
                return this.fill(1, 9, stack);
            case 2:
                return this.fill(10, 19, stack);
            case 3:
                return this.fill(20, 29, stack);
            case 4:
                return this.fill(30, 39, stack);
            case 5:
                return this.fill(40, 49, stack);
            default:
                return this.fill(50, 59, stack);
        }
    }

    public ScreenGUIBuilder fill(final int x, final int z, @Nullable final Item item) {
        return this.fill(x, z, item == null ? Button.DUMMY.settings.icon : new ItemStack(item));
    }

    public ScreenGUIBuilder fill(final int x, final int z, @Nullable final ItemStack stack) {
        ItemStack itemStack = stack == null ? Button.DUMMY.settings.icon : stack;
        for (int i = 0; i < x; i++) {
            for (int i1 = 0; i1 < z; i1++) {
                int a = i * x + i1;
                this.buttons.put(a, new Button(itemStack).build());
            }
        }

        return this;
    }

    public ScreenGUIBuilder setRows(final int rows) {
        validateRows(rows);
        this.rows = rows;
        return this;
    }

    public ScreenGUIBuilder titled(@NotNull final String string) {
        return this.titled(Texter.newText(string));
    }

    public ScreenGUIBuilder titled(@NotNull final Text title) {
        Validate.notNull(title, "The title for a GUI cannot be null!");
        this.title = title;
        return this;
    }

    public ScreenGUIBuilder setBackground(@Nullable ItemStack background) {
        this.background = background == null ? Button.DUMMY.settings.icon : background;
        return this;
    }

    public NamedScreenHandlerFactory build() {
        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return ScreenGUIBuilder.this.title;
            }

            @Override
            public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return ScreenGUIBuilder.this.createHandledScreen(i, playerInventory, ScreenGUIBuilder.this.buttons);
            }
        };
    }

    public void handleFor(@NotNull final ServerPlayerEntity player) {
        player.openHandledScreen(this.build());
    }

    private GUIScreen createHandledScreen(int sync, PlayerInventory inv, Map<Integer, GUIButton> buttons) {
        final int totalRows = this.rows == 0 ? (this.buttons.size() / 9) + 1 : this.rows;
        final ScreenHandlerType<?> handlerType;
        switch (totalRows) {
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

        GUIScreen screen = new GUIScreen(handlerType, sync, inv, new SimpleInventory(this.rows * 9), this.rows, buttons);

        for (int i = 0; i < screen.slots.size() - 36; i++) {
            screen.setStackInSlot(i, screen.getRevision(), this.background);
        }

        this.buttons.forEach((slot, btn) -> screen.setStackInSlot(slot, screen.getRevision(), btn.settings.icon));
        return screen;
    }

    public static class Icon {
        private final ItemStack icon;
        private final List<Text> loreTexts;

        public Icon(@NotNull final Item icon) {
            this(new ItemStack(icon));
        }

        public Icon(@NotNull final ItemStack icon) {
            this.icon = icon;
            this.loreTexts = Lists.newArrayList();
        }

        public Icon titled(@Nullable final String title) {
            return this.titled(Texter.newText(title));
        }

        public Icon titled(@Nullable final Text title) {
            this.icon.setCustomName(Texter.newText().formatted(Formatting.RESET).append(title));
            return this;
        }

        public Icon addLore(@Nullable final Text text) {
            this.loreTexts.add(Texter.newText().formatted(Formatting.RESET).append(text));
            return this;
        }

        public Icon withLore(@NotNull final List<Text> texts) {
            this.loreTexts.addAll(texts);
            return this;
        }

        public ItemStack build() {
            if (this.loreTexts.isEmpty()) {
                return this.icon;
            }

            NbtCompound tag = this.icon.getNbt();
            if (tag == null) {
                tag = new NbtCompound();
            }

            if (!tag.contains("display")) {
                tag.put("display", new NbtCompound());
            }

            NbtList lore = tag.getCompound("display").getList("Lore", NBTTypes.STRING);
            if (lore == null) {
                lore = new NbtList();
            }

            for (Text loreText : this.loreTexts) {
                lore.add(NbtString.of(Text.Serializer.toJson(loreText)));
            }
            tag.getCompound("display").put("Lore", lore);
            this.icon.setNbt(tag);

            return this.icon;
        }
    }

    public static class Button {
        public static final GUIButton DUMMY = new GUIButton(GUIButtonSettings.dummy());
        private final GUIButtonSettings settings;
        private final int forcedSlot;

        public Button(@NotNull final Item icon) {
            this(-1, new ItemStack(icon));
        }

        public Button(@NotNull final ItemStack icon) {
            this(-1, icon);
        }

        public Button(final int forcedSlot, @NotNull final Item icon) {
            this(forcedSlot, new ItemStack(icon));
        }

        public Button(final int forcedSlot, @NotNull final ItemStack icon) {
            this.settings = new GUIButtonSettings();
            this.forcedSlot = forcedSlot;
            this.settings.icon = icon;
            this.settings.name = icon.hasCustomName() ? icon.getName() : Texter.newText();
        }

        public Button withClickAction(@NotNull final GUIButton.ClickAction action, @Nullable final Runnable runnable) {
            Validate.notNull(action, "ClickAction must not be null!");
            if (this.settings.actions == null) {
                this.settings.actions = Maps.newHashMap();
            }

            this.settings.actions.put(action, runnable);
            return this;
        }

        public GUIButton build() {
            return new GUIButton(this.forcedSlot, this.settings);
        }
    }

    private static void validateRows(final int rows) {
        if (rows > 6 || rows < 1) {
            throw new IllegalArgumentException("The rows for a GUI cannot be less than 1 and more than 6!");
        }
    }
}
*/
