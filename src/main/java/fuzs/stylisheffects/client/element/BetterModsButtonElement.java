package fuzs.stylisheffects.client.element;

import fuzs.puzzleslib.config.option.OptionsBuilder;
import fuzs.puzzleslib.element.AbstractElement;
import fuzs.puzzleslib.element.side.IClientElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Optional;

public class BetterModsButtonElement extends AbstractElement implements IClientElement {
    private MainMenuMode mainMenuMode;
    private boolean modCount;
    private PauseScreenMode pauseScreenMode;

    @Override
    public String[] getDescription() {
        return new String[0];
    }

    @Override
    public void constructClient() {
        this.addListener(this::onInitGui);
    }

    @Override
    public void setupClientConfig(OptionsBuilder builder) {
        builder.define("Main Menu Mods Button", MainMenuMode.BELOW_REALMS).comment("Where to place mods button on main menu screen.").sync(v -> this.mainMenuMode = v);
        builder.define("Mod Count", true).comment("Add mod count to mods button.").sync(v -> this.modCount = v);
        builder.define("Pause Screen Mods Button", PauseScreenMode.BELOW_BUGS).comment("Where to place mods button on pause menu screen.").sync(v -> this.pauseScreenMode = v);
    }

    private void onInitGui(final GuiScreenEvent.InitGuiEvent.Post evt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (evt.getGui() instanceof MainMenuScreen) {
            if (this.mainMenuMode == null) {
                return;
            }
            this.getButton(evt.getWidgetList(), "fml.menu.mods").ifPresent(evt::removeWidget);
            ObfuscationReflectionHelper.setPrivateValue(MainMenuScreen.class, (MainMenuScreen) evt.getGui(), NotificationModUpdateScreen.init((MainMenuScreen) evt.getGui(), null), "modUpdateNotification");
            final int buttonStart = evt.getGui().height / 4 + 48;
            switch (this.mainMenuMode) {
                case BELOW_REALMS:
                    for (Widget widget : evt.getWidgetList()) {
                        if (containsKey(widget, "menu.online")) {
                            widget.setWidth(200);
                            widget.x = evt.getGui().width / 2 - 100;
                        }
                        if (buttonStart + 72 + 12 <= widget.y) {
                            widget.y += 12;
                        } else {
                            widget.y -= 12;
                        }
                    }
                    evt.addWidget(new Button(evt.getGui().width / 2 - 100, buttonStart + 24 * 3 - 12, 200, 20, this.getModsComponent(this.modCount, false), button -> {
                        minecraft.setScreen(new ModListScreen(evt.getGui()));
                    }));
                    break;
                case NEXT_TO_REALMS:
                    for (Widget widget : evt.getWidgetList()) {
                        if (containsKey(widget, "menu.online")) {
                            widget.x = evt.getGui().width / 2 - 100;
                            break;
                        }
                    }
                    evt.addWidget(new Button(evt.getGui().width / 2 + 2, buttonStart + 24 * 2, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        minecraft.setScreen(new ModListScreen(evt.getGui()));
                    }));
                    break;
                case REPLACE_REALMS:
                    this.getButton(evt.getWidgetList(), "menu.online").ifPresent(evt::removeWidget);
                    evt.addWidget(new Button(evt.getGui().width / 2 - 100, buttonStart + 24 * 2, 200, 20, this.getModsComponent(this.modCount, false), button -> {
                        minecraft.setScreen(new ModListScreen(evt.getGui()));
                    }));
                    break;
            }
        } else if (evt.getGui() instanceof IngameMenuScreen) {
            if (this.pauseScreenMode == null) {
                return;
            }
            switch (this.pauseScreenMode) {
                case BELOW_BUGS:
                    for (Widget widget : evt.getWidgetList()) {
                        if (evt.getGui().height / 4 + 96 + -16 <= widget.y) {
                            widget.y += 12;
                        } else {
                            widget.y -= 12;
                        }
                    }
                    evt.addWidget(new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 96 + -16 - 12, 204, 20, this.getModsComponent(this.modCount, false), button -> {
                        minecraft.setScreen(new ModListScreen(evt.getGui()));
                    }));
                    break;
                case REPLACE_BUGS:
                    this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                    evt.addWidget(new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 72 + -16, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        minecraft.setScreen(new ModListScreen(evt.getGui()));
                    }));
                    break;
            }
        }
    }

    private Optional<Widget> getButton(List<Widget> widgets, String s) {
        for (Widget widget : widgets) {
            if (containsKey(widget, s)) {
                return Optional.of(widget);
            }
        }
        return Optional.empty();
    }

    private ITextComponent getModsComponent(boolean withCount, boolean compact) {
        IFormattableTextComponent component = new TranslationTextComponent("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(new TranslationTextComponent(translationKey, ModList.get().size()));
        }
        return component;
    }

    private static boolean containsKey(Widget button, String key) {
        final ITextComponent message = button.getMessage();
        return message instanceof TranslationTextComponent && ((TranslationTextComponent) message).getKey().equals(key);
    }

    private enum MainMenuMode {
        REPLACE_REALMS, BELOW_REALMS, NEXT_TO_REALMS
    }

    private enum PauseScreenMode {
        REPLACE_BUGS, BELOW_BUGS
    }
}
