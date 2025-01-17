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
package me.machinemaker.vanillatweaks.adventure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract implementation of a component builder.
 *
 * @param <C> the component type
 * @param <B> the builder type
 */
public abstract class AbstractComponentBuilder<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> implements ComponentBuilder<C, B> {
    // We use an empty list by default to prevent unnecessary list creation for components with no children
    protected List<Component> children = Collections.emptyList();
    /*
     * We maintain two separate fields here - a style, and style builder. If we're creating this component builder from
     * another component, or someone provides a style via style(Style), then we don't need a builder - unless someone later
     * calls one of the style modification methods in this builder, at which time we'll convert 'style' to a style builder.
     */
    /**
     * The style.
     */
    private @Nullable Style style;
    /**
     * The style builder.
     */
    private Style.@Nullable Builder styleBuilder;

    protected AbstractComponentBuilder() {
    }

    protected AbstractComponentBuilder(final @NotNull C component) {
        final List<Component> children = component.children();
        if (!children.isEmpty()) {
            this.children = new ArrayList<>(children);
        }
        if (component.hasStyling()) {
            this.style = component.style();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B append(final @NotNull Component component) {
        if (component == Component.empty()) return (B) this;
        this.prepareChildren();
        this.children.add(component);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B append(final @NotNull Component@NotNull... components) {
        boolean prepared = false;
        for (int i = 0, length = components.length; i < length; i++) {
            final Component component = components[i];
            if (component != Component.empty()) {
                if (!prepared) {
                    this.prepareChildren();
                    prepared = true;
                }
                this.children.add(component);
            }
        }
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B append(final @NotNull ComponentLike @NotNull... components) {
        boolean prepared = false;
        for (int i = 0, length = components.length; i < length; i++) {
            final Component component = components[i].asComponent();
            if (component != Component.empty()) {
                if (!prepared) {
                    this.prepareChildren();
                    prepared = true;
                }
                this.children.add(component);
            }
        }
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B append(final @NotNull Iterable<? extends ComponentLike> components) {
        boolean prepared = false;
        for (final ComponentLike like : components) {
            final Component component = like.asComponent();
            if (component != Component.empty()) {
                if (!prepared) {
                    this.prepareChildren();
                    prepared = true;
                }
                this.children.add(component);
            }
        }
        return (B) this;
    }

    private void prepareChildren() {
        if (this.children == Collections.<Component>emptyList()) {
            this.children = new ArrayList<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B applyDeep(final @NotNull Consumer<? super ComponentBuilder<?, ?>> consumer) {
        this.apply(consumer);
        if (this.children == Collections.<Component>emptyList()) {
            return (B) this;
        }
        final ListIterator<Component> it = this.children.listIterator();
        while (it.hasNext()) {
            final Component child = it.next();
            if (!(child instanceof BuildableComponent<?, ?>)) {
                continue;
            }
            final ComponentBuilder<?, ?> childBuilder = ((BuildableComponent<?, ?>) child).toBuilder();
            childBuilder.applyDeep(consumer);
            it.set(childBuilder.build());
        }
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B mapChildren(final @NotNull Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
        if (this.children == Collections.<Component>emptyList()) {
            return (B) this;
        }
        final ListIterator<Component> it = this.children.listIterator();
        while (it.hasNext()) {
            final Component child = it.next();
            if (!(child instanceof BuildableComponent<?, ?>)) {
                continue;
            }
            final BuildableComponent<?, ?> mappedChild = function.apply((BuildableComponent<?, ?>) child);
            if (child == mappedChild) {
                continue;
            }
            it.set(mappedChild);
        }
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B mapChildrenDeep(final @NotNull Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
        if (this.children == Collections.<Component>emptyList()) {
            return (B) this;
        }
        final ListIterator<Component> it = this.children.listIterator();
        while (it.hasNext()) {
            final Component child = it.next();
            if (!(child instanceof BuildableComponent<?, ?>)) {
                continue;
            }
            final BuildableComponent<?, ?> mappedChild = function.apply((BuildableComponent<?, ?>) child);
            if (mappedChild.children().isEmpty()) {
                if (child == mappedChild) {
                    continue;
                }
                it.set(mappedChild);
            } else {
                final ComponentBuilder<?, ?> builder = mappedChild.toBuilder();
                builder.mapChildrenDeep(function);
                it.set(builder.build());
            }
        }
        return (B) this;
    }

    @Override
    public @NotNull List<Component> children() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B style(final @NotNull Style style) {
        this.style = style;
        this.styleBuilder = null;
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B style(final @NotNull Consumer<Style.Builder> consumer) {
        consumer.accept(this.styleBuilder());
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B font(final @Nullable Key font) {
        this.styleBuilder().font(font);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B color(final @Nullable TextColor color) {
        this.styleBuilder().color(color);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B colorIfAbsent(final @Nullable TextColor color) {
        this.styleBuilder().colorIfAbsent(color);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B decoration(final @NotNull TextDecoration decoration, final TextDecoration.@NotNull State state) {
        this.styleBuilder().decoration(decoration, state);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B clickEvent(final @Nullable ClickEvent event) {
        this.styleBuilder().clickEvent(event);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B hoverEvent(final @Nullable HoverEventSource<?> source) {
        this.styleBuilder().hoverEvent(source);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B insertion(final @Nullable String insertion) {
        this.styleBuilder().insertion(insertion);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B mergeStyle(final @NotNull Component that, final @NotNull Set<Style.Merge> merges) {
        this.styleBuilder().merge(that.style(), merges);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B resetStyle() {
        this.style = null;
        this.styleBuilder = null;
        return (B) this;
    }

    private Style.@NotNull Builder styleBuilder() {
        if (this.styleBuilder == null) {
            if (this.style != null) {
                this.styleBuilder = this.style.toBuilder();
                this.style = null;
            } else {
                this.styleBuilder = Style.style();
            }
        }
        return this.styleBuilder;
    }

    protected final boolean hasStyle() {
        return this.styleBuilder != null || this.style != null;
    }

    protected @NotNull Style buildStyle() {
        if (this.styleBuilder != null) {
            return this.styleBuilder.build();
        } else if (this.style != null) {
            return this.style;
        } else {
            return Style.empty();
        }
    }
}
