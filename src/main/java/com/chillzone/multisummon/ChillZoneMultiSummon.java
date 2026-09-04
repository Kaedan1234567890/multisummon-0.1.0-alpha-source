package com.chillzone.multisummon;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class ChillZoneMultiSummon implements ModInitializer {
    public static final String PERMISSION = "chillzonemultisummon.command.multisummon";
    public static final int MAX_COUNT = 500;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("multisummon")
                .requires(source -> hasPermission(source, PERMISSION))
                .then(Commands.argument("entity", StringArgumentType.word())
                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                        BuiltInRegistries.ENTITY_TYPE.keySet(), builder))
                    .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                        .executes(ctx -> summon(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "entity"),
                            IntegerArgumentType.getInteger(ctx, "count"),
                            null))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                            .executes(ctx -> summon(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "entity"),
                                IntegerArgumentType.getInteger(ctx, "count"),
                                Vec3Argument.getVec3(ctx, "pos")))))));
        });
    }

    private static boolean hasPermission(CommandSourceStack source, String permission) {
        // Console is allowed so the command remains usable from the server console.
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            return user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (IllegalStateException ignored) {
            // Minecraft 26.2 no longer exposes CommandSourceStack#hasPermission(int).
            // If LuckPerms is unavailable, fail closed for player sources.
            return false;
        }
    }

    private static int summon(CommandSourceStack source, String entityName, int count, Vec3 requestedPos) {
        Identifier id;
        try {
            id = Identifier.parse(entityName);
        } catch (Exception ignored) {
            source.sendFailure(Component.literal("Unknown entity: " + entityName));
            return 0;
        }
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            source.sendFailure(Component.literal("Unknown entity: " + entityName));
            return 0;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        if (type == null) {
            source.sendFailure(Component.literal("Unknown entity: " + entityName));
            return 0;
        }

        Vec3 pos = requestedPos != null ? requestedPos : source.getPosition();
        int spawned = 0;

        for (int i = 0; i < count; i++) {
            var entity = type.create(source.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (entity == null) {
                continue;
            }

            // Fix 2: every entity is placed at the exact same coordinates, with no random spread.
            entity.snapTo(pos.x, pos.y, pos.z, entity.getYRot(), entity.getXRot());
            if (source.getLevel().addFreshEntity(entity)) {
                spawned++;
            }
        }

        final int total = spawned;
        source.sendSuccess(() -> Component.literal(
            "Summoned " + total + " x " + id + " at " +
            String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z) + "."), true);

        return spawned > 0 ? Command.SINGLE_SUCCESS : 0;
    }
}
