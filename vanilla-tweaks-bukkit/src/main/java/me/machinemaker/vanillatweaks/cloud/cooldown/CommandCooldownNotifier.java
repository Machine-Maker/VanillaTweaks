/*
 * GNU General Public License v3
 *
 * VanillaTweaks, a performant replacement for the VanillaTweaks datapacks.
 *
 * Copyright (C) 2021 Machine_Maker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.machinemaker.vanillatweaks.cloud.cooldown;

import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;

@FunctionalInterface
public interface CommandCooldownNotifier<C> {

    static <C> @NonNull CommandCooldownNotifier<C> empty() {
        return (context, cooldown, secondsLeft) -> {};
    }

    void notify(@NonNull CommandPostprocessingContext<C> context, @NonNull Duration cooldown, long secondsLeft);
}
