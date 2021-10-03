package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.simplecommand.ICommandNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(CommandNode.class)
public abstract class CommandNodeMixin<S> implements ICommandNode {

    @Shadow @Final private Map<String, LiteralCommandNode<S>> literals;

    @Shadow @Final private Map<String, CommandNode<S>> children;

    @Override
    public void removeLiteral(String literal) {
        this.children.remove(literal);
        this.literals.remove(literal);
    }

}
