package me.proton.core.compose.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.use
import me.proton.core.presentation.compose.R

@Composable
fun rememberColorsFromXml(
    context: Context = LocalContext.current,
    isSystemDark: Boolean = isSystemInDarkTheme()
): ProtonColors = remember(context.theme) {
    colorsFromXml(context, isSystemDark)
}

@Suppress("LongMethod")
fun colorsFromXml(
    context: Context,
    isSystemDark: Boolean
): ProtonColors {
    fun TypedArray.getColor(@StyleableRes key: Int): Color = Color(getColorOrThrow(key))
    return context.obtainStyledAttributes(R.styleable.AppTheme).use { typedArray ->
        ProtonColors(
            isDark = isSystemDark,

            shade100 = Color(context.getColor(R.color.shade_100)),
            shade80 = Color(context.getColor(R.color.shade_80)),
            shade60 = Color(context.getColor(R.color.shade_60)),
            shade50 = Color(context.getColor(R.color.shade_50)),
            shade40 = Color(context.getColor(R.color.shade_40)),
            shade20 = Color(context.getColor(R.color.shade_20)),
            shade10 = Color(context.getColor(R.color.shade_10)),
            shade0 = Color(context.getColor(R.color.shade_0)),

            textNorm = typedArray.getColor(R.styleable.AppTheme_proton_text_norm),
            textWeak = typedArray.getColor(R.styleable.AppTheme_proton_text_weak),
            textHint = typedArray.getColor(R.styleable.AppTheme_proton_text_hint),
            textDisabled = typedArray.getColor(R.styleable.AppTheme_proton_text_disabled),
            textInverted = typedArray.getColor(R.styleable.AppTheme_proton_text_inverted),

            iconNorm = typedArray.getColor(R.styleable.AppTheme_proton_icon_norm),
            iconWeak = typedArray.getColor(R.styleable.AppTheme_proton_icon_weak),
            iconHint = typedArray.getColor(R.styleable.AppTheme_proton_icon_hint),
            iconDisabled = typedArray.getColor(R.styleable.AppTheme_proton_icon_disabled),
            iconInverted = typedArray.getColor(R.styleable.AppTheme_proton_icon_inverted),

            interactionStrongNorm = typedArray.getColor(R.styleable.AppTheme_proton_interaction_strong),
            interactionStrongPressed = typedArray.getColor(R.styleable.AppTheme_proton_interaction_strong_pressed),

            interactionWeakNorm = typedArray.getColor(R.styleable.AppTheme_proton_interaction_weak),
            interactionWeakPressed = typedArray.getColor(R.styleable.AppTheme_proton_interaction_weak_pressed),
            interactionWeakDisabled = typedArray.getColor(R.styleable.AppTheme_proton_interaction_weak_disabled),

            backgroundNorm = typedArray.getColor(R.styleable.AppTheme_proton_background_norm),
            backgroundSecondary = typedArray.getColor(R.styleable.AppTheme_proton_background_secondary),

            separatorNorm = typedArray.getColor(R.styleable.AppTheme_proton_separator_norm),

            blenderNorm = typedArray.getColor(R.styleable.AppTheme_proton_blender_norm),

            brandDarken40 = typedArray.getColor(R.styleable.AppTheme_brand_darken_40),
            brandDarken20 = typedArray.getColor(R.styleable.AppTheme_brand_darken_20),
            brandNorm = typedArray.getColor(R.styleable.AppTheme_brand_norm),
            brandLighten20 = typedArray.getColor(R.styleable.AppTheme_brand_lighten_20),
            brandLighten40 = typedArray.getColor(R.styleable.AppTheme_brand_lighten_40),

            notificationNorm = Color(context.getColor(R.color.shade_100)),
            notificationError = typedArray.getColor(R.styleable.AppTheme_proton_notification_error),
            notificationSuccess = typedArray.getColor(R.styleable.AppTheme_proton_notification_success),
            notificationWarning = typedArray.getColor(R.styleable.AppTheme_proton_notification_warning),

            interactionNorm = typedArray.getColor(R.styleable.AppTheme_proton_interaction_norm),
            interactionPressed = typedArray.getColor(R.styleable.AppTheme_proton_interaction_norm_pressed),
            interactionDisabled = typedArray.getColor(R.styleable.AppTheme_proton_interaction_norm_disabled),

            floatyBackground = (if (isSystemDark) ProtonColors.Dark else ProtonColors.Light).floatyBackground,
            floatyPressed = (if (isSystemDark) ProtonColors.Dark else ProtonColors.Light).floatyPressed,
            floatyText = (if (isSystemDark) ProtonColors.Dark else ProtonColors.Light).floatyText,

            shadowNorm = (if (isSystemDark) ProtonColors.Dark else ProtonColors.Light).shadowNorm,
            shadowRaised = (if (isSystemDark) ProtonColors.Dark else ProtonColors.Light).shadowRaised,
            shadowLifted = (if (isSystemDark) ProtonColors.Dark else ProtonColors.Light).shadowLifted,

            sidebarColors = null,
        ).let { colors ->
            colors.copy(
                sidebarColors = colors.copy(
                    backgroundNorm = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_background),
                    interactionWeakNorm = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_interaction_weak_norm),
                    interactionWeakPressed = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_interaction_weak_pressed),
                    separatorNorm = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_separator),
                    textNorm = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_text_norm),
                    textWeak = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_text_weak),
                    iconNorm = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_icon_norm),
                    iconWeak = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_icon_weak),
                    interactionPressed = typedArray.getColor(R.styleable.AppTheme_proton_sidebar_interaction_pressed),
                )
            )
        }
    }
}
