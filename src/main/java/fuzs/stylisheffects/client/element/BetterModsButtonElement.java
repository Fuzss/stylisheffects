package fuzs.stylisheffects.client.element;

import fuzs.puzzleslib.config.option.OptionsBuilder;
import fuzs.puzzleslib.element.AbstractElement;
import fuzs.puzzleslib.element.side.IClientElement;
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
    private boolean updateNotification;

    private NotificationModUpdateScreen gameMenuNotification;

    @Override
    public String[] getDescription() {
        return new String[]{"Your mods button your way.", "Your mods button, where you want it, and how you want it."};
    }

    @Override
    public void constructClient() {
        this.addListener(this::onInitGui);
        this.addListener(this::onDrawScreen);
    }

    @Override
    public void setupClientConfig(OptionsBuilder builder) {
        builder.define("Main Menu Mods Button", MainMenuMode.ABOVE_OPTIONS).comment("Where to place mods button on main menu screen.").sync(v -> this.mainMenuMode = v);
        builder.define("Mod Count", true).comment("Add mod count to mods button.").sync(v -> this.modCount = v);
        builder.define("Pause Screen Mods Button", PauseScreenMode.ABOVE_OPTIONS).comment("Where to place mods button on pause menu screen.").sync(v -> this.pauseScreenMode = v);
        builder.define("Update Notification", false).comment("Show a small green orb when mod updates are available.").sync(v -> this.updateNotification = v);
    }

    private void onInitGui(final GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui() instanceof MainMenuScreen) {
            if (this.mainMenuMode == null) {
                return;
            }
            this.getButton(evt.getWidgetList(), "fml.menu.mods").ifPresent(evt::removeWidget);
            Button modsButton = null;
            switch (this.mainMenuMode) {
                case ABOVE_OPTIONS:
                    for (Widget widget : evt.getWidgetList()) {
                        if (evt.getGui().height / 4 + 48 + 72 + 12 <= widget.y) {
                            widget.y += 12;
                        } else {
                            widget.y -= 12;
                        }
                    }
                    modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 3 - 12, 200, 20, this.getModsComponent(this.modCount, false), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    // break missing on purpose
                case NONE:
                    this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> {
                        widget.setWidth(200);
                        widget.x = evt.getGui().width / 2 - 100;
                    });
                    break;
                case LEFT_TO_REALMS:
                    modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
                case RIGHT_TO_REALMS:
                    this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> widget.x = evt.getGui().width / 2 - 100);
                    modsButton = new Button(evt.getGui().width / 2 + 2, evt.getGui().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
                case REPLACE_REALMS:
                    this.getButton(evt.getWidgetList(), "menu.online").ifPresent(evt::removeWidget);
                    modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 2, 200, 20, this.getModsComponent(this.modCount, false), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
            }
            if (modsButton != null) evt.addWidget(modsButton);
            ObfuscationReflectionHelper.setPrivateValue(MainMenuScreen.class, (MainMenuScreen) evt.getGui(), NotificationModUpdateScreen.init((MainMenuScreen) evt.getGui(), this.updateNotification ? modsButton : null), "modUpdateNotification");
        } else if (evt.getGui() instanceof IngameMenuScreen) {
            if (this.pauseScreenMode == null) {
                return;
            }
            Button modsButton = null;
            switch (this.pauseScreenMode) {
                case ABOVE_OPTIONS:
                    for (Widget widget : evt.getWidgetList()) {
                        if (evt.getGui().height / 4 + 96 + -16 <= widget.y) {
                            widget.y += 12;
                        } else {
                            widget.y -= 12;
                        }
                    }
                    modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 96 + -16 - 12, 204, 20, this.getModsComponent(this.modCount, false), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
                case REPLACE_FEEDBACK:
                    this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                    modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 72 + -16, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
                case REPLACE_BUGS:
                    this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                    modsButton = new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 72 + -16, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
                case REPLACE_FEEDBACK_AND_BUGS:
                    this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                    this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                    modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 72 + -16, 204, 20, this.getModsComponent(this.modCount, false), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
                case MOVE_LAN:
                    this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                    this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                    this.getButton(evt.getWidgetList(), "menu.shareToLan").ifPresent(widget -> {
                        widget.setWidth(204);
                        widget.x = evt.getGui().width / 2 - 102;
                        widget.y = evt.getGui().height / 4 + 72 + -16;
                    });
                    modsButton = new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 96 + -16, 98, 20, this.getModsComponent(this.modCount, true), button -> {
                        evt.getGui().getMinecraft().setScreen(new ModListScreen(evt.getGui()));
                    });
                    break;
            }
            if (modsButton != null) evt.addWidget(modsButton);
            this.gameMenuNotification = new NotificationModUpdateScreen(this.updateNotification ? modsButton : null);
            this.gameMenuNotification.resize(evt.getGui().getMinecraft(), evt.getGui().width, evt.getGui().height);
            this.gameMenuNotification.init();
        }
    }

    private void onDrawScreen(final GuiScreenEvent.DrawScreenEvent evt) {
        if (evt.getGui() instanceof IngameMenuScreen) {
            this.gameMenuNotification.render(evt.getMatrixStack(), evt.getMouseX(), evt.getMouseY(), evt.getRenderPartialTicks());
        }
    }

    private Optional<Widget> getButton(List<Widget> widgets, String s) {
        for (Widget widget : widgets) {
            if (this.containsKey(widget, s)) {
                return Optional.of(widget);
            }
        }
        return Optional.empty();
    }

    private boolean containsKey(Widget button, String key) {
        final ITextComponent message = button.getMessage();
        return message instanceof TranslationTextComponent && ((TranslationTextComponent) message).getKey().equals(key);
    }

    private ITextComponent getModsComponent(boolean withCount, boolean compact) {
        IFormattableTextComponent component = new TranslationTextComponent("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(new TranslationTextComponent(translationKey, ModList.get().size()));
        }
        return component;
    }

    private enum MainMenuMode {
        REPLACE_REALMS, LEFT_TO_REALMS, RIGHT_TO_REALMS, ABOVE_OPTIONS, NONE
    }

    private enum PauseScreenMode {
        REPLACE_FEEDBACK, REPLACE_BUGS, REPLACE_FEEDBACK_AND_BUGS, MOVE_LAN, ABOVE_OPTIONS, NONE
    }
}
