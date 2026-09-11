package dev.matthiesen.cobblehardcoremon.common.interfaces;

import com.cobblemon.mod.common.pokemon.Pokemon;
import org.jetbrains.annotations.Nullable;

public record PartyEntry(@Nullable Pokemon pokemon, PokeHealthStatus healthStatus) {
}
