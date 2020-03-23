package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.inventory.UserInventory;
import org.kilocraft.essentials.api.world.location.Location;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class NeverJoinedUser implements org.kilocraft.essentials.api.user.NeverJoinedUser {
}
