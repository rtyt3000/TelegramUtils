package in.ppsh.goidaworld.telegramUtils.telegram;

import io.github.natanimn.filters.CustomFilter;
import io.github.natanimn.types.updates.Update;

public class CallbackQueryButtonFilter implements CustomFilter {
    @Override
    public boolean check(Update update) {
        return update.callback_query.data.startsWith("accept:") || update.callback_query.data.startsWith("reject:");
    }

}
