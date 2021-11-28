package org.kilocraft.essentials.mixin.patch.technical;

import org.kilocraft.essentials.api.KiloEssentials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMixin {


    @Shadow
    private static void register(String string, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
    }

    @Inject(
            method = "bootStrap",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/arguments/selector/options/EntitySelectorOptions;register(Ljava/lang/String;Lnet/minecraft/commands/arguments/selector/options/EntitySelectorOptions$Modifier;Ljava/util/function/Predicate;Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 0
            )
    )
    private static void addPermissionOption(CallbackInfo ci) {
        register("permission", (entitySelectorReader) -> {
            boolean negate = entitySelectorReader.shouldInvertValue();
            String permission = entitySelectorReader.getReader().readUnquotedString();
            entitySelectorReader.addPredicate((entity) -> (KiloEssentials.hasPermissionNode(entity.createCommandSourceStack(), permission)) != negate);
        }, (entitySelectorReader) -> true, new TextComponent(""));
    }

}
