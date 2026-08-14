package com.souflow.utils;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class LocaleUtil {
	
	public String getName(String nameVn, String nameEng) {
        Locale locale = LocaleContextHolder.getLocale();
        return locale.getLanguage().equals("vi") ? nameVn : nameEng;
    }
	
	public String getDescription(String descriptionVn, String descriptionEng) {
        Locale locale = LocaleContextHolder.getLocale();
        return locale.getLanguage().equals("vi") ? descriptionVn : descriptionEng;
    }
}
