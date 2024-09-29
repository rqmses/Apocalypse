package org.apocalypse;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apocalypse.api.command.Command;
import org.apocalypse.api.command.CommandExecutor;
import org.apocalypse.api.command.CommandImplementation;
import org.apocalypse.api.service.container.Container;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public final class Apocalypse extends JavaPlugin {

    private static Apocalypse INSTANCE;

    public static Apocalypse getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        Container.register();

        this.registerListener();
        this.registerCommands();
    }

    @Override
    public void onDisable() {

    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    private void registerListener() {
        for (Class<? extends Listener> listener : findClasses(Listener.class)) {
            this.getServer().getPluginManager().registerEvents(listener.getConstructor().newInstance(), getInstance());
            Bukkit.getLogger().info("Registered Listener: " + listener.getSimpleName());
        }
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        final Set<Class<? extends CommandExecutor>> classes = findClasses(CommandExecutor.class);
        Bukkit.getLogger().info("Found " + classes.size() + " commands.");
        CommandMap commandMap;
        final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        for (final Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Command.class)) {
                final Command command = clazz.getAnnotation(Command.class);
                final CommandExecutor commandExecutor = (CommandExecutor) clazz.getConstructor().newInstance();
                commandExecutor.setMain(this);
                final CommandImplementation commandImplementation = new CommandImplementation(command.name(), commandExecutor, command);
                commandImplementation.setAliases(Arrays.asList(command.aliases()));
                commandMap.register(command.group(), commandImplementation);
                this.injectServices(commandExecutor);
                Bukkit.getLogger().info("Registered Command: " + command.name());
            }
        }
    }

    @SneakyThrows
    private void injectServices(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                field.set(object, Container.get(field.getType()));
            }
        }
    }

    public static Set<Class<?>> findClasses(final String packageName) throws IOException {
        return ClassPath.from(Apocalypse.class.getClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName().startsWith(packageName))
                .map(ClassPath.ClassInfo::load)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> findClasses(final Class<T> superClass) throws IOException {
        return ClassPath.from(Apocalypse.class.getClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName().startsWith("org.apocalypse.core"))
                .map(ClassPath.ClassInfo::load)
                .filter(clazz -> superClass.isAssignableFrom(clazz) && !clazz.equals(superClass))
                .map(clazz -> (Class<? extends T>) clazz)
                .collect(Collectors.toSet());
    }
}
