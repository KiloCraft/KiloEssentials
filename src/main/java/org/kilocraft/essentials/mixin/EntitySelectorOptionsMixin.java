package org.kilocraft.essentials.mixin;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMixin {

    @Shadow
    protected static void putOption(String string, EntitySelectorOptions.SelectorHandler selectorHandler, Predicate<EntitySelectorReader> predicate, Text text) {
    }

    @Inject(method = "register", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/command/EntitySelectorOptions;putOption(Ljava/lang/String;Lnet/minecraft/command/EntitySelectorOptions$SelectorHandler;Ljava/util/function/Predicate;Lnet/minecraft/text/Text;)V",
            ordinal = 0), cancellable = true)
    private static void addPermissionOption(CallbackInfo ci) {
        putOption("permission", (entitySelectorReader) -> {
            boolean negate = entitySelectorReader.readNegationCharacter();
            String permission = entitySelectorReader.getReader().readUnquotedString();
            entitySelectorReader.setPredicate((entity) -> (entity instanceof ServerPlayerEntity &&
                    KiloEssentials.getInstance().getPermissionUtil()
                            .hasPermission(entity.getCommandSource(), permission, 2))
                    != negate);
        }, (entitySelectorReader) -> true, new LiteralText(""));
    }

}
