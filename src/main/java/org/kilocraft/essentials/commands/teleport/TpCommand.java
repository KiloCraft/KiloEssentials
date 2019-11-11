package org.kilocraft.essentials.commands.teleport;

import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static org.kilocraft.essentials.KiloCommands.getCommandPermission;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.client.network.packet.PlayerPositionLookS2CPacket;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.DefaultPosArgument;
import net.minecraft.command.arguments.EntityAnchorArgumentType;
import net.minecraft.command.arguments.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.command.arguments.RotationArgumentType;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class TpCommand {

	// Vanilla tp command but with permissions
	public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
		LiteralCommandNode<ServerCommandSource> teleport = CommandManager.literal("ke_teleport")
				.requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport"), 2)).build();

		LiteralCommandNode<ServerCommandSource> tp = CommandManager.literal("ke_tp")
				.requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport"), 2)).redirect(teleport).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> targets = CommandManager.argument("targets",
				EntityArgumentType.entities()).build();

		ArgumentCommandNode<ServerCommandSource, PosArgument> location = CommandManager
				.argument("location", Vec3ArgumentType.vec3()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							EntityArgumentType.getEntities(commandContext_1, "targets"),
							((ServerCommandSource) commandContext_1.getSource()).getWorld(),
							Vec3ArgumentType.getPosArgument(commandContext_1, "location"), (PosArgument) null,
							(TpCommand.LookTarget) null);
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.others"), 2)).build();

		ArgumentCommandNode<ServerCommandSource, PosArgument> rotation = CommandManager
				.argument("rotation", RotationArgumentType.rotation()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							EntityArgumentType.getEntities(commandContext_1, "targets"),
							((ServerCommandSource) commandContext_1.getSource()).getWorld(),
							Vec3ArgumentType.getPosArgument(commandContext_1, "location"),
							RotationArgumentType.getRotation(commandContext_1, "rotation"),
							(TpCommand.LookTarget) null);
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.others"), 2)).build();

		LiteralCommandNode<ServerCommandSource> facing = CommandManager.literal("facing").build();
		LiteralCommandNode<ServerCommandSource> entity = CommandManager.literal("entity").build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> facingEntity = CommandManager
				.argument("facingEntity", EntityArgumentType.entity()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							EntityArgumentType.getEntities(commandContext_1, "targets"),
							((ServerCommandSource) commandContext_1.getSource()).getWorld(),
							Vec3ArgumentType.getPosArgument(commandContext_1, "location"), (PosArgument) null,
							new TpCommand.LookTarget(EntityArgumentType.getEntity(commandContext_1, "facingEntity"),
									EntityAnchorArgumentType.EntityAnchor.FEET));
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.others"), 2)).build();

		ArgumentCommandNode<ServerCommandSource, EntityAnchor> facingAnchor = CommandManager
				.argument("facingAnchor", EntityAnchorArgumentType.entityAnchor()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							EntityArgumentType.getEntities(commandContext_1, "targets"),
							((ServerCommandSource) commandContext_1.getSource()).getWorld(),
							Vec3ArgumentType.getPosArgument(commandContext_1, "location"), (PosArgument) null,
							new TpCommand.LookTarget(EntityArgumentType.getEntity(commandContext_1, "facingEntity"),
									EntityAnchorArgumentType.getEntityAnchor(commandContext_1, "facingAnchor")));
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.others"), 2)).build();

		ArgumentCommandNode<ServerCommandSource, PosArgument> facingLocation = CommandManager
				.argument("facingLocation", Vec3ArgumentType.vec3()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							EntityArgumentType.getEntities(commandContext_1, "targets"),
							((ServerCommandSource) commandContext_1.getSource()).getWorld(),
							Vec3ArgumentType.getPosArgument(commandContext_1, "location"), (PosArgument) null,
							new TpCommand.LookTarget(Vec3ArgumentType.getVec3(commandContext_1, "facingLocation")));
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.others"), 2)).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> destination = CommandManager
				.argument("destination", EntityArgumentType.entity()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							EntityArgumentType.getEntities(commandContext_1, "targets"),
							EntityArgumentType.getEntity(commandContext_1, "destination"));
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.others"), 2)).build();

		ArgumentCommandNode<ServerCommandSource, PosArgument> location2 = CommandManager
				.argument("location", Vec3ArgumentType.vec3()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							Collections
									.singleton(((ServerCommandSource) commandContext_1.getSource()).getEntityOrThrow()),
							((ServerCommandSource) commandContext_1.getSource()).getWorld(),
							Vec3ArgumentType.getPosArgument(commandContext_1, "location"), DefaultPosArgument.zero(),
							(TpCommand.LookTarget) null);
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.tolocation"), 2)).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> destination2 = CommandManager
				.argument("destination", EntityArgumentType.entity()).executes((commandContext_1) -> {
					return execute((ServerCommandSource) commandContext_1.getSource(),
							Collections
									.singleton(((ServerCommandSource) commandContext_1.getSource()).getEntityOrThrow()),
							EntityArgumentType.getEntity(commandContext_1, "destination"));
				}).requires(src -> hasPermissionOrOp(src, getCommandPermission("teleport.toentity"), 2)).build();

		facingEntity.addChild(facingAnchor);
		entity.addChild(facingEntity);
		facing.addChild(facingLocation);
		facing.addChild(entity);	
		location.addChild(facing);
		location.addChild(rotation);
		targets.addChild(location);
		targets.addChild(destination);	
		teleport.addChild(location2);
		teleport.addChild(destination2);
		commandDispatcher.getRoot().addChild(teleport);
		commandDispatcher.getRoot().addChild(tp);
	}

	private static int execute(ServerCommandSource serverCommandSource_1, Collection<? extends Entity> collection_1,
			Entity entity_1) {
		Iterator<? extends Entity> var3 = collection_1.iterator();

		while (var3.hasNext()) {
			Entity entity_2 = (Entity) var3.next();
			teleport(serverCommandSource_1, entity_2, (ServerWorld) entity_1.world, entity_1.getX(), entity_1.getY(),
					entity_1.getZ(), EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class), entity_1.yaw,
					entity_1.pitch, (TpCommand.LookTarget) null);
		}

		if (collection_1.size() == 1) {
			serverCommandSource_1.sendFeedback(
					new TranslatableText("commands.teleport.success.entity.single", new Object[] {
							((Entity) collection_1.iterator().next()).getDisplayName(), entity_1.getDisplayName() }),
					true);
		} else {
			serverCommandSource_1.sendFeedback(new TranslatableText("commands.teleport.success.entity.multiple",
					new Object[] { collection_1.size(), entity_1.getDisplayName() }), true);
		}

		return collection_1.size();
	}

	private static int execute(ServerCommandSource serverCommandSource_1, Collection<? extends Entity> collection_1,
			ServerWorld serverWorld_1, PosArgument posArgument_1, PosArgument posArgument_2,
			TpCommand.LookTarget teleportCommand$LookTarget_1) throws CommandSyntaxException {
		Vec3d vec3d_1 = posArgument_1.toAbsolutePos(serverCommandSource_1);
		Vec2f vec2f_1 = posArgument_2 == null ? null : posArgument_2.toAbsoluteRotation(serverCommandSource_1);
		Set<PlayerPositionLookS2CPacket.Flag> set_1 = EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class);
		if (posArgument_1.isXRelative()) {
			set_1.add(PlayerPositionLookS2CPacket.Flag.X);
		}

		if (posArgument_1.isYRelative()) {
			set_1.add(PlayerPositionLookS2CPacket.Flag.Y);
		}

		if (posArgument_1.isZRelative()) {
			set_1.add(PlayerPositionLookS2CPacket.Flag.Z);
		}

		if (posArgument_2 == null) {
			set_1.add(PlayerPositionLookS2CPacket.Flag.X_ROT);
			set_1.add(PlayerPositionLookS2CPacket.Flag.Y_ROT);
		} else {
			if (posArgument_2.isXRelative()) {
				set_1.add(PlayerPositionLookS2CPacket.Flag.X_ROT);
			}

			if (posArgument_2.isYRelative()) {
				set_1.add(PlayerPositionLookS2CPacket.Flag.Y_ROT);
			}
		}

		Iterator<? extends Entity> var9 = collection_1.iterator();

		while (var9.hasNext()) {
			Entity entity_1 = (Entity) var9.next();
			if (posArgument_2 == null) {
				teleport(serverCommandSource_1, entity_1, serverWorld_1, vec3d_1.x, vec3d_1.y, vec3d_1.z, set_1,
						entity_1.yaw, entity_1.pitch, teleportCommand$LookTarget_1);
			} else {
				teleport(serverCommandSource_1, entity_1, serverWorld_1, vec3d_1.x, vec3d_1.y, vec3d_1.z, set_1,
						vec2f_1.y, vec2f_1.x, teleportCommand$LookTarget_1);
			}
		}

		if (collection_1.size() == 1) {
			serverCommandSource_1.sendFeedback(new TranslatableText("commands.teleport.success.location.single",
					new Object[] { ((Entity) collection_1.iterator().next()).getDisplayName(), vec3d_1.x, vec3d_1.y,
							vec3d_1.z }),
					true);
		} else {
			serverCommandSource_1.sendFeedback(new TranslatableText("commands.teleport.success.location.multiple",
					new Object[] { collection_1.size(), vec3d_1.x, vec3d_1.y, vec3d_1.z }), true);
		}

		return collection_1.size();
	}

	private static void teleport(ServerCommandSource serverCommandSource_1, Entity entity_1, ServerWorld serverWorld_1,
			double double_1, double double_2, double double_3, Set<PlayerPositionLookS2CPacket.Flag> set_1,
			float float_1, float float_2, TpCommand.LookTarget teleportCommand$LookTarget_1) {
		if (entity_1 instanceof ServerPlayerEntity) {
			ChunkPos chunkPos_1 = new ChunkPos(new BlockPos(double_1, double_2, double_3));
			serverWorld_1.method_14178().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos_1, 1,
					entity_1.getEntityId());
			entity_1.stopRiding();
			if (((ServerPlayerEntity) entity_1).isSleeping()) {
				((ServerPlayerEntity) entity_1).wakeUp(true, true);
			}

			if (serverWorld_1 == entity_1.world) {
				((ServerPlayerEntity) entity_1).networkHandler.teleportRequest(double_1, double_2, double_3, float_1,
						float_2, set_1);
			} else {
				((ServerPlayerEntity) entity_1).teleport(serverWorld_1, double_1, double_2, double_3, float_1, float_2);
			}

			entity_1.setHeadYaw(float_1);
		} else {
			float float_3 = MathHelper.wrapDegrees(float_1);
			float float_4 = MathHelper.wrapDegrees(float_2);
			float_4 = MathHelper.clamp(float_4, -90.0F, 90.0F);
			if (serverWorld_1 == entity_1.world) {
				entity_1.setPositionAndAngles(double_1, double_2, double_3, float_3, float_4);
				entity_1.setHeadYaw(float_3);
			} else {
				entity_1.detach();
				entity_1.dimension = serverWorld_1.dimension.getType();
				Entity entity_2 = entity_1;
				entity_1 = entity_1.getType().create(serverWorld_1);
				if (entity_1 == null) {
					return;
				}

				entity_1.copyFrom(entity_2);
				entity_1.setPositionAndAngles(double_1, double_2, double_3, float_3, float_4);
				entity_1.setHeadYaw(float_3);
				serverWorld_1.method_18769(entity_1);
				entity_2.removed = true;
			}
		}

		if (teleportCommand$LookTarget_1 != null) {
			teleportCommand$LookTarget_1.look(serverCommandSource_1, entity_1);
		}

		if (!(entity_1 instanceof LivingEntity) || !((LivingEntity) entity_1).isFallFlying()) {
			entity_1.setVelocity(entity_1.getVelocity().multiply(1.0D, 0.0D, 1.0D));
			entity_1.onGround = true;
		}

	}

	static class LookTarget {
		private final Vec3d targetPos;
		private final Entity targetEntity;
		private final EntityAnchorArgumentType.EntityAnchor targetEntityAnchor;

		public LookTarget(Entity entity_1,
				EntityAnchorArgumentType.EntityAnchor entityAnchorArgumentType$EntityAnchor_1) {
			this.targetEntity = entity_1;
			this.targetEntityAnchor = entityAnchorArgumentType$EntityAnchor_1;
			this.targetPos = entityAnchorArgumentType$EntityAnchor_1.positionAt(entity_1);
		}

		public LookTarget(Vec3d vec3d_1) {
			this.targetEntity = null;
			this.targetPos = vec3d_1;
			this.targetEntityAnchor = null;
		}

		public void look(ServerCommandSource serverCommandSource_1, Entity entity_1) {
			if (this.targetEntity != null) {
				if (entity_1 instanceof ServerPlayerEntity) {
					((ServerPlayerEntity) entity_1).method_14222(serverCommandSource_1.getEntityAnchor(),
							this.targetEntity, this.targetEntityAnchor);
				} else {
					entity_1.lookAt(serverCommandSource_1.getEntityAnchor(), this.targetPos);
				}
			} else {
				entity_1.lookAt(serverCommandSource_1.getEntityAnchor(), this.targetPos);
			}

		}
	}
}
