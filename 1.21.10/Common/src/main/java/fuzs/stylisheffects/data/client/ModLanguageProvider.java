package fuzs.stylisheffects.data.client;

import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.stylisheffects.client.handler.EffectScreenHandlerImpl;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(EffectScreenHandlerImpl.KEY_DEBUG_MENU_TYPE, "Menu Type: %s");
    }
}
