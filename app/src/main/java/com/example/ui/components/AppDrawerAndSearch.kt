package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCategory
import com.example.model.CustomAppOverride
import com.example.model.IconColorTheme
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.ui.strings.AppStrings
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun AppDrawerOverlay(
    emblemChar: String,
    apps: List<InstalledApp>,
    totalAppCount: Int,
    selectedCategory: AppCategory,
    searchQuery: String,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    customBgEndHex: Long = 0xFF2B0F1CL,
    isIconGradientEnabled: Boolean = true,
    overrides: Map<String, CustomAppOverride>,
    iconSizeDp: Int = 58,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onSelectCategory: (AppCategory) -> Unit,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onCustomizeApp: (InstalledApp) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backdrop = when {
        isLiquidGlass && isDark -> Color(0xFF0A0D14).copy(alpha = 0.86f)
        isLiquidGlass && !isDark -> Color(0xFFF3F5FA).copy(alpha = 0.86f)
        isDark -> Color(0xFF0A0C10).copy(alpha = 0.96f)
        else -> Color(0xFFF6F3EC).copy(alpha = 0.96f)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backdrop)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("app_drawer_overlay")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(liquidGlassFillBrush(isDark))
                        .border(1.2.dp, liquidGlassBorderBrush(isDark), CircleShape)
                ) {
                    val clean = emblemChar.trim()
                    if (clean.isEmpty() || clean.equals("N", ignoreCase = true)) {
                        ModernGeometricNMonogram(
                            accentColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Text(
                            text = clean,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = strings.allAppsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.appsCountLabel(totalAppCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .testTag("close_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.closeCd,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        InlineSearchInput(
            query = searchQuery,
            placeholder = strings.searchPlaceholder,
            clearCd = strings.clearSearch,
            isLiquidGlass = isLiquidGlass,
            onQueryChange = onQueryChange,
            onSubmitFirst = { apps.firstOrNull()?.let(onLaunchApp) },
            autoFocus = false,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            AppCategory.entries.forEach { category ->
                val selected = category == selectedCategory
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCategory(category) },
                    label = {
                        Text(
                            text = strings.categoryName(category),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (apps.isEmpty()) {
            EmptyAppsHint(
                message = strings.noAppsFound,
                modifier = Modifier.weight(1f)
            )
        } else {
            val drawerIconSize = (iconSizeDp - 2).coerceIn(42, 74).dp
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = (iconSizeDp + 30).coerceIn(76, 112).dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("drawer_apps_grid")
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppIconTile(
                        app = app,
                        iconShape = iconShape,
                        iconColorTheme = iconColorTheme,
                        customBgHex = customBgHex,
                        customFgHex = customFgHex,
                        customBgEndHex = customBgEndHex,
                        isIconGradientEnabled = isIconGradientEnabled,
                        override = overrides[app.packageName],
                        isLiquidGlass = isLiquidGlass,
                        iconSize = drawerIconSize,
                        showLabel = true,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onCustomizeApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchOverlay(
    query: String,
    matchingApps: List<InstalledApp>,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    customBgEndHex: Long = 0xFF2B0F1CL,
    isIconGradientEnabled: Boolean = true,
    overrides: Map<String, CustomAppOverride>,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onCustomizeApp: (InstalledApp) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backdrop = when {
        isLiquidGlass && isDark -> Color(0xFF0A0D14).copy(alpha = 0.86f)
        isLiquidGlass && !isDark -> Color(0xFFF3F5FA).copy(alpha = 0.86f)
        isDark -> Color(0xFF0A0C10).copy(alpha = 0.96f)
        else -> Color(0xFFF6F3EC).copy(alpha = 0.96f)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backdrop)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("search_overlay")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            InlineSearchInput(
                query = query,
                placeholder = strings.searchPlaceholder,
                clearCd = strings.clearSearch,
                isLiquidGlass = isLiquidGlass,
                onQueryChange = onQueryChange,
                onSubmitFirst = { matchingApps.firstOrNull()?.let(onLaunchApp) },
                autoFocus = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                    .testTag("close_search_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.closeCd,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (matchingApps.isEmpty()) {
            EmptyAppsHint(
                message = strings.noAppsFound,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("search_results_list")
            ) {
                items(matchingApps, key = { it.packageName }) { app ->
                    val ov = overrides[app.packageName]
                    val label = ov?.customLabel?.takeIf { it.isNotBlank() } ?: app.label
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(20.dp),
                        isLiquidGlass = isLiquidGlass,
                        onClick = { onLaunchApp(app) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_result_${app.packageName}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            AppIconTile(
                                app = app,
                                iconShape = iconShape,
                                iconColorTheme = iconColorTheme,
                                customBgHex = customBgHex,
                                customFgHex = customFgHex,
                                customBgEndHex = customBgEndHex,
                                isIconGradientEnabled = isIconGradientEnabled,
                                override = ov,
                                isLiquidGlass = isLiquidGlass,
                                iconSize = 46.dp,
                                showLabel = false,
                                onClick = { onLaunchApp(app) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onCustomizeApp(app) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = strings.appCustomizeTitle,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineSearchInput(
    query: String,
    placeholder: String,
    clearCd: String,
    isLiquidGlass: Boolean,
    onQueryChange: (String) -> Unit,
    onSubmitFirst: () -> Unit,
    autoFocus: Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }

    LiquidGlassSurface(
        shape = RoundedCornerShape(28.dp),
        isLiquidGlass = isLiquidGlass,
        modifier = modifier.height(54.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f)
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onSubmitFirst() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("search_text_field")
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = clearCd,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}
